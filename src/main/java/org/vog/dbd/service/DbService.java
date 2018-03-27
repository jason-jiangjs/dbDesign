package org.vog.dbd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vog.base.model.mongo.BaseMongoMap;
import org.vog.base.service.BaseService;
import org.vog.common.util.DateTimeUtil;
import org.vog.dbd.dao.DbDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DbService extends BaseService {

    @Autowired
    private DbDao dbDao;

    @Autowired
    private ComSequenceService sequenceService;

    /**
     * 查询指定数据库
     */
    public BaseMongoMap findDbById(long dbId) {
        return dbDao.findDbById(dbId);
    }

    /**
     * 查询指定数据库类型
     */
    public int getDbTypeById(long dbId) {
        BaseMongoMap dbObj = dbDao.findDbById(dbId);
        int dbType = dbObj.getIntAttribute("type");
        if (dbType == 0) {
            logger.warn("未设置数据库类型 id={}", dbId);
        }
        return dbType;
    }

    /**
     * 查询数据库一览
     */
    public List<BaseMongoMap> findDbList(int page, int limit) {
        return dbDao.findDbList(page, limit, false);
    }

    /**
     * 统计用户个数
     */
    public long countDbList() {
        return dbDao.countDbList();
    }

    /**
     * 删除数据库(逻辑删除)
     */
    public void removeDb(long userId, long dbId) {
        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("deleteFlg", true);
        infoMap.put("modifier", userId);
        infoMap.put("modifiedTime", DateTimeUtil.getNowTime());
        dbDao.saveObject(dbId, infoMap, false);
    }

    /**
     * 保存数据库
     */
    public void saveDb(Long dbId, Map<String, Object> params) {
        if (dbId == null || dbId == 0) {
            dbId = sequenceService.getNextSequence(ComSequenceService.ComSequenceName.FX_USER_ID);
        }
        dbDao.saveObject(dbId, params, true);
    }
}
