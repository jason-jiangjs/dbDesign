package org.dbm.dbd.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.dbm.dbd.web.login.AjaxAwareAuthenticationEntryPoin;
import org.dbm.dbd.web.login.AuthenticationFailureHandlerImpl;
import org.dbm.dbd.web.login.AuthenticationProviderImpl;
import org.dbm.dbd.web.login.AuthenticationSuccessHandlerImpl;

import javax.servlet.http.HttpServletRequest;

/**
 * Spring Security的配置
 */
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthenticationSuccessHandlerImpl authenticationSuccessHandler;
    @Autowired
    private AuthenticationFailureHandlerImpl authenticationFailureHandler;
    @Autowired
    private AuthenticationProviderImpl authProviderImpl;
    @Autowired
    private AuthenticationDetailsSource<HttpServletRequest, WebAuthenticationDetails> authenticationDetailsSource;
    @Autowired
    private Environment environment;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(environment.getProperty("login.check.exclude_url", "").split(",")).permitAll()
                .anyRequest().authenticated()
            .and().formLogin()
                .successHandler(authenticationSuccessHandler) // 注意这里不能设置'defaultSuccessUrl'，不然successHandler不起作用
                .failureHandler(authenticationFailureHandler)
                .loginProcessingUrl("/doLogin")
                .loginPage("/index")
                .authenticationDetailsSource(authenticationDetailsSource)
                .permitAll()
            .and().logout().invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .logoutUrl("/logout")
                .logoutSuccessUrl("/index")
                .permitAll()
            .and().exceptionHandling()
                .authenticationEntryPoint(new AjaxAwareAuthenticationEntryPoin("/index"));

        http.csrf().disable();
        http.headers().frameOptions().disable();
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authProviderImpl);
    }

}
