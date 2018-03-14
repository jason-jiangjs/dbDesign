package org.vog.dbd.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.WriteResult;
import org.springframework.data.mongodb.core.query.BasicUpdate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.vog.base.dao.mongo.BaseMongoDao;
import org.vog.base.model.mongo.BaseMongoMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * 用户信息
 */
@Repository
public class UserDao extends BaseMongoDao {

    // mongo表名
    private static final String COLL_NAME = "user";

    @Override
    public String getTableName() {
        return COLL_NAME;
    }

    /**
     * 根据id查询用户
     */
    public BaseMongoMap getUserById(long iid) {
        Query queryObj = new Query(where("_id").is(iid));
        return mongoTemplate.findOne(queryObj, BaseMongoMap.class, COLL_NAME);
    }

    /**
     * 根据登录帐号查询用户
     */
    public BaseMongoMap getUserByAccount(String userId) {
        Query queryObj = new Query(where("userId").is(userId));
        queryObj.addCriteria(where("status").is(1));
        queryObj.fields().include("userId");
        queryObj.fields().include("userName");
        queryObj.fields().include("password");
        queryObj.fields().include("favorite");
        queryObj.fields().include("role");

        return mongoTemplate.findOne(queryObj, BaseMongoMap.class, COLL_NAME);
    }

    /**
     * 查询用户一览(全部)
     */
    public List<BaseMongoMap> getUserList(int page, int limit) {
        Query queryObj = new Query();
        queryObj.fields().include("userId");
        queryObj.fields().include("userName");
        queryObj.fields().include("role");
        queryObj.fields().include("status");
        queryObj.skip((page - 1) * limit);
        queryObj.limit(limit);

        return mongoTemplate.find(queryObj, BaseMongoMap.class, COLL_NAME);
    }

    /**
     * 统计用户个数(全部)
     */
    public long countUserList() {
        Query queryObj = new Query();
        return mongoTemplate.count(queryObj, COLL_NAME);
    }

    /**
     * 查询用户权限信息
     */
    public BaseMongoMap getUserRoleInfo(long userId) {
        Query queryObj = new Query(where("_id").is(userId));
        queryObj.fields().include("role");
        queryObj.fields().include("roleList");

        return mongoTemplate.findOne(queryObj, BaseMongoMap.class, COLL_NAME);
    }


}
