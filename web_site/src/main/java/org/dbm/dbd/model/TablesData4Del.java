package org.dbm.dbd.model;

/**
 * 数据model定义，批量删除表时使用
 */
public class TablesData4Del {

    private Long tableId;
    private String tableName;
    private Long modifiedTime;

    public Long getTableId() {
        return tableId;
    }
    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public String getTableName() {
        return tableName;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Long getModifiedTime() {
        return modifiedTime;
    }
    public void setModifiedTime(Long modifiedTime) {
        this.modifiedTime = modifiedTime;
    }
}
