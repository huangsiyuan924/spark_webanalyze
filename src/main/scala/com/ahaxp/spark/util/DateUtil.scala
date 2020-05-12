package com.ahaxp.spark.util



import java.util.Date

import org.apache.commons.lang3.time.FastDateFormat

/**
 * @author Haxp
 * @email huangsiyuan924@gmail.com
 * @date 2020/05/11 21:52
 */
object DateUtil {
  val DATE = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss")
  val TAG_FORMAT = FastDateFormat.getInstance("YYYYMMdd")

  /**
   * 将time转为timestamp
   * @param time
   */
  def getTime(time: String): Long = {
    DATE.parse(time).getTime
  }

  def parseToMin(time: String): String ={
    TAG_FORMAT.format(new Date(getTime(time)))
  }


  /**
   * 2020-05-11 21:48:01
   * 转为
   * 20200511
   * @param args
   */
  def main(args: Array[String]): Unit = {
    println(parseToMin("2020-05-11 21:48:01"))
  }

}
