package org.vog.dbd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vog.dbd.dao.UserDao;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    /**
     * 保存用户的默认工作数据库
     */
    public void setUserFavorite(long userId, long dbId) {
        userDao.setUserFavorite(userId, dbId);
    }

}
