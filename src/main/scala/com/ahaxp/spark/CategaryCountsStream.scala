package com.ahaxp.spark

import com.ahaxp.spark.domain.ClickLog
import com.ahaxp.spark.util.{DateUtil, JedisPoolUtil}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import redis.clients.jedis.Jedis
/**
 * @author Haxp
 * @email huangsiyuan924@gmail.com
 * @date 2020/05/11 21:35
 */
object CategaryCountsStream {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("KafkaDirectStream").setMaster("local[*]")
    val ssc = new StreamingContext(sparkConf, Seconds(5))

    val kafkaParams = Map[String, Object](
      /*
       * 指定 broker 的地址清单，清单里不需要包含所有的 broker 地址，生产者会从给定的 broker 里查找其他 broker 的信息。
       * 不过建议至少提供两个 broker 的信息作为容错。
       */
      "bootstrap.servers" -> "localhost:9092,localhost:9093,localhost:9094",
      /*键的序列化器*/
      "key.deserializer" -> classOf[StringDeserializer],
      /*值的序列化器*/
      "value.deserializer" -> classOf[StringDeserializer],
      /*消费者所在分组的 ID*/
      "group.id" -> "spark-streaming-group",
      /*
       * 该属性指定了消费者在读取一个没有偏移量的分区或者偏移量无效的情况下该作何处理:
       * latest: 在偏移量无效的情况下，消费者将从最新的记录开始读取数据（在消费者启动之后生成的记录）
       * earliest: 在偏移量无效的情况下，消费者将从起始位置读取分区的记录
       */
      "auto.offset.reset" -> "latest",
      /*是否自动提交*/
      "enable.auto.commit" -> (true: java.lang.Boolean)
    )

    /*可以同时订阅多个主题*/
    val topics = Array("flumeTopic")
    val logs: DStream[String] = KafkaUtils.createDirectStream[String, String](
      ssc,
      /*位置策略*/
      PreferConsistent,
      /*订阅主题*/
      Subscribe[String, String](topics, kafkaParams)
    ).map(_.value())

    // 201.125.123.202 2020-05-11 20:18:57     "GET /www/7 HTTP/1.0"    -       200   河南
    // 去除掉了脏数据, 如非/www/开头的栏目
    val cleanLog: DStream[ClickLog] = logs.map(lines => {
      val infos: Array[String] = lines.split("\t")
      val url: String = infos(2).split(" ")(1) // /www/7
      var categaryId = 0
      // 过滤掉脏数据
      if (url.startsWith("/www/")) {
        // 栏目id
        categaryId = url.split("/")(2).toInt
      }
      // 转为case类
      ClickLog(infos(0), DateUtil.parseToMin(infos(1)), categaryId, infos(3), infos(4).toInt, infos(5))
    }).filter(log => log.categaryId != 0)

    // 格式为"(20200512_x, yy)", 即每日各个栏目点击量
    val categaryCounts: DStream[(String, Int)] = cleanLog.map(log => {
      (log.time.substring(0, 8) + "_" + log.categaryId, 1)
    }).reduceByKey(_ + _)

    // 保存每日栏目点击数到redis
    categaryCounts.foreachRDD { rdd =>
      rdd.foreachPartition { partitionOfRecords =>
        var jedis: Jedis = null
        try {
          // 获取redis连接
          jedis = JedisPoolUtil.getConnection
          // 写入到redis
          partitionOfRecords.foreach( record => jedis.hincrBy("categaryCounts", record._1, record._2))
        } catch {
          case ex: Exception =>
            ex.printStackTrace()
        } finally {
          if (jedis != null) {
            jedis.close()
          }
        }
      }
    }


    ssc.start()
    ssc.awaitTermination()
  }
}
