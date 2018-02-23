package org.vog.dbd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vog.base.model.mongo.BaseMongoMap;
import org.vog.common.util.DateTimeUtil;
import org.vog.common.util.JacksonUtil;
import org.vog.dbd.dao.UpdateHisDao;
import org.vog.dbd.web.login.CustomerUserDetails;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Service
public class UpdateHisService {

    @Autowired
    private UpdateHisDao updateHisDao;

    /**
     * 保存操作历史
     */
    public void saveUpdateHis(CustomerUserDetails userObj, long dbId, BaseMongoMap infoMap, Map<String, Object> params) {
        Map<String, Object> data = new HashMap<>();
        data.put("dbId", dbId);
        int hisType = 0;
        if (infoMap == null) {
            // 新增
            hisType = 1;
            data.put("tableId", params.get("_tbl_id"));
            data.put("tableName", params.get("_tbl_name"));
            data.put("contentAft", JacksonUtil.bean2Json(params));
        } else if (params == null) {
            // 删除
            hisType = 3;
            data.put("tableId", infoMap.getLongAttribute("_id"));
            data.put("tableName", infoMap.getStringAttribute("tableName"));
            data.put("contentBef", infoMap.toString());
        } else {
            // 修改
            hisType = 2;
            data.put("tableId", infoMap.getLongAttribute("_id"));
            data.put("tableName", infoMap.getStringAttribute("tableName"));
            data.put("contentBef", infoMap.toString());
            data.put("contentAft", JacksonUtil.bean2Json(params));
        }

        data.put("type", hisType);
        data.put("userId", userObj.getId());
        data.put("userName", userObj.getUsername());
        data.put("modifiedTime", new Timestamp(DateTimeUtil.getDate().getTime()));
        updateHisDao.saveUpdateHis(data);
    }

}
