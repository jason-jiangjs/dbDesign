package org.dbm.dbd.service;

import org.dbm.common.base.model.mongo.BaseMongoMap;
import org.dbm.common.base.service.BaseService;
import org.dbm.dbd.dao.TableHistoryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class TableHistoryService extends BaseService {

    @Autowired
    private TableHistoryDao tableHistoryDao;

    /**
     * 根据条件从历史表中查询表一览
     */
    public List<BaseMongoMap> getTableListFromHistory(String tblName, List<String> tagNameList, int type, int page, int limit, boolean needSort) {
        Query queryObj = createQueryParam4TableHistory(tblName, tagNameList, type);
        queryObj.fields().include("tableId");
        queryObj.fields().include("versionId");
        queryObj.fields().include("tableName");
        queryObj.fields().include("aliasName");
        queryObj.fields().include("auditData.modifierName");
        queryObj.fields().include("auditData.modifiedTime");

        if (needSort) {
            queryObj.with(new Sort(Sort.Direction.ASC, "tableName"));
        }
        if (limit > 0) {
            queryObj.skip((page - 1) * limit);
            queryObj.limit(limit);
        }
        return tableHistoryDao.getMongoMapList(queryObj);
    }

    /**
     * 根据条件在历史表中统计个数
     */
    public long countTableListFromHistory(String tblName, List<String> tagNameList, int type) {
        return tableHistoryDao.countList(createQueryParam4TableHistory(tblName, tagNameList, type));
    }

    /**
     * 创建查询条件对象
     */
    private Query createQueryParam4TableHistory(String tblName, List<String> tagNameList, int type) {
        Query queryObj = new Query(where("searchKey").in(tagNameList));
        if (tblName != null) {
            // 这里查询表名和别名
            Criteria criteria0 = new Criteria();
            Criteria criteria1 = Criteria.where("tableName").regex(tblName, "i");
            Criteria criteria2 = Criteria.where("aliasName").regex(tblName, "i");
            criteria0.orOperator(criteria1, criteria2);
            queryObj.addCriteria(criteria0);
        }
        if (type > 0) {
            queryObj.addCriteria(where("viewType").is(type));
        }
        queryObj.addCriteria(where("auditData.valid").is(true));
        return queryObj;
    }
}
