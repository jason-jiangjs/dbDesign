package org.dbm.dbd.web.util;

import org.dbm.common.Constants;
import org.dbm.common.util.DateTimeUtil;
import org.dbm.dbd.model.AuditDataBean;
import org.dbm.dbd.web.login.CustomerUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Map;

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

    public static AuditDataBean createAuditData() {
        long nowtime = DateTimeUtil.getDate().getTime();
        long userId = BizCommUtil.getLoginUserId();
        AuditDataBean auditData = new AuditDataBean();

        auditData.setCreatorId(userId);
        auditData.setCreatedTime(nowtime);
        auditData.setModifierId(userId);
        auditData.setModifierName(BizCommUtil.getLoginUserName());
        auditData.setModifiedTime(nowtime);
        return auditData;
    }

    public static void setModifyAuditData(Map<String, Object> params) {
        params.put("auditData.modifierId", BizCommUtil.getLoginUserId());
        params.put("auditData.modifierName", BizCommUtil.getLoginUserName());
        params.put("auditData.modifiedTime", DateTimeUtil.getDate().getTime());
    }

    public static void setModifyAuditData(Map<String, Object> params, long modifiedTime) {
        params.put("auditData.modifierId", BizCommUtil.getLoginUserId());
        params.put("auditData.modifierName", BizCommUtil.getLoginUserName());
        params.put("auditData.modifiedTime", modifiedTime);
    }

}
