package com.gxz;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

//Jedis是redis的一种java客户端，可以通过java代码连接并操作redis
//Jedis本身是线程不安全的，并且频繁的创建和销毁连接会有性能损耗，因此使用Jedis连接池
public class JedisConnectionFacotry {

    private static final JedisPool jedisPool;
    static {
        //配置连接池
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(8);//最大连接数
        poolConfig.setMaxIdle(4);//最大空闲连接
        poolConfig.setMinIdle(0);//最小空闲连接
        poolConfig.setMaxWaitMillis(1000);//池满时的等待时长
        //创建连接池对象
        jedisPool = new JedisPool(poolConfig,
                "218.194.61.139",9872,1000,"password");
    }

    public static Jedis getJedis(){
        return jedisPool.getResource();
    }

}