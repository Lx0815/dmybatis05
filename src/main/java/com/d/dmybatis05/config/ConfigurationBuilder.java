package com.d.dmybatis05.config;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: 配置类构造器
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-07-16 22:32:28
 * @modify:
 */

public class ConfigurationBuilder {

    private ConfigurationBuilder() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    private static Document document;

    public static Configuration build(String resource) {
        SAXReader reader = new SAXReader();
        try {
            document = reader.read(ConfigurationBuilder.class.getClassLoader()
                    .getResourceAsStream(resource));
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }

        // 解析 connection 标签
        ConnectionInfo connectionInfo = parseConnection();
        DaoInfo daoInfo = parseDaos();
        return new Configuration(connectionInfo, daoInfo);

    }

    private static DaoInfo parseDaos() {
        try {

            List<Node> daoNodeList = document.selectNodes("/config/daos/dao");
            Map<String, SqlInfo> sqlMap = new HashMap<>();
            for (Node node : daoNodeList) {
                Element daoEle = (Element) node;
                String classPath = daoEle.attributeValue("id");

                List<Element> elementList = daoEle.elements();
                for (Element element : elementList) {
                    String sqlId = element.attributeValue("id");
                    String sql = element.getTextTrim();
                    SqlInfo.SqlType sqlType = SqlInfo.SqlType.of(element.getName());
                    Class<?> rowType = null;
                    if (sqlType == SqlInfo.SqlType.SELECT) {
                        String rowTypeStr = element.attributeValue("rowType");
                        rowType = Class.forName(rowTypeStr);
                    }
                    sqlMap.put(classPath + "." + sqlId, new SqlInfo(sql, sqlType, rowType));
                }
            }
            return new DaoInfo(sqlMap);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static ConnectionInfo parseConnection() {
        ConnectionInfo connectionInfo = new ConnectionInfo();

        Element urlEle = (Element) document.selectSingleNode("/config/connection/url");
        connectionInfo.setUrl(urlEle.getTextTrim());

        Element driverEle = (Element) document.selectSingleNode("/config/connection/driver");
        connectionInfo.setDriverClassName(driverEle.getTextTrim());

        Element usernameEle = (Element) document.selectSingleNode("/config/connection/username");
        connectionInfo.setUsername(usernameEle.getTextTrim());

        Element passwordEle = (Element) document.selectSingleNode("/config/connection/password");
        connectionInfo.setPassword(passwordEle.getTextTrim());

        return connectionInfo;
    }
}
