package org.dbm.dbd.web.login;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dell on 2017/4/25.
 */
public class CustomWebAuthenticationDetails extends WebAuthenticationDetails {

    private final String verifycode;
    private final HttpServletRequest request;

    public CustomWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        verifycode = request.getParameter("verifycode");
        this.request = request;
    }

    public String getVerifycode() {
        return verifycode;
    }

    public HttpServletRequest getServletRequest() {
        return request;
    }

}
