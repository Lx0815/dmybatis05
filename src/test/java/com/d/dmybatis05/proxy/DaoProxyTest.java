package com.d.dmybatis05.proxy;

import com.d.dmybatis05.UserDao;
import com.d.dmybatis05.config.Configuration;
import com.d.dmybatis05.config.ConfigurationBuilder;
import com.d.dmybatis05.config.ConfigurationBuilderTest;
import com.d.dmybatis05.config.ConnectionInfo;
import com.d.dmybatis05.session.SqlSession;
import com.d.dmybatis05.session.SqlSessionFactory;
import org.junit.Test;

import java.lang.reflect.Proxy;

/**
 * @description: 代理类测试类
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-07-22 9:13:47
 * @modify:
 */

public class DaoProxyTest {

    @Test
    public void testCreateProxy() {
        Configuration configuration = ConfigurationBuilder.build("dmybatis-config.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactory(configuration);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserDao userDao = sqlSession.getDao(UserDao.class);
        System.out.println(userDao.selectAll());
    }

}
