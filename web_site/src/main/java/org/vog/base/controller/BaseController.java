package org.vog.base.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.vog.common.ErrorCode;
import org.vog.common.util.ApiResponseUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 基础控制类
 *
 * @author aooer 2016/10/26.
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class BaseController {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected HttpServletRequest request;

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Map<String, Object> jsonHandler(HttpServletRequest request, Exception e) {
        logger.error("未知错误 path={} msg={}", request.getServletPath(), e.getMessage(), e);
        return ApiResponseUtil.error(ErrorCode.S9001, "未知错误 msg={}", e.getMessage());
    }

}
