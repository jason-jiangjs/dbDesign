package org.vog.base.dao.mongo;

import com.mongodb.WriteResult;
import org.vog.base.model.mongo.BaseMongoMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.Map;

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
     * 添加记录
     * 子类要使用此方法必须覆盖getTableName()方法
     */
    public void addData(Map<String, Object> dataObj) {
        mongoTemplate.save(dataObj, getTableName());
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
     * (单条)更新
     */
    public WriteResult update(Query query, Update update) {
        return mongoTemplate.updateFirst(query, update, getTableName());
    }

    /**
     * 删除记录
     */
    public WriteResult remove(Query queryObj) {
        return mongoTemplate.remove(queryObj, getTableName());
    }

}
