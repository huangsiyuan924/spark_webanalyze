package com.ahaxp.spark.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author Haxp
 * @email huangsiyuan924@gmail.com
 * @date 2020/05/11 14:54
 */
public class JedisPoolUtil {
    private static volatile JedisPool jedisPool = null;
    private static final String HOST = "localhost";
    private static final int PORT = 6379;


    // 单例懒汉式，　获取连接对象
    public static Jedis getConnection() {
        // 双重检查锁
        if (jedisPool == null) {
            synchronized (JedisPoolUtil.class) {
                if (jedisPool == null) {
                    JedisPoolConfig config = new JedisPoolConfig();
                    config.setMaxTotal(30);
                    config.setMaxIdle(10);
                    jedisPool = new JedisPool(config, HOST, PORT);
                }
            }
        }
        return jedisPool.getResource();
    }
}
