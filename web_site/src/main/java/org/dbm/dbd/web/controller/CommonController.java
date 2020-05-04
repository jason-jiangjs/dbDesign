package org.dbm.dbd.web.controller;

import org.dbm.common.Constants;
import org.dbm.common.ErrorCode;
import org.dbm.common.base.controller.BaseController;
import org.dbm.common.base.model.mongo.BaseMongoMap;
import org.dbm.common.util.ApiResponseUtil;
import org.dbm.common.util.CommUtil;
import org.dbm.common.util.DateTimeUtil;
import org.dbm.common.util.StringUtil;
import org.dbm.dbd.service.DbService;
import org.dbm.dbd.service.MetaDataService;
import org.dbm.dbd.web.util.SystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 查询表及列的一览
 */
@RestController
public class CommonController extends BaseController {

    @Autowired
    private DbService dbService;

    @Autowired
    private MetaDataService metaDataService;

    /**
     * 获取数据类型的定义, 注意这里返回的对象类型
     */
    @RequestMapping(value = "/ajax/getColDataType", method = RequestMethod.GET)
    public List<Map<String, Object>> getColumnDataType(@RequestParam Map<String, String> params) {
        Long dbId = (Long) request.getSession().getAttribute(Constants.KEY_DB_ID);
        BaseMongoMap dbMap = dbService.findDbById(dbId);
        int dataTypeId = dbMap.getIntAttribute("dataTypeId");

        List<Map<String, Object>> dataList = metaDataService.getColumnDataTypeDefine(dataTypeId);
        if (dataList == null) {
            dataList = Collections.EMPTY_LIST;
        }
        return dataList;
    }

    /**
     * 查询表设计时的表头定义数据
     */
    @RequestMapping(value = "/ajax/getColDef", method = RequestMethod.GET)
    public Map<String, Object> getGridHeadDefine(@RequestParam Map<String, String> params) {
        Long dbId = (Long) request.getSession().getAttribute(Constants.KEY_DB_ID);
        BaseMongoMap dbMap = dbService.findDbById(dbId);
        int gridHeadType = dbMap.getIntAttribute("gridHeadType");

        // 列的表头定义
        List<List<Map<String, Object>>> columnsList = new ArrayList<>(1);
        columnsList.add(metaDataService.getColDefineByType(gridHeadType));
        Map<String, Object> data = new HashMap<>();
        data.put("columns", columnsList);
        return ApiResponseUtil.success(data);
    }

    /**
     * 清空缓存
     */
    @RequestMapping(value = "/admin/ui/clearCacheMap", method = RequestMethod.GET)
    public Map<String, Object> clearCacheMap(@RequestParam Map<String, String> params) {
        Integer chkKey = StringUtil.convertToInt(params.get("key"), null);
        if (chkKey == null) {
            return ApiResponseUtil.error(ErrorCode.W1001, "缺少必须的参数.");
        }
        String nowDate = DateTimeUtil.getNow(DateTimeUtil.COMPRESS_MONTHDAY_FORMAT);
        if (chkKey != Integer.parseInt(nowDate.substring(0, 2)) + Integer.parseInt(nowDate.substring(2, 4)) + 1) {
            return ApiResponseUtil.error(ErrorCode.W1001, "参数错误.");
        }

        SystemProperty.clearCacheMap();
        return ApiResponseUtil.success();
    }
}
