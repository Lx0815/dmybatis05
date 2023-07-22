package com.d.dmybatis05.util;

import java.lang.reflect.Method;

/**
 * @description:
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-07-22 22:34:18
 * @modify:
 */

public class StringUtil {

    private StringUtil() {}

    public static String resolvePropertyName(Method pojoMethod) {
        String propertyName = pojoMethod.getName().substring(3);
        char[] charArray = propertyName.toCharArray();
        charArray[0] += 32;
        return new String(charArray);
    }

}
