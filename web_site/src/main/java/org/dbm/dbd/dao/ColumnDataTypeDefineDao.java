package org.dbm.dbd.dao;

import org.dbm.common.base.dao.mongo.BaseMongoDao;
import org.springframework.stereotype.Repository;

/**
 * 列定义信息
 */
@Repository
public class ColumnDataTypeDefineDao extends BaseMongoDao {

    // mongo表名
    private static final String COLL_NAME = "col_data_type_define";

    @Override
    public String getTableName() {
        return COLL_NAME;
    }

}
