package org.vog.dbd.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.vog.base.service.BaseService;
import org.vog.common.Constants;
import org.vog.dbd.service.UserService;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 会话超时处理
 */
@WebListener
public class AppSessionListener extends BaseService implements HttpSessionListener, HttpSessionAttributeListener {

    @Autowired
    private UserService userService;

    @Override
    public void attributeAdded(HttpSessionBindingEvent httpSessionBindingEvent) {
    } // 无处理

    @Override
    public void attributeRemoved(HttpSessionBindingEvent httpSessionBindingEvent) {
        //logger.info("--attributeRemoved--{} {}", httpSessionBindingEvent.getName(), httpSessionBindingEvent.getValue());
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent httpSessionBindingEvent) {
    } // 无处理

    @Override
    public void sessionCreated(HttpSessionEvent event) {
    } // 无处理

    @Override
    public void sessionDestroyed(HttpSessionEvent event) throws ClassCastException {
        //logger.info("---sessionDestroyed----{}", event.getSource().toString());
        HttpSession session = event.getSession();
        Long userId = (Long) session.getAttribute(Constants.KEY_USER_ID);
        if (userId == null || userId == 0) {
            logger.info("当前会话没有userid sessionid={}", session.getId());
            return;
        }

        // 标记当前用户为未登录
        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("inLogin", 0);
        infoMap.put("loginTime", null);
        userService.updateUserInfo(userId, infoMap);
    }

}