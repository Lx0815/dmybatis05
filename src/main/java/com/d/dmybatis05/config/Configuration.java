package com.d.dmybatis05.config;

/**
 * @description: 配置对象
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-07-16 21:26:17
 * @modify:
 */

public class Configuration {

    /**
     * 数据库连接信息
     */
    private ConnectionInfo connectionInfo;

    /**
     * Dao 信息
     */
    private DaoInfo daoInfo;

    public ConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    public void setConnectionInfo(ConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    public DaoInfo getDaoInfo() {
        return daoInfo;
    }

    public void setDaoInfo(DaoInfo daoInfo) {
        this.daoInfo = daoInfo;
    }
}
