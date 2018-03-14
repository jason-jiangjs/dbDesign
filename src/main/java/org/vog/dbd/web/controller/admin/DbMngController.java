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
import org.vog.common.util.AESCoderUtil;
import org.vog.common.util.ApiResponseUtil;
import org.vog.common.util.StringUtil;
import org.vog.dbd.service.DbService;
import org.vog.dbd.service.UpdateHisService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库管理（添加／删除／修改等）
 */
@Controller
public class DbMngController extends BaseController {

    @Autowired
    private DbService dbService;

    @Autowired
    private UpdateHisService updateHisService;

    /**
     * 转到数据库一览画面
     */
    @RequestMapping(value = "/mng/db_mng", method = RequestMethod.GET)
    public ModelAndView gotoDbMngPage(@RequestParam Map<String, String> params) {
        ModelAndView model = new ModelAndView();
        model.addObject("adminId", request.getSession().getAttribute(Constants.KEY_USER_ID));
        model.addObject("hasAuth", 1);
        model.setViewName("admin/db_mng");
        return model;
    }

    /**
     * 查询数据库一览数据
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/mng/getDbList", method = RequestMethod.GET)
    public Map<String, Object> getDbList(@RequestParam Map<String, String> params) {
        int page = StringUtil.convertToInt(params.get("page"));
        int rows = StringUtil.convertToInt(params.get("rows"));
        List<BaseMongoMap> dbList = dbService.findDbList(page, rows);

        Map<String, Object> model = new HashMap<>();
        model.put("rows", dbList);
        model.put("total", dbService.countDbList());
        return model;
    }

    /**
     * 保存数据库信息
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/mng/saveDbInfo", method = RequestMethod.POST)
    public Map<String, Object> saveDbInfo(@RequestBody Map<String, Object> params) {
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

//        if (optType == 1) {
//            userService.addUser(params);
//        } else {
//            BaseMongoMap userObj = userService.getUserById(tiid);
//            if (userObj == null) {
//                logger.warn("deleteUser 用户不存在/已删除 userId={}", tiid);
//                return ApiResponseUtil.error(ErrorCode.E5011, "该用户不存在/已删除 userId={}", tiid);
//            }
//            userService.updateUser(userObj, params);
//        }
        return ApiResponseUtil.success();
    }

    /**
     * 删除用户
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/mng/delDb", method = RequestMethod.POST)
    public Map<String, Object> deleteDb(@RequestParam Map<String, String> params) {
        String userId = StringUtils.trimToNull(params.get("userId"));
        if (userId == null) {
            logger.warn("deleteUser 缺少参数 userId");
            return ApiResponseUtil.error(ErrorCode.W1001, "缺少参数 userId");
        }
//        BaseMongoMap userObj = userService.getUserByAccount(userId);
//        if (userObj == null) {
//            logger.warn("deleteUser 用户不存在/已删除 userId={}", userId);
//            return ApiResponseUtil.error(ErrorCode.E5011, "该用户不存在/已删除 userId={}", userId);
//        }
//
//        userService.removeUser(userObj.getLongAttribute("_id"));
        return ApiResponseUtil.success();
    }

    /**
     * 查询操作历史一览
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/mng/getUpdHisList", method = RequestMethod.GET)
    public Map<String, Object> getUpdateHistory(@RequestParam Map<String, String> params) {
        int page = StringUtil.convertToInt(params.get("page"));
        int rows = StringUtil.convertToInt(params.get("rows"));

        Map<String, Object> model = new HashMap<>();
        String dbIdStr = StringUtils.trimToNull(params.get("ddId"));
        if (dbIdStr == null) {
            // 数据库不存在
            logger.warn("getUpdateHistory 缺少参数dbId");
            model.put("rows", 0);
            model.put("total", Collections.EMPTY_LIST);
            return model;
        }

        Long dbId = StringUtil.convertToLong(AESCoderUtil.decode(dbIdStr));
        model.put("rows", updateHisService.getUpdHisList(dbId, page, rows));
        model.put("total", updateHisService.countUpdHisList(dbId));
        return model;
    }

}
