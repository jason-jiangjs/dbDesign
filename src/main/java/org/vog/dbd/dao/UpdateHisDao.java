package org.vog.dbd.dao;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.vog.base.dao.mongo.BaseMongoDao;
import org.vog.base.model.mongo.BaseMongoMap;

import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * 保存操作历史
 */
@Repository
public class UpdateHisDao extends BaseMongoDao {

    // mongo表名
    private static final String COLL_NAME = "update_history";

    @Override
    public String getTableName() {
        return COLL_NAME;
    }

    /**
     * 查询操作历史一览
     */
    public List<BaseMongoMap> findUpdHisList(Long dbId, int page, int limit) {
        Query queryObj = new Query(where("dbId").is(dbId));
        if (limit > 0) {
            queryObj.skip((page - 1) * limit);
            queryObj.limit(limit);
        }

        return mongoTemplate.find(queryObj, BaseMongoMap.class, COLL_NAME);
    }

    /**
     * 统计操作历史一览个数(全部)
     */
    public long countUpdHisList(Long dbId) {
        Query queryObj = new Query(where("dbId").is(dbId));
        return mongoTemplate.count(queryObj, COLL_NAME);
    }

    /**
     * 保存操作历史
     */
    public void saveUpdateHis(Map infoMap) {
        mongoTemplate.save(infoMap, COLL_NAME);
    }

}
