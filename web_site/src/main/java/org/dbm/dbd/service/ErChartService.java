package org.dbm.dbd.service;

import org.dbm.common.base.model.mongo.BaseMongoMap;
import org.dbm.common.base.service.BaseService;
import org.dbm.common.util.CommUtil;
import org.dbm.common.util.DateTimeUtil;
import org.dbm.dbd.dao.ErChartDao;
import org.dbm.dbd.dao.ErChartHistoryDao;
import org.dbm.dbd.web.util.BizCommUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class ErChartService extends BaseService {

    @Autowired
    private ErChartDao erChartDao;

    @Autowired
    private ErChartHistoryDao erChartHistoryDao;

    @Autowired
    private ComSequenceService sequenceService;

    /**
     * 查询指定ER图(目前限定一个数据库只能有一个ER图)
     */
    public BaseMongoMap findErChartByDbId(long dbId) {
        Query queryObj = new Query(where("dbId").is(dbId));
        queryObj.addCriteria(where("auditData.valid").is(true));
        return erChartDao.getMongoMap(queryObj);
    }

    /**
     * 创建一个新的空白ER图, 内容从配置文件中取得
     */
    public void createBlankErChart(long userId, Long dbId, String title) {
        Map<String, Object> infoMap = new HashMap<>();
        if (title != null) {
            infoMap.put("title", title);
        }

        long erId = sequenceService.getNextSequence(ComSequenceService.ComSequenceName.FX_USER_ID);
        infoMap.put("_id", erId);
        infoMap.put("dbId", dbId);
        infoMap.put("content", CommUtil.getFileString(new ClassPathResource("public/default-blank-diagram.xml")));
        long currTime = DateTimeUtil.getDate().getTime();
        infoMap.put("auditData.creatorId", userId);
        infoMap.put("auditData.createdTime", currTime);
        infoMap.put("auditData.modifierId", userId);
        infoMap.put("auditData.modifierName", BizCommUtil.getLoginUserName());
        infoMap.put("auditData.modifiedTime", currTime);
        erChartDao.updateObject(erId, infoMap, true);
    }

    /**
     * 保存ER图
     */
    public void saveErChartInfo(long userId, Long dbId, Long ercId, String title, String info) {
        Map<String, Object> infoMap = new HashMap<>();
        if (title != null) {
            infoMap.put("title", title);
        }
        long currTime = DateTimeUtil.getDate().getTime();
        infoMap.put("content", info);
        infoMap.put("versionId", currTime);
        infoMap.put("auditData.modifierId", userId);
        infoMap.put("auditData.modifierName", BizCommUtil.getLoginUserName());
        infoMap.put("auditData.modifiedTime", currTime);
        erChartDao.updateObject(ercId, infoMap, false);

        // 保存到ER图历史记录表
        Map<String, Object> historyInfo = new HashMap<>();
        if (title != null) {
            historyInfo.put("title", title);
        }
        historyInfo.put("dbId", dbId);
        historyInfo.put("erChartId", ercId);
        historyInfo.put("content", info);
        historyInfo.put("versionId", currTime);
        historyInfo.put("auditData.modifierId", userId);
        historyInfo.put("auditData.modifierName", BizCommUtil.getLoginUserName());
        historyInfo.put("auditData.modifiedTime", currTime);
        erChartHistoryDao.insertObject(historyInfo);
    }
}
