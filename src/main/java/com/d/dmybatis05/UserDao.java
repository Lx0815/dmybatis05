package com.d.dmybatis05;

import java.util.List;

/**
 * @description:
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-05-31 15:45:53
 * @modify:
 */

public interface UserDao {

    List<User> selectAll();

    Integer insert(User user);

}
