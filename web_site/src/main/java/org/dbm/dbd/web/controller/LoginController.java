package org.dbm.dbd.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.dbm.common.Constants;
import org.dbm.common.ErrorCode;
import org.dbm.common.base.controller.BaseController;
import org.dbm.common.base.model.mongo.BaseMongoMap;
import org.dbm.common.util.ApiResponseUtil;
import org.dbm.common.util.DateTimeUtil;
import org.dbm.common.util.StringUtil;
import org.dbm.dbd.service.DbService;
import org.dbm.dbd.service.UserService;
import org.dbm.dbd.web.login.CustomerUserDetails;
import org.dbm.dbd.web.util.BizCommUtil;
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

    @Autowired
    private DbService dbService;

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
        BizCommUtil.setModifyAuditData(infoMap);
        userService.updateUserInfo(userId, infoMap);
        return ApiResponseUtil.success();
    }

    /**
     * 设置/取消缺省工作环境
     */
    @RequestMapping(value = "/ajax/setDefaultDbEnv", method = RequestMethod.POST)
    public Map<String, Object> setDefaultDbEnv(@RequestBody Map<String, Object> params) {
        Long userId = getLoginUserId();
        Long dbId = StringUtil.convertToLong(params.get("dbId"));
        if (dbId == 0) {
            logger.warn("setDefaultDbEnv 缺少dbId userId={}", userId);
            return ApiResponseUtil.error(ErrorCode.W1001, "错误操作,未选择数据库.(缺少参数 dbId)");
        }
        BaseMongoMap dbInfo = dbService.findDbById(dbId);
        if (dbInfo == null) {
            logger.warn("setDefaultDbEnv 指定项目不存在 userId={} dbId={}", userId, dbId);
            return ApiResponseUtil.error(ErrorCode.E5001, "错误,选择的数据库不存在.");
        }
        request.getSession().setAttribute("_curr_proj_db_name", dbInfo.getStringAttribute("dbName"));

        // 要先判断是否有权限读取该er图
        if (!userService.hasReadAuthorization(userId, dbId)) {
            logger.warn("getErChartInfo 当前登录用户没有读权限 userId={} dbId={}", userId, dbId);
            return ApiResponseUtil.error(ErrorCode.E5104, "对不起,你没有权限读取该ER图");
        }

        Long oldDbId = BizCommUtil.getSelectedDbId();
        if (!dbId.equals(oldDbId)) {
            request.getSession().setAttribute(Constants.KEY_DB_ID, dbId);
        }

        CustomerUserDetails userObj = BizCommUtil.getLoginUserDetails();
        int checkFlg = StringUtil.convertToInt(params.get("checkFlg"), -1);
        if (checkFlg == 1) {
            // 保存默认工作环境
            userObj.setFavorite(dbId);
            userService.setUserFavorite(userId, dbId);
        } else if (checkFlg == 0) {
            // 取消默认工作环境
            userObj.setFavorite(0L);
            userService.setUserFavorite(userId, 0);
        }

        return ApiResponseUtil.success();
    }
}
