package org.vog.dbd.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.WriteResult;
import org.springframework.data.mongodb.core.query.BasicUpdate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.vog.base.dao.mongo.BaseMongoDao;
import org.vog.base.model.mongo.BaseMongoMap;

import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * 表定义
 */
@Repository
public class TableDao extends BaseMongoDao {

    // mongo表名
    private static final String COLL_NAME = "table_list";

    @Override
    public String getTableName() {
        return COLL_NAME;
    }

    /**
     * 查询指定数据库的表一览
     */
    public List<BaseMongoMap> findTableList(long dbId, int type) {
        Query queryObj = new Query(where("dbId").is(dbId));
        queryObj.addCriteria(where("type").is(type));
        queryObj.addCriteria(where("deleteFlg").is(false));
        queryObj.fields().include("tableName");

        return mongoTemplate.find(queryObj, BaseMongoMap.class, COLL_NAME);
    }

    /**
     * 查询指定数据库的表一览
     */
    public List<BaseMongoMap> findTableListByName(String tblName, int type) {
        Query queryObj = new Query(where("tableName").regex(tblName, "i"));
        queryObj.addCriteria(where("type").is(type));
        queryObj.addCriteria(where("deleteFlg").is(false));
        queryObj.fields().include("tableName");

        return mongoTemplate.find(queryObj, BaseMongoMap.class, COLL_NAME);
    }

    /**
     * 查询指定表的定义
     */
    public BaseMongoMap findTableById(long tblId) {
        Query queryObj = new Query(where("_id").is(tblId));
        queryObj.addCriteria(where("deleteFlg").is(false));
        queryObj.fields().include("tableName");
        queryObj.fields().include("tableNameCN");
        queryObj.fields().include("desc");
        queryObj.fields().include("column_list");

        return mongoTemplate.findOne(queryObj, BaseMongoMap.class, COLL_NAME);
    }

    /**
     * 修改(保存)指定表的定义
     */
    public WriteResult saveTblDefInfo(long tblId, Map infoMap) {
        Query query = new Query(where("_id").is(tblId));

        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put("$set", infoMap);
        Update update = new BasicUpdate(basicDBObject);

        return mongoTemplate.upsert(query, update, COLL_NAME);
    }

    public WriteResult delTableById(long tblId) {
        Query query = new Query(where("_id").is(tblId));
        return mongoTemplate.remove(query, COLL_NAME);
    }
}
