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

import java.util.*;

/**
 * 列定义（包括索引）管理
 */
@Controller
public class ColumnListController extends BaseController {

    @Autowired
    private TableService tableService;

    @Autowired
    private ComSequenceService sequenceService;

    @Autowired
    private UpdateHisService updateHisService;

    private static final String[] _idxSign = new String[] { "", "①", "②", "③", "④", "⑤", "⑥", "⑦", "⑧", "⑨", "⑩", "⑪", "⑫", "⑬", "⑭", "⑮", "⑯", "⑰", "⑱", "⑲", "⑳" };

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

            // 判断上次更新时间
            Long nowDate = StringUtil.convertToLong(params.get("_tbl_last_upd"));
            Long lastUpd = dbMap.getLongAttribute("modifiedTime");
            if (lastUpd > 0 && nowDate > 0 && nowDate < lastUpd) {
                logger.warn("saveColDefine 已经有人更新过了 tblId={}, userId={}，date1={},date2={}", tblId, userId, nowDate.toString(), lastUpd.toString());
                return ApiResponseUtil.error(ErrorCode.E5102, "已经有人更新过了 tblId={}", tblId);
            }
        }

        long nowTime = DateTimeUtil.getNowTime();
        Map<String, Object> retData = new HashMap<>();
        retData.put("lastUpd", Long.toString(nowTime));
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
            tblData.put("currEditorId", 0);
            tblData.put("startEditTime", null);
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

    /**
     * 查询指定表的索引定义
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/getTblIdxList", method = RequestMethod.GET)
    public List<Map> getTblIdxList(@RequestParam Map<String, String> params) {
        Long userId = (Long) request.getSession().getAttribute(Constants.KEY_USER_ID);

        long tblId = StringUtil.convertToLong(params.get("tblId"));
        if (tblId == 0) {
            logger.warn("getTblIdxList 缺少tblId userId={}", userId);
            return Collections.EMPTY_LIST;
        }

        BaseMongoMap dbMap = tableService.getTableById(tblId);
        if (dbMap == null || dbMap.isEmpty()) {
            // 表不存在
            logger.warn("getTblIdxList 表不存在 tblId={}, userId={}", tblId, userId);
            return Collections.EMPTY_LIST;
        }
        List<Map> colList = (List<Map>) dbMap.get("index_list");
        if (colList == null) {
            colList = Collections.EMPTY_LIST;
        }
        return colList;
    }

    /**
     * 保存指定表的索引定义
     * TODO-- 这里目前还是采用完全覆盖的办法，直接保存数据，先不考虑保存修改历史
     * 上传的数据必须是完整的，因为没有临时保存的概念
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/saveTblIdxDefine", method = RequestMethod.POST)
    public Map<String, Object> saveTblIdxDefine(@RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getSession().getAttribute(Constants.KEY_USER_ID);
        CustomerUserDetails userObj = (CustomerUserDetails) ((Authentication) request.getUserPrincipal()).getPrincipal();

        Long tblId = StringUtil.convertToLong(params.get("tblId")); // tblId < 100视为新增表定义
        if (tblId == 0 || tblId < 100) {
            logger.warn("saveTblIdxDefine 缺少参数_tbl_id");
            return ApiResponseUtil.error(ErrorCode.W1001, "缺少参数_tbl_id");
        }
        BaseMongoMap tdlMap = tableService.getTableById(tblId);
        if (tdlMap == null || tdlMap.isEmpty()) {
            // 表不存在
            logger.warn("saveColDefine 表不存在 tblId={}, userId={}", tblId, userId);
            return ApiResponseUtil.error(ErrorCode.E5101, "指定的表不存在 tblId={}", tblId);
        }
        List<Map> colList = (List<Map>) tdlMap.get("column_list");
        if (colList == null || colList.isEmpty()) {
            logger.warn("saveColDefine 该表定义为空 tblId={}, userId={}", tblId, userId);
            return ApiResponseUtil.error(ErrorCode.W1001, "该表定义为空，不能定义索引");
        }

        List<Map<String, Object>> idxDataList = (List<Map<String, Object>>) params.get("idxList");
        if (idxDataList == null || idxDataList.isEmpty()) {
            logger.warn("saveColDefine 缺少参数idxList tblId={}, userId={}", tblId, userId);
            return ApiResponseUtil.error(ErrorCode.W1001, "缺少参数idxList，不能保存空的索引定义");
        }

        // 先清空旧索引定义
        for (Map<String, Object> colData : colList) {
            colData.put("indexDef", "");
        }

        int colIdx = 0;
        for (Map<String, Object> idxData : idxDataList) {
            colIdx ++;
            Long idxId = StringUtil.convertToLong(idxData.get("idxId"));
            if (idxId < 100) {
                idxData.put("idxId", sequenceService.getNextSequence(ComSequenceService.ComSequenceName.FX_COLUMN_ID));
            }

            // 设置缺省值
            int idxType = StringUtil.convertToInt(idxData.get("idxType"));
            if (idxType == 0) {
                idxData.put("idxType", 1);
            }
            int idxMethod = StringUtil.convertToInt(idxData.get("idxMethod"));
            if (idxMethod == 0) {
                idxData.put("idxMethod", 1);
            }

            // 过滤出索引标记 ①②③④⑤⑥⑦⑧⑨⑩⑪⑫⑬⑭⑮⑯⑰⑱⑲⑳
            String[] idxCols = StringUtils.trimToEmpty((String) idxData.get("idxCol")).split(",");
            for (String idxCol : idxCols) {
                for (Map<String, Object> colData : colList) {
                    if (idxCol.equals(colData.get("columnName"))) {
                        String indexDef = StringUtils.trimToEmpty((String) colData.get("indexDef"));
                        if ("".equals(indexDef)) {
                            indexDef = _idxSign[colIdx];
                        } else {
                            indexDef += "," + _idxSign[colIdx];
                        }
                        colData.put("indexDef", indexDef);
                    }
                }
            }
        }

        Map<String, Object> tblData = new HashMap<>();
        tblData.put("modifier", userId);
        tblData.put("modifiedTime", DateTimeUtil.getNowTime());
        // 注意这里是完全覆盖现有定义，不考虑合并值
        tblData.put("index_list", idxDataList);
        tblData.put("column_list", colList);
        tableService.saveTblDefInfo(tblId, tblData);

        Long dbId = (Long) request.getSession().getAttribute("_dbId");
        updateHisService.saveUpdateHis(userObj, dbId, tdlMap, params);
        return ApiResponseUtil.success();
    }

}