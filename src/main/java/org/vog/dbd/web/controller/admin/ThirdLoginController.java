package org.vog.dbd.web.controller.admin;

import org.apache.commons.lang3.StringUtils;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.vog.base.controller.BaseController;
import org.vog.common.Constants;
import org.vog.common.HttpResponseErrorHandler;
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

    @Value("${security.oauth2.client.userAuthorizationUri}")
    private String authorizePath;
    @Value("${security.oauth2.client.accessTokenUri}")
    private String tokenPath;

    @Value("${security.oauth2.client.clientId}")
    private String clientId;
    @Value("${security.oauth2.client.clientSecret}")
    private String clientSecret;
    @Value("${myapp.gitlab.callback_url}")
    private String callbackUrl;
    @Value("${security.oauth2.resource.userInfoUri}")
    private String userInfoUrl;

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
                .authorizationLocation(authorizePath)
                .setClientId(clientId)
                .setRedirectURI(callbackUrl)
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
                           HttpServletRequest req, @RequestParam Map<String, String> params) throws Throwable {
         // 这里的错误信息就直接把内容传到页面，不用其他方式了
        if (StringUtils.isNotBlank(error)) {
            logger.error("authorization fails with error={} and error description={}", error, errorDescription);
            return "redirect:/sys_error?msg=" + StringUtil.encode("认证gitlab用户时出错,请联系系统管理员。<br/>") + error + "<br/>" + errorDescription;
        } else {
            logger.info("callback request receives with code={}", code);

            OAuthClientRequest request = OAuthClientRequest
                    .tokenLocation(tokenPath)
                    .setGrantType(GrantType.AUTHORIZATION_CODE)
                    .setClientId(clientId)
                    .setClientSecret(clientSecret)
                    .setRedirectURI(callbackUrl)
                    .setCode(code)
                    .buildQueryMessage();

            logger.info("build authorize request with code:{} and client secret", code);

            OAuthJSONAccessTokenResponse response = oAuthClient.accessToken(request);
            String accessToken = response.getAccessToken();
            logger.info("access token got: {}", accessToken);

            //
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.setErrorHandler(new HttpResponseErrorHandler());
            String errMsg = null;
            try {
                ResponseEntity<Map> retObj = restTemplate.getForEntity(userInfoUrl, Map.class, accessToken);
                errMsg = retObj.getHeaders().getFirst("_innerError");
                if (errMsg != null) {
                    return "redirect:/sys_error?msg=" + StringUtil.encode("从gitlab取用户信息时出错,请联系系统管理员。<br/>") + errMsg + "<br/>" + retObj.getBody().toString();
                }
                Map user = retObj.getBody();
                UserDetails userObj = loginService.loadUserByUsernameWithChkReg((String) user.get("username"), false);
                if (userObj == null) {
                    // 客户未保存过，则创建
                    userService.addUserByTrdLogin((String) user.get("username"));
                    userObj = loginService.loadUserByUsernameWithChkReg((String) user.get("username"), false);
                    if (userObj == null) {
                        return "redirect:/sys_error?msg=" + StringUtil.encode("从gitlab认证完成，保存用户时出错,请联系系统管理员。");
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
        }

        return "redirect:/home";
    }

}
