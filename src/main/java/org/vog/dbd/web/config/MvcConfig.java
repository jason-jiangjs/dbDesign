package org.vog.dbd.web.config;

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
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/to_drawio").setViewName("index2");
//        registry.addViewController("/index").setViewName("index"); // 参见CommonController.tologin()
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
