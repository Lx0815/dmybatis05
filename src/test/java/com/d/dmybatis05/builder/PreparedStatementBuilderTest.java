package com.d.dmybatis05.builder;

import com.d.dmybatis05.User;
import com.d.dmybatis05.UserDao;
import com.d.dmybatis05.config.Configuration;
import com.d.dmybatis05.config.ConfigurationBuilder;
import com.d.dmybatis05.session.SqlSession;
import com.d.dmybatis05.session.SqlSessionFactory;
import org.junit.Test;

/**
 * @description:
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-07-22 17:16:58
 * @modify:
 */

public class PreparedStatementBuilderTest {

    @Test
    public void testSqlPrepare() {
        Configuration configuration = ConfigurationBuilder.build("dmybatis-config.xml");
        SqlSessionFactory factory = new SqlSessionFactory(configuration);
        SqlSession sqlSession = factory.openSession();
        UserDao userDao = sqlSession.getDao(UserDao.class);

        System.out.println(userDao.selectAll());
        System.out.println("---------");
        System.out.println(userDao.insert(new User()));
    }

}
