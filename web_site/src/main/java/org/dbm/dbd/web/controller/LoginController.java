package org.dbm.dbd.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.dbm.common.ErrorCode;
import org.dbm.common.base.controller.BaseController;
import org.dbm.common.base.model.mongo.BaseMongoMap;
import org.dbm.common.util.ApiResponseUtil;
import org.dbm.common.util.DateTimeUtil;
import org.dbm.common.util.StringUtil;
import org.dbm.dbd.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户登录操作
 */
@RestController
public class LoginController extends BaseController {

    @Autowired
    private UserService userService;

    /**
     * 保存新密码(session中必须已有user_id)
     */
    @RequestMapping(value = "/ajax/savePasswd", method = RequestMethod.POST)
    public Map<String, Object> savePasswd(@RequestBody Map<String, Object> params) {
        Long userId = getLoginUserId();
        String oldPasswd = StringUtils.trimToNull((String) params.get("oldPasswd"));
        String newPasswd = StringUtils.trimToNull((String) params.get("newPasswd"));
        String newPasswdCfm = StringUtils.trimToNull((String) params.get("newPasswdCfm"));
        if (oldPasswd == null || newPasswd == null || newPasswdCfm == null) {
            logger.warn("savePasswd 缺少参数 params={}", params.toString());
            return ApiResponseUtil.error(ErrorCode.W1001, "缺少必须值.");
        }

        BaseMongoMap userObj = userService.getUserById(userId);

        // 先密码匹配验证
        BCryptPasswordEncoder cryptEncoder = new BCryptPasswordEncoder();
        if (!cryptEncoder.matches(oldPasswd, userObj.getStringAttribute("password"))) {
            logger.warn("savePasswd 旧密码错误");
            return ApiResponseUtil.error(ErrorCode.E5010, "旧密码错误.");
        }

        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("password", cryptEncoder.encode(newPasswd));
        infoMap.put("status", 1);
        infoMap.put("modifier", userId);
        infoMap.put("modifiedTime", DateTimeUtil.getNowTime());
        userService.updateUserInfo(userId, infoMap);
        return ApiResponseUtil.success();
    }

    /**
     * 设置/取消缺省工作环境
     */
    @RequestMapping(value = "/ajax/setDefaultDbEnv", method = RequestMethod.POST)
    public Map<String, Object> setDefaultDbEnv(@RequestBody Map<String, Object> params) {
        Long userId = getLoginUserId();
        int checkFlg = StringUtil.convertToInt(params.get("checkFlg"));
        if (checkFlg == 1) {
            // 保存默认工作环境
            long dbId = StringUtil.convertToLong(params.get("dbId"));
            if (dbId == 0) {
                logger.warn("getColumnList 缺少dbId userId={}", userId);
                return ApiResponseUtil.error(ErrorCode.W1001, "错误操作,未选择指定的表.(缺少参数 dbId)");
            }
            userService.setUserFavorite(userId, dbId);
        } else {
            // 取消默认工作环境
            userService.setUserFavorite(userId, 0);
        }

        return ApiResponseUtil.success();
    }
}
