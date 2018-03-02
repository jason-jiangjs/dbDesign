package org.vog.dbd.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.vog.base.model.mongo.BaseMongoMap;
import org.vog.base.service.BaseService;
import org.vog.common.util.StringUtil;
import org.vog.dbd.dao.DbDao;
import org.vog.dbd.dao.UserDao;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService extends BaseService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private DbDao dbDao;

    @Autowired
    private ComSequenceService sequenceService;

    /**
     * 根据id查询用户
     */
    public BaseMongoMap getUserById(long iid) {
        return userDao.getUserById(iid);
    }

    /**
     * 根据登录帐号查询用户
     */
    public BaseMongoMap getUserByAccount(String userId) {
        return userDao.getUserByAccount(userId);
    }

    /**
     * 保存用户的默认工作数据库
     */
    public void setUserFavorite(long userId, long dbId) {
        userDao.setUserFavorite(userId, dbId);
    }

    /**
     * 查询用户一览
     */
    public List<BaseMongoMap> findUserList(int page, int limit) {
        return userDao.getUserList(page, limit);
    }

    /**
     * 统计用户个数
     */
    public long countUserList() {
        return userDao.countUserList();
    }

    /**
     * 查询用户一览
     */
    public List<Map<String, Object>> findUserRoleList(long userId) {
        BaseMongoMap userMap = userDao.getUserRoleInfo(userId);
        if (userMap == null) {
            return Collections.EMPTY_LIST;
        }

        List<Map<String, Object>> userList = (List<Map<String, Object>>) userMap.get("roleList");
        if (userList == null || userList.isEmpty()) {
            logger.warn("getUserRoleList 该用户未设置权限 iid={}", userId);
            return Collections.EMPTY_LIST;
        }
        for (Map<String, Object> roleMap : userList) {
            BaseMongoMap dbMap = dbDao.findDbById(StringUtil.convertToLong(roleMap.get("dbId")));
            if (dbMap == null) {
                continue;
            }
            String dbTxt = StringUtils.trimToEmpty(dbMap.getStringAttribute("dbName"));
            String cnName = StringUtils.trimToNull(dbMap.getStringAttribute("dbNameCN"));
            if (cnName != null) {
                dbTxt = dbTxt + "  =>  " + cnName;
            }
            String dbVerStr = StringUtils.trimToNull(dbMap.getStringAttribute("typeStr"));
            if (dbVerStr != null) {
                dbTxt = dbTxt + " (" + dbVerStr + ")";
            }
            roleMap.put("dbName", dbTxt);
        }
        return userList;
    }

    /**
     * 查询用户一览
     */
    public List<Map<String, Object>> findUserDbList(long userId) {
        BaseMongoMap userMap = userDao.getUserRoleInfo(userId);
        if (userMap == null) {
            return Collections.EMPTY_LIST;
        }

        List<Map<String, Object>> userList = (List<Map<String, Object>>) userMap.get("roleList");
        if (userList == null || userList.isEmpty()) {
            logger.warn("getUserRoleList 该用户未设置权限 iid={}", userId);
            return Collections.EMPTY_LIST;
        }
        for (Map<String, Object> roleMap : userList) {
            roleMap.remove("role");
            BaseMongoMap dbMap = dbDao.findDbById(StringUtil.convertToLong(roleMap.get("dbId")));
            if (dbMap == null) {
                continue;
            }
            String dbTxt = StringUtils.trimToEmpty(dbMap.getStringAttribute("dbName"));
            String cnName = StringUtils.trimToNull(dbMap.getStringAttribute("dbNameCN"));
            if (cnName != null) {
                dbTxt = dbTxt + "  =>  " + cnName;
            }
            String dbVerStr = StringUtils.trimToNull(dbMap.getStringAttribute("typeStr"));
            if (dbVerStr != null) {
                dbTxt = dbTxt + " (" + dbVerStr + ")";
            }
            roleMap.put("dbNameTxt", dbTxt);
        }
        return userList;
    }

    /**
     * 删除用户
     */
    public void removeUser(long userId) {
        userDao.removeUser(userId);
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
        userObj.put("role", StringUtil.convertToInt(params.get("role")));
        List<Map<String, Object>> roleList = (List<Map<String, Object>>) params.get("roleList");
        for (Map<String, Object> item : roleList) {
            item.put("dbId", StringUtil.convertToLong(item.get("dbId")));
            item.put("role", StringUtil.convertToInt(item.get("role")));
            item.remove("dbName");
            item.remove("default");
        }
        userObj.put("roleList", roleList);

        userDao.saveUser(iid, userObj);
    }

    /**
     * 修改用户信息
     */
    public void updateUser(BaseMongoMap userObj, Map<String, Object> params) {
        long iid = userObj.getLongAttribute("_id");
        userObj.put("userId", params.get("accNo"));
        userObj.put("userName", params.get("accName"));
        userObj.put("status", StringUtil.convertToInt(params.get("status")));
        userObj.put("role", StringUtil.convertToInt(params.get("role")));
        List<Map<String, Object>> roleList = (List<Map<String, Object>>) params.get("roleList");
        for (Map<String, Object> item : roleList) {
            item.put("dbId", StringUtil.convertToLong(item.get("dbId")));
            item.put("role", StringUtil.convertToInt(item.get("role")));
            item.remove("dbName");
            item.remove("default");
        }
        userObj.put("roleList", roleList);

        userDao.saveUser(iid, userObj);
    }
}
