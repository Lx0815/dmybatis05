package com.d.dmybatis05.builder;

import com.d.dmybatis05.UserDao;
import com.d.dmybatis05.config.Configuration;
import com.d.dmybatis05.config.ConfigurationBuilder;
import com.d.dmybatis05.proxy.DaoProxy;

import java.lang.reflect.Proxy;

/**
 * @description:
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-07-22 17:16:58
 * @modify:
 */

public class PreparedStatementBuilderTest {

    public void testSqlPrepare() {
        Configuration configuration = ConfigurationBuilder.build("dmybatis-config.xml");

        Object instance = Proxy.newProxyInstance(DaoProxy.class.getClassLoader(),
                new Class[]{UserDao.class},
                new DaoProxy(configuration));

        UserDao userDao = (UserDao) instance;
        System.out.println(userDao.selectAll());
    }

}
