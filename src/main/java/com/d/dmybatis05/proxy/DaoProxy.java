package com.d.dmybatis05.proxy;

import com.d.dmybatis05.builder.PreparedStatementBuilder;
import com.d.dmybatis05.config.Configuration;
import com.d.dmybatis05.config.SqlInfo;
import com.d.dmybatis05.result.ResultSetParser;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @description: Dao 接口的代理类
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-07-22 9:01:45
 * @modify:
 */

public class DaoProxy implements InvocationHandler {

    private Configuration configuration;

    private Connection connection;

    public DaoProxy(Configuration configuration, Connection connection) {
        this.configuration = configuration;
        this.connection = connection;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass().isInstance(Object.class)) {
            return method.invoke(proxy, args);
        }
        return execute(proxy, method, args);
    }

    private Object execute(Object proxy, Method method, Object[] args) {
        // 获取被代理的方法所绑定的 SQL 语句，所以这里需要传入 Configuration 对象
        String sqlId = method.getDeclaringClass().getName() + "." + method.getName();
        SqlInfo sqlInfo = configuration.getDaoInfo().getSql(sqlId);

        System.out.println("SQL: \n" + sqlInfo.getSql());

        PreparedStatementBuilder statementBuilder = new PreparedStatementBuilder(sqlInfo.getSql(), args, connection, method);
        PreparedStatement preparedStatement = statementBuilder.build();
        try {

            switch (sqlInfo.getSqlType()) {
                case UPDATE:
                case DELETE:
                case INSERT:
                    int row = preparedStatement.executeUpdate();
                    System.out.println("被影响的行数： " + row);
                    return row;
                case SELECT:
                    ResultSet resultSet = preparedStatement.executeQuery();
                    System.out.println("读取到的数据长度为：" + resultSet.getFetchSize());
                    return new ResultSetParser(resultSet, method.getReturnType(), sqlInfo.getRowType()).parse();
                default:
                    throw new RuntimeException();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
