package org.dbm.dbd.web.controller;

import org.dbm.common.base.controller.BaseController;
import org.dbm.common.base.model.mongo.BaseMongoMap;
import org.dbm.common.util.ApiResponseUtil;
import org.dbm.common.util.StringUtil;
import org.dbm.dbd.service.ErChartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * er图设计相关的操作，目前只有读取和保存
 */
@RestController
public class ErChartController extends BaseController {

    @Autowired
    private ErChartService erChartService;

    /**
     * 读取er图
     */
    @RequestMapping(value = "/ajax/er/getErChartInfo", method = RequestMethod.POST)
    public Map<String, Object> getErChartInfo(@RequestBody Map<String, Object> params) {
        long dbId = StringUtil.convertToLong(params.get("dbId"));
        long tableId = StringUtil.convertToLong(params.get("tableId"));

        BaseMongoMap erObj = erChartService.findErChartById(1001);
        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("title", erObj.getStringAttribute("title"));
        infoMap.put("content", erObj.getStringAttribute("content"));
        return ApiResponseUtil.success(infoMap);
    }

    /**
     * 保存er图
     */
    @RequestMapping(value = "/ajax/er/saveErChartInfo", method = RequestMethod.POST)
    public Map<String, Object> saveErChartInfo(@RequestBody Map<String, Object> params) {
        String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + (String) params.get("content");
        erChartService.saveErChartInfo(1001, 1001L, null, content);
        return ApiResponseUtil.success();
    }
}
