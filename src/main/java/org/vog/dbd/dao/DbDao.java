package org.vog.dbd.dao;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.vog.base.dao.mongo.BaseMongoDao;
import org.vog.base.model.mongo.BaseMongoMap;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * 库定义
 */
@Repository
public class DbDao extends BaseMongoDao {

    // mongo表名
    private static final String COLL_NAME = "db_list";

    @Override
    public String getTableName() {
        return COLL_NAME;
    }

    /**
     * 查询指定数据库
     */
    public BaseMongoMap findDbById(long dbId) {
        Query queryObj = new Query(where("_id").is(dbId));
        return mongoTemplate.findOne(queryObj, BaseMongoMap.class, COLL_NAME);
    }

    /**
     * 查询数据库的表一览
     */
    public List<BaseMongoMap> findDbList(int page, int limit, boolean checked) {
        Query queryObj = new Query();
        if (checked) {
            queryObj.addCriteria(where("deleteFlg").is(false));
        }
        if (limit > 0) {
            queryObj.skip((page - 1) * limit);
            queryObj.limit(limit);
        }

        return mongoTemplate.find(queryObj, BaseMongoMap.class, COLL_NAME);
    }

    /**
     * 统计数据库个数(全部)
     */
    public long countDbList() {
        Query queryObj = new Query();
        return mongoTemplate.count(queryObj, COLL_NAME);
    }


}
