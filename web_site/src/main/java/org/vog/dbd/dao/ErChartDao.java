package org.vog.dbd.dao;

import org.springframework.stereotype.Repository;
import org.vog.common.base.dao.mongo.BaseMongoDao;

/**
 * ER图信息
 */
@Repository
public class ErChartDao extends BaseMongoDao {

    // mongo表名
    private static final String COLL_NAME = "er_chart_list";

    @Override
    public String getTableName() {
        return COLL_NAME;
    }

}
