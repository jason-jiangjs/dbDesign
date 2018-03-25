package org.vog.dbd.service;

import com.mongodb.BasicDBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.BasicUpdate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
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
    public List<BaseMongoMap> getTableList(String tblName, long dbId, int type) {
        if (dbId == 0 && tblName == null) {
            return Collections.EMPTY_LIST;
        }
        return tableDao.findTableList(dbId, tblName, type);
    }

    /**
     * 查询指定表名称查询
     * 这里返回数组形式是为了防止万一有数据重复
     */
    public List<BaseMongoMap> findTableByName(long dbId, String tblName) {
        if (dbId == 0 && tblName == null) {
            return Collections.EMPTY_LIST;
        }
        return tableDao.findTableByName(dbId, tblName);
    }

    /**
     * 查询指定的表
     */
    public BaseMongoMap getTableById(long tblId) {
        return tableDao.findTableById(tblId);
    }

    /**
     * 删除指定的表(逻辑删除)
     */
    public void delTableById(long userId, long tblId) {
        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("deleteFlg", true);
        infoMap.put("modifier", userId);
        infoMap.put("modifiedTime", DateTimeUtil.getDate());
        tableDao.saveObject(tblId, infoMap, false);
    }

    /**
     * 查询表定义
     */
    public List<Map<String, Object>> getColDefineByType(int type) {
        BaseMongoMap colMap = columnDefineDao.getColDefineByType(type);
        if (colMap == null) {
            return null;
        }
        return (List<Map<String, Object>>) colMap.get("head_define");
    }

    /**
     * 修改(保存)指定表的定义
     */
    public void saveTblDefInfo(Long tblId, Map infoMap) {
        tableDao.saveObject(tblId, infoMap, true);
    }

    /**
     * 开始编辑表
     */
    public void startEditTable(long userId, long tblId) {
        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("currEditorId", userId);
        infoMap.put("startEditTime", DateTimeUtil.getDate());

        Query query = new Query(where("_id").is(tblId));

        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put("$set", infoMap);
        Update update = new BasicUpdate(basicDBObject);
        tableDao.update(query, update);
    }
}
