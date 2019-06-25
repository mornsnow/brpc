## brpc V2.0.0

> 添加对spring cloud config的支持

> spring cloud config 接入步骤：

         1.修改项目Application.Java里的@ComponentScan,修改为 @ComponentScan({"com.brpc.client", "com.quancheng.*.*"})
    
         2.gitlab spring-cloud-bus-config项目中新建名称为自己项目的文件夹，假设为abc，该文件夹包含common,daily,prod子文件夹，详情参考gsk
    
         3.common文件夹下添加abc-common.properties配置文件，存储daily和prod环境通用的配置信息
    
         4.daily文件夹添加abc-conf.properties配置文件，存放daily和开发环境配置信息
    
         5.prod文件夹添加abc-conf.properties配置文件，存放prod环境配置信息
    
         6.修改项目application.properties,添加配置项configDir=common,redis,datasource,mq,abc;其中common,redis,datasource,mq可选
      这些配置存在spring-cloud-bus-config的common目录下对应环境文件夹里
      
         7.brpc包里有SpringPropertyReader.getProperty(),可供测试使用
    
    
### 说明：gitlab里存储的配置信息，daily环境下可以被本地application.properties配置覆盖，prod环境不会被本地配置覆盖  