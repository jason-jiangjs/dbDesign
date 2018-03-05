package dataimport;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import java.io.File;

/**
 * Created by dell on 2018/3/5.
 */
public class Data4Xls {

    private static String _dirName = "";
    private static String[] _FileNames = { "数据库表设计_2.xlsx" };
//    private static String[] _FileNames = { "数据库表设计.xlsx", "数据库表设计_admin.xlsx",
//            "数据库表设计_order.xlsx", "数据库表设计_product.xlsx" };

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

    private void convertFile(File file) throws Exception {
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook(file);
        XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(0);

        int rowstart = xssfSheet.getFirstRowNum();
        int rowEnd = xssfSheet.getLastRowNum();
        for (int i = rowstart; i <= rowEnd; i++) {
            XSSFRow row = xssfSheet.getRow(i);
            if (null == row) continue;
            int cellStart = row.getFirstCellNum();
            int cellEnd = row.getLastCellNum();

            for (int k = cellStart; k <= cellEnd; k++) {
                XSSFCell cell = row.getCell(k);
                if (null == cell) continue;

                switch (cell.getCellType()) {
                    case HSSFCell.CELL_TYPE_NUMERIC: // 数字
                        System.out.print(cell.getNumericCellValue()
                                + "   ");
                        break;
                    case HSSFCell.CELL_TYPE_STRING: // 字符串
                        System.out.print(cell.getStringCellValue()
                                + "   ");
                        break;
                    default:
                        System.out.print("未知类型 " + cell.getRawValue());
                        break;
                }

            }
            System.out.print("\n");
        }
    }

    private void scanSheet(XSSFSheet xssfSheet) throws Exception {
        int rowEnd = xssfSheet.getLastRowNum();
        for (int i = 6; i <= rowEnd; i++) {
            XSSFRow row = xssfSheet.getRow(i);
            if (null == row) continue;

            XSSFCell cell = row.getCell(2);
            XSSFCell cell2 = row.getCell(11);
            XSSFCell cell3 = row.getCell(12);
            if (null == cell) continue;

            System.out.print((i+1) + "\n");
            String colName = StringUtils.trimToEmpty(cell.getStringCellValue());
            if ("".equals(colName)) {
                break;
            }
            System.out.println(colName);
            if (cell2 != null) {
                System.out.println(StringUtils.trimToEmpty(cell2.getStringCellValue()));
            }
            if (cell3 != null) {
                System.out.println(StringUtils.trimToEmpty(cell3.getStringCellValue()));
            }

            System.out.print("\n");
        }
    }
}
