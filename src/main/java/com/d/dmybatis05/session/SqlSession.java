package com.d.dmybatis05.session;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 * @description: 会话对象
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-07-21 23:29:37
 * @modify:
 */

public class SqlSession {

    private Connection connection;

    public SqlSession(Connection connection) {
        Objects.requireNonNull(connection, "连接对象为 null");
        this.connection = connection;
    }

    /**
     * 提交事务
     */
    public void commit() {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 回滚事务
     */
    public void rollback() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 关闭会话，即关闭连接
     */
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
