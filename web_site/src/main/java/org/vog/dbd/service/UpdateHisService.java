package org.vog.dbd.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.vog.common.base.model.mongo.BaseMongoMap;
import org.vog.common.base.service.BaseService;
import org.vog.common.util.DateTimeUtil;
import org.vog.common.util.JacksonUtil;
import org.vog.dbd.dao.UpdateHisDao;
import org.vog.dbd.web.login.CustomerUserDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class UpdateHisService extends BaseService {

    @Autowired
    private UpdateHisDao updateHisDao;

    /**
     * 保存操作历史
     */
    public void saveUpdateHis(CustomerUserDetails userObj, Long dbId, BaseMongoMap infoMap, Map<String, Object> params) {
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
        data.put("modifiedTime", DateTimeUtil.getNowTime());
        updateHisDao.updateObject(999, data, true);
    }

    /**
     * 查询操作历史一览
     */
    public List<Map<String, Object>> getUpdHisList(Long dbId, int page, int limit) {
        Query queryObj = new Query(where("dbId").is(dbId));
        if (limit > 0) {
            queryObj.skip((page - 1) * limit);
            queryObj.limit(limit);
        }
        List<BaseMongoMap> hisList = updateHisDao.getMongoMapList(queryObj);
        if (hisList == null || hisList.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        List<Map<String, Object>> dataList = new ArrayList<>();
        for (BaseMongoMap item : hisList) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("name", item.getStringAttribute("userName"));
            dataMap.put("updTime", item.getStringAttribute("modifiedTime"));
            dataMap.put("type", item.getIntAttribute("type"));
            dataMap.put("target", item.getStringAttribute("tableName"));
            dataMap.put("info", item.getStringAttribute("info"));
            StringBuilder updDesc = new StringBuilder();
            String desc = StringUtils.trimToNull(item.getStringAttribute("desc"));
            if (desc != null) {
                updDesc.append(desc);
            }
            String contentBef = StringUtils.trimToNull(item.getStringAttribute("contentBef"));
            if (contentBef != null) {
                updDesc.append("\n修改前\n");
                updDesc.append(contentBef);
            }
            String contentAft = StringUtils.trimToNull(item.getStringAttribute("contentAft"));
            if (contentAft != null) {
                updDesc.append("\n修改后\n");
                updDesc.append(contentAft);
            }
            dataMap.put("desc", updDesc.toString());
            dataList.add(dataMap);
        }
        return dataList;
    }

    /**
     * 统计操作历史一览个数
     */
    public long countUpdHisList(Long dbId) {
        Query queryObj = new Query(where("dbId").is(dbId));
        return updateHisDao.countList(queryObj);
    }

}
