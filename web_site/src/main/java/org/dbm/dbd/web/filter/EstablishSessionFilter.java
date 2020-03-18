package org.dbm.dbd.web.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.filter.GenericFilterBean;
import org.dbm.common.Constants;
import org.dbm.common.ErrorCode;
import org.dbm.common.util.CommUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 验证会话，成功后将userid放到request中
 */
//@Order(1)
//@WebFilter(filterName = "testFilter1", urlPatterns = "/*")
public class EstablishSessionFilter extends GenericFilterBean {

    private static final Logger logger = LoggerFactory.getLogger(EstablishSessionFilter.class);

    @Autowired
    private Environment environment;

    private List<String> excludeUrls = null;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        if (excludeUrls == null) {
            excludeUrls = Arrays.asList(environment.getProperty("login.check.exclude_url").split(","));
            String url = httpReq.getServletPath();
            if (!"".equals(url) && !"/".equals(url)) {
                for (int i = 0; i < excludeUrls.size(); i ++) {
                    excludeUrls.set(i, url + excludeUrls.get(i));
                }
            }
        }
        boolean excludedRequestFound = CommUtil.checkRequestUrlPattern(httpReq, excludeUrls);
        if (excludedRequestFound) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpSession session = ((HttpServletRequest) request).getSession();
        Long custId = (Long) session.getAttribute(Constants.KEY_USER_ID);
        if (custId != null) {
            request.setAttribute(Constants.KEY_USER_ID, custId);
        } else {
            logger.warn("用户未登录 session={}", session.getId());
            Map result = new HashMap();
            result.put("code", ErrorCode.S9004);
            CommUtil.outToPrintWriter(response, result);
            return;
        }
        //session.setAttribute(acceccToken, "_last_access_time", DateTimeUtil.getNow(DateTimeUtil.COMPRESS_DATETIME_FORMAT));
        filterChain.doFilter(request, response);
    }

}
