/**
 * @author Haxp
 * @email huangsiyuan924@gmail.com
 * @date 2020/05/12 11:24
 */
object Test {
  def main(args: Array[String]): Unit = {
    val url: String = "GET /www/7 HTTP/1.0"
    val splits: String = url.split(" ")(1)
    if (splits.startsWith("/www/")) {
      // /www/7
      val id: Int = splits.split("/")(2).toInt
      println(id)
    }


  }

}
