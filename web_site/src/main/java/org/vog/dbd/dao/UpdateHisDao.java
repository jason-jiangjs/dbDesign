package org.vog.dbd.dao;

import org.springframework.stereotype.Repository;
import org.vog.base.dao.mongo.BaseMongoDao;

/**
 * 保存操作历史
 */
@Repository
public class UpdateHisDao extends BaseMongoDao {

    // mongo表名
    private static final String COLL_NAME = "update_history";

    @Override
    public String getTableName() {
        return COLL_NAME;
    }

}
