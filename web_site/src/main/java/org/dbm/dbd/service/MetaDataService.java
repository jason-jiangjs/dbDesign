package org.dbm.dbd.service;

import org.dbm.common.base.model.mongo.BaseMongoMap;
import org.dbm.common.base.service.BaseService;
import org.dbm.dbd.dao.ColumnDataTypeDefineDao;
import org.dbm.dbd.dao.ColumnGridHeadDefineDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class MetaDataService extends BaseService {

    @Autowired
    private ColumnGridHeadDefineDao columnDefineDao;

    @Autowired
    private ColumnDataTypeDefineDao dataTypeDefineDao;

    /**
     * 查询数据类型定义
     */
    public List<Map<String, Object>> getColumnDataTypeDefine(int typeId) {
        Query queryObj = new Query(where("_id").is(typeId));
        queryObj.fields().exclude("_id");
        queryObj.fields().include("propValue");
        BaseMongoMap colMap = dataTypeDefineDao.getMongoMap(queryObj);
        if (colMap == null) {
            return null;
        }
        return (List<Map<String, Object>>) colMap.get("propValue");
    }

    /**
     * 查询表头定义
     */
    public List<Map<String, Object>> getColDefineByType(int type) {
        Query queryObj = new Query(where("type").is(type));
        queryObj.fields().exclude("_id");
        queryObj.fields().include("head_define");
        BaseMongoMap colMap = columnDefineDao.getMongoMap(queryObj);
        if (colMap == null) {
            return null;
        }
        return (List<Map<String, Object>>) colMap.get("head_define");
    }

}
