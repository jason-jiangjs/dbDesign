package org.dbm.dbd.web.tag.processor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.extras.springsecurity4.auth.AuthUtils;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.processor.AbstractStandardExpressionAttributeTagProcessor;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.util.EscapedAttributeUtils;

/**
 * 判断当前登录用户是否对指定数据库脚本有读写权限
 * Created by dell on 2018/3/15.
 */
public final class AuthorizeAttrProcessor extends AbstractStandardExpressionAttributeTagProcessor {

    public static final int ATTR_PRECEDENCE = 300;
    public static final String ATTR_NAME = "authorize";


    public AuthorizeAttrProcessor(final TemplateMode templateMode, final String dialectPrefix) {
        super(templateMode, dialectPrefix, ATTR_NAME, ATTR_PRECEDENCE, false, false);
    }

    @Override
    protected void doProcess(
            final ITemplateContext context,
            final IProcessableElementTag tag,
            final AttributeName attributeName, final String attributeValue,
            final Object expressionResult,
            final IElementTagStructureHandler structureHandler) {

        final String newAttributeValue = EscapedAttributeUtils.escapeAttribute(getTemplateMode(), expressionResult == null ? null : expressionResult.toString());
        if (newAttributeValue == null || newAttributeValue.length() == 0) {
            structureHandler.removeElement();
            return;
        }

        final Authentication authentication = AuthUtils.getAuthenticationObject();
        if (authentication == null) {
            structureHandler.removeElement();
            return;
        }
        UserDetails userObj = (UserDetails) authentication.getPrincipal();
        if (userObj == null || CollectionUtils.isEmpty(userObj.getAuthorities())) {
            structureHandler.removeElement();
            return;
        }

        int userReadonly = 1;
        for (GrantedAuthority item : userObj.getAuthorities()) {
            if ("ROLE_WRITABLE".equals(item.getAuthority()) || "PROJ_MNG_USER".equals(item.getAuthority())
                    || "ADMIN_USER".equals(item.getAuthority())) {
                // 有可写权限
                userReadonly = 0;
                break;
            }
        }

        if (userReadonly == 1) {
            structureHandler.removeElement();
            return;
        }
    }

}
