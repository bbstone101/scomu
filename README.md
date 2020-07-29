# scomu

#### 介绍
socket通信的客户端和服务端的通用库，基于Netty和ProtoBuf。

#### 软件架构

![scomu sequence diagram](https://images.gitee.com/uploads/images/2020/0722/131812_8b6303e5_7784580.jpeg "scomu-sd.JPG")

#### 软件架构说明
	








#### 安装教程

1.  按照以下顺序分别在scomu-comm, scomu-parent, scomu-server, scomu-client 执行:

    mvn clean install 

2.  scomu-client-demo 和 scomu-server-deom 为SpringBoot项目，执行以下命令打包：

    mvn clean packe -Dmaven.test.skip=true


#### 使用说明

1.  scomu-client和scomu-server分别在test目录(src/test/java)有对应的测试启动类ClientStarter.java和ServerStarter.java，
    
    可以直接run as -> Java Application 启动。
	
2.  demo启动方式：

    client demo 启动：java -jar /your_path/scomu-client-demo-0.0.1-SNAPSHOT.jar

    server demo 启动：java -jar /your_path/scomu-server-demo-0.0.1-SNAPSHOT.jar

    client demo访问: http://localhost:9001/swagger-ui.html

    server demo访问: http://localhost:9001/swagger-ui.html


#### Roadmap


    1. 通过访问redis获取用户密码 （留了placehold，代码未实现）
    2. server验证srvRandAnswer通过后生成的accessToken保存Redis（未实现）
    3. 


