package org.dbm.common.base.dao.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import org.springframework.data.mongodb.core.query.BasicUpdate;
import org.dbm.common.base.model.mongo.BaseMongoMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;

public abstract class BaseMongoDao {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected MongoTemplate mongoTemplate;

    /**
     * 取得该DAO所对应的表名称
     */
    public String getTableName() {
        return "";
    }

    /**
     * 根据指定条件查询数据
     * 子类要使用此方法必须覆盖getTableName()方法
     */
    public BaseMongoMap getMongoMap(Query queryObj) {
        return mongoTemplate.findOne(queryObj, BaseMongoMap.class, getTableName());
    }

    /**
     * 根据指定条件查询数据
     * 子类要使用此方法必须覆盖getTableName()方法
     */
    public List<BaseMongoMap> getMongoMapList(Query queryObj) {
        return mongoTemplate.find(queryObj, BaseMongoMap.class, getTableName());
    }

    /**
     * 统计指定条件的数据数
     * 子类要使用此方法必须覆盖getTableName()方法
     */
    public long countList(Query queryObj) {
        return mongoTemplate.count(queryObj, getTableName());
    }

    /**
     * 插入数据，指定表
     */
    public void insertObject(List<DBObject> dataObj, String tblName) {
        mongoTemplate.insert(dataObj, tblName);
    }

    /**
     * 保存数据(查询关键字为"_id")
     */
    public WriteResult updateObject(long id, Map<String, Object> infoMap, boolean upsert) {
        Query query = new Query(where("_id").is(id));
        return updateObject(query, infoMap, upsert, false);
    }

    public WriteResult updateObject(Query query, Map<String, Object> infoMap, boolean upsert, boolean multi) {
        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put("$set", infoMap);
        Update update = new BasicUpdate(basicDBObject);

        if (upsert) {
            return mongoTemplate.upsert(query, update, getTableName());
        } else {
            if (multi) {
                return mongoTemplate.updateMulti(query, update, getTableName());
            } else {
                return mongoTemplate.updateFirst(query, update, getTableName());
            }
        }
    }

}
