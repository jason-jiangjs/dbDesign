package org.vog.dbd.dao;

import com.mongodb.WriteResult;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.vog.base.dao.mongo.BaseMongoDao;
import org.vog.base.model.mongo.BaseMongoMap;

import java.util.List;

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
    public List<BaseMongoMap> findTableList(long dbId, String tblName, int type) {
        Query queryObj = new Query(where("dbId").is(dbId));
        if (tblName != null) {
            queryObj.addCriteria(where("tableName").regex(tblName, "i"));
        }
        queryObj.addCriteria(where("type").is(type));
        queryObj.addCriteria(where("deleteFlg").is(false));
        queryObj.fields().include("tableName");

        queryObj.with(new Sort(Sort.Direction.ASC, "tableName"));
        return mongoTemplate.find(queryObj, BaseMongoMap.class, COLL_NAME);
    }

    /**
     * 查询指定表名称查询
     */
    public List<BaseMongoMap> findTableByName(long dbId, String tblName) {
        Query queryObj = new Query(where("dbId").is(dbId));
        queryObj.addCriteria(where("tableName").is(tblName));
        queryObj.addCriteria(where("deleteFlg").is(false));

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
        queryObj.fields().include("dbId");
        queryObj.fields().include("modifiedTime");

        return mongoTemplate.findOne(queryObj, BaseMongoMap.class, COLL_NAME);
    }

}
