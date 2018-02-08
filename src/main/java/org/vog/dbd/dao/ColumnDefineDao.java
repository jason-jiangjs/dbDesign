package org.vog.dbd.dao;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.vog.base.dao.mongo.BaseMongoDao;
import org.vog.base.model.mongo.BaseMongoMap;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * 用户信息
 */
@Repository
public class ColumnDefineDao extends BaseMongoDao {

    // mongo表名
    private static final String COLL_NAME = "col_head_define";

    @Override
    public String getTableName() {
        return COLL_NAME;
    }

    /**
     * 根据登录帐号（手机号）查询用户
     */
    public BaseMongoMap getColDefineByType(int type) {
        Query queryObj = new Query(where("type").is(type));
        queryObj.fields().exclude("_id");
        queryObj.fields().include("head_define");

        return mongoTemplate.findOne(queryObj, BaseMongoMap.class, COLL_NAME);
    }

}
