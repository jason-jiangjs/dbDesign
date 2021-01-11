package org.dbm.dbd.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.dbm.common.Constants;
import org.dbm.common.ErrorCode;
import org.dbm.common.base.controller.BaseController;
import org.dbm.common.base.model.mongo.BaseMongoMap;
import org.dbm.common.util.ApiResponseUtil;
import org.dbm.common.util.DateTimeUtil;
import org.dbm.common.util.JacksonUtil;
import org.dbm.dbd.service.DbService;
import org.dbm.dbd.service.TableService;
import org.dbm.dbd.web.util.BizCommUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 导出表定义, 以及版本管理
 */
@RestController
public class TableListExportController extends BaseController {

    @Autowired
    private TableService tableService;

    @Autowired
    private DbService dbService;

    /**
     * 创建新版本
     */
    @RequestMapping(value = "/ajax/releaseNewTag", method = RequestMethod.POST)
    public Map<String, Object> createNewTag(@RequestParam Map<String, String> params) {
        // 先检查所有表是否都有备份（table_history中有一致的数据）
        List<BaseMongoMap> inEditingList = tableService.getTableInEditing(BizCommUtil.getSelectedDbId());
        if (inEditingList.size() > 0) {
            // 有编辑
            logger.info("chkTableInEditing 有人正在编辑 count={}, userId={}", inEditingList.size(), getLoginUserId());
            Map<String, Object> data = new HashMap<>();
            data.put("inEditingList", inEditingList);
            return ApiResponseUtil.error(ErrorCode.E5102, null, data);
        }

        return ApiResponseUtil.success();
    }

    /**
     * 导出SQL文
     */
    @RequestMapping(value = "/ajax/exportSql", method = RequestMethod.POST)
    public String exportSql(HttpServletRequest request, HttpServletResponse resp) {
        Long userId = getLoginUserId();
        Long dbId = (Long) request.getSession().getAttribute(Constants.KEY_DB_ID);
        if (dbId == null || dbId == 0) {
            logger.warn("exportSql 缺少dbId");
            return ApiResponseUtil.error(ErrorCode.W1001, null).toString();
        }
        BaseMongoMap dbMap = dbService.findDbById(dbId);
        int dbType = dbMap.getIntAttribute("dataTypeId");
        if (dbType != 1) {
            logger.warn("exportSql 暂不支持 dbid={}", dbId);
            return ApiResponseUtil.error(ErrorCode.E5001, "暂不支持输出脚本").toString();
        }

        List<String> outputStr = new ArrayList<>();
        outputStr.add("/*\n");
        outputStr.add("Output Date: " + DateTimeUtil.getNow() + "\n");
        outputStr.add("*/\n\n");

        String tblIdListStr = StringUtils.trimToNull(request.getParameter("tblIdList"));
        List<Long> tblIdList = JacksonUtil.jsonToBeanList(tblIdListStr, Long.class);
        List<BaseMongoMap> tblList = tableService.getTableByIds(dbId, tblIdList, true);
        String fileName = null;

        for (BaseMongoMap tblMap :tblList) {
            // 针对每个表
            String tblName = tblMap.getStringAttribute("tableName");
            fileName = tblName;
            outputStr.add("-- ----------------------------\n");
            outputStr.add("-- Table structure for " + tblName + "\n");
            outputStr.add("-- ----------------------------\n");
            outputStr.add("DROP TABLE IF EXISTS `" + tblName + "`;\n");
            outputStr.add("CREATE TABLE `" + tblName + "` (\n");

            // 针对表中的所有项目
            List<Map<String, Object>> colList = (List<Map<String, Object>>) tblMap.get("column_list");
            if (colList != null && colList.size() > 0) {
                int size = colList.size() - 1;
                int idx = 0;
                List<String> primKey = new ArrayList<>();
                for (Map<String, Object> colItem : colList) {
                    String colName = (String) colItem.get("columnName");
                    String line = "  `" + colName + "` ";
                    String colType = (String) colItem.get("type");
                    if ("timestamp".equalsIgnoreCase(colType)) {
                        line += colType + " NULL";
                    } else {
                        line += colType;
                    }

                    if (StringUtils.isNotBlank((String) colItem.get("columnLens"))) {
                        line += "(" + colItem.get("columnLens") + ")";
                    }

                    if ("Y".equalsIgnoreCase((String) colItem.get("primary"))) {
                        primKey.add((String) colItem.get("columnName"));
                    }
                    if ("Y".equalsIgnoreCase((String) colItem.get("notnull"))) {
                        line += " NOT NULL";
                    }

                    if ("Y".equalsIgnoreCase((String) colItem.get("increment"))) {
                        line += " AUTO_INCREMENT";
                    }
                    if (StringUtils.isNotBlank((String) colItem.get("default"))) {
                        String defaultValStr = (String) colItem.get("default");
                        if ("tinyint".equalsIgnoreCase(colType) || "smallint".equalsIgnoreCase(colType) || "int".equalsIgnoreCase(colType) || "bigint".equalsIgnoreCase(colType)) {
                            defaultValStr.replaceAll("'", "");
                        }
                        line += " DEFAULT " + defaultValStr;
                    }

                    // 时间格式固定输出
                    if ("updated_date".equalsIgnoreCase(colName)) {
                        line += " ON UPDATE CURRENT_TIMESTAMP";
                    }

                    if (StringUtils.isNotBlank((String) colItem.get("aliasName"))) {
                        line += " COMMENT '" + StringUtils.trimToEmpty((String) colItem.get("aliasName")) + "'";
                    }

                    if (idx == size) {
                        // 最后一条了，如果没有主键或索引，则行尾没有","
                        if (primKey.size() > 0) {
                            line += ",\n";
                        } else {
                            line += "\n";
                        }
                    } else {
                        line += ",\n";
                    }

                    outputStr.add(line);
                    idx ++;
                }
                // 主键/索引/..等等
                if (primKey.size() > 0) {
                    idx = 0;
                    String line = "  PRIMARY KEY (";
                    for (String key : primKey) {
                        if (idx == 0) {
                            line += "`" + key + "`";
                            idx ++;
                        } else {
                            line += ",`" + key + "`";
                        }
                    }
                    line += ")\n";
                    outputStr.add(line);
                }
            }
            String tableNameCN = tblMap.getStringAttribute("aliasName");
            if (StringUtils.isBlank(tableNameCN)) {
                outputStr.add(");\n");
            } else {
                outputStr.add(") COMMENT='" + StringUtils.trimToEmpty(tableNameCN) + "';\n");
            }
            outputStr.add("\n");
        }

        resp.setContentType("application/octet-stream");
        if (tblList.size() == 1) {
            fileName = "attachment;filename=sql_" + fileName + "_" + DateTimeUtil.getNow(DateTimeUtil.COMPRESS_DATETIME_FORMAT) + ".sql";
        } else {
            fileName = "attachment;filename=sql_" + DateTimeUtil.getNow(DateTimeUtil.COMPRESS_DATETIME_FORMAT) + ".sql";
        }
        resp.setHeader("Content-Disposition", fileName);
//        resp.setContentLength((int) file.length());

        try {
            for (String line : outputStr) {
                resp.getOutputStream().write(line.getBytes("UTF-8"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                resp.getOutputStream().flush();
                resp.getOutputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }
}
