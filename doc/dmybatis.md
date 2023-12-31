本框架是一个 ORM （Object Relationship Mapping，即对象关系映射）。旨在让开发者更加高效的使用、管理和维护 SQL 来操作数据库。
# 1 原始 JDBC 的使用方式
```java
package com.d.jdbc01;

import com.d.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * @description:
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-05-19 22:19:51
 * @modify:
 */

public class JDBC01 {

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        // 加载驱动
        String driver = "com.mysql.cj.jdbc.Driver";
        Class.forName(driver);

        // 连接数据库需要的信息
        String jdbcUrl = "jdbc:mysql://localhost:1103/dmybatis";
        String jdbcUsername = "root";
        String jdbcPassword = "123456";

        // 打开一个连接
        Connection connection = DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);

        insertOne(connection);
        selectAll(connection);

    }

    private static void insertOne(Connection connection) throws SQLException {
        // 新建一个对象
        User newUser = new User(
                UUID.randomUUID().toString().replace("-", ""),
                UUID.randomUUID().toString().replace("-", ""),
                UUID.randomUUID().toString().replace("-", ""),
                LocalDateTime.now(),
                LocalDateTime.now(),
                0
        );

        try {
            // 写 SQL
            String sql =
                    "INSERT INTO dmybatis.user (id, username, password, create_date_time, update_date_time, deleted)"
                    + "VALUES (?, ?, ?, ?, ?, ?);";

            // 获取 SQL 预处理之后的语句
            PreparedStatement statement = connection.prepareStatement(sql);

            // 给 ? 赋值
            statement.setString(1, newUser.getId());
            statement.setString(2, newUser.getUsername());
            statement.setString(3, newUser.getPassword());
            statement.setObject(4, newUser.getCreateDateTime());
            statement.setObject(5, newUser.getUpdateDateTime());
            statement.setInt(6, newUser.getDeleted());

            // 执行更新操作
            int row = statement.executeUpdate();
            System.out.println(row);
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                // ignore
            }
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    private static void selectAll(Connection connection) throws SQLException {
        // 写 SQL
        String sql = "SELECT id, username, password, create_date_time, update_date_time, deleted "
                     + "FROM dmybatis.user";
        // 预处理 SQL 语句
        PreparedStatement statement = connection.prepareStatement(sql);

        // 执行查询
        ResultSet resultSet = statement.executeQuery();

        // 对返回结果进行封装
        List<User> userList = new LinkedList<>();
        while (resultSet.next()) {
            String id = resultSet.getString("id");
            String username = resultSet.getString("username");
            String password = resultSet.getString("password");
            LocalDateTime createDateTime = resultSet.getObject("create_date_time", LocalDateTime.class);
            LocalDateTime updateDateTime = resultSet.getObject("update_date_time", LocalDateTime.class);
            int deleted = resultSet.getInt("deleted");

            userList.add(new User(
                    id,
                    username,
                    password,
                    createDateTime,
                    updateDateTime,
                    deleted
            ));

        }

        // 打印
        System.out.println(userList);
    }

}

```
## 1.1 流程分析
![](https://cdn.nlark.com/yuque/0/2023/jpeg/34254608/1685501764198-5e501071-b3b6-4d6b-ad63-323abf664add.jpeg)
在每一次与数据库交互的过程中，只有 数据库连接信息、SQL、SQL 所需的参数、返回值类型 是在变动的，这些就需要交给用户进行配置后使用。

- 数据库连接信息
   - 只使用一次，用于初始化，所以一般使用配置文件进行配置。
- SQL 所需参数 和 SQL 执行后的返回值类型 这两者能让你们联想到什么？参数、返回值，有没有联想到一个方法签名，那么是不是可以让用户定义一个方法，该方法的参数需要传入 SQL 中的 ? 占位符，该方法的返回值需要从 ResultSet 结果集来进行封装。
- SQL 呢？
   - 我们的 SQL 也可以和上面那个方法进行绑定，只要用户调用上面那个方法，我们就拿到方法对应的 SQL 语句，然后把参数注入到 SQL 中，然后执行 SQL 并封装返回值。

那么总结一下这些在变动的属性如何存储：

- 数据库连接信息
   - 配置文件，例如：
      - xml: `<property name="username" value="root"/>`
      - properties: `username=root`
      - .......
- SQL 所需参数和 SQL 执行后的返回值类型
   - 一个方法，且不需要方法体
      - `List<User> selectAll();`
      - `User selectById(int id);`
- SQL
   - 配置文件，例如：
      - xml：`<sql id="selectAll">SELECT * FROM table_name;</sql>`
      - properties: `selectAll=SELECT * FROM table_name;`
      - ......
## 1.2 如何将 SQL 和 操作数据库的方法 进行绑定？
经过 1.1 的分析，我们已经能够使用户对 SQL 执行流程中需要变化的地方进行自定义了。而且，我们是在用户调用了和 SQL 绑定的 方法 时获取并执行对应的 SQL。这一步要怎么实现呢？**我们不可能知道用户在什么接口中定义了什么方法，也就是说，我们无法在编译时就得知用户定义的方法的信息，也就是说，我们需要在运行时获取用户定义的方法的信息。**
还没想到？还没想到？还没想到？在运行时获取类和方法的信息，**反射**啊**。**用户只需要告诉我们：定义了什么接口，该接口的方法都是用于操作数据库的，而且在配置文件中完成了接口方法和 SQL 的一一对应的配置关系。我们就能通过该接口的类对象，获取该接口的所有方法，进而将方法和 SQL 进行绑定。

## 1.3 如何使用户调用 操作数据库的方法 时，由我们（框架）代替？
有前文可知，用户只需要调用 操作数据的方法 即可利用框架完成对数据库的操作。而框架要做的事就是在用户调用这个操作数据库的方法的时候，执行对应的 SQL，那么怎么做到这一步呢？
正常手段来说，我们可以写一个类去继承用户定义的接口，然后实现其方法，在其方法中执行对应的 SQL。但问题是：用户定义的接口不是编译时可知的，同样也需要在运行时动态获取，这里应该就不需要提示了，没错，和上文一样，通过 **反射 ** 在运行时动态的创建一个代理类去实现用户定义的接口。这样就能使用户调用接口方法时跳转到我们定义的方法里了。其实这是 JDK 的动态代理，说这么多，可能不大懂，上代码吧。
[JDK 动态代理](https://github.com/Lx0815/blogs/blob/main/java/%E6%BA%90%E7%A0%81%E8%A7%A3%E6%9E%90/JDK%20%E5%8A%A8%E6%80%81%E4%BB%A3%E7%90%86.md)

# 2 DMyBatis框架的使用方式
理清了框架需要干的活和用户需要干的活之后，就可以重新梳理一下流程了。
用户端的代码将简化为下面这种形式：

```xml
<config>
  <!-- 数据库连接信息 -->
  <connection>
    <url>
      jdbc:mysql:localhost:3306/xxx_db
    </url>
    <driver>
      com.mysql.cj.jdbc.Driver
    </driver>
    <username>
      root
    </username>
    <password>
      123456
    </password>
  </connection>

  <!-- dao 数据访问层的 SQL 配置 -->
  <daos>
    <!-- UserDao 的 SQL 配置，id 为 UserDao 的全类名 -->
    <dao id="com.xxx.UserDao">
			<!-- UserDao 中的 selectAll 方法所绑定的 SQL 语句 -->
      <select id="selectAll">
        SELECT * FROM user;
      </select>
    </dao>
  </daos>
</config>
```
```java
interface UserDao {
    List<User> selectAll();
}
public class Main {
    public static void main(String[] args) {
        // 构造配置对象
        Configuration config = ConfigurationBuilder.build("dmybatis-config.xml");
        // 构造会话工厂
        SqlSessionFactory factory = new SqlSessionFactory(config);
        // 开启会话
        SqlSession session = factory.openSession();
        // 获取 Dao 代理类
		UserDao userDao = session.getDao(UserDao.class);
        // 查询数据库
        List<User> userList = userDao.selectAll();
        // 打印结果
        System.out.println(userList);
        // 提交事务
        session.commit();

        // 如果失败，则在 catch 语句中进行回滚
        // session.rollback();
    }
}
```
上述代码可以结合以下时序图：
![时序图](https://raw.githubusercontent.com/Lx0815/blog_image_repository/main/images/202307212318251.png)
那么框架需要干什么呢？

1. 解析配置文件
2. 创建会话工厂，即数据库连接工厂
3. 通过会话工厂开启会话并开启事务
4. 通过会话对象创建 Dao 接口的代理对象
5. 封装 SQL 参数
6. 执行 SQL
7. 封装SQL 执行后返回的结果集
8. 提交事务
9. 回滚事务（可能）

那么我们就定义五个类，一个类干一件事情。

1. 解析配置文件
   1. `ConfigurationBuilder` 构造 `Configuration` 对象
   2. `Configuration` 对象包含 `ConnectionInfo` 对象和 `DaoInfo` 对象，分别对应配置文件中的 `connection` 标签和 `daos` 标签
2. 提供会话工厂
   1. `SqlSessionFactory`：其构造方法接受一个 `Configuration` 对象，用于创建会话对象（开启事务）
   2. `SqlSession`：其构造方法接受一个 `Configuration` 对象，通过 `Connection`  对象开启事务（会话）、提交事务等操作。
3. 创建 Dao 接口的代理对象
   1. 此步骤通过 `SqlSession` 对象完成
4. 封装 SQL 参数
   1. `SqlBuilder`：解析 EL 表达式
5. 执行 SQL
   1. `SqlExecutor`：将 SQL 提交到数据库执行
6. 封装 SQL 执行后返回的结果集
   1. `ResultSetBuilder`：将返回结果根据 SQL 语句的类型进行封装
7. 提交事务/回滚事务
   1. 通过 `SqlSession` 对象完成
# 3 详细设计

为了便于后续设计时进行测试，我们先进行一些准备工作：

1. 创建测试用的数据库和表

```sql
DROP DATABASE IF EXISTS dmybatis05;
CREATE DATABASE IF NOT EXISTS dmybatis05;

USE dmybatis05;

CREATE TABLE `user`
(
    `id` CHAR(32) PRIMARY KEY COMMENT '主键 ID',
    `username` VARCHAR(32) NOT NULL COMMENT '用户名',
    `password` VARCHAR(16) NOT NULL COMMENT '密码',
    `create_date_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_date_time` DATETIME NOT NULL COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除字段，0 表示未删除'
) COMMENT '用户表';
```

2. 创建对应的类

UserDao 和 User：

```java
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

    int insert(User user);

}

```

```java
package com.d.dmybatis05;

import java.time.LocalDateTime;

/**
 * @description:
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-05-19 22:29:22
 * @modify:
 */

public class User {

    private String id;

    private String username;

    private String password;

    private LocalDateTime createDateTime;

    private LocalDateTime updateDateTime;

    private Integer deleted;

    public User() {
    }

    public User(String id, String username, String password, LocalDateTime createDateTime, LocalDateTime updateDateTime,
                Integer deleted) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.createDateTime = createDateTime;
        this.updateDateTime = updateDateTime;
        this.deleted = deleted;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public LocalDateTime getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(LocalDateTime createDateTime) {
        this.createDateTime = createDateTime;
    }

    public LocalDateTime getUpdateDateTime() {
        return updateDateTime;
    }

    public void setUpdateDateTime(LocalDateTime updateDateTime) {
        this.updateDateTime = updateDateTime;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "User{" +
               "id='" + id + '\'' +
               ", username='" + username + '\'' +
               ", password='" + password + '\'' +
               ", createDateTime=" + createDateTime +
               ", updateDateTime=" + updateDateTime +
               ", deleted=" + deleted +
               '}';
    }
}

```

3. 修改配置文件如下：(具体的配置根据你们自己的情况写   )

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<config>
    <!-- 数据库连接信息 -->
    <connection>
        <url>
            jdbc:mysql:localhost:3306/dmybatis05
        </url>
        <driver>
            com.mysql.cj.jdbc.Driver
        </driver>
        <username>
            root
        </username>
        <password>
            123456
        </password>
    </connection>

    <!-- dao 数据访问层的 SQL 配置 -->
    <daos>
        <!-- UserDao 的 SQL 配置，id 为 UserDao 的全类名 -->
        <dao id="com.d.dmybatis05.UserDao">
            <!-- UserDao 中的 selectAll 方法所绑定的 SQL 语句 -->
            <select id="selectAll">
                SELECT * FROM user;
            </select>
        </dao>
    </daos>
</config>
```

4. 引入 JDBC 驱动

```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.31</version>
</dependency>
```



## 3.1 解析配置文件
### 3.1.1 依赖引入
配置文件这边选用了 XML 格式，所以解析配置文件将使用如下依赖：
```xml
<dependency>
    <groupId>org.dom4j</groupId>
    <artifactId>dom4j</artifactId>
    <version>2.1.4</version>
</dependency>
<dependency>
    <groupId>jaxen</groupId>
    <artifactId>jaxen</artifactId>
    <version>2.0.0</version>
</dependency>
```
### 3.1.2 类设计
再次回顾一下 XML 配置的内容：[DMybatis的核心配置文件示例](https://laputa.yuque.com/ze41wg/guil3k/bzr70mvo1hnhqtoh?inner=htHi3)
我们使用一个 Configuration 类来存储 config 标签下的内容，使用 ConnectionInfo 类来存储 connection 标签下的内容，使用 DaoInfo 类来存储 daos 标签下的内容。类之间的关系也和 XML 中关系一样，ConnectionInfo 类和 DaoInfo 类将作为 Configuration 类的属性。
下面是代码：


```java
// Configuration.java
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

    public Configuration() {
    }

    public Configuration(ConnectionInfo connectionInfo, DaoInfo daoInfo) {
        this.connectionInfo = connectionInfo;
        this.daoInfo = daoInfo;
    }

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

```
```java
// ConnectionInfo.java
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

```
```java
// DaoInfo.java
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

```
```java
// ConfigurationBuilder.java
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
        List<Node> daoNodeList = document.selectNodes("/config/daos/dao");
        Map<String, String> sqlMap = new HashMap<>();
        for (Node node : daoNodeList) {
            Element daoEle = (Element) node;
            String classPath = daoEle.attributeValue("id");

            List<Element> elementList = daoEle.elements();
            for (Element element : elementList) {
                String sqlId = element.attributeValue("id");
                String sql = element.getTextTrim();
                sqlMap.put(classPath + "." + sqlId, sql);
            }
        }
        return new DaoInfo(sqlMap);
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

```

### 3.1.3 测试
然后在 test 包下建 ConfigurationBuilderTest 类进行测试。
记得引入 junit 测试包，这里选用 junit4 的
```xml
<dependency>
   <groupId>junit</groupId>
   <artifactId>junit</artifactId>
   <version>4.13.2</version>
</dependency>
```

```java
package com.d.dmybatis05.config;

import org.junit.Test;

import java.lang.reflect.Field;

/**
 * @description: ConfigurationBuilder 的测试类
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-07-17 23:04:00
 * @modify:
 */

public class ConfigurationBuilderTest {

    @Test
    public void testBuilder() throws NoSuchFieldException, IllegalAccessException {
        Configuration configuration = ConfigurationBuilder.build("dmybatis-config.xml");
        ConnectionInfo connectionInfo = configuration.getConnectionInfo();
        DaoInfo daoInfo = configuration.getDaoInfo();
        System.out.println(connectionInfo.getUrl());
        System.out.println(connectionInfo.getDriverClassName());
        System.out.println(connectionInfo.getUsername());
        System.out.println(connectionInfo.getPassword());

        Field sqlMapField = DaoInfo.class.getDeclaredField("sqlMap");
        sqlMapField.setAccessible(true);
        Object sqlMap = sqlMapField.get(daoInfo);
        System.out.println(sqlMap.toString());

        /*
            jdbc:mysql:localhost:3306/xxx_db
            com.mysql.cj.jdbc.Driver
            root
            123456
            {com.xxx.UserDao.selectAll=SELECT * FROM user;}
         */
    }

}

```

运行结果如下：
```text
jdbc:mysql:localhost:3306/xxx_db
com.mysql.cj.jdbc.Driver
root
123456
{com.xxx.UserDao.selectAll=SELECT * FROM user;}
```

可知测试成功，我们现在已经能够获取到配置文件了！

## 3.2 连接数据库 & 开启会话

### 3.2.1 SqlSessionFactory 和 SqlSession 的创建

读取到用户配置的数据库文件后，就可以开始连接数据库了！一个会话其实就对应着一个连接，那么一个 `SqlSession` 对象里一定包含一个 `Connection`  对象。那我们暂且把会话理解成连接，那么由谁来创建数据库连接呢？这里可以用到工厂模式，我们专门写一个工厂类 `SqlSessionFactory` ，使其专门创建和数据库的连接对象，至于连接之后干什么？执行什么SQL都和他无关，这就是单一职责原则，每个类只干好自己的事，别人的事情管不着，也不需要管。

那么下面就开始代码实现：

```java
*package com.d.dmybatis05.session;

import com.d.dmybatis05.config.Configuration;
import com.d.dmybatis05.config.ConnectionInfo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @description: SqlSessionFactory 的工厂类
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-07-21 23:30:15
 * @modify:
 */

public class SqlSessionFactory {

    private Configuration configuration;

    public SqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
        // 注册数据库驱动
        try {
            Class.forName(configuration.getConnectionInfo().getDriverClassName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("数据库驱动注册失败", e);
        }
    }

    public SqlSession openSession() {
        return new SqlSession(newConnection());
    }

    private Connection newConnection() {
        try {
            ConnectionInfo connectionInfo = configuration.getConnectionInfo();
            return DriverManager.getConnection(connectionInfo.getUrl(), connectionInfo.getUsername(), connectionInfo.getPassword());
        } catch (SQLException e) {
            throw new RuntimeException("创建数据库连接失败", e);
        }
    }
}
```



```java
package com.d.dmybatis05.session;

import java.sql.Connection;

/**
 * @description: 会话对象
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-07-21 23:29:37
 * @modify:
 */

public class SqlSession {

    private Connection connection;

    public SqlSession(Connection connection) {
        this.connection = connection;
    }

}
```

### 3.2.2 新增会话管理操作

```java
package com.d.dmybatis05.session;

import com.d.dmybatis05.UserDao;
import com.d.dmybatis05.config.Configuration;
import com.d.dmybatis05.proxy.DaoProxy;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 * @description: 会话对象
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-07-21 23:29:37
 * @modify:
 */

public class SqlSession {

    private Connection connection;

    public SqlSession(Configuration configuration, Connection connection) {
        Objects.requireNonNull(connection, "连接对象为 null");
        this.connection = connection;
    }

    /**
     * 获取 Dao 的代理对象
     * @param daoClz dao 接口的类对象
     * @return 返回 Dao 的代理对象
     * @param <T> Dao 接口的类型
     */
    @SuppressWarnings("unchecked")
    public <T> T getDao(Class<T> daoClz) {
        return (T) Proxy.newProxyInstance(DaoProxy.class.getClassLoader(),
                new Class[]{UserDao.class},
                new DaoProxy(configuration));
    }

    /**
     * 提交事务
     */
    public void commit() {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 回滚事务
     */
    public void rollback() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 关闭会话，即关闭连接
     */
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

```

## 3.3 获取 Dao 的代理类

开启了事务之后，就应该获取 SQL，然后给 SQL 中的参数赋值，然后执行 SQL。那么如何执行我们写在配置文件里的 SQL 呢？前面提到了使用 JDK 的代理模式，即定义一个接口，使该接口的方法与 SQL 语句一一绑定，然后创建其代理类，让其代理类代为执行 SQL 语句。

那么就先定义该代理类吧，该代理类定义如下：

```java
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

```

现在刚拿到 SQL，我们不妨写一个测试类看看是否真的能获取到绑定的 SQL

测试类：

```java
package com.d.dmybatis05.proxy;

import com.d.dmybatis05.UserDao;
import com.d.dmybatis05.config.Configuration;
import com.d.dmybatis05.config.ConfigurationBuilder;
import com.d.dmybatis05.config.ConfigurationBuilderTest;
import com.d.dmybatis05.config.ConnectionInfo;
import org.junit.Test;

import java.lang.reflect.Proxy;

/**
 * @description: 代理类测试类
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-07-22 9:13:47
 * @modify:
 */

public class DaoProxyTest {

    @Test
    public void testCreateProxy() {
        Configuration configuration = ConfigurationBuilder.build("dmybatis-config.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactory(configuration);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserDao userDao = sqlSession.getDao(UserDao.class);
        System.out.println(userDao.selectAll());
    }

}

```

测试结果如下：

```text
SELECT * FROM user;
null
```

说明已经能够获取到 SQL 了。

## 3.4 注入 SQL 参数

刚刚获取到的 SQL 只是一条最简单的全表查询语句，这里面是不需要从外部传入参数的，那么什么 SQL 需要从程序里传入参数呢？例如新增用户的时候：

```sql
INSERT INTO `user` (`id`, `username`, `password`, `create_date_time`, `update_date_time`, `deleted`)
VALUES (#{id}, #{username}, #{password}, #{create_date_time}, #{update_date_time}, #{deleted});
```

又或者用户登录的时候：

```sql
SELECT * 
FROM `user`
WHERE `username` = #{username}
	AND `password` = #{password};
```

这里是通过 #{propertyName} 的方式来表达占位符的，即把什么属性放在什么地方。

所以为了便于后续的测试，这里新增一条插入语句。

```xml
<insert id="insert">
    INSERT INTO `user` (`id`, `username`, `password`, `create_date_time`, `update_date_time`, `deleted`)
    VALUES (#{id}, #{username}, #{password}, #{create_date_time}, #{update_date_time}, #{deleted});
</insert>
```

### 3.4.1 SQL 解析

那么现在就开始编写一个类，用于解析 SQL 中的参数占位符，并根据参数占位符中的参数名称，把用户传入的参数替换到参数占位符的位置。

先解析 SQL 中的参数占位符，代码实现：

```java
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

```

然后修改 DaoProxy 中的方法，使其调用该类：

```java
package com.d.dmybatis05.proxy;

import com.d.dmybatis05.builder.PreparedStatementBuilder;
import com.d.dmybatis05.config.Configuration;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * @description: Dao 接口的代理类
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-07-22 9:01:45
 * @modify:
 */

public class DaoProxy implements InvocationHandler {

    private Configuration configuration;

    private Connection connection;

    public DaoProxy(Configuration configuration, Connection connection) {
        this.configuration = configuration;
        this.connection = connection;
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

        PreparedStatementBuilder statementBuilder = new PreparedStatementBuilder(sql, args, connection);
        PreparedStatement build = statementBuilder.build();
        return null;
    }
}

```

在这个过程中发现，DaoProxy 还需要

 connection 对象，SqlSession 还需要 Configuration 对象，我们通过构造器传入即可。

下面是测试类：

```java
package com.d.dmybatis05.builder;

import com.d.dmybatis05.User;
import com.d.dmybatis05.UserDao;
import com.d.dmybatis05.config.Configuration;
import com.d.dmybatis05.config.ConfigurationBuilder;
import com.d.dmybatis05.session.SqlSession;
import com.d.dmybatis05.session.SqlSessionFactory;
import org.junit.Test;

/**
 * @description:
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-07-22 17:16:58
 * @modify:
 */

public class PreparedStatementBuilderTest {

    @Test
    public void testSqlPrepare() {
        Configuration configuration = ConfigurationBuilder.build("dmybatis-config.xml");
        SqlSessionFactory factory = new SqlSessionFactory(configuration);
        SqlSession sqlSession = factory.openSession();
        UserDao userDao = sqlSession.getDao(UserDao.class);

        System.out.println(userDao.selectAll());
        System.out.println("---------");
        System.out.println(userDao.insert(new User()));
    }

}

```

测试结果如下：

```
SELECT * FROM user;
[]
null
---------
INSERT INTO `user` (`id`, `username`, `password`, `create_date_time`, `update_date_time`, `deleted`) VALUES (#{id}, #{username}, #{password}, #{create_date_time}, #{update_date_time}, #{deleted});
[id, username, password, create_date_time, update_date_time, deleted]
null

Process finished with exit code 0
```

可以发现，现在已经能够成功获取到 SQL 中占位符的信息，以及创建 PrepareStatement 了。

### 3.4.2 解析方法参数

要解析方法参数，只有一个 Object[] 类型的 args肯定是不够的，之前提到过，方法参数与 SQL 中的参数的绑定是需要用到反射的，所以这边还需要增加一个 Method 字段，并通过构造器传入。

然后我们就应该思考，Dao 接口方法的参数可能会是什么呢？

如果是 SQL ：

```sql
SELECT * 
FROM `user`
WHERE `username` = #{username}
	AND `password` = #{password};
```

那么可能用户定义的方法签名长这样：

```java
User selectByUsernameAndPassword(String username, String password);
```

也可能长这样：

```java
User login(User user);// user 对象中包含 username 和 password 属性
```

也可能用户正在写原生的Servlet项目，喜欢用Map：

```java
User login(Map<String, Object> paramMap);
```

你或许会想：为什么不能是数组？是List？

因为数组和List其实是一类，即不存在两者之间的对应关系，如 username 这个属性名对应的值可以是 123 可以是 abc，这种对应关系可以通过定义多个参数，定义对象，Map 的方式来表示，但是无法通过列表表示。

那么为什么需要这种对应关系呢？把数组中的数据挨个放到 SQL 的参数中不行吗？行，但是不利于维护，也很容易出错，一个Object[] 里面卖的什么药，只有你自己知道，同样的Map也是。所以我们这里只实现第一二种。

问题又双来了，无特殊配置的JVM是获取不到方法参数原名称的，大家可以自行实验，获取到的都是 arg1，arg2，.... ，这是为什么呢？这是因为Java编译器在把 Java 源文件编译为 class 字节码的时候做了语法分析将其替换掉了（为什么替换我也不知道），这需要通过一个 JVM 的启动参数进行开启：`-parameters` ，在 Maven 中需要通过 pom 文件配置开启：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>${maven.compiler.version}</version>
    <configuration>
        <source>1.8</source>
        <target>1.8</target>
        <compilerArgs>
            <arg>-parameters</arg>
        </compilerArgs>
    </configuration>
</plugin>
```

我们这里就不开启了，我们通过一个 @Param 注解来实现类似的功能。

注解代码如下：

```java
package com.d.dmybatis05.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description:
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-07-22 18:15:44
 * @modify:
 */

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {

    /**
     * 方法参数名称，即对应 SQL 中的参数占位符名称
     * @return
     */
    String value();
    
}
```

参数解析类：

```java
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
        return null;
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

```

参数解析类测试类：

```java
    @Test
    public void testResolveParam() {
        Configuration configuration = ConfigurationBuilder.build("dmybatis-config.xml");
        SqlSessionFactory factory = new SqlSessionFactory(configuration);
        SqlSession sqlSession = factory.openSession();
        UserDao userDao = sqlSession.getDao(UserDao.class);

        System.out.println(userDao.insert(new User("1", "123", "123", LocalDateTime.now(), LocalDateTime.now(), 0)));
    }
```

```text
SQL: 
INSERT INTO `user` (`id`, `username`, `password`, `create_date_time`, `update_date_time`, `deleted`) VALUES (#{id}, #{username}, #{password}, #{create_date_time}, #{update_date_time}, #{deleted});
paramMap: 
{password=123, deleted=0, id=1, updateDateTime=2023-07-22T18:58:49.343, username=123, createDateTime=2023-07-22T18:58:49.343}
null
```

现在已经可以获取到方法参数了。

### 3.4.3 注入方法参数到 SQL 中

有了前两节的铺垫，终于可以把方法参数成功的注入到 SQL 中了，代码实现如下：

```java
    // PreparedStatementBuilder.java
    public PreparedStatement build() {
        String[] sqlParamNameArr = resolveSqlParam(originalSql);
        // SQL 解析完了，现在应该解析参数了
        Map<String, Object> paramMap = resolveParam(method, args);
        System.out.println("paramMap: \n" + paramMap);

        try {
            for (int i = 0; i < sqlParamNameArr.length; i++) {
                preparedStatement.setObject(i, paramMap.get(sqlParamNameArr[i]));
            }
            return preparedStatement;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
```

但是问题来了，方法参数注入完了，需要真正的执行 SQL 了，PreparedStatement 有两个提交 SQL 的方法，一个是 executeUpdate() 一个是 executeQuery() ，我怎么知道当前 SQL 是哪一种呢？

所以，我们在读取 SQL 的时候不能只读取 SQL 字符串，还需要保存其他的参数，下面修改如下：

1. 新增 SqlInfo 类

```java
package com.d.dmybatis05.config;

/**
 * @description:
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-07-22 19:44:26
 * @modify:
 */

public class SqlInfo {

    private String sql;

    private SqlType sqlType;

    public SqlInfo() {
    }

    public SqlInfo(String sql, SqlType sqlType) {
        this.sql = sql;
        this.sqlType = sqlType;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public SqlType getSqlType() {
        return sqlType;
    }

    public void setSqlType(SqlType sqlType) {
        this.sqlType = sqlType;
    }

    public enum SqlType {
        INSERT, UPDATE, DELETE, SELECT;

        public static SqlType of(String value) {
            if ("insert".equalsIgnoreCase(value)) {
                return INSERT;
            } else if ("update".equalsIgnoreCase(value)) {
                return UPDATE;
            } else if ("delete".equalsIgnoreCase(value)) {
                return DELETE;
            } else if ("select".equalsIgnoreCase(value)) {
                return SELECT;
            } else {
                throw new RuntimeException("标签错误");
            }
        }
    }

}
```

2. 修改原有的类

```java
    private static DaoInfo parseDaos() {
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
                sqlMap.put(classPath + "." + sqlId, new SqlInfo(sql, sqlType));
            }
        }
        return new DaoInfo(sqlMap);
    }

// -----------------

public class DaoInfo {

    /**
     * 存储 SQl 语句，格式为：{@code <sqlId, sql>} , {@code sqlId} 的格式为：XxxDao的全类名.方法名。
     * <p>notice：该 Map 不可修改</p>
     */
    private final Map<String, SqlInfo> sqlMap;

    /**
     *
     * @param sqlMap 格式为：{@code <sqlId, sql>} 的 Map
     */
    public DaoInfo(Map<String, SqlInfo> sqlMap) {
        this.sqlMap = Collections.unmodifiableMap(sqlMap);
    }

    /**
     * 获取 SQL
     * @param sqlId sqlId，格式为：XxxDao的全类名.方法名
     * @return 返回获取到的 SQL 语句，可能为 null
     */
    public SqlInfo getSql(String sqlId) {
        Objects.requireNonNull(sqlId, "sqlId is null");
        return sqlMap.get(sqlId);
    }

}
```

然后便可执行真正的 SQL 了，由于查询方法返回的 ResultSet 需要进行封装，而 插入语句却不需要额外的封装，所以这里先用 insert 方法进行测试。

只需要改动 DaoProxy 的 execute 方法即可：

```java
private Object execute(Object proxy, Method method, Object[] args) {
        // 获取被代理的方法所绑定的 SQL 语句，所以这里需要传入 Configuration 对象
        String sqlId = method.getDeclaringClass().getName() + "." + method.getName();
        SqlInfo sqlInfo = configuration.getDaoInfo().getSql(sqlId);

        System.out.println("SQL: \n" + sqlInfo.getSql());

        PreparedStatementBuilder statementBuilder = new PreparedStatementBuilder(sqlInfo.getSql(), args, connection, method);
        PreparedStatement preparedStatement = statementBuilder.build();
        try {

            switch (sqlInfo.getSqlType()) {
                case UPDATE:
                case DELETE:
                case INSERT:
                    int row = preparedStatement.executeUpdate();
                    System.out.println("被影响的行数： " + row);
                    return row;
                case SELECT:
                    ResultSet resultSet = preparedStatement.executeQuery();
                    System.out.println("读取到的数据长度为：" + resultSet.getFetchSize());
                    return null;
                default:
                    throw new RuntimeException();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
```

测试方法如下：

```java
    @Test
    public void testPreparedStatementBuilder() {
        Configuration configuration = ConfigurationBuilder.build("dmybatis-config.xml");
        SqlSessionFactory factory = new SqlSessionFactory(configuration);
        SqlSession sqlSession = factory.openSession();
        UserDao userDao = sqlSession.getDao(UserDao.class);

        System.out.println(userDao.insert(new User("1", "123", "123", LocalDateTime.now(), LocalDateTime.now(), 0)));
    }
```

根据测试结果：

```text
SQL: 
INSERT INTO `user` (`id`, `username`, `password`, `create_date_time`, `update_date_time`, `deleted`) VALUES (#{id}, #{username}, #{password}, #{createDateTime}, #{updateDateTime}, #{deleted});
paramMap: 
{password=123, deleted=0, id=1, updateDateTime=2023-07-22T21:46:11.733, username=123, createDateTime=2023-07-22T21:46:10.313}
被影响的行数： 1
1
```

可知，数据已经插入到数据库了，那么查询一下数据库看看是不是？

![image](https://raw.githubusercontent.com/Lx0815/blog_image_repository/main/images/202307222154527.png)

## 3.5 解析 SQL 执行的返回值

我们一般使用的返回值是 ResultSet，那么现在我们要做的就是，通过反射，从 ResultSet 中获取数据并将转换为对应的返回值。

那么就需要一个 ResultSetParser 来解析 ResultSet，下面定义这个类：

```java
package com.d.dmybatis05.result;

import org.junit.Test;

import java.lang.reflect.Method;
import java.sql.ResultSet;

/**
 * @description: ResultSet 解析器
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-07-22 21:58:01
 * @modify:
 */

public class ResultSetParser<T> {

    private ResultSet resultSet;

    private Class<T> returnType;

    public ResultSetParser(ResultSet resultSet, Class<T> returnType) {
        this.resultSet = resultSet;
        this.returnType = returnType;
    }

    public T parse() {
        return null;
    }
}

```

定义完 parse 方法后，又发现了问题，我们无法知道确切的返回值类型是什么。例如，如果返回值是 `List<User>` ，那么我们只能获取到 List.class ，而 List 中包含的具体的数据类型是什么我们无法获取。相信你们也不希望用户调用完查询接口之后，返回一个 `List<Object> ` ，然后让用户自己手动强转吧，这样也很容易导致类型转换异常。那么怎么解决呢？学过 MyBatis 的小伙伴肯定知道，MyBatis 的 XML 配置有一个属性是 returnType 的，里面指定的是返回数据的类型。注意了，这里指的不一定是方法返回值的类型，而是返回的数据中，某一行所对应的数据类型，即 List 的泛型。

那么又要开始改代码了，，，

1. 在 SqlInfo 中添加一个 Class 类型的属性叫 rowType
2. 修改 ConfigurationBuilder 类的 parseDaos 方法如下：

```java
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
                    String rowTypeStr = element.attributeValue("rowType");
                    Class<?> rowType = Class.forName(rowTypeStr);
                    sqlMap.put(classPath + "." + sqlId, new SqlInfo(sql, sqlType, rowType));
                }
            }
            return new DaoInfo(sqlMap);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
```

ok。如此一来，我们就可以获取到行的数据类型了。

下面就开始编写返回值的封装。

代码实现：

```java
public T parse() {
        try {
            while (resultSet.next()) {

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
```

代码写到这，又卡住了。我们怎么把列名和实体类的属性名一一对应呢？现在有两种方法：

1. 按照之前将方法参数名和SQL占位符对应的方法，即创建一个注解指定
2. 通过规范约束，即直接将驼峰命名转换为下划线命名。

很明显第一种方式的可扩展性更强，因为如果用户的表名包含前缀或者不规范呢？（其实就是懒得写，第二种方式像一种算法，第一种像面向对象编程）

我们就选择用第一种方式进行实现吧，我们定义一个注解 @TableField，如下：

```java
package com.d.dmybatis05.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description:
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-07-22 22:30:38
 * @modify:
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TableField {

    String value();

}

```

这个 value() 的返回值即为数据库的列名称，或者说查询语句返回的列名称

然后就可以继续编写解析器了，代码实现如下：

```java
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

```

修改 DaoProxy 中的 execute 方法如下：

```java
private Object execute(Object proxy, Method method, Object[] args) {
        // 获取被代理的方法所绑定的 SQL 语句，所以这里需要传入 Configuration 对象
        String sqlId = method.getDeclaringClass().getName() + "." + method.getName();
        SqlInfo sqlInfo = configuration.getDaoInfo().getSql(sqlId);

        System.out.println("SQL: \n" + sqlInfo.getSql());

        PreparedStatementBuilder statementBuilder = new PreparedStatementBuilder(sqlInfo.getSql(), args, connection, method);
        PreparedStatement preparedStatement = statementBuilder.build();
        try {

            switch (sqlInfo.getSqlType()) {
                case UPDATE:
                case DELETE:
                case INSERT:
                    int row = preparedStatement.executeUpdate();
                    System.out.println("被影响的行数： " + row);
                    return row;
                case SELECT:
                    ResultSet resultSet = preparedStatement.executeQuery();
                    System.out.println("读取到的数据长度为：" + resultSet.getFetchSize());
                    return new ResultSetParser(resultSet, method.getReturnType(), sqlInfo.getRowType()).parse();
                default:
                    throw new RuntimeException();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
```

编写测试类如下：

```java
package com.d.dmybatis05.result;

import com.d.dmybatis05.User;
import com.d.dmybatis05.UserDao;
import com.d.dmybatis05.config.Configuration;
import com.d.dmybatis05.config.ConfigurationBuilder;
import com.d.dmybatis05.session.SqlSession;
import com.d.dmybatis05.session.SqlSessionFactory;
import org.junit.Test;

import java.time.LocalDateTime;

/**
 * @description:
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-07-22 22:44:35
 * @modify:
 */

public class ResultSetParserTest {

    @Test
    public void test() {
        Configuration configuration = ConfigurationBuilder.build("dmybatis-config.xml");
        SqlSessionFactory factory = new SqlSessionFactory(configuration);
        SqlSession sqlSession = factory.openSession();
        UserDao userDao = sqlSession.getDao(UserDao.class);

        System.out.println(userDao.selectAll());
    }

}

```

测试结果如下：

```text
SQL: 
SELECT * FROM user;
paramMap: 
{}
读取到的数据长度为：0
[User{id='1', username='123', password='123', createDateTime=2023-07-22T21:46:10, updateDateTime=2023-07-22T21:46:12, deleted=0}]

```

读取数据长度为 0 那个不用管，那个方法的返回值好像不是数据的行数。

