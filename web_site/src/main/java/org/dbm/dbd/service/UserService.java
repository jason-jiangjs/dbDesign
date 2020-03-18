package org.dbm.dbd.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.dbm.common.base.model.mongo.BaseMongoMap;
import org.dbm.common.base.service.BaseService;
import org.dbm.common.util.DateTimeUtil;
import org.dbm.common.util.StringUtil;
import org.dbm.dbd.dao.UserDao;

import java.util.*;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class UserService extends BaseService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private ComSequenceService sequenceService;

    @Autowired
    private DbService dbService;

    /**
     * 根据id查询用户
     */
    public BaseMongoMap getUserById(long iid) {
        Query queryObj = new Query(where("_id").is(iid));
        return userDao.getMongoMap(queryObj);
    }

    /**
     * 保存用户的默认工作数据库
     */
    public void setUserFavorite(long userIId, long dbId) {
        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("favorite", dbId);
        infoMap.put("modifier", userIId);
        infoMap.put("modifiedTime", DateTimeUtil.getNowTime());
        userDao.updateObject(userIId, infoMap, false);
    }

    /**
     * 查询用户一览
     */
    public List<BaseMongoMap> findUserList(int page, int limit) {
        Query queryObj = new Query();
        queryObj.fields().include("userId");
        queryObj.fields().include("userName");
        queryObj.fields().include("role");
        queryObj.fields().include("status");
        queryObj.fields().include("from");
        if (limit > 0) {
            queryObj.skip((page - 1) * limit);
            queryObj.limit(limit);
        }
        return userDao.getMongoMapList(queryObj);
    }

    /**
     * 统计用户个数
     */
    public long countUserList() {
        return userDao.countList(null);
    }

    /**
     * 查询用户权限信息
     * 参数needCrpId表示是否加密输出dbId
     */
    public List<Map<String, Object>> findUserDbList(long userId, boolean needCrpId) {
        Query queryObj = new Query(where("_id").is(userId));
        queryObj.fields().include("role");
        queryObj.fields().include("roleList");
        BaseMongoMap userMap = userDao.getMongoMap(queryObj);
        if (userMap == null) {
            return Collections.EMPTY_LIST;
        }

        List<Map<String, Object>> roleList = null;
        // 如果该用户是系统管理员，则返回所有的数据库一览
        if (userMap.getIntAttribute("role") == 9) {
            List<BaseMongoMap> dDbList = dbService.findDbList(0, 0, true);
            if (dDbList == null || dDbList.isEmpty()) {
                logger.error("getUserRoleList 无数据库 iid={}", userId);
                return Collections.EMPTY_LIST;
            }
            roleList = new ArrayList<>();
            for (BaseMongoMap dbMap : dDbList) {
                Map<String, Object> item = new HashMap<>();
                Long dbId = dbMap.getLongAttribute("_id");
                if (needCrpId) {
                    item.put("id", dbId.toString());
                } else {
                    item.put("dbId", dbId);
                }
                item.put("role", 9);

                item.put("dbNameTxt", getDbNameTxt(dbMap));
                roleList.add(item);
            }
            return roleList;
        }

        // 如果不是，只返回当前用户有权限操作的数据库一览
        roleList = (List<Map<String, Object>>) userMap.get("roleList");
        if (roleList == null || roleList.isEmpty()) {
            logger.warn("getUserRoleList 该用户未设置权限 iid={}", userId);
            return Collections.EMPTY_LIST;
        }
        for (Map<String, Object> roleMap : roleList) {
            BaseMongoMap dbMap = dbService.findDbById(StringUtil.convertToLong(roleMap.get("dbId")));
            if (dbMap == null) {
                continue;
            }
            if (needCrpId) {
                Long dbId = dbMap.getLongAttribute("_id");
                roleMap.put("id", dbId.toString());
                roleMap.remove("dbId");
            }
            roleMap.put("dbNameTxt", getDbNameTxt(dbMap));
        }
        return roleList;
    }

    private static String getDbNameTxt(BaseMongoMap dbMap) {
        String dbTxt = StringUtils.trimToEmpty(dbMap.getStringAttribute("dbName"));
        String cnName = StringUtils.trimToNull(dbMap.getStringAttribute("dbNameCN"));
        if (cnName != null) {
            dbTxt = dbTxt + "  =>  " + cnName;
        }
        String dbVerStr = StringUtils.trimToNull(dbMap.getStringAttribute("typeStr"));
        if (dbVerStr != null) {
            dbTxt = dbTxt + " (" + dbVerStr + ")";
        }
        return dbTxt;
    }

    /**
     * 删除用户
     */
    public void removeUser(Long adminId, long userId) {
        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("status", 4);
        infoMap.put("modifier", adminId);
        infoMap.put("modifiedTime", DateTimeUtil.getNowTime());
        userDao.updateObject(userId, infoMap, false);
    }

    /**
     * 添加用户
     */
    public void addUser(Map<String, Object> params) {
        long iid = sequenceService.getNextSequence(ComSequenceService.ComSequenceName.FX_USER_ID);
        Map<String, Object> userObj = new HashMap<>();
        userObj.put("_id", iid);
        userObj.put("userId", params.get("accNo"));
        userObj.put("userName", params.get("accName"));
        userObj.put("password", new BCryptPasswordEncoder().encode("abc.2018"));
        userObj.put("status", 0);
        userObj.put("registered", true);
        userObj.put("role", StringUtil.convertToInt(params.get("role")));
        List<Map<String, Object>> roleList = (List<Map<String, Object>>) params.get("roleList");
        userObj.put("roleList", roleList);
        userDao.updateObject(iid, userObj, true);
    }

    /**
     * 保存用户信息
     */
    public void updateUserInfo(Long iid, Map<String, Object> params) {
        userDao.updateObject(iid, params, false);
    }

    /**
     * 添加用户(第三方)
     */
    public void addUserByTrdLogin(String userId, String userName, String fromSrc) {
        long iid = sequenceService.getNextSequence(ComSequenceService.ComSequenceName.FX_USER_ID);
        Map<String, Object> userObj = new HashMap<>();
        userObj.put("_id", iid);
        userObj.put("userId", userId);
        userObj.put("userName", userName);
        userObj.put("password", "");
        userObj.put("status", 1);
        userObj.put("registered", false);
        userObj.put("from", fromSrc);
        userObj.put("creator", iid);
        userObj.put("createdTime", DateTimeUtil.getNowTime());
        userDao.updateObject(iid, userObj, true);
    }

}
