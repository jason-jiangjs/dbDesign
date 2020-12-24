package org.dbm.dbd.web.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.dbm.common.base.model.mongo.BaseMongoMap;
import org.dbm.common.util.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.List;

@Aspect
@Component
public class LogAspect
{
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Pointcut("execution(public * org.dbm.common.base.dao.mongo.BaseMongoDao.*(..))")
    public void normalLog()
    {
    }

    @Before("normalLog()")
    public void doBefore(JoinPoint joinPoint)
    {
        RequestAttributes reqAttrs = RequestContextHolder.getRequestAttributes();
        String reqUrl = null;
        if (reqAttrs != null) {
            reqUrl = (String) reqAttrs.getAttribute("curr_url_req", 0);
        }
        logger.debug("{} {}.{} param: {}", reqUrl,
                joinPoint.getTarget().getClass().getName(),
                joinPoint.getSignature().getName(),
                JacksonUtil.bean2JsonNotNull(joinPoint.getArgs()));
    }

    @AfterReturning(returning = "result", pointcut = "normalLog()")
    public void doAfterReturning(JoinPoint joinPoint, Object result)
    {
        RequestAttributes reqAttrs = RequestContextHolder.getRequestAttributes();
        String reqUrl = null;
        if (reqAttrs != null) {
            reqUrl = (String) reqAttrs.getAttribute("curr_url_req", 0);
        }
        if (result != null && result instanceof BaseMongoMap) {
            if (logger.isDebugEnabled()) {
                logger.debug("{} {}.{} 查询有结果 数据：{}", reqUrl,
                        joinPoint.getTarget().getClass().getName(),
                        joinPoint.getSignature().getName(), JacksonUtil.bean2Json(result));
            } else {
                logger.info("{} {}.{} 查询有结果", reqUrl,
                        joinPoint.getTarget().getClass().getName(),
                        joinPoint.getSignature().getName());
            }

        } else if (result != null && result instanceof List) {
            if (logger.isDebugEnabled()) {
                logger.debug("{} {}.{} 查询有结果 件数={} 数据：{}", reqUrl,
                        joinPoint.getTarget().getClass().getName(),
                        joinPoint.getSignature().getName(), ((List) result).size(), JacksonUtil.bean2Json(result));
            } else {
                logger.info("{} {}.{} 查询有结果 件数={}", reqUrl,
                        joinPoint.getTarget().getClass().getName(),
                        joinPoint.getSignature().getName(), ((List) result).size());
            }
        }
    }

}