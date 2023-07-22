package com.d.dmybatis05.builder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @description: PreparedStatement 建造者
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-07-22 16:28:58
 * @modify:
 */

public class PreparedStatementBuilder {

    private String originalSql;

    private Object[] args;

    private Connection connection;

    private PreparedStatement preparedStatement;

    public PreparedStatementBuilder(String originalSql, Object[] args, Connection connection) {
        this.originalSql = originalSql;
        this.args = args;
        this.connection = connection;
    }

    public PreparedStatement build() {
        String[] paramArr = resolveSqlParam(originalSql);
        System.out.println(Arrays.toString(paramArr));
        return null;
    }

    private String[] resolveSqlParam(String originalSql) {
        char[] sqlCharArray = originalSql.toCharArray();
        StringBuilder prepareStatementAppender = new StringBuilder();
        StringBuilder propertyValueAppender = new StringBuilder();
        List<String> paramList = new ArrayList<>();

        for (int i = 0; i < sqlCharArray.length - 3; i++) {
            if (sqlCharArray[i] == '#' && sqlCharArray[i + 1] == '{') {
                // 找到占位符了
                // VALUES (#{id},
                //         i
                int j = i + 2;
                for (; j < sqlCharArray.length; j++) {
                    if (sqlCharArray[j] == '}') {
                        break;
                    } else {
                        propertyValueAppender.append(sqlCharArray[j]);
                    }
                }

                paramList.add(propertyValueAppender.toString());
                propertyValueAppender.delete(0, propertyValueAppender.length());
                prepareStatementAppender.append("?");

                i = j + 1;
            } else {
                prepareStatementAppender.append(sqlCharArray[i]);
            }
        }
        String prepareSql = prepareStatementAppender.toString();
        try {
            preparedStatement = connection.prepareStatement(prepareSql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return paramList.toArray(new String[0]);
    }
}
