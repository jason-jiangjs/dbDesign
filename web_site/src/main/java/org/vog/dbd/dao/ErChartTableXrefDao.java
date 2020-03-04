package org.vog.dbd.dao;

import org.springframework.stereotype.Repository;
import org.vog.common.base.dao.mongo.BaseMongoDao;

/**
 * ER图-表设计的关联信息
 */
@Repository
public class ErChartTableXrefDao extends BaseMongoDao {

    // mongo表名
    private static final String COLL_NAME = "er_table_xref";

    @Override
    public String getTableName() {
        return COLL_NAME;
    }

}
