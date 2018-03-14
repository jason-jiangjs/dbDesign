package org.vog.dbd.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.vog.base.model.mongo.BaseMongoMap;
import org.vog.base.service.BaseService;
import org.vog.common.util.AESCoderUtil;
import org.vog.common.util.DateTimeUtil;
import org.vog.common.util.StringUtil;
import org.vog.dbd.dao.DbDao;
import org.vog.dbd.dao.UserDao;

import java.util.ArrayList;
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
     * 保存用户的默认工作数据库
     */
    public void setUserFavorite(long userId, long dbId) {
        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("favorite", dbId);
        userDao.saveObject(userId, infoMap, false);
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
    public List<Map<String, Object>> findUserDbList(long userId, boolean needCrpId) {
        BaseMongoMap userMap = userDao.getUserRoleInfo(userId);
        if (userMap == null) {
            return Collections.EMPTY_LIST;
        }

        List<Map<String, Object>> roleList = null;
        // 如果该用户是系统管理员，则返回所有的数据库一览
        if (userMap.getIntAttribute("role") == 9) {
            List<BaseMongoMap> dDbList = dbDao.findDbList(0, 0, true);
            if (dDbList == null || dDbList.isEmpty()) {
                logger.error("getUserRoleList 无数据库 iid={}", userId);
                return Collections.EMPTY_LIST;
            }
            roleList = new ArrayList<>();
            for (BaseMongoMap dbMap : dDbList) {
                Map<String, Object> item = new HashMap<>();
                Long dbId = dbMap.getLongAttribute("_id");
                if (needCrpId) {
                    item.put("id", AESCoderUtil.encode(dbId.toString()));
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
            BaseMongoMap dbMap = dbDao.findDbById(StringUtil.convertToLong(roleMap.get("dbId")));
            if (dbMap == null) {
                continue;
            }
            if (needCrpId) {
                Long dbId = dbMap.getLongAttribute("_id");
                roleMap.put("id", AESCoderUtil.encode(dbId.toString()));
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
        infoMap.put("modifiedTime", DateTimeUtil.getDate());
        userDao.saveObject(userId, infoMap, false);
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

        userDao.saveObject(iid, userObj, true);
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

        userDao.saveObject(iid, userObj, false);
    }
}
