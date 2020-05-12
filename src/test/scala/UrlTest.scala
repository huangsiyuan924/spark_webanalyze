/**
 * @author Haxp
 * @email huangsiyuan924@gmail.com
 * @date 2020/05/12 11:41
 */
object UrlTest {
  def main(args: Array[String]): Unit = {
    //  https://www.google.com/search?q=蔡徐坤
    val url = "https://www.google.com/search?q=蔡徐坤"
    val refer: String = url.split("/")(2)
    println(refer)
  }

}
