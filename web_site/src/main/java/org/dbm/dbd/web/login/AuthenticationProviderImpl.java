package org.dbm.dbd.web.login;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Created by dell on 2017/4/6.
 */
@Component
public class AuthenticationProviderImpl implements AuthenticationProvider {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserDetailsServiceImpl userService;

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        String username = StringUtils.trimToNull(authentication.getName());
        if (username == null) {
            throw new UsernameNotFoundException("username.required");
        }
        String password = StringUtils.trimToNull((String) authentication.getCredentials());
        if (password == null) {
            throw new UsernameNotFoundException("password.required");
        }

        CustomerUserDetails user = (CustomerUserDetails) userService.loadUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("nameorpasswd.invalid");
        }

        // 先密码匹配验证
        if (!passwordEncoder().matches(password, user.getPassword())) {
            throw new UsernameNotFoundException("nameorpasswd.invalid");
        }

        // 判断用户是否已定义角色
        if (CollectionUtils.isEmpty(user.getAuthorities())) {
            throw new DisabledException("user.isInvalid");
        }

        // 再判断用户状态
        int userSts = user.getStatus();
        if (userSts == 1) {
            // 正常情况
            return new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities());
        } else if (userSts == 0) {
            throw new AccountExpiredException("user.needChangePwd," + user.getUserId());
        } else if (userSts == 2) {
            throw new LockedException("user.islocked");
        } else if (userSts == 4) {
            throw new DisabledException("user.isInvalid");
        } else {
            logger.error("未知状态 userId={}", username);
            throw new DisabledException("user.isInvalid");
        }
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}