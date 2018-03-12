package org.vog.dbd.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vog.base.model.mongo.BaseMongoMap;
import org.vog.base.service.BaseService;
import org.vog.common.util.AESCoderUtil;
import org.vog.dbd.dao.ColumnDefineDao;
import org.vog.dbd.dao.DbDao;
import org.vog.dbd.dao.TableDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TableService extends BaseService {

    @Autowired
    private DbDao dbDao;

    @Autowired
    private TableDao tableDao;

    @Autowired
    private ColumnDefineDao columnDefineDao;

    /**
     * 查询指定数据库
     */
    public BaseMongoMap findDbById(long dbId) {
        return dbDao.findDbById(dbId);
    }

    /**
     * 查询指定数据库
     */
    public int getDbTypeById(long dbId) {
        BaseMongoMap dbObj = dbDao.findDbById(dbId);
        int dbType = dbObj.getIntAttribute("type");
        if (dbType == 0) {
            logger.warn("未设置数据库类型 id={}", dbId);
        }
        return dbType;
    }

    /**
     * 查询数据库的表一览
     */
    public List<Map<String, Object>> findDbList() {
        List<BaseMongoMap> dbList = dbDao.findDbList(0, 0, true);
        if (dbList == null || dbList.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        List<Map<String, Object>> dataList = new ArrayList<>();
        dbList.forEach(item -> {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("id", AESCoderUtil.encode(item.getLongAttribute("_id").toString()));
            String dbTxt = StringUtils.trimToEmpty(item.getStringAttribute("dbName"));
            String cnName = StringUtils.trimToNull(item.getStringAttribute("dbNameCN"));
            if (cnName != null) {
                dbTxt = dbTxt + "  =>  " + cnName;
            }
            String dbVerStr = StringUtils.trimToNull(item.getStringAttribute("typeStr"));
            if (dbVerStr != null) {
                dbTxt = dbTxt + " (" + dbVerStr + ")";
            }
            dataMap.put("text", dbTxt);
            dataList.add(dataMap);
        });
        return dataList;
    }

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
     * 删除指定的表
     */
    public void delTableById(long tblId) {
        tableDao.delTableById(tblId);
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
        tableDao.saveTblDefInfo(tblId, infoMap);
    }

}
