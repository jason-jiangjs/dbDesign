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
import org.vog.base.controller.BaseController;
import org.vog.base.model.mongo.BaseMongoMap;
import org.vog.common.Constants;
import org.vog.common.ErrorCode;
import org.vog.common.util.ApiResponseUtil;
import org.vog.common.util.DateTimeUtil;
import org.vog.common.util.StringUtil;
import org.vog.dbd.service.ComSequenceService;
import org.vog.dbd.service.TableService;
import org.vog.dbd.service.UpdateHisService;
import org.vog.dbd.web.login.CustomerUserDetails;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * 查询表及列的一览
 */
@Controller
public class ColumnListController extends BaseController {

    @Autowired
    private TableService tableService;

    @Autowired
    private ComSequenceService sequenceService;

    @Autowired
    private UpdateHisService updateHisService;

    /**
     * 获取数据类型的定义
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/getColDataType", method = RequestMethod.GET)
    public String getColDef(@RequestParam Map<String, String> params) {
        int dbType = StringUtil.convertToInt(params.get("dbType"));
        if (dbType == 1) {
            return "[ {\"text\":\"char\"}, {\"text\":\"varchar\"}, {\"text\":\"tinytext\"}, {\"text\":\"text\"}, {\"text\":\"blob\"}, {\"text\":\"mediumtext\"}, {\"text\":\"mediumblob\"}, {\"text\":\"longtext\"}, {\"text\":\"longblob\"}, {\"text\":\"tinyint\"}, {\"text\":\"smallint\"}, {\"text\":\"mediumint\"}, {\"text\":\"int\"}, {\"text\":\"bigint\"}, {\"text\":\"float\"}, {\"text\":\"double\"}, {\"text\":\"decimal\"}, {\"text\":\"date\"}, {\"text\":\"datetime\"}, {\"text\":\"timestamp\"} ]";
        } else if (dbType == 2) {
            return "[{\"text\":\"objectId\"},{\"text\":\"bool\"},{\"text\":\"int\"},{\"text\":\"long\"},{\"text\":\"double\"},{\"text\":\"decimal\"},{\"text\":\"string\"},{\"text\":\"object\"},{\"text\":\"array\"},{\"text\":\"binData\"},{\"text\":\"date\"},{\"text\":\"timestamp\"},{\"text\":\"regex\"}]";
        } else if (dbType == 3) {
            return "[{\"text\":\"ancestor_path\"}, {\"text\":\"descendent_path\"}, {\"text\":\"binary\"}, {\"text\":\"boolean\"}, {\"text\":\"booleans\"}, {\"text\":\"currency\"}, {\"text\":\"date\"}, {\"text\":\"dates\"}, {\"text\":\"double\"}, {\"text\":\"doubles\"}, {\"text\":\"float\"}, {\"text\":\"floats\"}, {\"text\":\"ignored\"}, {\"text\":\"int\"}, {\"text\":\"ints\"}, {\"text\":\"location\"}, {\"text\":\"location_rpt\"}, {\"text\":\"long\"}, {\"text\":\"longs\"}, {\"text\":\"lowercase\"}, {\"text\":\"phonetic_en\"}, {\"text\":\"point\"}, {\"text\":\"random\"}, {\"text\":\"string\"}, {\"text\":\"strings\"}, {\"text\":\"tdate\"}, {\"text\":\"tdates\"}, {\"text\":\"tint\"}, {\"text\":\"tints\"}, {\"text\":\"tlong\"}, {\"text\":\"tlongs\"}, {\"text\":\"tfloat\"}, {\"text\":\"tfloats\"}, {\"text\":\"tdouble\"}, {\"text\":\"tdoubles\"}, {\"text\":\"text_cjk\"}, {\"text\":\"text_en\"}, {\"text\":\"text_en_splitting\"}, {\"text\":\"text_en_splitting_tight\"}, {\"text\":\"text_general\"}, {\"text\":\"text_general_rev\"}, {\"text\":\"text_ws\"}, {\"text\":\"textComplex\"}, {\"text\":\"textMaxWord\"}, {\"text\":\"textSimple\"}, {\"text\":\"text_ik\"}]";
        } else {
            return "[]";
        }
    }

    /**
     * 查询指定表的所有列一览
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/getColumnList", method = RequestMethod.GET)
    public Map<String, Object> getColumnList(@RequestParam Map<String, String> params) {
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

        BaseMongoMap dbMap = tableService.getTableById(tblId);
        if (dbMap == null || dbMap.isEmpty()) {
            // 表不存在
            logger.warn("getColumnList 表不存在 tblId={}, userId={}", tblId, userId);
            return ApiResponseUtil.error(ErrorCode.E5101, "指定的表不存在 tblId={}", tblId);
        }
        List<Map> colList = (List<Map>) dbMap.get("column_list");

        Map<String, Object> model = new HashMap<>();
        model.put("rows", colList);
        model.put("total", colList.size());
        return model;
    }

    /**
     * 保存指定表的定义
     * TODO-- 这里目前还是采用完全覆盖的办法，直接保存数据，先不考虑保存修改历史
     * 上传的数据必须是完整的，因为没有临时保存的概念
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/saveColDefine", method = RequestMethod.POST)
    public Map<String, Object> saveColDefine(@RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getSession().getAttribute(Constants.KEY_USER_ID);
        CustomerUserDetails userObj = (CustomerUserDetails) ((Authentication) request.getUserPrincipal()).getPrincipal();
        if (userObj == null || userId == null || userId == 0) {
            logger.error("用户未登录 sessionid={}", request.getSession().getId());
            return ApiResponseUtil.error(ErrorCode.S9004, "用户未登录");
        }

        Long dbId = StringUtil.convertToLong(params.get("dbId"));
        if (dbId == 0) {
            logger.warn("saveColDefine 缺少参数dbId");
            return ApiResponseUtil.error(ErrorCode.W1001, "缺少参数dbId");
        }
        Long tblId = StringUtil.convertToLong(params.get("_tbl_id")); // tblId < 100视为新增表定义
        if (tblId == 0) {
            logger.warn("saveColDefine 缺少参数_tbl_id");
            return ApiResponseUtil.error(ErrorCode.W1001, "缺少参数_tbl_id");
        }

        String tblName = StringUtils.trimToNull((String) params.get("_tbl_name"));
        if (tblName == null) {
            logger.warn("saveColDefine 缺少参数_tbl_name tblId={}, userId={}", tblId, userId);
            return ApiResponseUtil.error(ErrorCode.W1001, "缺少参数_tbl_name");
        }

        List<Map<String, Object>> colDataList = (List<Map<String, Object>>) params.get("column_list");
        if (colDataList == null || colDataList.isEmpty()) {
            logger.warn("saveColDefine 缺少参数col_data tblId={}, userId={}", tblId, userId);
            return ApiResponseUtil.error(ErrorCode.W1001, "缺少参数col_data，不能保存空的表定义");
        }

        BaseMongoMap dbMap = null;
        if (tblId > 100) {
            dbMap = tableService.getTableById(tblId);
            if (dbMap == null || dbMap.isEmpty()) {
                // 表不存在
                logger.warn("saveColDefine 表不存在 tblId={}, userId={}", tblId, userId);
                return ApiResponseUtil.error(ErrorCode.E5101, "指定的表不存在 tblId={}", tblId);
            }
        }

        Timestamp nowTime = new Timestamp(DateTimeUtil.getDate().getTime());
        Map<String, Object> retData = new HashMap<>();
        Map<String, Object> tblData = new HashMap<>();
        tblData.put("tableName", params.get("_tbl_name"));
        tblData.put("tableNameCN", params.get("_tbl_name_cn"));
        tblData.put("desc", params.get("_tbl_desc"));

        if (tblId < 100) {
            tblId = sequenceService.getNextSequence(ComSequenceService.ComSequenceName.FX_TABLE_ID);
            tblData.put("_id", tblId);
            retData.put("_newTblId", tblId);
            tblData.put("dbId", dbId);
            tblData.put("deleteFlg", false);
            tblData.put("type", 1); // 此值要根据数据库类型来定
            tblData.put("creator", userId);
            tblData.put("createdTime", nowTime);
            params.put("_tbl_id", tblId);
        } else {
            tblData.put("modifier", userId);
            tblData.put("modifiedTime", nowTime);
        }

        ListIterator<Map<String, Object>> lit = colDataList.listIterator();
        while (lit.hasNext()) {
            Map<String, Object> colData = lit.next();
            if (StringUtils.trimToNull((String) colData.get("columnName")) == null) {
                logger.warn("没有列名 info={}", colData.toString());
                lit.remove();
                continue;
            }
            if (colData.get("columnId") == null) {
                colData.put("columnId", sequenceService.getNextSequence(ComSequenceService.ComSequenceName.FX_COLUMN_ID));
                colData.put("tableId", tblId);
                colData.put("creator", userId);
                colData.put("createdTime", nowTime);
            } else {
                colData.put("modifier", userId);
                colData.put("modifiedTime", nowTime);
            }
        }

        tblData.put("column_list", colDataList);
        tableService.saveTblDefInfo(tblId, tblData);
        updateHisService.saveUpdateHis(userObj, dbId, dbMap, params);
        return ApiResponseUtil.success(retData);
    }

}
