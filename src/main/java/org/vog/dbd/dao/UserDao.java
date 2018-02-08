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
     * 根据登录帐号查询用户
     */
    public BaseMongoMap getUserByAccount(String userId) {
        Query queryObj = new Query(where("userId").is(userId));
        queryObj.addCriteria(where("deleteFlg").is(false));
        queryObj.fields().include("userId");
        queryObj.fields().include("userName");
        queryObj.fields().include("password");
        queryObj.fields().include("favorite");

        return mongoTemplate.findOne(queryObj, BaseMongoMap.class, COLL_NAME);
    }

//    /**
//     * 根据ID查询用户
//     */
//    public BaseMongoMap getUserById(long innId) {
//        Query queryObj = new Query(where("innId").is(innId));
//        queryObj.addCriteria(where("deleteFlg").is(false));
//        queryObj.fields().exclude("_id");
//        queryObj.fields().include("nickName");
//        queryObj.fields().include("password");
//        queryObj.fields().include("headIconUrl");
//
//        return mongoTemplate.findOne(queryObj, BaseMongoMap.class, COLL_NAME);
//    }

    /**
     * 保存用户的默认工作数据库
     */
    public WriteResult setUserFavorite(long userId, long dbId) {
        Query query = new Query(where("_id").is(userId));

        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("favorite", dbId);
        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put("$set", infoMap);
        Update update = new BasicUpdate(basicDBObject);

        return mongoTemplate.upsert(query, update, COLL_NAME);
    }

}
