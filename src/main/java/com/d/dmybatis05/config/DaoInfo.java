package com.d.dmybatis05.config;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * @description: Dao 信息
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-07-16 21:27:50
 * @modify:
 */

public class DaoInfo {

    /**
     * 存储 SQl 语句，格式为：{@code <sqlId, sql>} , {@code sqlId} 的格式为：XxxDao的全类名.方法名。
     * <p>notice：该 Map 不可修改</p>
     */
    private final Map<String, String> sqlMap;

    /**
     *
     * @param sqlMap 格式为：{@code <sqlId, sql>} 的 Map
     */
    public DaoInfo(Map<String, String> sqlMap) {
        this.sqlMap = Collections.unmodifiableMap(sqlMap);
    }

    /**
     * 获取 SQL
     * @param sqlId sqlId，格式为：XxxDao的全类名.方法名
     * @return 返回获取到的 SQL 语句，可能为 null
     */
    public String getSql(String sqlId) {
        Objects.requireNonNull(sqlId, "sqlId is null");
        return sqlMap.get(sqlId);
    }

}
