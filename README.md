

## 项目说明

此项目是一个实时统计计算某电影网站相关数据点击量的spark项目

## 项目环境

操作系统: Ubuntu18.04  64位

SDK选择: scala2.11.12 + JDK1.8

数据库: redis4.0.9(hbase, mysql, oracle也可以)

数据处理框架: 使用Spark2.4.5进行实时数据流处理

Zookeeper3.4.14集群

Flume1.6.0-cdh5.15.2充当生产者

Kafka2.4.1集群进行消费 

**注: 此项目使用的是单机分布伪集群, 真实生产环境应使用分布式集群**

## 数据准备

由于没有实际数据, 所以此项目使用Python来实现模拟数据, 脚本在仓库的[sh目录](sh)下有一份py脚本和一个shell脚本方便执行, 实际开发中的数据应该从javaweb中获取

此项目数据生成在/usr/app/logs/log文件, 可通过linux下的watch命令每一段时间向log文件追加20条数据

~~~shell
 例如:watch -n 1 log_generator.sh
 每一秒执行一次log_generator.sh脚本
~~~

数据格式为:

~~~
ip地址	  时间		请求头的部分		来源		状态码			地区
例: 
202.123.84.201	2020-05-12 12:47:30	"GET /www/3 HTTP/1.0"	-	302	广州
~~~

具体数据格式请参照仓库下的py脚本查看源码

## zookeeper服务集群

zookeeper集群端口:

```
zookeeper01    port:2181 
zookeeper02    port:2182
zookeeper03    port:2183
```

### 开启zookeeper服务并检查

```
./zookeeper01/bin/zkServer.sh start
./zookeeper02/bin/zkServer.sh start
./zookeeper03/bin/zkServer.sh start
```

```
./zookeeper01/bin/zkServer.sh status
./zookeeper02/bin/zkServer.sh status
./zookeeper03/bin/zkServer.sh status
```

```
ZooKeeper JMX enabled by default
Using config: /usr/app/zookeeper01/bin/../conf/zoo.cfg
Mode: follower

ZooKeeper JMX enabled by default
Using config: /usr/app/zookeeper02/bin/../conf/zoo.cfg
Mode: leader

ZooKeeper JMX enabled by default
Using config: /usr/app/zookeeper03/bin/../conf/zoo.cfg
Mode: follower
```



## kafka集群

#### 启动kafka集群

~~~
./bin/kafka-server-start.sh config/server-1.properties    #port:9092
./bin/kafka-server-start.sh config/server-2.properties    #port:9093
./bin/kafka-server-start.sh config/server-3.properties    #port:9094
~~~

#### 列出主题 

~~~
./bin/kafka-topics.sh --list --zookeeper localhost:2181
~~~

#### 创建主题(flumeTopic)

~~~
./bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 3 --partitions 1 --topic flumeTopic
~~~

#### 启动消费者:

```
./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic flumeTopic
./bin/kafka-console-consumer.sh --bootstrap-server localhost:9093 --topic flumeTopic
./bin/kafka-console-consumer.sh --bootstrap-server localhost:9094 --topic flumeTopic
```

## flume配置

#### flume-topic.conf 配置文件内容

```
a1.sources = s1
a1.channels = c1
a1.sinks = k1                                                                                         

a1.sources.s1.type=exec
a1.sources.s1.command=tail -F /usr/app/logs/log
a1.sources.s1.channels=c1 

#设置Kafka接收器
a1.sinks.k1.type= org.apache.flume.sink.kafka.KafkaSink
#设置Kafka地址
a1.sinks.k1.brokerList=localhost:9092,localhost:9093,localhost:9094
#设置发送到Kafka上的主题
a1.sinks.k1.topic=flumeTopic
a1.sinks.k1.batchSize=20
a1.sinks.k1.requiredAcks=1
#设置序列化方式
a1.sinks.k1.serializer.class=kafka.serializer.StringEncoder
a1.sinks.k1.channel=c1     

a1.channels.c1.type=memory
a1.channels.c1.capacity=10000
a1.channels.c1.transactionCapacity=100   
```

#### 启动flume

```
flume-ng agent \
--conf conf \
--conf-file /usr/app/flume/conf/flume-topic.conf \
--name a1 -Dflume.root.logger=INFO,console
```

## 启动redis

```
redis-server
```



