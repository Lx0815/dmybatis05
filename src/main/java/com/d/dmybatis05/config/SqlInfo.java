package com.d.dmybatis05.config;

/**
 * @description:
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-07-22 19:44:26
 * @modify:
 */

public class SqlInfo {

    private String sql;

    private SqlType sqlType;

    private Class<?> rowType;

    public SqlInfo() {
    }

    public SqlInfo(String sql, SqlType sqlType, Class<?> rowType) {
        this.sql = sql;
        this.sqlType = sqlType;
        this.rowType = rowType;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public SqlType getSqlType() {
        return sqlType;
    }

    public void setSqlType(SqlType sqlType) {
        this.sqlType = sqlType;
    }

    public Class<?> getRowType() {
        return rowType;
    }

    public void setRowType(Class<?> rowType) {
        this.rowType = rowType;
    }

    public enum SqlType {
        INSERT, UPDATE, DELETE, SELECT;

        public static SqlType of(String value) {
            if ("insert".equalsIgnoreCase(value)) {
                return INSERT;
            } else if ("update".equalsIgnoreCase(value)) {
                return UPDATE;
            } else if ("delete".equalsIgnoreCase(value)) {
                return DELETE;
            } else if ("select".equalsIgnoreCase(value)) {
                return SELECT;
            } else {
                throw new RuntimeException("标签错误");
            }
        }
    }

}
