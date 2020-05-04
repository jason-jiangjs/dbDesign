package org.dbm.dbd.dao;

import org.dbm.common.base.dao.mongo.BaseMongoDao;
import org.springframework.stereotype.Repository;

/**
 * 项目/数据库工程-表设计的版本关联信息
 */
@Repository
public class ProjectTableVerTagXrefDao extends BaseMongoDao {

    // mongo表名
    private static final String COLL_NAME = "table_db_tag_xref";

    @Override
    public String getTableName() {
        return COLL_NAME;
    }

}
