package org.vog.dbd.dao;

import org.springframework.stereotype.Repository;
import org.vog.base.dao.mongo.BaseMongoDao;

/**
 * 列定义信息
 */
@Repository
public class ColumnDefineDao extends BaseMongoDao {

    // mongo表名
    private static final String COLL_NAME = "col_head_define";

    @Override
    public String getTableName() {
        return COLL_NAME;
    }

}
