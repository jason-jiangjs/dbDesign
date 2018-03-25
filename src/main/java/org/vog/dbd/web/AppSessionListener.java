package org.vog.dbd.web;

import org.vog.base.service.BaseService;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * 会话超时处理
 */
@WebListener
public class AppSessionListener extends BaseService implements HttpSessionListener, HttpSessionAttributeListener {

    @Override
    public void attributeAdded(HttpSessionBindingEvent httpSessionBindingEvent) {
        logger.info("--attributeAdded--");
        HttpSession session=httpSessionBindingEvent.getSession();
        logger.info("key----:"+httpSessionBindingEvent.getName());
        logger.info("value---:"+httpSessionBindingEvent.getValue());

    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent httpSessionBindingEvent) {
        logger.info("--attributeRemoved--");
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent httpSessionBindingEvent) {
        logger.info("--attributeReplaced--");
    }

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        logger.info("---sessionCreated----");
        HttpSession session = event.getSession();
        ServletContext application = session.getServletContext();

    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) throws ClassCastException {
        logger.info("---sessionDestroyed----");
        HttpSession session = event.getSession();
        logger.info("deletedSessionId: "+session.getId());
        System.out.println(session.getCreationTime());
        System.out.println(session.getLastAccessedTime());

    }

}