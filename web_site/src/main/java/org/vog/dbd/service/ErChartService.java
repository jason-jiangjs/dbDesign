package org.vog.dbd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.vog.common.base.model.mongo.BaseMongoMap;
import org.vog.common.base.service.BaseService;
import org.vog.common.util.DateTimeUtil;
import org.vog.dbd.dao.ErChartDao;

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
        infoMap.put("modifier", userId);
        infoMap.put("modifiedTime", DateTimeUtil.getNowTime());
        erChartDao.updateObject(ercId, infoMap, false);
    }
}
