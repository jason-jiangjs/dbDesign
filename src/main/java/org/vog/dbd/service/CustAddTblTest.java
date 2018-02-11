package org.vog.dbd.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.vog.dbd.web.DbDesignApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jason.jiang on 2016/08/30
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DbDesignApplication.class)
public class CustAddTblTest {

    private final static Logger logger = LoggerFactory.getLogger(CustAddTblTest.class);

    @Autowired
    private TableService tableService;
    @Autowired
    private ComSequenceService sequenceService;

    @Test
    public void testAddCustomer1() {
        try {
            File file = new File("D:\\SVN-DOC\\12.Sneakerhead-app\\blf_snapp.sql");
            // 读取文件，并且以utf-8的形式写出去
            BufferedReader bufread;
            String read;
            bufread = new BufferedReader(new FileReader(file));

            Long tblId = null;
            String tblName = null;
            List<Map<String,Object>> colList = null;

            while ((read = bufread.readLine()) != null) {
                if (read.startsWith("--") || read.startsWith("DROP ") || read.startsWith("KEY ")) {
                    continue;
                }
                if (read.startsWith("CREATE TABLE ")) {
                    // 有新的表
                    tblName = read.substring(13).trim();
                    tblName = tblName.substring(0, tblName.length() - 1).trim();
                    tblName = tblName.replace("`", "");
                    colList = new ArrayList<>();
                    tblId = sequenceService.getNextSequence(ComSequenceService.ComSequenceName.FX_TABLE_ID);
                    continue;
                }
                if (read.startsWith(") ENGINE=InnoDB")) {
                    // 定义结束，创建该表到mongodb
                    if (colList == null || colList.isEmpty()) {
                        continue;
                    }
                    Map<String, Object> tblData = new HashMap<>();
                    tblData.put("tableName", tblName);
                    tblData.put("_id", tblId);
                    tblData.put("dbId", 1001);
                    tblData.put("deleteFlg", false);
                    tblData.put("type", 1);

                    tblData.put("column_list", colList);
                    tableService.saveTblDefInfo(tblId, tblData);
                    System.out.println(tblData.toString());
                    tblId = null;
                    tblName = null;
                    colList.clear();
                    colList = null;
                    continue;
                }

                if (tblName != null && colList != null) {
                    if (read.startsWith("PRIMARY KEY ") || read.startsWith("KEY ")) {
                        continue;
                    }
                    String[] cols = read.split(" ");
                    if (cols.length >= 2) {
                        Map<String, Object> colData = new HashMap<>();
                        colData.put("columnId", sequenceService.getNextSequence(ComSequenceService.ComSequenceName.FX_COLUMN_ID));
                        colData.put("tableId", tblId);
                        colData.put("columnName", cols[0].replace("`", ""));

                        String colType = cols[1];
                        int i1 = colType.indexOf('(');
                        int i2 = colType.indexOf(')');
                        if (i1 > 0 && i2 > 0) {
                            colData.put("type", colType.substring(0, i1));
                            colData.put("columnLens", colType.substring(i1 + 1, i2));
                        } else {
                            colData.put("type", colType);
                            colData.put("columnLens", "");
                        }
                        colList.add(colData);
                    }
                }
            }
            bufread.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}