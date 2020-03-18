package org.dbm.dbd.web.login;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by dell on 2017/4/11.
 */
public class AjaxAwareAuthenticationEntryPoin extends LoginUrlAuthenticationEntryPoint {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public AjaxAwareAuthenticationEntryPoin(String loginFormUrl) {
        super(loginFormUrl);
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        logger.error("用户验证时异常", authException);
        String ajaxHeader = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(ajaxHeader)) {
            // ajax请求, 强制转向登录画面(由前端js控制)
            response.sendError(HttpServletResponse.SC_REQUEST_TIMEOUT);
        } else {
            super.commence(request, response, authException);
        }
    }

}
