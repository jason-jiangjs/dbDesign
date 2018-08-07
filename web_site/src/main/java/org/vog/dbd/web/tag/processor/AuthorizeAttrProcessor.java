package org.vog.dbd.web.tag.processor;

import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.extras.springsecurity4.auth.AuthUtils;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.spring4.context.SpringContextUtils;
import org.thymeleaf.standard.processor.AbstractStandardExpressionAttributeTagProcessor;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.util.EscapedAttributeUtils;
import org.vog.common.util.StringUtil;
import org.vog.dbd.service.UserService;
import org.vog.dbd.web.login.CustomerUserDetails;

import java.util.List;
import java.util.Map;

/**
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
        Long dbId = StringUtil.convertToLong(newAttributeValue);
        if (dbId < 100) {
            structureHandler.removeElement();
            return;
        }

        final Authentication authentication = AuthUtils.getAuthenticationObject();
        if (authentication == null) {
            structureHandler.removeElement();
            return;
        }
        CustomerUserDetails userObj = (CustomerUserDetails) authentication.getPrincipal();
        if (userObj == null) {
            structureHandler.removeElement();
            return;
        }

        ApplicationContext appCtx = SpringContextUtils.getApplicationContext(context);
        if (appCtx == null) {
            structureHandler.removeElement();
            return;
        }
        UserService userService = appCtx.getBean(UserService.class);
        if (userService == null) {
            structureHandler.removeElement();
            return;
        }
        List<Map<String, Object>> roleList = userService.findUserDbList(userObj.getId(), false);
        if (roleList == null || roleList.isEmpty()) {
            structureHandler.removeElement();
            return;
        }
        int userReadonly = -1;
        for (Map<String, Object> item : roleList) {
            if (dbId.equals(item.get("dbId"))) {
                int role = StringUtil.convertToInt(item.get("role"));
                if (role == 1) {
                    userReadonly = 1;
                } else if (role == 2 || role == 8 || role == 9) {
                    userReadonly = 0;
                }
                break;
            }
        }

        if (userReadonly == -1 || userReadonly == 1) {
            structureHandler.removeElement();
            return;
        }
    }

}
