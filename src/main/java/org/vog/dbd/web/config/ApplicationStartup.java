package org.vog.dbd.web.config;

import org.vog.common.SystemProperty;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.vog.dbd.service.ComConfigService;

/**
 * Created by dell on 2017/4/19.
 */
public class ApplicationStartup implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        // 加载配置文件
        ComConfigService configService = event.getApplicationContext().getBean(ComConfigService.class);
        if (configService != null) {
            SystemProperty.initComConfig(configService.findProperties());
        }

    }

}