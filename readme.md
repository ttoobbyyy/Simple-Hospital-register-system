## Simple-Hospital-register-system

### 技术选型
1. MySQL：简单，使用广泛
2. JavaFX：可以像安卓一样，将view和controller分离，不像awt一样将一些视图和控制代码都写在一起

### 环境问题
#### 开发环境
1. 数据库：mysql 8.0.22  Community
2. JDK：Java11.0.8
3. javafx：javafx-sdk-11.0.2

### 配置问题
1. 数据库：由于是硬性编码，因此使用的数据库名和用户名和MySQL使用的端口都是固定的。
    * 创建用户：CREATE USER 'hospital'@'localhost' IDENTIFIED BY '654321';
    * 创建数据库：CREATE DATABASE hospitalManage;
    * 用户授权：GRANT ALL ON hospitalManage.* TO 'hospital'@'localhost';
    * 构建数据：登录用户，运行主目录下的data.sql，即：source data.sql
   
2. JavaFx配置：由于Java9开始，javafx从jdk中剥离了，而且引入模块化概念，使得运行javafx没有那么方便了需要加上参数
```java
--module-path D:\computer\java\javafx-sdk-11.0.2\lib //这是lib的位置，lib的问题需要自行解决
--add-modules javafx.controls,javafx.fxml,com.jfoenix
```