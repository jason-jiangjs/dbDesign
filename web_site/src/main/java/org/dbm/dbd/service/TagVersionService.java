package org.dbm.dbd.service;

import org.dbm.common.base.model.mongo.BaseMongoMap;
import org.dbm.common.base.service.BaseService;
import org.dbm.dbd.dao.ProjectTableVerTagXrefDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class TagVersionService extends BaseService {

    @Autowired
    private ProjectTableVerTagXrefDao verTagXrefDao;

    /**
     * 保存版本数据
     */
    public void saveTagDetail(Map dataObj) {
        verTagXrefDao.insertObject(dataObj);
    }

    /**
     * 查询指定数据库的历史版本
     */
    public BaseMongoMap getTagInfoByName(String tagName) {
        Query queryObj = new Query(where("tagName").is(tagName));
        queryObj.addCriteria(where("auditData.valid").is(true));
        queryObj.fields().exclude("_id");
        queryObj.fields().include("searchKeys");
        return verTagXrefDao.getMongoMap(queryObj);
    }

    /**
     * 查询指定数据库的所有历史版本(倒序)
     */
    public List<BaseMongoMap> getTagList(long dbId, int page, int limit) {
        Query queryObj = new Query(where("dbId").is(dbId));
        queryObj.addCriteria(where("auditData.valid").is(true));
        queryObj.fields().exclude("_id");
        queryObj.fields().include("tagName");
        queryObj.fields().include("tagDesc");
        queryObj.fields().include("auditData.modifierName");
        queryObj.fields().include("auditData.modifiedTime");

        if (limit > 0) {
            queryObj.skip((page - 1) * limit);
            queryObj.limit(limit);
        }
        queryObj.with(new Sort(Sort.Direction.DESC, "auditData.modifiedTime"));
        return verTagXrefDao.getMongoMapList(queryObj);
    }
}
