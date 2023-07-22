package com.d.dmybatis05.builder;

import com.d.dmybatis05.annotation.Param;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

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

    private Method method;

    private PreparedStatement preparedStatement;

    public PreparedStatementBuilder(String originalSql, Object[] args, Connection connection, Method method) {
        this.originalSql = originalSql;
        this.args = args;
        this.connection = connection;
        this.method = method;
    }

    public PreparedStatement build() {
        String[] sqlParamNameArr = resolveSqlParam(originalSql);
        // SQL 解析完了，现在应该解析参数了
        Map<String, Object> paramMap = resolveParam(method, args);
        System.out.println("paramMap: \n" + paramMap);

        try {
            for (int i = 0; i < sqlParamNameArr.length; i++) {
                preparedStatement.setObject(i + 1, paramMap.get(sqlParamNameArr[i]));
            }
            return preparedStatement;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> resolveParam(Method method, Object[] args) {
        Parameter[] parameterArr = method.getParameters();
        Map<String, Object> paramMap = new HashMap<>();
        for (int i = 0; i < parameterArr.length; i++) {
            Parameter parameter = parameterArr[i];
            Param param = parameter.getAnnotation(Param.class);
            if (Objects.nonNull(param)) {
                // 那就是单个的对象，例如基本类型和String
                paramMap.put(param.value(), args[i]);
            } else {
                // 那就是 POJO 对象
                try {
                    Class<?> type = parameter.getType();
                    for (Method pojoMethod : type.getMethods()) {
                        if (pojoMethod.getName().startsWith("get") && ! "getClass".equals(pojoMethod.getName())) {
                            String propertyName = resolvePropertyName(pojoMethod);
                            paramMap.put(propertyName, pojoMethod.invoke(args[i]));
                        }
                    }
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return paramMap;
    }

    private String resolvePropertyName(Method pojoMethod) {
        String propertyName = pojoMethod.getName().substring(3);
        char[] charArray = propertyName.toCharArray();
        charArray[0] += 32;
        return new String(charArray);
    }

    private String[] resolveSqlParam(String originalSql) {
        char[] sqlCharArray = originalSql.toCharArray();
        StringBuilder prepareStatementAppender = new StringBuilder();
        StringBuilder propertyValueAppender = new StringBuilder();
        List<String> paramList = new ArrayList<>();

        for (int i = 0; i < sqlCharArray.length; i++) {
            if (i < sqlCharArray.length - 1 && sqlCharArray[i] == '#' && sqlCharArray[i + 1] == '{') {
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

                i = j;
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
