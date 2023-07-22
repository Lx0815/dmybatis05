package com.d.dmybatis05.proxy;

import com.d.dmybatis05.config.Configuration;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @description: Dao 接口的代理类
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-07-22 9:01:45
 * @modify:
 */

public class DaoProxy implements InvocationHandler {

    private Configuration configuration;

    public DaoProxy(Configuration configuration) {
        this.configuration = configuration;
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
        String sql = configuration.getDaoInfo().getSql(sqlId);

        System.out.println(sql);
        // TODO: 2023/7/22 暂未写完
        return null;
    }
}
