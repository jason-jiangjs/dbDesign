package org.dbm.dbd.dao;

import org.springframework.stereotype.Repository;
import org.dbm.common.base.dao.mongo.BaseMongoDao;

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

}
