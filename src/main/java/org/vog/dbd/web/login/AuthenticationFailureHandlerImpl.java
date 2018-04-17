package org.vog.dbd.web.login;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.vog.common.Constants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Created by dell on 2017/4/6.
 */
@Component
public class AuthenticationFailureHandlerImpl implements AuthenticationFailureHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException authenticationException)
            throws IOException, ServletException {
        // 验证失败，回到登陆画面
        logger.error("用户验证失败", authenticationException);
        String errCode = "";
        if (authenticationException instanceof InternalAuthenticationServiceException) {
            if (authenticationException.getCause() instanceof AccountExpiredException) {
                // 初次登录或密码过期，去重置密码画面
                procAccountExpired(request, response, (AccountExpiredException) authenticationException.getCause());
                return;
            }
            errCode = StringUtils.trimToEmpty(authenticationException.getCause().getMessage());
        } else if (authenticationException instanceof AccountExpiredException) {
            // 初次登录或密码过期，去重置密码画面
            procAccountExpired(request, response, (AccountExpiredException) authenticationException);
            return;

        } else {
            errCode = StringUtils.trimToEmpty(authenticationException.getMessage());
        }

        // 要添加错误信息
        String paramsStr = "?msg=" + errCode;
        response.sendRedirect(request.getContextPath() + "/index" + paramsStr);
    }

    // 这里是用户初次登录需要修改密码
    private void procAccountExpired(HttpServletRequest request, HttpServletResponse response, AccountExpiredException expiredException) throws IOException, ServletException {
        String msg = expiredException.getMessage();
        if (!msg.startsWith("user.needChangePwd")) {
            return;
        }
        String[] msgArr = msg.split(",");
        HttpSession session = request.getSession();
        session.setAttribute(Constants.KEY_USER_ID, NumberUtils.toLong(msgArr[1]));
        // 初次登录或密码过期，去重置密码画面
        response.sendRedirect(request.getContextPath() + "/changePasswd");
    }
}