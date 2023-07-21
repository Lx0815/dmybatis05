package com.d.dmybatis05.session;

import java.sql.Connection;

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
        this.connection = connection;
    }

}
