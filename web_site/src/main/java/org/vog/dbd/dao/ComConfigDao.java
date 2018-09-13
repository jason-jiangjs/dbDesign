package org.vog.dbd.dao;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.vog.common.base.dao.mongo.BaseMongoDao;


import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;

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

    /**
     * 取得配置属性值
     */
    public Object getProperty(String key) {
        Query query = new Query(where("propName").is(key));
        query.fields().exclude("_id");
        query.fields().include("propValue");
        Map rst = mongoTemplate.findOne(query, Map.class, COLL_NAME);
        if (rst == null) {
            return null;
        }
        return rst.get("propValue");
    }
}
