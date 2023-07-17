package com.d.dmybatis05.config;

import org.junit.Test;

import java.lang.reflect.Field;

/**
 * @description: ConfigurationBuilder 的测试类
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-07-17 23:04:00
 * @modify:
 */

public class ConfigurationBuilderTest {

    @Test
    public void testBuilder() throws NoSuchFieldException, IllegalAccessException {
        Configuration configuration = ConfigurationBuilder.build("dmybatis-config.xml");
        ConnectionInfo connectionInfo = configuration.getConnectionInfo();
        DaoInfo daoInfo = configuration.getDaoInfo();
        System.out.println(connectionInfo.getUrl());
        System.out.println(connectionInfo.getDriverClassName());
        System.out.println(connectionInfo.getUsername());
        System.out.println(connectionInfo.getPassword());

        Field sqlMapField = DaoInfo.class.getDeclaredField("sqlMap");
        sqlMapField.setAccessible(true);
        Object sqlMap = sqlMapField.get(daoInfo);
        System.out.println(sqlMap.toString());

        /*
            jdbc:mysql:localhost:3306/xxx_db
            com.mysql.cj.jdbc.Driver
            root
            123456
            {com.xxx.UserDao.selectAll=SELECT * FROM user;}
         */
    }

}
