package dataimport;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import javax.sql.DataSource;
import java.io.File;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 使用jdbc提供的metadata来获取表定义
 */
public class Data4Meta {

    private static String _username = "xx";
    private static String _password = "xx";
    private static String _url = "jdbc:mysql://xx:xx/blf_snapp?autoReconnect=true&amp;useOldAliasMetadataBehavior=true&amp;useUnicode=true&amp;characterEncoding=UTF-8&amp;zeroDateTimeBehavior=round&amp;allowMultiQueries=true";

    @Test
    public void testFind() {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUser(_username);
        dataSource.setPassword(_password);
        dataSource.setUrl(_url);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        try {
            DatabaseMetaData dbmd = dataSource.getConnection().getMetaData();
            ResultSet rs = dbmd.getTables("", "", "", null);
            while (rs.next()) {
                System.out.println(rs.getString("TABLE_NAME"));
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }

        String sql = "select * from blc_order limit 1";
        List<KeyValue<String, String>> list = jdbcTemplate.query(sql,
            new ResultSetExtractor<List<KeyValue<String, String>>>() {

                @Override
                public List<KeyValue<String, String>> extractData(ResultSet rs) throws SQLException, DataAccessException {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int count = metaData.getColumnCount();
                    List<KeyValue<String, String>> l = new ArrayList<>();

                    for (int i = 1; i < count+1; i++) {
                        StringBuilder txt = new StringBuilder();
                        String fieldName = metaData.getColumnName(i);
                        txt.append(fieldName);
                        txt.append(" : ");
                        String typeName = metaData.getColumnTypeName(i);
                        txt.append(typeName);
                        txt.append(" : ");
                        txt.append(metaData.getColumnDisplaySize(i));
                        txt.append(" : ");
                        txt.append(metaData.getPrecision(i));
                        txt.append(":");
                        txt.append(metaData.getScale(i));

                        System.out.println(txt.toString());
                    }
                    return l;
                }
            });

        for (KeyValue<String, String> obj : list) {
            System.out.println(obj);
        }
    }

}
