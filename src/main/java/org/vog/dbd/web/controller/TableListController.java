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
import org.vog.common.util.AESCoderUtil;
import org.vog.common.util.ApiResponseUtil;
import org.vog.common.util.StringUtil;
import org.vog.dbd.service.DbService;
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

    @Autowired
    private DbService dbService;

    /**
     * 查询表的一览
     */
    @RequestMapping(value = "/table_list", method = RequestMethod.GET)
    public ModelAndView getTableList(@RequestParam Map<String, String> params) {
        CustomerUserDetails userObj = (CustomerUserDetails) ((Authentication) request.getUserPrincipal()).getPrincipal();
        if (userObj != null) {

        }

        ModelAndView model = new ModelAndView();
        model.setViewName("table/table_list");
        String dbIdStr = StringUtils.trimToNull(params.get("ddId"));
        if (dbIdStr == null) {
            // 数据库不存在
            logger.warn("getTableList 缺少参数dbId");
            return model;
        }

        Long dbId = StringUtil.convertToLong(AESCoderUtil.decode(dbIdStr));
        int checkFlg = StringUtil.convertToInt(params.get("checkFlg"));
        if (checkFlg == 1) {
            // 保存默认工作环境
            userService.setUserFavorite(userObj.getId(), dbId);
        }

        BaseMongoMap dbMap = dbService.findDbById(dbId);
        if (dbMap == null || dbMap.isEmpty()) {
            // 数据库不存在
            logger.warn("getTableList 数据库不存在 id={}", dbId);
            model.addObject("dbId", 0);
        } else {
            model.addObject("dbId", dbId);
            int dbType = dbMap.getIntAttribute("type");
            if (dbType == 0) {
                logger.warn("getColumnList 未设置数据库类型 id={}", dbId);
                model.addObject("dbId", 1);
                return model;
            }

            List<Map<String, Object>> roleList = userService.findUserDbList(userObj.getId(), false);
            if (roleList == null || roleList.isEmpty()) {
                model.addObject("dbId", 2);
                return model;
            }
            int userReadonly = -1;
            for (Map<String, Object> item : roleList) {
                if (dbId.equals(item.get("dbId"))) {
                    int role = StringUtil.convertToInt(item.get("role"));
                    if (role == 1) {
                        userReadonly = 1;
                    } else if (role == 2 || role == 8 || role == 9) {
                        userReadonly = 0;
                    }
                    break;
                }
            }

            if (userReadonly == -1) {
                model.addObject("dbId", 3);
                return model;
            }

            model.addObject("userReadonly", userReadonly);
            model.addObject("dbType", dbType);
            model.addObject("dbName", dbMap.getStringAttribute("dbName"));
        }
        return model;
    }

    /**
     * 查询表的一览
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/getTableList", method = RequestMethod.GET)
    public List<BaseMongoMap> getTableListByDbId(@RequestParam Map<String, Object> params) {
        String dbIdStr = (String) params.get("dbId");
        long dbId = StringUtil.convertToLong(dbIdStr);
        if (dbId == 0) {
            dbIdStr = AESCoderUtil.decode(dbIdStr);
            dbId = StringUtil.convertToLong(dbIdStr);
        }
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

        BaseMongoMap dbMap = tableService.getTableById(tblId);
        if (dbMap == null || dbMap.isEmpty()) {
            // 表不存在
            logger.warn("getColumnList 表不存在 tblId={}, userId={}", tblId, userId);
            return ApiResponseUtil.error(ErrorCode.E5101, "指定的表不存在 tblId={}", tblId);
        }
        Long dbId = dbMap.getLongAttribute("dbId");

        Map<String, Object> data = new HashMap<>();
        data.put("tblId", tblId);
        data.put("tblName", dbMap.getStringAttribute("tableName"));
        data.put("tblNameCn", dbMap.getStringAttribute("tableNameCN"));
        data.put("tblDesc", dbMap.getStringAttribute("desc"));
        data.put("lastUpd", dbMap.getLongAttribute("modifiedTime").toString()); // 这里必须转换为字符串，传long型会丢失精度

        // 列的表头定义
        List<List<Map<String, Object>>> columnsList = new ArrayList<>(1);
        columnsList.add(tableService.getColDefineByType(dbService.getDbTypeById(dbId)));
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

        BaseMongoMap tblMap = tableService.getTableById(tblId);
        if (tblMap == null || tblMap.isEmpty()) {
            // 表不存在
            logger.warn("getColumnList 表不存在 tblId={}, userId={}", tblId, userId);
            return ApiResponseUtil.error(ErrorCode.E5101, "指定的表不存在 tblId={}", tblId);
        }

        tableService.delTableById(userId, tblId);
        updateHisService.saveUpdateHis(userObj, tblMap.getLongAttribute("dbId"), tblMap, null);
        return ApiResponseUtil.success();
    }

    /**
     * 检查指定表是否已在被编辑，或者自从上次查看后被人编辑保存过了
     * 返回code:  1表示有人正在编辑　2表示已被保存过，需要刷新
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/chkTblEditable", method = RequestMethod.POST)
    public Map<String, Object> chkTblEditable(@RequestParam Map<String, String> params) {
        Long userId = (Long) request.getSession().getAttribute(Constants.KEY_USER_ID);
        if (userId == null || userId == 0) {
            logger.error("用户未登录 sessionid={}", request.getSession().getId());
            return ApiResponseUtil.error(ErrorCode.S9004, "用户未登录");
        }

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
    @ResponseBody
    @RequestMapping(value = "/ajax/forceTblEditable", method = RequestMethod.POST)
    public Map<String, Object> forceTblEditable(@RequestParam Map<String, String> params) {
        Long userId = (Long) request.getSession().getAttribute(Constants.KEY_USER_ID);
        if (userId == null || userId == 0) {
            logger.error("用户未登录 sessionid={}", request.getSession().getId());
            return ApiResponseUtil.error(ErrorCode.S9004, "用户未登录");
        }

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
    @ResponseBody
    @RequestMapping(value = "/ajax/endEditable", method = RequestMethod.POST)
    public Map<String, Object> endEditable(@RequestParam Map<String, String> params) {
        Long userId = (Long) request.getSession().getAttribute(Constants.KEY_USER_ID);
        if (userId == null || userId == 0) {
            logger.error("用户未登录 sessionid={}", request.getSession().getId());
            return ApiResponseUtil.error(ErrorCode.S9004, "用户未登录");
        }

//        long tblId = StringUtil.convertToLong(params.get("tableId"));
//        if (tblId == 0) {
//            logger.warn("forceTblEditable 缺少参数tableId");
//            return ApiResponseUtil.error(ErrorCode.W1001, "缺少参数tableId");
//        }
//
//        if (tblId < 1000) {
//            logger.warn("forceTblEditable 新建表不需要检查编辑冲突");
//            return ApiResponseUtil.success();
//        }
//        BaseMongoMap tblMap = tableService.getTableById(tblId);
//        if (tblMap == null || tblMap.isEmpty()) {
//            // 表不存在
//            logger.warn("chkTblEditable 表不存在 tblId={}, userId={}", tblId, userId);
//            return ApiResponseUtil.error(ErrorCode.E5101, "指定的表不存在 tblId={}", tblId);
//        }
//
//        tableService.startEditTable(userId, tblId);
        return ApiResponseUtil.success();
    }

}
