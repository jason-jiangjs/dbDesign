package org.dbm.common.base.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.dbm.common.Constants;
import org.dbm.common.ErrorCode;
import org.dbm.common.base.error.BizErrorException;
import org.dbm.common.util.ApiResponseUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 基础控制类
 */
public abstract class BaseController {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected HttpServletRequest request;

    @ExceptionHandler(value = Exception.class)
    final public Map<String, Object> jsonHandler(HttpServletRequest request, Exception e) {
        logger.error("未知错误 path={} msg={}", request.getServletPath(), e.getMessage(), e);
        if (e instanceof BizErrorException) {
            return ApiResponseUtil.error((BizErrorException) e);
        }
        return ApiResponseUtil.error(ErrorCode.S9001, "未知错误 msg={}", e.getMessage());
    }

    final public Long getLoginUserId() {
        Long userId = (Long) request.getSession().getAttribute(Constants.KEY_USER_ID);
        if (userId == null || userId == 0) {
            logger.error("用户未登录 sessionid={}", request.getSession().getId());
            throw new BizErrorException(ErrorCode.S9004, "用户未登录");
        }
        return userId;
    }

}
