package org.dbm.dbd.dao;

import org.dbm.common.base.dao.mongo.BaseMongoDao;
import org.springframework.stereotype.Repository;

/**
 * 表定义
 */
@Repository
public class TableHistoryDao extends BaseMongoDao {

    // mongo表名
    private static final String COLL_NAME = "table_history";

    @Override
    public String getTableName() {
        return COLL_NAME;
    }

}
