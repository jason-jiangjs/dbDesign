package org.vog.dbd.web.login;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
                response.sendRedirect(request.getContextPath() + "/changePasswd");
                return;
            }
            errCode = StringUtils.trimToEmpty(authenticationException.getCause().getMessage());
        } else {
            errCode = StringUtils.trimToEmpty(authenticationException.getMessage());
        }

        // 要添加错误信息
        String paramsStr = "?msg=" + errCode;
        response.sendRedirect(request.getContextPath() + "/index" + paramsStr);
    }

}