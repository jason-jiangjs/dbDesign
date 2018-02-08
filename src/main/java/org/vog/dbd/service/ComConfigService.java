package org.vog.dbd.service;

import org.vog.dbd.dao.ComConfigDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ComConfigService {

    @Autowired
    private ComConfigDao omConfigDao;

    /**
     * 取得配置属性值
     * 业务中使用PropertyPlaceholder取值
     */
    public List<Map> findProperties() {
        return omConfigDao.getProperties();
    }

}
