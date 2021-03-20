# second-kill

#### 介绍
    采用技术spring cloud、spring config、lombok、eureka、hystrix、kafka、redis、mysql实现的秒杀系统(
    数据访问量在百万级左右,高并发下保证数据一致性)

#### 软件架构
1.second-kill-config

    项目所有配置文件

2.second-kill-config-server

    端口号:9090
    配置中心服务

3.second-kill-eureka

    端口号:8081
    服务中心

4.second-kill-product

    端口号:8082
    商品服务

5.second-kill-web

    端口号:8083
    前端界面服务

6.second-kill-order

    端口号:8084
    订单服务、支付服务


#### 安装教程
1.启动mysql8

    初始化数据库 ddl\second_kill.sql

2.启动redis 3.2服务

3.启动rocketmq 4.8服务

4.启动second-kill-config-server

    运行com.second.kill.config.server.app.SecondKillConfigServerApplication

5.启动second-kill-eureka

    运行com.second.kill.eureka.app.EurekaServerApplication

6.启动second-kill-product

    运行com.second.kill.product.app.ProductServiceApplication

7.启动second-kill-web

    运行com.second.kill.web.app.WebApplication

#### 使用说明

1.秒杀接口
    使用前请先设置redis缓存 商品活动状态

    POST http://localhost:8083/second-kill-web/sk/secondKill
    { "skuId":"1", "userId":"1" }


#### Redis Key介绍
1.商品活动状态(0结束 1进行中)

    格式:{appId}_product_{skuId}_activity
    例如:second_kill_product_1_activity

1.商品库存数量

    格式:{appId}_product_{skuId}_stock
    例如:second_kill_product_1_stock

#### 参与贡献
1.majian
