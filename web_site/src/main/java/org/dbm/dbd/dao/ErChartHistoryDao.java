package org.dbm.dbd.dao;

import org.dbm.common.base.dao.mongo.BaseMongoDao;
import org.springframework.stereotype.Repository;

/**
 * ER图历史记录
 */
@Repository
public class ErChartHistoryDao extends BaseMongoDao {

    // mongo表名
    private static final String COLL_NAME = "er_chart_history";

    @Override
    public String getTableName() {
        return COLL_NAME;
    }

}
