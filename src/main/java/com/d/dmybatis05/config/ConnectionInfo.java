package com.d.dmybatis05.config;

/**
 * @description: 数据库连接信息
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-07-16 21:27:00
 * @modify:
 */

public class ConnectionInfo {

    /**
     * 驱动程序全类名
     */
    private String driverClassName;

    /**
     * 数据库连接 url
     */
    private String url;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
