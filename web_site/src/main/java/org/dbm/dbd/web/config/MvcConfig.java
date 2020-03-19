package org.dbm.dbd.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by dell on 2017/3/30.
 */
@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 下面几项是drawio所用(注意原drawio首页是index.html，这里以免冲突改为index2.html)
        registry.addViewController("/drawio").setViewName("index2");
        registry.addViewController("/app.html").setViewName("app");
        registry.addViewController("/offline.html").setViewName("offline");
        registry.addViewController("/open.html").setViewName("open");
        registry.addViewController("/open2.html").setViewName("open2");
    }

//    @Override
//    public Validator getValidator() {
//        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
//        validator.setValidationMessageSource( messageSource() );
//        return validator;
//    }
//
//    @Bean
//    public MessageSource messageSource() {
//        final ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
//
//        messageSource.setBasenames("messages","pages");
//        messageSource.setFallbackToSystemLocale(false);
//        messageSource.setCacheSeconds(0);
//
//        return messageSource;
//    }
}
