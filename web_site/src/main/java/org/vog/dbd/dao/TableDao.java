package org.vog.dbd.dao;

import org.springframework.stereotype.Repository;
import org.vog.common.base.dao.mongo.BaseMongoDao;

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

}
