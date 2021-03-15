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