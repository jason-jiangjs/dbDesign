package org.dbm.dbd.web.controller;

import org.dbm.common.ErrorCode;
import org.dbm.common.base.controller.BaseController;
import org.dbm.common.base.model.mongo.BaseMongoMap;
import org.dbm.common.util.ApiResponseUtil;
import org.dbm.dbd.service.ErChartService;
import org.dbm.dbd.service.UserService;
import org.dbm.dbd.web.util.BizCommUtil;
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

    @Autowired
    private UserService userService;

    /**
     * 读取er图
     */
    @RequestMapping(value = "/ajax/er/getErChartInfo", method = RequestMethod.POST)
    public Map<String, Object> getErChartInfo(@RequestBody Map<String, Object> params) {
        long dbId = BizCommUtil.getSelectedDbId();
        long userId = BizCommUtil.getLoginUserId();

        // 要先判断是否有权限读取该er图
        if (!userService.hasReadAuthorization(userId, dbId)) {
            logger.warn("getErChartInfo 当前登录用户没有读权限 userId={} dbId={}", userId, dbId);
            return ApiResponseUtil.error(ErrorCode.E5104, "对不起,你没有权限读取该ER图");
        }

        BaseMongoMap erObj = erChartService.findErChartByDbId(dbId);
        if (erObj == null) {
            // 没有时先创建一个
            erChartService.createBlankErChart(userId, dbId, "Blank Diagram");
            erObj = erChartService.findErChartByDbId(dbId);
        }
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
        long dbId = BizCommUtil.getSelectedDbId();
        long userId = BizCommUtil.getLoginUserId();

        // 要先判断是否有权限保存该er图
        if (!userService.hasWriteAuthorization(userId, dbId)) {
            logger.warn("getErChartInfo 当前登录用户没有写权限 userId={} dbId={}", userId, dbId);
            return ApiResponseUtil.error(ErrorCode.E5104, "对不起,你没有权限修改该ER图");
        }
        BaseMongoMap erObj = erChartService.findErChartByDbId(dbId);

        String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + (String) params.get("content");
        erChartService.saveErChartInfo(userId, dbId, erObj.getLongAttribute("_id"), null, content);
        return ApiResponseUtil.success();
    }
}
