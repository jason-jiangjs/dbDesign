package org.vog.dbd.web.controller.admin;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.vog.base.controller.BaseController;
import org.vog.base.model.mongo.BaseMongoMap;
import org.vog.common.Constants;
import org.vog.common.ErrorCode;
import org.vog.common.util.ApiResponseUtil;
import org.vog.common.util.StringUtil;
import org.vog.dbd.service.UserService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理（添加／删除／修改权限等）
 */
@Controller
public class UserMngController extends BaseController {

    @Autowired
    private UserService userService;

    /**
     * 转到用户一览画面
     */
    @RequestMapping(value = "/mng/user_mng", method = RequestMethod.GET)
    public ModelAndView gotoUserMngPage(@RequestParam Map<String, String> params) {
        ModelAndView model = new ModelAndView();
        model.addObject("adminId", request.getSession().getAttribute(Constants.KEY_USER_ID));
        model.setViewName("admin/user_mng");
        return model;
    }

    /**
     * 查询用户一览数据
     */
    @ResponseBody
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
    @ResponseBody
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
        List<Map<String, Object>> userList = userService.findUserRoleList(iid);
        model.put("rows", userList);
        model.put("total", userList.size());
        return model;
    }

    /**
     * 保存用户信息
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/mng/saveUserInfo", method = RequestMethod.POST)
    public Map<String, Object> saveUserInfo(@RequestBody Map<String, Object> params) {
        Long tiid = StringUtil.convertToLong(params.get("tiid"));
        String accNo = StringUtils.trimToNull((String) params.get("accNo"));
        String accName = StringUtils.trimToNull((String) params.get("accName"));
        int optType = StringUtil.convertToInt(params.get("optType")); // 业务类型，为１时表示新增用户
        int accRole = StringUtil.convertToInt(params.get("role"));
        int accStatus = StringUtil.convertToInt(params.get("status"));
        List<Map<String, Object>> roleList = (List<Map<String, Object>>) params.get("roleList");
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
        if (roleList == null || roleList.isEmpty()) {
            logger.warn("saveUserInfo 缺少参数 roleList");
            return ApiResponseUtil.error(ErrorCode.W1001, "没有设置访问权限");
        }

        if (optType == 1) {
            userService.addUser(params);
        } else {
            BaseMongoMap userObj = userService.getUserById(tiid);
            if (userObj == null) {
                logger.warn("deleteUser 用户不存在/已删除 userId={}", tiid);
                return ApiResponseUtil.error(ErrorCode.E5011, "该用户不存在/已删除 userId={}", tiid);
            }
            userService.updateUser(userObj, params);
        }
        return ApiResponseUtil.success();
    }

    /**
     * 删除用户
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/mng/delUser", method = RequestMethod.POST)
    public Map<String, Object> deleteUser(@RequestParam Map<String, String> params) {
        String userId = StringUtils.trimToNull(params.get("userId"));
        if (userId == null) {
            logger.warn("deleteUser 缺少参数 userId");
            return ApiResponseUtil.error(ErrorCode.W1001, "缺少参数 userId");
        }
        BaseMongoMap userObj = userService.getUserByAccount(userId);
        if (userObj == null) {
            logger.warn("deleteUser 用户不存在/已删除 userId={}", userId);
            return ApiResponseUtil.error(ErrorCode.E5011, "该用户不存在/已删除 userId={}", userId);
        }

        userService.removeUser(userObj.getLongAttribute("_id"));
        return ApiResponseUtil.success();
    }

    /**
     * 查询该管理员可管理的库的一览
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/getDbListByUser", method = RequestMethod.GET)
    public List<Map<String, Object>> getDbListByUser(@RequestParam Map<String, String> params) {
        long iid = StringUtil.convertToLong(params.get("iid"));
        if (iid == 0) {
            logger.warn("getDbListByUser 缺少参数iid");
            return Collections.EMPTY_LIST;
        }

        return userService.findUserDbList(iid);
    }
}
