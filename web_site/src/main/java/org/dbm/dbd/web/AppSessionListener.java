package org.dbm.dbd.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.dbm.common.base.service.BaseService;
import org.dbm.common.Constants;
import org.dbm.common.util.JacksonUtil;
import org.dbm.dbd.service.TableService;
import org.dbm.dbd.service.UserService;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 会话超时处理
 * 在开发环境中运行时(启动主类DbDesignApplication), attributeRemoved()和sessionDestroyed()都会被调用, logout()方法会被执行两编
 */
@WebListener
public class AppSessionListener extends BaseService implements HttpSessionListener, HttpSessionAttributeListener {

    @Autowired
    private UserService userService;

    @Autowired
    private TableService tableService;

    @Override
    public void attributeAdded(HttpSessionBindingEvent httpSessionBindingEvent) {
    } // 无处理

    @Override
    public void attributeRemoved(HttpSessionBindingEvent event) {
        logger.info("--attributeRemoved-- name: {} value: {}", event.getName(), event.getValue());
        if (Constants.KEY_USER_ID.equals(event.getName())) {
            logout((Long) event.getValue());
        }
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent httpSessionBindingEvent) {
    } // 无处理

    @Override
    public void sessionCreated(HttpSessionEvent event) {
    } // 无处理

    @Override
    public void sessionDestroyed(HttpSessionEvent event) throws ClassCastException {
        logger.info("---sessionDestroyed----{}", JacksonUtil.bean2Json(event.toString()));
        HttpSession session = event.getSession();
        logout((Long) session.getAttribute(Constants.KEY_USER_ID));
    }

    private void logout(Long userId) {
        if (userId == null || userId == 0) {
            logger.warn("当前会话没有userid"); // 这里不再输出其他session信息,因为没必要，即使出错也没法追查
            return;
        }

        // 标记当前用户为未登录
        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("inLogin", 0);
        infoMap.put("loginTime", null);
        userService.updateUserInfo(userId, infoMap);

        // 如果正在编辑表，则取消
        tableService.endEditTable4User(userId);
    }
}