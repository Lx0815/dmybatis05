package com.d.dmybatis05.result;

import com.d.dmybatis05.annotation.TableField;
import com.d.dmybatis05.util.StringUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @description: ResultSet 解析器
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-07-22 21:58:01
 * @modify:
 */

public class ResultSetParser {

    private ResultSet resultSet;

    private Class<?> methodReturnType;

    private Class<?> rowType;

    public ResultSetParser(ResultSet resultSet, Class<?> methodReturnType, Class<?> rowType) {
        this.resultSet = resultSet;
        this.methodReturnType = methodReturnType;
        this.rowType = rowType;
    }

    public Object parse() {
        if (methodReturnType.isAssignableFrom(List.class)) {
            return parseList();
        } else {
            try {
                resultSet.next();
                return parseOne();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Object parseList() {
        try {

            List<Object> tList = new LinkedList<>();
            while (resultSet.next()) {
                tList.add(parseOne());
            }
            return tList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private Object parseOne() {
        try {
            Object instance = rowType.getConstructor().newInstance();

            Method[] rowTypeMethods = rowType.getMethods();
            for (Method rowTypeMethod : rowTypeMethods) {
                if (rowTypeMethod.getName().startsWith("set")) {
                    String propertyName = StringUtil.resolvePropertyName(rowTypeMethod);
                    Field field = rowType.getDeclaredField(propertyName);
                    TableField tableField = field.getAnnotation(TableField.class);
                    String columnName = tableField.value();

                    Object value = resultSet.getObject(columnName, field.getType());
                    rowTypeMethod.invoke(instance, value);
                }
            }

            return instance;
        } catch (SQLException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
