package org.vog.dbd.web.config;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.mongodb.core.query.BasicUpdate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.vog.common.SystemProperty;
import org.vog.common.util.DateTimeUtil;
import org.vog.common.util.JacksonUtil;
import org.vog.dbd.dao.ComConfigDao;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * Created by dell on 2017/4/19.
 */
public class ApplicationStartup implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 加载属性配置
        ApplicationContext _applicationContext = event.getApplicationContext();
        SystemProperty.initComConfig(_applicationContext);

        // 导入初始数据（没有时才导入），表：col_head_define/com_config/com_sequence/user
        ComConfigDao configDao = _applicationContext.getBean(ComConfigDao.class);
        List<Map> propList = configDao.getProperties();
        if (propList == null || propList.isEmpty()) {
            copyInitData("col_head_define", configDao);
            copyInitData("com_sequence", configDao);
            copyInitData("com_config", configDao);
            copyInitData("user", configDao);
        }
    }

    private void copyInitData(String tblName, ComConfigDao configDao) {
        // 导入初始数据（没有时才导入），表：col_head_define/com_config/com_sequence/user
        String path = getClass().getClassLoader().getResource("static/data/" + tblName + ".json").toString();
        if (path.contains(":")) {
            path = path.replace("file:/", "");
        }

        try {
            List<DBObject> objList = new ArrayList<>();
            LineIterator input = FileUtils.lineIterator(new File(path), "UTF-8");
            while (input.hasNext()) {
                String val = input.next();
                DBObject bson = (DBObject) JSON.parse(val);
                objList.add(bson);
            }
            if (objList.size() > 0) {
                configDao.insertObject(objList, tblName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApplicationContextException(e.getMessage());
        }
    }
}