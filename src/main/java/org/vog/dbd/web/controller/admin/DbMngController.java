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
import org.vog.common.util.DateTimeUtil;
import org.vog.common.util.StringUtil;
import org.vog.dbd.service.DbService;
import org.vog.dbd.service.UpdateHisService;
import org.vog.dbd.service.UserService;

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
    private UserService userService;

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
        List<BaseMongoMap> dbList = dbService.findDbList(page, rows, false);

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
        Long adminId = (Long) request.getSession().getAttribute(Constants.KEY_USER_ID);
        if (adminId == null) {
            logger.error("用户未登录 sessionid={}", request.getSession().getId());
            return ApiResponseUtil.error(ErrorCode.S9004, "用户未登录");
        }

        Long dbId = StringUtil.convertToLong(params.get("_id"));
        String dbName = StringUtils.trimToNull((String) params.get("dbName"));
        String typeStr = StringUtils.trimToNull((String) params.get("typeStr"));
        int type = StringUtil.convertToInt(params.get("type"));

        if (dbName == null || type == 0 || typeStr == null) {
            logger.warn("saveDbInfo 缺少参数 params={}", params.toString());
            return ApiResponseUtil.error(ErrorCode.W1001, "缺少参数，请填写完整再保存。");
        }

        if (dbId == 0) {
            // 新增
            params.put("deleteFlg", false);
            params.put("creator", adminId);
            params.put("createdTime", DateTimeUtil.getNowTime());
            dbService.saveDb(dbId, params);
        } else {
            // 修改
            BaseMongoMap dbObj = dbService.findDbById(dbId);
            if (dbObj == null) {
                logger.warn("saveDbInfo 数据库不存在/已删除 dbId={}", dbId);
                return ApiResponseUtil.error(ErrorCode.E5001, "该数据库不存在/已删除 dbId={}", dbId);
            }
            params.put("modifier", adminId);
            params.put("modifiedTime", DateTimeUtil.getNowTime());
            dbService.saveDb(dbId, params);
        }
        return ApiResponseUtil.success();
    }

    /**
     * 删除用户
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/mng/delDb", method = RequestMethod.POST)
    public Map<String, Object> deleteDb(@RequestParam Map<String, String> params) {
        Long dbId = StringUtil.convertToLong(params.get("dbId"));
        if (dbId == 0) {
            logger.warn("deleteDb 缺少参数 dbId");
            return ApiResponseUtil.error(ErrorCode.W1001, "缺少参数dbId，请选择数据库后再操作。");
        }
        Long userId = (Long) request.getSession().getAttribute(Constants.KEY_USER_ID);
        if (userId == null || userId == 0) {
            logger.error("用户未登录 sessionid={}", request.getSession().getId());
            return ApiResponseUtil.error(ErrorCode.S9004, "用户未登录");
        }
        BaseMongoMap userObj = userService.getUserById(userId);
        if (userObj == null) {
            logger.warn("deleteDb 用户不存在/已删除 userId={}", userId);
            return ApiResponseUtil.error(ErrorCode.S9004, "该登录用户不存在/已删除 userId={}", userId);
        }

        if (userObj.getIntAttribute("role") != 9) {
            logger.warn("deleteDb 用户无权限 userId={}", userId);
            return ApiResponseUtil.error(ErrorCode.E5001, "该登录用户无删除权限 userId={}", userId);
        }

        dbService.removeDb(userId, dbId);
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
