package com.d.dmybatis05.session;

import com.d.dmybatis05.UserDao;
import com.d.dmybatis05.config.Configuration;
import com.d.dmybatis05.proxy.DaoProxy;

import java.lang.reflect.Proxy;
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

    private Configuration configuration;

    private Connection connection;

    public SqlSession(Configuration configuration, Connection connection) {
        Objects.requireNonNull(connection, "连接对象为 null");
        this.configuration = configuration;
        this.connection = connection;
    }

    /**
     * 获取 Dao 的代理对象
     * @param daoClz dao 接口的类对象
     * @return 返回 Dao 的代理对象
     * @param <T> Dao 接口的类型
     */
    @SuppressWarnings("unchecked")
    public <T> T getDao(Class<T> daoClz) {
        return (T) Proxy.newProxyInstance(DaoProxy.class.getClassLoader(),
                new Class[]{UserDao.class},
                new DaoProxy(configuration, connection));
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
