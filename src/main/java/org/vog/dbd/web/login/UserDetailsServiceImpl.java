package org.vog.dbd.web.login;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.vog.base.model.mongo.BaseMongoMap;
import org.vog.dbd.dao.UserDao;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is being un-deprecated because we want the query for the customer to happen through Hibernate instead of
 * through raw JDBC, which is the case when <sec:jdbc-user-service /> is used. We need the query to go through Hibernate
 * so that we are able to attach the necessary filters in certain circumstances.
 * 
 * @author Andre Azzolini (apazzolini)
 * @author Phillip Verheyden (phillipuniverse)
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserDao userDao;

    @Override
    public UserDetails loadUserByUsername(String userId) {
        BaseMongoMap employee = userDao.getUserByAccount(userId);
        if (employee == null) {
            return null;
        }

        CustomerUserDetails userObj = new CustomerUserDetails(StringUtils.trimToNull((String) employee.get("userName")), (String) employee.get("password"),
                createGrantedAuthorities(userId, StringUtils.trimToNull((String) employee.get("type")), StringUtils.trimToNull((String) employee.get("role"))));
        userObj.setAccount(userId);
        userObj.setId(employee.getLongAttribute("_id"));
        userObj.setFavorite(employee.getLongAttribute("favorite"));
        userObj.setStatus(0);
        return userObj;
    }

    private List<GrantedAuthority> createGrantedAuthorities(String userId, String userType, String roleListStr) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
        if (userType == null) {
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        } else {
            if (roleListStr == null) {
                int userTypeVal = NumberUtils.toInt(userType);
                if (userTypeVal == 2) {
                    grantedAuthorities.add(new SimpleGrantedAuthority("DIST_USER"));
                } else if (userTypeVal == 3) {
                    grantedAuthorities.add(new SimpleGrantedAuthority("CSR_USER"));
                } else if (userTypeVal == 9) {
                    grantedAuthorities.add(new SimpleGrantedAuthority("ADMIN_USER"));
                } else {
                    logger.error("该用户的类型设置不正确 userid={}", userId);
                    grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                }
            } else {
                return AuthorityUtils.commaSeparatedStringToAuthorityList(roleListStr);
            }
        }
        return grantedAuthorities;
    }

}
