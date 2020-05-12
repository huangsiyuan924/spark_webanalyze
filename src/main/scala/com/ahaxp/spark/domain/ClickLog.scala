package com.ahaxp.spark.domain

/**
 * @author Haxp
 * @email huangsiyuan924@gmail.com
 * @date 2020/05/11 22:12
 */

// 201.125.123.202 2020-05-11 20:18:57     "GET /www/7 HTTP/1.0"    -       200      河南
case class ClickLog(ip: String,
                    time: String,
                    categaryId:Int,
                    reference: String,
                    status: Int,
                    area: String)