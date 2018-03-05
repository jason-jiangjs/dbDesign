package dataimport;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.test.context.junit4.SpringRunner;
import org.vog.dbd.service.ComSequenceService;
import org.vog.dbd.service.TableService;
import org.vog.dbd.web.DbDesignApplication;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Created by dell on 2018/3/5.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DbDesignApplication.class)
public class Data4Native {

    private static String _username = "xx";
    private static String _password = "xx";
    private static String _url = "jdbc:mysql://xx:xx/blf_snapp?autoReconnect=true&amp;useOldAliasMetadataBehavior=true&amp;useUnicode=true&amp;characterEncoding=UTF-8&amp;zeroDateTimeBehavior=round&amp;allowMultiQueries=true";

    private static String _sql = "select * from `information_schema`.`COLUMNS` where `TABLE_SCHEMA` = 'blf_snapp' and `TABLE_NAME` = '%s' order by ORDINAL_POSITION";

    @Autowired
    private TableService tableService;

    @Autowired
    private ComSequenceService sequenceService;

    // 直接访问mysql的information_schema来获取表定义
    @Test
    public void testFindbyMysql() {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUser(_username);
        dataSource.setPassword(_password);
        dataSource.setUrl(_url);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // 先获取所有表名
        String sql = "select TABLE_NAME from information_schema.TABLES where TABLE_TYPE='BASE TABLE' AND TABLE_SCHEMA='blf_snapp'";
        List<String> tblList = jdbcTemplate.query(sql,
            new ResultSetExtractor<List<String>>() {
                @Override
                public List<String> extractData(ResultSet rs) throws SQLException, DataAccessException {
                    List<String> l = new ArrayList<>();
                    while (rs.next()) {
                       l.add(rs.getString("TABLE_NAME"));
                    }
                    return l;
                }
            });

        for (String tblName : tblList) {
            sql = String.format(_sql, tblName);
            Map<String, Object> tblData = new HashMap<>();
            tblData.put("tableName", tblName);
            long tblId = sequenceService.getNextSequence(ComSequenceService.ComSequenceName.FX_TABLE_ID);
            tblData.put("_id", tblId);
            tblData.put("dbId", 1001);
            tblData.put("deleteFlg", false);
            tblData.put("type", 1); // 此值要根据数据库类型来定

            List<Map<String, Object>> colDataList = new ArrayList<>();

            jdbcTemplate.query(sql,
                new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        Map<String, Object> colData = new HashMap<>();
                        colData.put("columnName", rs.getString("COLUMN_NAME"));

                        String colType = rs.getString("COLUMN_TYPE");
                        int idx = colType.indexOf('(');
                        if (idx < 1) {
                            colData.put("type", colType);
                        } else {
                            colData.put("type", colType.substring(0, idx));
                            colData.put("columnLens", colType.substring(idx + 1, colType.length() - 1));
                        }

                        colData.put("columnId", sequenceService.getNextSequence(ComSequenceService.ComSequenceName.FX_COLUMN_ID));
                        colData.put("tableId", tblId);
                        colDataList.add(colData);
                    }
                });

            tblData.put("column_list", colDataList);
            tableService.saveTblDefInfo(tblId, tblData);
        }
    }

}
