import random
import time

ip_slice = [123, 132, 101, 125, 27, 46, 84, 201, 202, 203, 119]
status_code = [200, 302, 404]
areas = ["河南", "深圳", "广州", "上海", "北京", "unknown"]
urls = [
    "/www/6",  # 综艺
    "/www/4",  # 动漫
    "/www/3",  # 纪录片
    "/www/8",  # 游戏
    "/www/25",  # 资讯
    "/www/7",  # 娱乐
    "/other/123",  # 需过滤的数据
    "/other/312",  # 需过滤的数据
]

references = [
    "https://www.google.com/search?q={query}",
    "https://www.baidu.com/search?q={query}",
    "https://search.yahoo.com/search?q={query}",
    "https://www.sogou.com/search?q={query}"
]

keywords = [
    "战狼",
    "蔡徐坤",
    "跑男",
    "青春有你",
    "快乐大本营",
    "余欢水"
]

#  random.sample(list, num) --> list
#  在list中随机取num条记录, 返回一个list


def random_ip():
    slice = random.sample(ip_slice, 4)
    #  xx.xx.xx.xx格式的ip地址
    return ".".join([str(item) for item in slice])

def random_status():
    return random.sample(status_code, 1)[0]

def random_area():
    return random.sample(areas, 1)[0]

def random_url():
    return random.sample(urls, 1)[0]


def random_reference():
    if random.uniform(0, 1) > 0.2:
        return "-"
    reference = random.sample(references, 1)[0]
    keyword = random.sample(keywords, 1)[0]
    return reference.format(query=keyword)

def generate_log(count=10):
    local_time = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
    f = open("/usr/app/logs/log", "a+")

    while count > 0:
        log = "{ip}\t{localtime}\t\"GET {url} HTTP/1.0\"\t{reference}\t{status}\t{area}".format(ip=random_ip(), localtime=local_time, url=random_url(), reference=random_reference(), status=random_status(), area=random_area())
        f.write(log + "\n")
        count -= 1

if __name__ == '__main__':
    generate_log(20)
