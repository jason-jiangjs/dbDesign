package org.dbm.dbd.web.util;

import org.dbm.common.Constants;
import org.dbm.dbd.web.login.CustomerUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * 共通处理辅助类
 */
public final class BizCommUtil {

    /**
     * 取得当前登录用户对象
     */
    public static CustomerUserDetails getLoginUserDetails() {
        return (CustomerUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /**
     * 取得当前登录用户的用户ID
     */
    public static Long getLoginUserId() {
        CustomerUserDetails user = (CustomerUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null) {
            return null;
        }
        return user.getUserId();
    }

    /**
     * 取得当前登录用户的用户名(不是登录帐号)
     */
    public static String getLoginUserName() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null) {
            return null;
        }
        return user.getUsername();
    }

    public static Long getSelectedDbId() {
        return (Long) RequestContextHolder.currentRequestAttributes().getAttribute(
                Constants.KEY_DB_ID, RequestAttributes.SCOPE_SESSION);
    }

}
