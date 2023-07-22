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

    public SqlInfo() {
    }

    public SqlInfo(String sql, SqlType sqlType) {
        this.sql = sql;
        this.sqlType = sqlType;
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
