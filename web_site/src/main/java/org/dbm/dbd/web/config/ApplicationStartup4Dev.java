package org.dbm.dbd.web.config;

import org.dbm.dbd.web.util.SystemProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Created by dell on 2017/4/19.
 */
@Profile("dev")
public class ApplicationStartup4Dev implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 加载属性配置
        ApplicationContext _applicationContext = event.getApplicationContext();
        SystemProperty.initComConfig(_applicationContext);
    }

}