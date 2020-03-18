package org.dbm.dbd.web.login;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.dbm.common.Constants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Created by dell on 2017/4/6.
 */
@Component
public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        // 用户信息
        CustomerUserDetails user = (CustomerUserDetails) authentication.getPrincipal();
        HttpSession session = request.getSession();
        session.setAttribute(Constants.KEY_USER_ID, user.getUserId());
        session.setAttribute(Constants.KEY_USER_NAME, user.getUsername() == null ? user.getAccount() : user.getUsername());

        // 登录成功后，画面跳转
        String targetUrl = StringUtils.trimToNull(request.getParameter("defaultSuccessUrl"));
        if (targetUrl == null) {
            // 没有指定跳转画面，则去首页
            response.sendRedirect(request.getContextPath() + "/home");
        }

    }

}