package org.dbm.dbd.web.controller.admin;

import org.apache.commons.lang3.StringUtils;
import org.dbm.common.ErrorCode;
import org.dbm.common.base.controller.BaseController;
import org.dbm.common.base.model.mongo.BaseMongoMap;
import org.dbm.common.util.ApiResponseUtil;
import org.dbm.common.util.DateTimeUtil;
import org.dbm.common.util.StringUtil;
import org.dbm.dbd.service.UserService;
import org.dbm.dbd.web.util.BizCommUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 用户管理（添加／删除／修改权限等）
 */
@RestController
public class UserMngController extends BaseController {

    @Autowired
    private UserService userService;

    /**
     * 查询用户一览数据
     */
    @RequestMapping(value = "/ajax/mng/getUserList", method = RequestMethod.GET)
    public Map<String, Object> getUserList(@RequestParam Map<String, String> params) {
        int page = StringUtil.convertToInt(params.get("page"));
        int rows = StringUtil.convertToInt(params.get("rows"));
        List<BaseMongoMap> userList = userService.findUserList(page, rows);

        Map<String, Object> model = new HashMap<>();
        model.put("rows", userList);
        model.put("total", userService.countUserList());
        return model;
    }

    /**
     * 查询用户权限一览数据
     */
    @RequestMapping(value = "/ajax/mng/getUserRoleList", method = RequestMethod.GET)
    public Map<String, Object> getUserRoleList(@RequestParam Map<String, String> params) {
        Map<String, Object> model = new HashMap<>();
        long iid = StringUtil.convertToLong(params.get("iid"));
        if (iid == 0) {
            logger.warn("getUserRoleList 缺少参数 userId");
            model.put("rows", Collections.EMPTY_LIST);
            model.put("total", 0);
            return model;
        }
        List<Map<String, Object>> userList = userService.findUserDbList(iid, false);
        model.put("rows", userList);
        model.put("total", userList.size());
        return model;
    }

    /**
     * 保存用户信息
     */
    @RequestMapping(value = "/ajax/mng/saveUserInfo", method = RequestMethod.POST)
    public Map<String, Object> saveUserInfo(@RequestBody Map<String, Object> params) {
        Long adminId = getLoginUserId();
        Long tiid = StringUtil.convertToLong(params.get("tiid"));
        String accNo = StringUtils.trimToNull((String) params.get("accNo")); // 登录帐号
        int optType = StringUtil.convertToInt(params.get("optType")); // 业务类型，为１时表示新增用户
        int accStatus = StringUtil.convertToInt(params.get("status"));
        if (optType == 0 && tiid == 0) {
            logger.warn("saveUserInfo 缺少参数 tiid");
            return ApiResponseUtil.error(ErrorCode.W1001, "缺少参数 tiid");
        }
        if (optType == 1 && accStatus != 0) {
            logger.warn("saveUserInfo 用户状态错误");
            return ApiResponseUtil.error(ErrorCode.W1001, "创建新用户时状态值只能是'创建'");
        }
        if (accNo == null) {
            logger.warn("saveUserInfo 缺少参数 accNo");
            return ApiResponseUtil.error(ErrorCode.W1001, "缺少登录帐号");
        }

        List<Map<String, Object>> roleList = (List<Map<String, Object>>) params.get("roleList");
        if (roleList != null) {
            Iterator<Map<String, Object>> iter = roleList.iterator();
            while (iter.hasNext()) {
                Map<String, Object> item = iter.next();
                long dbId = StringUtil.convertToLong(item.get("dbId"));
                if (dbId == 0) {
                    iter.remove();
                    continue;
                }
                item.put("dbId", dbId);
                item.put("role", StringUtil.convertToInt(item.get("role")));
                item.remove("dbNameTxt");
                item.remove("default");
            }
        }

        if (optType == 1) {
            // 新增用户
            userService.addUser(params);
        } else {
            BaseMongoMap userObj = userService.getUserById(tiid);
            if (userObj == null) {
                logger.warn("deleteUser 用户不存在/已删除 userId={}", tiid);
                return ApiResponseUtil.error(ErrorCode.E5011, "该用户不存在/已删除 userId={}", tiid);
            }

            Map<String, Object> valMap = new HashMap<>();
            valMap.put("account", params.get("accNo"));
            valMap.put("userName", params.get("accName"));
            valMap.put("status", StringUtil.convertToInt(params.get("status")));
            valMap.put("role", StringUtil.convertToInt(params.get("role")));
            valMap.put("roleList", params.get("roleList"));
            valMap.put("auditData.modifierId", adminId);
            valMap.put("auditData.modifierName", BizCommUtil.getLoginUserName());
            valMap.put("auditData.modifiedTime", DateTimeUtil.getDate().getTime());
            userService.updateUserInfo(userObj.getLongAttribute("_id"), valMap);
        }
        return ApiResponseUtil.success();
    }

    /**
     * 删除用户
     */
    @RequestMapping(value = "/ajax/mng/delUser", method = RequestMethod.POST)
    public Map<String, Object> deleteUser(@RequestParam Map<String, String> params) {
        Long userId = StringUtil.convertToLong(params.get("userId"));
        if (userId == 0) {
            logger.warn("deleteUser 缺少参数 userId");
            return ApiResponseUtil.error(ErrorCode.W1001, "缺少参数 userId");
        }
        BaseMongoMap userObj = userService.getUserById(userId);
        if (userObj == null) {
            logger.warn("deleteUser 用户不存在/已删除 userId={}", userId);
            return ApiResponseUtil.error(ErrorCode.E5011, "该用户不存在/已删除 userId={}", userId);
        }

        userService.removeUser(userId);
        return ApiResponseUtil.success();
    }

    /**
     * 查询该管理员可管理的库的一览
     */
    @RequestMapping(value = "/ajax/getDbListByUser", method = RequestMethod.GET)
    public List<Map<String, Object>> getDbListByUser(@RequestParam Map<String, String> params) {
        long iid = StringUtil.convertToLong(params.get("iid"));
        if (iid == 0) {
            logger.warn("getDbListByUser 缺少参数iid");
            return Collections.EMPTY_LIST;
        }

        return userService.findUserDbList(iid, false);
    }
}
