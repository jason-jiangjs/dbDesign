

#### 1.整合drawio到项目  
从github下载工程（注意加参数--depth=1，不需要历史信息，可以减少下载时间）  
git clont --depth=1 https://github.com/jgraph/drawio.git  
  
1.1 复制文件  
把源工程目录 \src\main\java\ 下所有java代码复制到本工程目录 \drawio\src\main\java\  
把源工程目录 \src\main\webapp\ 下所有文件(不包括该目录下的html文件、不包括目录'META-INF'和'WEB-INF')复制到本工程目录 \drawio\src\main\resources\static\   
把源工程目录 \src\main\webapp\ 下所有html文件复制到本工程目录 \drawio\src\main\resources\templates\  
  
1.2 修改代码  
源工程是基于spring mvc的普通web工程，要改为符合spring boot工程的要求，主要是转换现有的web.xml配置文件  
直接修改java代码，添加注解，支持 servlet-mapping  
举例：  
  <servlet>
    <description/>
    <display-name>SaveServlet</display-name>
    <servlet-name>SaveServlet</servlet-name>
    <servlet-class>com.mxgraph.online.SaveServlet</servlet-class>
  </servlet>
  <servlet-mapping>
    <servlet-name>SaveServlet</servlet-name>
    <url-pattern>/SaveServlet</url-pattern>
  </servlet-mapping>
  <servlet-mapping>
    <servlet-name>SaveServlet</servlet-name>
    <url-pattern>/save</url-pattern>
  </servlet-mapping>

  <servlet>
    <description/>
    <display-name>OpenServlet</display-name>
    <servlet-name>OpenServlet</servlet-name>
    <servlet-class>com.mxgraph.online.OpenServlet</servlet-class>
  </servlet>
  <servlet-mapping>
    <servlet-name>OpenServlet</servlet-name>
    <url-pattern>/open</url-pattern>
  </servlet-mapping>
  
分别找到代码SaveServlet和OpenServlet  
添加注解  
@WebServlet(name = "SaveServlet", urlPatterns = {"/save", "/SaveServlet"})  
@WebServlet(name = "OpenServlet", urlPatterns = "/open")  

1.3 添加mxgraph-core.jar依赖  
jar包位于：源工程 \src\main\webapp\WEB-INF\lib\mxgraph-core.jar  
因为mxgraph包没有注册到Maven中央仓库，所以需要手工添加依赖  
  
如果已搭建maven私服Nexus Repository，直接安装该jar包到私服  
mvn deploy:deploy-file -Dfile=${Your Base Dir}\src\main\webapp\WEB-INF\lib\mxgraph-core.jar
 -DgroupId=com.jgraph 
 -DartifactId=mxgraph 
 -Dversion=3.9.11
 -Dpackaging=jar 
 -Durl=http://${Your Nexus Repository}/nexus/content/repositories/releases/ 
 -DrepositoryId=releases

如果没有搭建maven私服，则必须安装到本地仓库
mvn install:install-file -Dfile=${Your Base Dir}\src\main\webapp\WEB-INF\lib\mxgraph-core.jar
 -DgroupId=com.jgraph 
 -DartifactId=mxgraph 
 -Dversion=3.9.11
 -Dpackaging=jar 
 
然后添加pom依赖  
    <dependencies>
        <dependency>
            <groupId>com.jgraph</groupId>
            <artifactId>mxgraph</artifactId>
            <version>3.9.11</version>
        </dependency>
        ......
    </dependencies>
    
    