## 一款简单的网页版数据库表设计工具  
  
[English](./README.md) | [中文](./README-CN.md)
  
#### 1.开发背景  
　　经历过很多项目，在做数据库表设计时，没有一个统一的数据库表设计模板或者规范格式，每一个项目都不一样，基本是使用excel。
问题是如果表比较少，用Excel还可以胜任，如果是200个以上的表呢，再用Excel就太累了。基于此原因，开发了一个简易网页版数据库表设计工具。


目前基本功能都已具备，以后再持续改进以下功能

访问权限 （用户只能访问所属项目的库）
协同编辑 （只做简单的锁定功能，只允许一个人能编辑）
导出sql文
自动生成ER图 （听起来很美好，实现起来有些困难）
支持其他非关系型数据库 （mongodb, solr）

#### 2.安装及使用  

从GitHub下载工程源代码:
git clone https://github.com/jason-jiangjs/dbDesign.git  
导入到Idea，配置数据库联接，本工具使用mongodb来存储运行数据。
数据库必须事先创建好，初始数据在系统第一次运行时自动加载。
开发环境修改:\src\main\resources\application-dev.properties，正式环境修改:\src\main\resources\application-prod.properties
修改"spring.data.mongodb.uri"为实际的数据库连接。
maven编译安装时需要指定参数 "-P dev"或"-P prod"。


