package org.dbm.dbd.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.dbm.common.ErrorCode;
import org.dbm.common.base.controller.BaseController;
import org.dbm.common.base.model.mongo.BaseMongoMap;
import org.dbm.common.util.ApiResponseUtil;
import org.dbm.common.util.StringUtil;
import org.dbm.dbd.model.TablesData4Del;
import org.dbm.dbd.service.*;
import org.dbm.dbd.web.util.BizCommUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 表管理
 */
@RestController
public class TableListController extends BaseController {

    @Autowired
    private TableService tableService;

    @Autowired
    private UpdateHisService updateHisService;

    @Autowired
    private UserService userService;

    @Autowired
    private DbService dbService;

    @Autowired
    private MetaDataService metaDataService;

    /**
     * 查询表的一览
     */
    @RequestMapping(value = "/ajax/getTableList", method = RequestMethod.GET)
    public Map<String, Object> getTableListByDbId(@RequestParam Map<String, Object> params) {
        String dbIdStr = (String) params.get("dbId");
        long dbId = StringUtil.convertToLong(dbIdStr);
        int targetType = StringUtil.convertToInt(params.get("targetType"));
        if (targetType == 0) {
            targetType = 1;
        }
        int page = StringUtil.convertToInt(params.get("page"));
        int rows = StringUtil.convertToInt(params.get("rows"));
        String tblName = StringUtils.trimToNull((String) params.get("tblName"));

        Map<String, Object> model = new HashMap<>();
        List<BaseMongoMap> dataList = tableService.getTableNameList(tblName, dbId, targetType, page, rows);
        model.put("rows", dataList);
        model.put("total", tableService.countTableList(tblName, dbId, targetType));
        return model;
    }

    /**
     * 查询指定表的定义
     */
    @RequestMapping(value = "/ajax/getTable", method = RequestMethod.GET)
    public Map<String, Object> getTable(@RequestParam Map<String, String> params) {
        Long userId = getLoginUserId();
        long tblId = StringUtil.convertToLong(params.get("tblId"));
        if (tblId == 0) {
            logger.warn("getTable 缺少tblId userId={}", userId);
            return ApiResponseUtil.error(ErrorCode.W1001, "错误操作,未选择指定的表.(缺少参数 tblId)");
        }

        BaseMongoMap dbMap = tableService.getTableById(tblId);
        if (dbMap == null || dbMap.isEmpty()) {
            // 表不存在
            logger.warn("getTable 表不存在 tblId={}, userId={}", tblId, userId);
            return ApiResponseUtil.error(ErrorCode.E5101, "指定的表不存在 tblId={}", tblId);
        }
        Long dbId = dbMap.getLongAttribute("dbId");

        Map<String, Object> data = new HashMap<>();
        data.put("tblId", tblId);
        data.put("tblName", dbMap.getStringAttribute("tableName"));
        data.put("tblNameCn", dbMap.getStringAttribute("tableNameCN"));
        data.put("tblDesc", dbMap.getStringAttribute("desc"));
        Long modifiedTime = dbMap.getLongObject("modifiedTime");
        if (modifiedTime != null) {
            data.put("lastUpd", modifiedTime.toString()); // !!:这里必须转换为字符串，传long型会丢失精度
        }

        // 列的表头定义
        List<List<Map<String, Object>>> columnsList = new ArrayList<>(1);
        columnsList.add(metaDataService.getColDefineByType(dbService.getDbTypeById(dbId)));
        data.put("columns", columnsList);
        return ApiResponseUtil.success(data);
    }

    /**
     * 删除表定义(单个)
     */
    @RequestMapping(value = "/ajax/delTableDef", method = RequestMethod.POST)
    public Map<String, Object> delTableDef(@RequestBody Map<String, Object> params) {
        Long userId = getLoginUserId();
        long tblId = StringUtil.convertToLong(params.get("tblId"));
        if (tblId == 0) {
            logger.warn("delTableDef 缺少tblId userId={}", userId);
            return ApiResponseUtil.error(ErrorCode.W1001, "错误操作,未选择指定的表");
        }

        BaseMongoMap tblMap = tableService.getTableById(tblId);
        if (tblMap == null || tblMap.isEmpty()) {
            // 表不存在
            logger.warn("delTableDef 表不存在 tblId={}, userId={}", tblId, userId);
            return ApiResponseUtil.error(ErrorCode.E5101, "数据错误,选定的表不存在.");
        }

        tableService.delTableById(userId, tblId);
        updateHisService.saveUpdateHis(tblMap, null);
        return ApiResponseUtil.success();
    }

    /**
     * 删除表/视图定义(多个)
     */
    @RequestMapping(value = "/ajax/bulkDelTableDef", method = RequestMethod.POST)
    public Map<String, Object> bulkDeleteTableDef(@RequestBody List<TablesData4Del> params) {
        Long userId = getLoginUserId();
        if (params == null || params.isEmpty()) {
            logger.warn("bulkDeleteTableDef 缺少tableList userId={}", userId);
            return ApiResponseUtil.error(ErrorCode.W1001, "错误操作,未选择指定的表.");
        }
        Map<Long, TablesData4Del> maps = params.stream().collect(Collectors.toMap(
                TablesData4Del::getTableId, Function.identity()));

        List<Long> tblIds = params.stream().map(item -> item.getTableId()).collect(Collectors.toList());
        List<BaseMongoMap> tblDataList = tableService.getTableByIds(BizCommUtil.getSelectedDbId(), tblIds, false, false);
        if (tblDataList == null || tblDataList.isEmpty()) {
            // 表不存在
            logger.warn("bulkDeleteTableDef 表不存在 tblIds={}, userId={}", tblIds.toString(), userId);
            return ApiResponseUtil.error(ErrorCode.E5101, "数据错误,选定的表不存在.");
        }
        // 再逐个校验数据是否有效,只有有一个有问题就返回错误
        for (BaseMongoMap tblMap : tblDataList) {
            Long tblId = tblMap.getLongObject("_id");
            String tblName = tblMap.getStringAttribute("tableName");
            Long updatedTime = tblMap.getLongObject("modifiedTime");
            TablesData4Del newData = maps.get(tblId);
            if (!tblName.equals(newData.getTableName())) {
                // 防止表名被改过
                return ApiResponseUtil.error(ErrorCode.E5102, "数据错误,选定的表'{}'已被改名为'{}',<br>请确认后再操作.", newData.getTableName(), tblName);
            }
            if (newData.getModifiedTime() == null) {
                if (updatedTime != null ) {
                    // 防止表被修改过
                    return ApiResponseUtil.error(ErrorCode.E5102, "数据错误,选定的表'{}'已被修改过,<br>请确认后再操作.", tblName);
                }
            } else {
                if (!newData.getModifiedTime().equals(updatedTime)) {
                    // 防止表被修改过
                    return ApiResponseUtil.error(ErrorCode.E5102, "数据错误,选定的表'{}'已被修改过,<br>请确认后再操作.", tblName);
                }
            }
            // 再验证表是否正在被编辑
            Long currEditorId = tblMap.getLongAttribute("currEditorId");
            if (currEditorId.compareTo(0L) > 0) {
                return ApiResponseUtil.error(ErrorCode.E5102, "选定的表'{}'正在被{}编辑,请确认后再操作.", tblName, tblMap.getStringAttribute("currEditorName"));
            }
        }

        for (BaseMongoMap tblMap : tblDataList) {
            Long tblId = tblMap.getLongObject("_id");

            tableService.delTableById(userId, tblId);
            updateHisService.saveUpdateHis(tblMap, null);
        }
        return ApiResponseUtil.success();
    }

    /**
     * 检查是否有表在被编辑（检查所有表）
     * 返回code:  5102表示有人正在编辑
     */
    @RequestMapping(value = "/ajax/chkTableInEditing", method = RequestMethod.POST)
    public Map<String, Object> chkTableInEditing(@RequestParam Map<String, String> params) {
        List<BaseMongoMap> inEditingList = tableService.getTableInEditing(BizCommUtil.getSelectedDbId());
        if (inEditingList.size() > 0) {
            // 有编辑
            logger.info("chkTableInEditing 有人正在编辑 count={}, userId={}", inEditingList.size(), getLoginUserId());
            Map<String, Object> data = new HashMap<>();
            data.put("inEditingList", inEditingList);
            return ApiResponseUtil.error(ErrorCode.E5102, null, data);
        }

        return ApiResponseUtil.success();
    }

    /**
     * 检查指定表是否已在被编辑，或者自从上次查看后被人编辑保存过了
     * 返回code:  1表示有人正在编辑　2表示已被保存过，需要刷新
     */
    @RequestMapping(value = "/ajax/chkTblEditable", method = RequestMethod.POST)
    public Map<String, Object> chkTblEditable(@RequestParam Map<String, String> params) {
        Long userId = getLoginUserId();
        long tblId = StringUtil.convertToLong(params.get("tableId"));
        if (tblId == 0) {
            logger.warn("chkTblEditable 缺少参数tableId");
            return ApiResponseUtil.error(ErrorCode.W1001, "缺少参数tableId");
        }

        if (tblId < 1000) {
            logger.warn("chkTblEditable 新建表不需要检查编辑冲突");
            return ApiResponseUtil.success();
        }
        BaseMongoMap tblMap = tableService.getTableById(tblId);
        if (tblMap == null || tblMap.isEmpty()) {
            // 表不存在
            logger.warn("chkTblEditable 表不存在 tblId={}, userId={}", tblId, userId);
            return ApiResponseUtil.error(ErrorCode.E5101, "指定的表不存在 tblId={}", tblId);
        }

        Long currEditorId = tblMap.getLongAttribute("currEditorId");
        if (currEditorId == 0 || currEditorId.equals(userId)) {
            // 没人在编辑／或是自己在编辑(这种情况应该是出错了)
            // 再判断自从打开后是否被修改过
            long lastUpd = StringUtil.convertToLong(params.get("lastUpd"));
            long newUpd = tblMap.getLongAttribute("modifiedTime");
            if ((lastUpd == 0 && newUpd > 0) || (lastUpd > 0 && newUpd > 0 && lastUpd < newUpd)) {
                // 已经被编辑过了
                long modifier = tblMap.getLongAttribute("modifier");
                if (modifier == 0) {
                    return ApiResponseUtil.error(2, "该表定义已经被修改过了，需要重新加载。" );
                }
                BaseMongoMap userMap = userService.getUserById(modifier);
                if (userMap == null) {
                    logger.warn("chkTblEditable 用户不存在 modifier={}", modifier);
                    return ApiResponseUtil.error(2, "该表定义已经被用户(id={})修改过了，需要重新加载。", modifier);
                } else  {
                    return ApiResponseUtil.error(2, "该表定义已经被{}(id={})修改过了，需要重新加载。", userMap.getStringAttribute("userName"), modifier);
                }
            }

            tableService.startEditTable(userId, tblId);
            return ApiResponseUtil.success();
        }

        BaseMongoMap userMap = userService.getUserById(currEditorId);
        if (userMap == null) {
            logger.warn("chkTblEditable 用户不存在 currEditorId={}", currEditorId);
            return ApiResponseUtil.error(ErrorCode.E5011, null);
        }
        if (userMap.getIntAttribute("status") != 1) {
            logger.warn("chkTblEditable 用户状态异常 currEditorId={}, status={}", currEditorId, userMap.getIntAttribute("status"));
            return ApiResponseUtil.error(ErrorCode.E5012, userMap.getStringAttribute("userName") + "(" + userMap.getStringAttribute("userId") + ")");
        }

        // 已在编辑状态
        return ApiResponseUtil.error(1, userMap.getStringAttribute("userName") + "(" + userMap.getStringAttribute("userId") + ") 正在编辑该表，<br/>去催催吧。" );
    }

    /**
     * 强制开始编辑指定表, 覆盖现有数据
     */
    @RequestMapping(value = "/ajax/forceTblEditable", method = RequestMethod.POST)
    public Map<String, Object> forceTblEditable(@RequestParam Map<String, String> params) {
        Long userId = getLoginUserId();
        long tblId = StringUtil.convertToLong(params.get("tableId"));
        if (tblId == 0) {
            logger.warn("forceTblEditable 缺少参数tableId");
            return ApiResponseUtil.error(ErrorCode.W1001, "缺少参数tableId");
        }

        if (tblId < 1000) {
            logger.warn("forceTblEditable 新建表不需要检查编辑冲突");
            return ApiResponseUtil.success();
        }
        BaseMongoMap tblMap = tableService.getTableById(tblId);
        if (tblMap == null || tblMap.isEmpty()) {
            // 表不存在
            logger.warn("chkTblEditable 表不存在 tblId={}, userId={}", tblId, userId);
            return ApiResponseUtil.error(ErrorCode.E5101, "指定的表不存在 tblId={}", tblId);
        }

        tableService.startEditTable(userId, tblId);
        return ApiResponseUtil.success();
    }

    /**
     * 结束编辑状态, 不保存数据！！！
     */
    @RequestMapping(value = "/ajax/endEditable", method = RequestMethod.POST)
    public Map<String, Object> endEditable(@RequestParam Map<String, String> params) {
        Long userId = getLoginUserId();
        long tblId = StringUtil.convertToLong(params.get("tableId"));
        if (tblId == 0) {
            logger.warn("endEditable 缺少参数tableId");
            return ApiResponseUtil.error(ErrorCode.W1001, "缺少参数tableId");
        }

        if (tblId < 1000) {
            logger.warn("endEditable 新建表不需要检查编辑冲突");
            return ApiResponseUtil.success();
        }
        BaseMongoMap tblMap = tableService.getTableById(tblId);
        if (tblMap == null || tblMap.isEmpty()) {
            // 表不存在
            logger.warn("endEditable 表不存在 tblId={}, userId={}", tblId, userId);
            return ApiResponseUtil.error(ErrorCode.E5101, "指定的表不存在 tblId={}", tblId);
        }

        Long curEditorId = tblMap.getLongAttribute("currEditorId");
        if (curEditorId == 0) {
            logger.info("endEditable 当前表没有人在编辑 tblId={}, userId={}", tblId, userId);
            return ApiResponseUtil.success();
        }
        if (!curEditorId.equals(userId)) {
            BaseMongoMap userMap = userService.getUserById(curEditorId);
            if (userMap == null) {
                logger.info("endEditable 当前表由无效用户在编辑 tblId={}, curEditorId={}，userId={}", tblId, curEditorId, userId);
                return ApiResponseUtil.error(ErrorCode.E5101, "当前表是其他人在编辑，请联系管理员。");
            } else {
                logger.info("endEditable 当前表是其他人在编辑 tblId={}, curEditorId={}，userId={}", tblId, curEditorId, userId);
                return ApiResponseUtil.error(ErrorCode.E5101, "{}正在编辑该表，不能取消", userMap.getStringAttribute("userName"));
            }
        }

        tableService.endEditTable(tblId);
        return ApiResponseUtil.success();
    }

}
