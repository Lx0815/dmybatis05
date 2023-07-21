package com.d.dmybatis05.session;

import com.d.dmybatis05.config.Configuration;
import com.d.dmybatis05.config.ConnectionInfo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @description: SqlSessionFactory 的工厂类
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-07-21 23:30:15
 * @modify:
 */

public class SqlSessionFactory {

    private Configuration configuration;

    public SqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
        // 注册数据库驱动
        try {
            Class.forName(configuration.getConnectionInfo().getDriverClassName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("数据库驱动注册失败", e);
        }
    }

    public SqlSession openSession() {
        return new SqlSession(newConnection());
    }

    private Connection newConnection() {
        try {
            ConnectionInfo connectionInfo = configuration.getConnectionInfo();
            return DriverManager.getConnection(connectionInfo.getUrl(), connectionInfo.getUsername(), connectionInfo.getPassword());
        } catch (SQLException e) {
            throw new RuntimeException("创建数据库连接失败", e);
        }
    }
}
