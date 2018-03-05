package org.vog.dbd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vog.base.model.mongo.BaseMongoMap;
import org.vog.base.service.BaseService;
import org.vog.dbd.dao.DbDao;

import java.util.List;

@Service
public class DbService extends BaseService {

    @Autowired
    private DbDao dbDao;

    @Autowired
    private ComSequenceService sequenceService;

    /**
     * 查询用户一览
     */
    public List<BaseMongoMap> findDbList(int page, int limit) {
        return dbDao.findDbList(page, limit, false);
    }

    /**
     * 统计用户个数
     */
    public long countDbList() {
        return dbDao.countDbList();
    }

}
