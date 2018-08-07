package org.vog.dbd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.vog.base.model.mongo.BaseMongoMap;
import org.vog.base.service.BaseService;
import org.vog.common.util.DateTimeUtil;
import org.vog.dbd.dao.ColumnDefineDao;
import org.vog.dbd.dao.TableDao;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class TableService extends BaseService {

    @Autowired
    private TableDao tableDao;

    @Autowired
    private ColumnDefineDao columnDefineDao;

    /**
     * 查询指定数据库的表一览, 优先使用名称查询
     */
    public List<BaseMongoMap> getTableNameList(String tblName, long dbId, int type) {
        if (dbId == 0 && tblName == null) {
            return Collections.EMPTY_LIST;
        }

        Query queryObj = new Query(where("dbId").is(dbId));
        if (tblName != null) {
            queryObj.addCriteria(where("tableName").regex(tblName, "i"));
        }
        queryObj.addCriteria(where("type").is(type));
        queryObj.addCriteria(where("deleteFlg").is(false));
        queryObj.fields().include("tableName");

        queryObj.with(new Sort(Sort.Direction.ASC, "tableName"));
        return tableDao.getMongoMapList(queryObj);
    }

    /**
     * 查询指定数据库的表一览, 优先使用名称查询
     */
    public List<BaseMongoMap> getTableList(long dbId, int type, String tblName) {
        Query queryObj = new Query(where("dbId").is(dbId));
        queryObj.addCriteria(where("type").is(type));
        queryObj.addCriteria(where("deleteFlg").is(false));
        if (tblName != null) {
            queryObj.addCriteria(where("tableName").regex(tblName, "i"));
        }
        queryObj.fields().include("tableName");
        queryObj.fields().include("tableNameCN");
        queryObj.fields().include("column_list");

        queryObj.with(new Sort(Sort.Direction.ASC, "tableName"));
        return tableDao.getMongoMapList(queryObj);
    }

    /**
     * 查询指定表名称查询
     * 这里返回数组形式是为了防止万一有数据重复
     */
    public List<BaseMongoMap> findTableByName(long dbId, String tblName) {
        if (dbId == 0 && tblName == null) {
            return Collections.EMPTY_LIST;
        }

        Query queryObj = new Query(where("dbId").is(dbId));
        queryObj.addCriteria(where("tableName").is(tblName));
        queryObj.addCriteria(where("deleteFlg").is(false));
        return tableDao.getMongoMapList(queryObj);
    }

    /**
     * 查询指定的表
     */
    public BaseMongoMap getTableById(long tblId) {
        Query queryObj = new Query(where("_id").is(tblId));
        queryObj.addCriteria(where("deleteFlg").is(false));
        queryObj.fields().include("tableName");
        queryObj.fields().include("tableNameCN");
        queryObj.fields().include("desc");
        queryObj.fields().include("column_list");
        queryObj.fields().include("index_list");
        queryObj.fields().include("dbId");
        queryObj.fields().include("modifiedTime");
        queryObj.fields().include("currEditorId");
        queryObj.fields().include("startEditTime");
        return tableDao.getMongoMap(queryObj);
    }

    /**
     * 删除指定的表(逻辑删除)
     */
    public void delTableById(long userId, long tblId) {
        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("deleteFlg", true);
        infoMap.put("modifier", userId);
        infoMap.put("modifiedTime", DateTimeUtil.getNowTime());
        tableDao.updateObject(tblId, infoMap, false);
    }

    /**
     * 查询表定义
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

    /**
     * 修改(保存)指定表的定义
     */
    public void saveTblDefInfo(Long tblId, Map infoMap) {
        tableDao.updateObject(tblId, infoMap, true);
    }

    /**
     * 开始编辑表
     */
    public void startEditTable(long userId, long tblId) {
        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("currEditorId", userId);
        infoMap.put("startEditTime", DateTimeUtil.getNowTime());

        tableDao.updateObject(tblId, infoMap, false);
    }

    /**
     * 结束编辑表
     */
    public void endEditTable(long tblId) {
        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("currEditorId", null);
        infoMap.put("startEditTime", null);

        tableDao.updateObject(tblId, infoMap, false);
    }

    /**
     * 结束编辑表
     */
    public void endEditTable4User(long userId) {
        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("currEditorId", null);
        infoMap.put("startEditTime", null);

        Query query = new Query(where("currEditorId").is(userId));
        tableDao.updateObject(query, infoMap, false, true);
    }

}
