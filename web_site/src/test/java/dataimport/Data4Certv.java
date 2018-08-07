package dataimport;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.vog.base.model.mongo.BaseMongoMap;
import org.vog.dbd.service.TableService;
import org.vog.dbd.web.DbDesignApplication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 大写转小写
 * Created by dell on 2018/3/5.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DbDesignApplication.class)
public class Data4Certv {

    @Autowired
    private TableService tableService;

    @Test
    public void importData() {
        Long tblId = 0L;

        // 根据表名查询
        List<BaseMongoMap> mapList = tableService.getTableList(1042, 1, null);


        for (BaseMongoMap tblObj : mapList) {

            List<Map<String, Object>> colList = (List<Map<String, Object>>) tblObj.get("column_list");
            if (colList == null || colList.isEmpty()) {
                // 要新建该表
                continue;
            }

            for (Map<String, Object> item : colList) {
                item.put("columnName", ((String) item.get("columnName")).toLowerCase());
            }

            Map<String, Object> params = new HashMap<>();
            params.put("column_list", colList);
            tableService.saveTblDefInfo(tblObj.getLongAttribute("_id"), params);
        }

    }
}
