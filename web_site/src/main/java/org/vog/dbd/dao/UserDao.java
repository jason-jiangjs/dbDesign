package org.vog.dbd.dao;

import org.springframework.stereotype.Repository;
import org.vog.common.base.dao.mongo.BaseMongoDao;

/**
 * 用户信息
 */
@Repository
public class UserDao extends BaseMongoDao {

    // mongo表名
    private static final String COLL_NAME = "user";

    @Override
    public String getTableName() {
        return COLL_NAME;
    }

}
