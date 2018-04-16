package org.vog.dbd.web.controller.admin;

import org.apache.commons.lang3.StringUtils;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.vog.base.controller.BaseController;
import org.vog.common.Constants;
import org.vog.common.HttpResponseErrorHandler;
import org.vog.common.SystemProperty;
import org.vog.common.util.StringUtil;
import org.vog.dbd.service.UserService;
import org.vog.dbd.web.login.CustomWebAuthenticationDetails;
import org.vog.dbd.web.login.CustomerUserDetails;
import org.vog.dbd.web.login.UserDetailsServiceImpl;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 用户管理（添加／删除／修改权限等）
 */
@Controller
public class ThirdLoginController extends BaseController {

    @Resource
    private UserDetailsServiceImpl loginService;

    @Resource
    private UserService userService;

    OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

    @PreDestroy
    public void cleanUp() {
        oAuthClient.shutdown();
    }

    @RequestMapping("/trdlogin/gitlab")
    public String index(HttpServletRequest req, HttpServletResponse response) throws Throwable {
        logger.info("first login, build oauth request >..");
        OAuthClientRequest request = OAuthClientRequest
                .authorizationLocation(SystemProperty.resolveStringProperty("gitlab_oauth2_client_userAuthorizationUri"))
                .setClientId(SystemProperty.resolveStringProperty("gitlab_oauth2_client_clientId"))
                .setRedirectURI(SystemProperty.resolveStringProperty("myapp_gitlab_callback_url"))
                .setResponseType("code")
                .buildQueryMessage();

        String gitlabAuthUrl = request.getLocationUri();
        logger.info("redirect to : " + gitlabAuthUrl);
        return "redirect:" + gitlabAuthUrl;
    }

    // gitlab认证的回调
    @RequestMapping("/trdlogin/gitlab/callback")
    public String callback(@RequestParam(value = "code", required = false) String code,
                           @RequestParam(value = "error", required = false) String error,
                           @RequestParam(value = "error_description", required = false) String errorDescription,
                           HttpServletRequest req) {
         // 这里的错误信息就直接把内容传到页面，不用其他方式了
        if (StringUtils.isNotBlank(error)) {
            logger.error("authorization fails with error={} and error description={}", error, errorDescription);
            return "redirect:/sys_error?msg=" + StringUtil.encode("认证gitlab用户时出错,请联系系统管理员。<br/>") + error + "<br/>" + errorDescription;
        }

        logger.debug("callback request receives with code={}", code);
        String accessToken = null;
        String errMsg = null;
        try {
            OAuthClientRequest request = OAuthClientRequest
                    .tokenLocation(SystemProperty.resolveStringProperty("gitlab_oauth2_client_accessTokenUri"))
                    .setGrantType(GrantType.AUTHORIZATION_CODE)
                    .setClientId(SystemProperty.resolveStringProperty("gitlab_oauth2_client_clientId"))
                    .setClientSecret(SystemProperty.resolveStringProperty("gitlab_oauth2_client_clientSecret"))
                    .setRedirectURI(SystemProperty.resolveStringProperty("myapp_gitlab_callback_url"))
                    .setCode(code)
                    .buildQueryMessage();

            OAuthJSONAccessTokenResponse response = oAuthClient.accessToken(request);
            accessToken = response.getAccessToken();
            logger.debug("access token got: {}", accessToken);
        } catch (Exception exp) {
            logger.error("从gitlab认证用户时出错", exp);
            errMsg = StringUtils.trimToNull(exp.getMessage());
            if (errMsg == null) {
                errMsg = exp.toString();
            }
            return "redirect:/sys_error?msg=" + StringUtil.encode("从gitlab认证用户时出错,请联系系统管理员。<br/>") + errMsg;
        }

        // 再从gitlab取用户信息
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new HttpResponseErrorHandler());
        try {
            ResponseEntity<Map> retObj = restTemplate.getForEntity(SystemProperty.resolveStringProperty("gitlab_oauth2_resource_userInfoUri"), Map.class, accessToken);
            errMsg = retObj.getHeaders().getFirst("_innerError"); /** 这个变量名"_innerError"要参照 {@link HttpResponseErrorHandler.handleError()} */
            if (errMsg != null) {
                return "redirect:/sys_error?msg=" + StringUtil.encode("从gitlab取用户信息时出错,请联系系统管理员。<br/>") + errMsg + "<br/>" + retObj.getBody().toString();
            }
            Map user = retObj.getBody();
            String userName = (String) user.get("username");
            CustomerUserDetails userObj = (CustomerUserDetails) loginService.loadUserByUsernameWithChkReg(userName, false, Constants.ThirdLogin.GITLAB.getValue());
            if (userObj == null) {
                // 客户未保存过，则创建
                userService.addUserByTrdLogin(userName, userName);
                userObj = (CustomerUserDetails) loginService.loadUserByUsernameWithChkReg(userName, false, Constants.ThirdLogin.GITLAB.getValue());
                if (userObj == null) {
                    return "redirect:/sys_error?msg=" + StringUtil.encode("从gitlab认证完成，保存用户时出错,请联系系统管理员。");
                }
            } else {
                // 已存在，必须验证是否用户名冲突
                if (!"GitLab".equals(userObj.getFromSrc()) || userObj.isRegistered()) {
                    userService.addUserByTrdLogin(userName, userName + "_GitLab");
                    userObj = (CustomerUserDetails) loginService.loadUserByUsernameWithChkReg(userName, false, Constants.ThirdLogin.GITLAB.getValue());
                    if (userObj == null) {
                        return "redirect:/sys_error?msg=" + StringUtil.encode("从gitlab认证完成，保存用户时出错,请联系系统管理员。");
                    }
                }
            }
            req.getSession().setAttribute(Constants.KEY_USER_ID, ((CustomerUserDetails) userObj).getId());
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userObj, null, null);
            token.setDetails(new CustomWebAuthenticationDetails(req));
            SecurityContextHolder.getContext().setAuthentication(token);

        } catch (Exception exp) {
            logger.error("从gitlab取用户信息时出错", exp);
            errMsg = StringUtils.trimToNull(exp.getMessage());
            if (errMsg == null) {
                errMsg = exp.toString();
            }
            return "redirect:/sys_error?msg=" + StringUtil.encode("从gitlab取用户信息时出错,请联系系统管理员。<br/>") + errMsg;
        }

        return "redirect:/home";
    }

}
