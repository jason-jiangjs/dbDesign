package org.dbm.dbd.service;

import org.dbm.common.base.model.mongo.BaseMongoMap;
import org.dbm.common.base.service.BaseService;
import org.dbm.dbd.dao.DbDao;
import org.dbm.dbd.web.util.BizCommUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class DbService extends BaseService {

    @Autowired
    private DbDao dbDao;

    /**
     * 查询指定数据库
     */
    public BaseMongoMap findDbById(long dbId) {
        Query queryObj = new Query(where("_id").is(dbId));
        return dbDao.getMongoMap(queryObj);
    }

    /**
     * 查询指定数据库类型
     */
    public int getDbTypeById(long dbId) {
        BaseMongoMap dbObj = findDbById(dbId);
        int dbType = dbObj.getIntAttribute("gridHeadType");
        if (dbType == 0) {
            logger.warn("未设置数据库类型 id={}", dbId);
        }
        return dbType;
    }

    /**
     * 查询数据库一览
     */
    public List<BaseMongoMap> findDbList(int page, int limit, boolean checked) {
        Query queryObj = new Query();
        if (checked) {
            queryObj.addCriteria(where("auditData.valid").is(true));
        }
        if (limit > 0) {
            queryObj.skip((page - 1) * limit);
            queryObj.limit(limit);
        }
        return dbDao.getMongoMapList(queryObj);
    }

    /**
     * 统计数据库个数(全部)
     */
    public long countDbList() {
        return dbDao.countList(null);
    }

    /**
     * 删除数据库项目(逻辑删除)
     */
    public void removeDb(long userId, long dbId) {
        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("projectStatus", 4);
        BizCommUtil.setModifyAuditData(infoMap);
        dbDao.updateObject(dbId, infoMap, false);
    }

    /**
     * 更新数据库信息
     */
    public void saveDb(Long dbId, Map<String, Object> params) {
        dbDao.updateObject(dbId, params, true);
    }

}
