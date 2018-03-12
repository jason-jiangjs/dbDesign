package dataimport;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.vog.base.model.mongo.BaseMongoMap;
import org.vog.dbd.service.ComSequenceService;
import org.vog.dbd.service.TableService;
import org.vog.dbd.web.DbDesignApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dell on 2018/3/5.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DbDesignApplication.class)
public class Data4Xls {

    private static String _dirName = "";
    private static String[] _FileNames = { "数据库表设计_product.xlsx" };
//    private static String[] _FileNames = { "数据库表设计.xlsx", "数据库表设计_admin.xlsx",
//            "数据库表设计_order.xlsx", "数据库表设计_product.xlsx" };

    @Autowired
    private TableService tableService;

    @Autowired
    private ComSequenceService sequenceService;

    @Test
    public void importData() {
        try {
            for (String fileName : _FileNames) {
                File file = new File(_dirName + fileName);
                XSSFWorkbook xssfWorkbook = new XSSFWorkbook(file);
                int cnt = xssfWorkbook.getNumberOfSheets();
                for (int i = 0; i < cnt; i ++) {
                    XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(i);
                    scanSheet(xssfSheet);
                }
                xssfWorkbook.close();
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    private void scanSheet(XSSFSheet xssfSheet) throws Exception {
        String tblName = null;
        String tblNameCn = null;
        String tblDesc = null;
        XSSFRow hrow = xssfSheet.getRow(1);
        if (null != hrow) {
            tblName = StringUtils.trimToNull(hrow.getCell(3).getStringCellValue());
        }
        if (tblName == null) {
            return;
        }
        hrow = xssfSheet.getRow(2);
        if (null != hrow) {
            tblNameCn = StringUtils.trimToNull(hrow.getCell(3).getStringCellValue());
        }
        hrow = xssfSheet.getRow(3);
        if (null != hrow) {
            tblDesc = StringUtils.trimToNull(hrow.getCell(3).getStringCellValue());
        }

        Long tblId = 0L;
        BaseMongoMap tblObj = null;
        // 根据表名查询
        List<BaseMongoMap> mapList = tableService.findTableByName(1001, tblName);
        if (mapList == null || mapList.isEmpty()) {
            // 要新建该表
            tblObj = new BaseMongoMap();
            tblObj.put("tableNameCN", tblNameCn);
            tblObj.put("desc", tblDesc);
            tblObj.put("dbId", 1001L);
        } else if (mapList.size() > 1) {
            System.out.println("重复定义表: " + tblName);
            return;
        } else {
            tblObj = mapList.get(0);
            tblId = tblObj.getLongAttribute("_id");
            tblObj.put("tableNameCN", tblNameCn);
            tblObj.put("desc", tblDesc);
        }

        List<Map<String, Object>> infoList = new ArrayList<>();
        int rowEnd = xssfSheet.getLastRowNum();
        for (int i = 6; i <= rowEnd; i++) {
            XSSFRow row = xssfSheet.getRow(i);
            if (null == row) continue;

            XSSFCell cell = row.getCell(2);
            if (null == cell) continue;

            String colName = StringUtils.trimToNull(cell.getStringCellValue());
            if (colName == null || "索引".equals(colName)) {
                break;
            }

            Map<String, Object> colData = new HashMap<>();
            colData.put("columnId", sequenceService.getNextSequence(ComSequenceService.ComSequenceName.FX_COLUMN_ID));
            colData.put("tableId", tblId);
            colData.put("columnName", colName);
            infoList.add(colData);

            String colType = StringUtils.trimToEmpty(row.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue());
            int idx = colType.indexOf('(');
            if (idx < 1) {
                colData.put("type", colType);
            } else {
                colData.put("type", colType.substring(0, idx));
                colData.put("columnLens", colType.substring(idx + 1, colType.length() - 1));
            }

            colData.put("primary", StringUtils.trimToEmpty(row.getCell(4, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue()));
            colData.put("notnull", StringUtils.trimToEmpty(row.getCell(5, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue()));
            colData.put("indexDef", StringUtils.trimToEmpty(row.getCell(6, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue()));
            colData.put("unique", StringUtils.trimToEmpty(row.getCell(7, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue()));
            colData.put("foreign", StringUtils.trimToEmpty(row.getCell(8, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue()));
            colData.put("increment", StringUtils.trimToEmpty(row.getCell(9, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue()));
            colData.put("default", StringUtils.trimToEmpty(row.getCell(10, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getRawValue()));
            colData.put("columnNameCN", StringUtils.trimToEmpty(row.getCell(11, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue()));
            colData.put("desc", StringUtils.trimToEmpty(row.getCell(12, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue()));
        }

        tblObj.put("column_list", infoList);
        tableService.saveTblDefInfo(tblId, tblObj);
    }
}
