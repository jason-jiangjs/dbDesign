package org.dbm.dbd.web.controller.admin;

import org.apache.commons.lang3.StringUtils;
import org.dbm.common.ErrorCode;
import org.dbm.common.base.controller.BaseController;
import org.dbm.common.base.model.mongo.BaseMongoMap;
import org.dbm.common.util.ApiResponseUtil;
import org.dbm.common.util.DateTimeUtil;
import org.dbm.common.util.StringUtil;
import org.dbm.dbd.service.ComSequenceService;
import org.dbm.dbd.service.DbService;
import org.dbm.dbd.service.ErChartService;
import org.dbm.dbd.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库管理（添加／删除／修改等）
 */
@RestController
public class DbMngController extends BaseController {

    @Autowired
    private DbService dbService;

    @Autowired
    private UserService userService;

    @Autowired
    private ComSequenceService sequenceService;

    @Autowired
    private ErChartService erChartService;

    /**
     * 查询数据库一览数据
     */
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
    @RequestMapping(value = "/ajax/mng/saveDbInfo", method = RequestMethod.POST)
    public Map<String, Object> saveDbInfo(@RequestBody Map<String, Object> params) {
        Long adminId = getLoginUserId();
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
            dbId = sequenceService.getNextSequence(ComSequenceService.ComSequenceName.FX_USER_ID);
            params.put("_id", dbId);
            params.put("deleteFlg", false);
            params.put("creator", adminId);
            params.put("createdTime", DateTimeUtil.getNowTime());
            dbService.saveDb(dbId, params);

            // 创建一个默认的空白ER图, 关联到该数据库,
            // 这里另一种方案是懒加载，到第一次读取的时候再创建
            erChartService.createBlankErChart(adminId, dbId, dbName);

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
     * 删除数据库项目
     */
    @RequestMapping(value = "/ajax/mng/delDb", method = RequestMethod.POST)
    public Map<String, Object> deleteDb(@RequestParam Map<String, String> params) {
        Long dbId = StringUtil.convertToLong(params.get("dbId"));
        if (dbId == 0) {
            logger.warn("deleteDb 缺少参数 dbId");
            return ApiResponseUtil.error(ErrorCode.W1001, "缺少参数dbId，请选择数据库后再操作。");
        }
        Long userId = getLoginUserId();
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

}
