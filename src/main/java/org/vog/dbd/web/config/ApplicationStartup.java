package org.vog.dbd.web.config;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.vog.common.SystemProperty;

/**
 * Created by dell on 2017/4/19.
 */
public class ApplicationStartup implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        SystemProperty.initComConfig(event.getApplicationContext());
    }

}