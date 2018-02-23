package org.vog.dbd.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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
import org.vog.dbd.service.TableService;
import org.vog.dbd.service.UpdateHisService;
import org.vog.dbd.service.UserService;
import org.vog.dbd.web.login.CustomerUserDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询表及列的一览
 */
@Controller
public class TableListController extends BaseController {

    @Autowired
    private TableService tableService;

    @Autowired
    private UpdateHisService updateHisService;

    @Autowired
    private UserService userService;

    /**
     * 查询表的一览
     */
    @RequestMapping(value = "/table_list", method = RequestMethod.POST)
    public ModelAndView getTableList(@RequestParam Map<String, Object> params) {
        CustomerUserDetails userObj = (CustomerUserDetails) ((Authentication) request.getUserPrincipal()).getPrincipal();
        if (userObj != null) {

        }

        Long dbId = StringUtil.convertToLong(params.get("dbId"));
        int checkFlg = StringUtil.convertToInt(params.get("checkFlg"));
        if (checkFlg == 1) {
            // 保存默认工作环境
            userService.setUserFavorite(userObj.getId(), dbId);
        }

        ModelAndView model = new ModelAndView();
        model.setViewName("table/table_list");

        BaseMongoMap dbMap = tableService.findDbById(dbId);
        if (dbMap == null || dbMap.isEmpty()) {
            // 数据库不存在
            model.addObject("dbId", 0);
        } else {
            model.addObject("dbId", dbId);
            int dbType = dbMap.getIntAttribute("type");
            if (dbType == 0) {
                logger.warn("getColumnList 未设置数据库类型 id={}", dbId);
            }
            model.addObject("dbType", dbType);
            model.addObject("dbName", dbMap.getStringAttribute("dbName"));
        }
        return model;
    }

    /**
     * 查询表的一览
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/getTableList", method = RequestMethod.POST)
    public List<BaseMongoMap> getTableListByDbId(@RequestParam Map<String, Object> params) {
        long dbId = StringUtil.convertToLong(params.get("dbId"));
        int targetType = StringUtil.convertToInt(params.get("targetType"));
        if (targetType == 0) {
            targetType = 1;
        }
        String tblName = StringUtils.trimToNull((String) params.get("tblName"));
        return tableService.getTableList(tblName, dbId, targetType);
    }

    /**
     * 查询指定表的定义
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/getTable", method = RequestMethod.GET)
    public Map<String, Object> getTable(@RequestParam Map<String, String> params) {
        Long userId = (Long) request.getSession().getAttribute(Constants.KEY_USER_ID);
        if (userId == null || userId == 0) {
            logger.error("用户未登录 sessionid={}", request.getSession().getId());
            return ApiResponseUtil.error(ErrorCode.S9004, "用户未登录");
        }

        long tblId = StringUtil.convertToLong(params.get("tblId"));
        if (tblId == 0) {
            logger.warn("getColumnList 缺少tblId userId={}", userId);
            return ApiResponseUtil.error(ErrorCode.W1001, "错误操作,未选择指定的表.(缺少参数 tblId)");
        }
        String tblName = StringUtils.trimToNull(params.get("tblName"));
        if (tblName == null) {
            logger.warn("getColumnList 缺少tblName tblId={}, userId={}", tblId, userId);
            return ApiResponseUtil.error(ErrorCode.W1001, "错误操作,未选择指定的表.(缺少参数 tblName) (tblId={})", tblId);
        }

        BaseMongoMap dbMap = tableService.getTableById(tblId);
        if (dbMap == null || dbMap.isEmpty()) {
            // 表不存在
            logger.warn("getColumnList 表不存在 tblId={}, userId={}", tblId, userId);
            return ApiResponseUtil.error(ErrorCode.E5101, "指定的表不存在 tblId={} tblName={}", tblId, tblName);
        }
        Long dbId = dbMap.getLongAttribute("dbId");

        Map<String, Object> data = new HashMap<>();
        data.put("tblId", tblId);
        data.put("tblName", tblName);
        data.put("tblNameCn", dbMap.getStringAttribute("tableNameCN"));
        data.put("tblDesc", dbMap.getStringAttribute("desc"));

        // 列的表头定义
        List<List<Map<String, Object>>> columnsList = new ArrayList<>(1);
        columnsList.add(tableService.getColDefineByType(tableService.getDbTypeById(dbId)));
        data.put("columns", columnsList);
        return ApiResponseUtil.success(data);
    }

    /**
     * 查询指定表的定义
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/getColDef", method = RequestMethod.GET)
    public Map<String, Object> getColDef(@RequestParam Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();

        // 列的表头定义
        List<List<Map<String, Object>>> columnsList = new ArrayList<>(1);
        columnsList.add(tableService.getColDefineByType(StringUtil.convertToInt(params.get("type"))));
        data.put("columns", columnsList);
        return ApiResponseUtil.success(data);
    }

    /**
     * 删除表定义
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/delTableDef", method = RequestMethod.POST)
    public Map<String, Object> delTableDef(@RequestBody Map<String, Object> params) {
        CustomerUserDetails userObj = (CustomerUserDetails) ((Authentication) request.getUserPrincipal()).getPrincipal();
        Long userId = (Long) request.getSession().getAttribute(Constants.KEY_USER_ID);
        if (userObj == null || userId == null || userId == 0) {
            logger.error("用户未登录 sessionid={}", request.getSession().getId());
            return ApiResponseUtil.error(ErrorCode.S9004, "用户未登录");
        }

        long tblId = StringUtil.convertToLong(params.get("tblId"));
        if (tblId == 0) {
            logger.warn("getColumnList 缺少tblId userId={}", userId);
            return ApiResponseUtil.error(ErrorCode.W1001, "错误操作,未选择指定的表.(缺少参数 tblId)");
        }

        BaseMongoMap dbMap = tableService.getTableById(tblId);
        if (dbMap == null || dbMap.isEmpty()) {
            // 表不存在
            logger.warn("getColumnList 表不存在 tblId={}, userId={}", tblId, userId);
            return ApiResponseUtil.error(ErrorCode.E5101, "指定的表不存在 tblId={}", tblId);
        }

        tableService.delTableById(tblId);
        updateHisService.saveUpdateHis(userObj, dbMap.getLongAttribute("dbId"), dbMap, null);
        return ApiResponseUtil.success();
    }
}
