package org.vog.dbd.dao;

import org.vog.base.dao.mongo.BaseMongoDao;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 系统属性配置
 */
@Repository
public class ComConfigDao extends BaseMongoDao {

    //mongo表名
    private static final String COLL_NAME = "com_config";

    /**
     * 取得配置属性值
     */
    public List<Map> getProperties() {
        Query query = new Query();
        query.fields().exclude("_id");
        query.fields().exclude("remarks");
        return mongoTemplate.find(query, Map.class, COLL_NAME);
    }

}
