package org.dbm.dbd.service;

import org.dbm.common.base.model.mongo.BaseMongoMap;
import org.dbm.common.base.service.BaseService;
import org.dbm.common.util.DateTimeUtil;
import org.dbm.dbd.dao.TableDao;
import org.dbm.dbd.web.util.BizCommUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class TableService extends BaseService {

    @Autowired
    private TableDao tableDao;

    /**
     * 查询指定数据库的表一览, 优先使用名称查询
     */
    public List<BaseMongoMap> getTableNameList(String tblName, long dbId, int type, int page, int limit) {
        if (dbId == 0 && tblName == null) {
            return Collections.EMPTY_LIST;
        }

        Query queryObj = new Query(where("dbId").is(dbId));
        if (tblName != null) {
            // 这里查询表名和别名
            Criteria criteria0 = new Criteria();
            Criteria criteria1 = Criteria.where("tableName").regex(tblName, "i");
            Criteria criteria2 = Criteria.where("aliasName").regex(tblName, "i");
            criteria0.orOperator(criteria1, criteria2);
            queryObj.addCriteria(criteria0);
        }
        queryObj.addCriteria(where("viewType").is(type));
        queryObj.addCriteria(where("auditData.valid").is(true));
        queryObj.fields().include("tableName");
        queryObj.fields().include("aliasName");
        queryObj.fields().include("currEditorId");
        queryObj.fields().include("currEditorName");
        queryObj.fields().include("startEditTime");
        queryObj.fields().include("auditData.modifierName");
        queryObj.fields().include("auditData.modifiedTime");

        queryObj.with(new Sort(Sort.Direction.ASC, "tableName"));
        if (limit > 0) {
            queryObj.skip((page - 1) * limit);
            queryObj.limit(limit);
        }
        return tableDao.getMongoMapList(queryObj);
    }

    /**
     * 统计表个数
     */
    public long countTableList(String tblName, long dbId, int type) {
        Query queryObj = new Query(where("dbId").is(dbId));
        queryObj.addCriteria(where("viewType").is(type));
        queryObj.addCriteria(where("auditData.valid").is(true));
        if (tblName != null) {
            // 这里查询表名和别名
            Criteria criteria0 = new Criteria();
            Criteria criteria1 = Criteria.where("tableName").regex(tblName, "i");
            Criteria criteria2 = Criteria.where("aliasName").regex(tblName, "i");
            criteria0.orOperator(criteria1, criteria2);
            queryObj.addCriteria(criteria0);
        }
        return tableDao.countList(queryObj);
    }

    /**
     * 根据tableId查询指定的表
     */
    public BaseMongoMap getTableById(long tblId) {
        Query queryObj = new Query(where("_id").is(tblId));
        queryObj.addCriteria(where("auditData.valid").is(true));
        queryObj.fields().include("tableName");
        queryObj.fields().include("aliasName");
        queryObj.fields().include("desc");
        queryObj.fields().include("column_list");
        queryObj.fields().include("index_list");
        queryObj.fields().include("dbId");
        queryObj.fields().include("auditData.modifiedTime");
        queryObj.fields().include("currEditorId");
        queryObj.fields().include("startEditTime");
        return tableDao.getMongoMap(queryObj);
    }

    /**
     * 批量查询指定的表
     * 这里的参数从后台seesion获取，防止前台篡改tblId,操作没有权限访问的表
     */
    public List<BaseMongoMap> getTableByIds(Long dbId, List<Long> tblIds, boolean needDetail, boolean needSorted) {
        Query queryObj = new Query(where("_id").in(tblIds));
        queryObj.addCriteria(where("dbId").is(dbId));
        queryObj.addCriteria(where("auditData.valid").is(true));
        queryObj.fields().include("tableName");
        queryObj.fields().include("aliasName");
        if (needDetail) {
            queryObj.fields().include("column_list");
            queryObj.fields().include("index_list");
        }
        queryObj.fields().include("auditData.modifiedTime");
        queryObj.fields().include("currEditorId");
        queryObj.fields().include("currEditorName");
        queryObj.fields().include("startEditTime");
        if (needSorted) {
            queryObj.with(new Sort(Sort.Direction.ASC, "tableName"));
        }
        return tableDao.getMongoMapList(queryObj);
    }

    /**
     * 删除指定的表(逻辑删除)
     */
    public void delTableById(long userId, long tblId) {
        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("auditData.valid", false);
        infoMap.put("auditData.modifierId", userId);
        infoMap.put("auditData.modifierName", BizCommUtil.getLoginUserName());
        infoMap.put("auditData.modifiedTime", DateTimeUtil.getDate().getTime());
        tableDao.updateObject(tblId, infoMap, false);
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
        infoMap.put("currEditorName", BizCommUtil.getLoginUserName());
        infoMap.put("startEditTime", DateTimeUtil.getDate().getTime());

        tableDao.updateObject(tblId, infoMap, false);
    }

    /**
     * 结束编辑表(没有通常意义下的结束编辑，应该是取消编辑)
     * 正常情况下应该是和保存时一起
     */
    public void endEditTable(long tblId) {
        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("currEditorId", null);
        infoMap.put("currEditorName", null);
        infoMap.put("startEditTime", null);

        tableDao.updateObject(tblId, infoMap, false);
    }

    /**
     * 结束编辑表(页面超时的情况下才使用该方法来清除编辑标识)
     */
    public void endEditTable4User(long userId) {
        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("currEditorId", null);
        infoMap.put("currEditorName", null);
        infoMap.put("startEditTime", null);

        Query query = new Query(where("currEditorId").is(userId));
        tableDao.updateObject(query, infoMap, false, true);
    }


    /**
     * 获取正在被编辑的表（只查询前10条数据）
     */
    public List<BaseMongoMap> getTableInEditing(long dbId) {
        Query queryObj = new Query(where("dbId").is(dbId));
        queryObj.addCriteria(where("currEditorId").gt(0));
        queryObj.addCriteria(where("auditData.valid").is(true));

        queryObj.fields().include("tableName");
        queryObj.fields().include("currEditorName");
        queryObj.fields().include("startEditTime");
        queryObj.limit(10);
        return tableDao.getMongoMapList(queryObj);
    }
}
