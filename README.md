



开启zookeeper服务集群:

```
./zookeeper01/bin/zkServer.sh start
./zookeeper02/bin/zkServer.sh start
./zookeeper03/bin/zkServer.sh start
```

检查zookeeper服务是否启动成功:

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



开启kafka集群:

进入kafka路径

~~~
./bin/kafka-server-start.sh config/server-1.properties
./bin/kafka-server-start.sh config/server-2.properties
./bin/kafka-server-start.sh config/server-3.properties
~~~



列出主题 

~~~
./bin/kafka-topics.sh --list --zookeeper localhost:2181
~~~

创建主题()

~~~
./bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 3 --partitions 1 --topic flumeTopic
~~~

启动消费者:

```
./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic flumeTopic
./bin/kafka-console-consumer.sh --bootstrap-server localhost:9093 --topic flumeTopic
./bin/kafka-console-consumer.sh --bootstrap-server localhost:9094 --topic flumeTopic
```

flume-topic.conf 配置文件内容:

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





启动flume

```
flume-ng agent \
--conf conf \
--conf-file /usr/app/flume/conf/flume-topic.conf \
--name a1 -Dflume.root.logger=INFO,console
```





