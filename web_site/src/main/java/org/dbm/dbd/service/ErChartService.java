package org.dbm.dbd.service;

import org.dbm.dbd.web.util.BizCommUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.dbm.common.base.model.mongo.BaseMongoMap;
import org.dbm.common.base.service.BaseService;
import org.dbm.common.util.DateTimeUtil;
import org.dbm.dbd.dao.ErChartDao;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class ErChartService extends BaseService {

    @Autowired
    private ErChartDao erChartDao;

    /**
     * 查询指定ER图
     */
    public BaseMongoMap findErChartById(long ercId) {
        Query queryObj = new Query(where("_id").is(ercId));
        return erChartDao.getMongoMap(queryObj);
    }

    /**
     * 保存ER图
     */
    public void saveErChartInfo(long userId, Long ercId, String title, String info) {
        Map<String, Object> infoMap = new HashMap<>();
        if (title != null) {
            infoMap.put("title", title);
        }
        infoMap.put("content", info);
        infoMap.put("auditData.modifierId", userId);
        infoMap.put("auditData.modifierName", BizCommUtil.getLoginUserName());
        infoMap.put("auditData.modifiedTime", DateTimeUtil.getDate().getTime());
        erChartDao.updateObject(ercId, infoMap, false);
    }
}
