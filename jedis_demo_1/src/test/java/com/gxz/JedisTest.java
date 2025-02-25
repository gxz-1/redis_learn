package com.gxz;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

import java.util.*;

public class JedisTest {
    private Jedis jedis;

    @BeforeEach
    void setJedis(){
        //1.使用Jedis直连的方式
//        jedis = new Jedis("218.194.61.139",9872);
//        jedis = new Jedis("192.168.1.106",6379);
        //2.Jedis本身是线程不安全的，并且频繁的创建和销毁连接会有性能损耗，因此使用Jedis连接池代替Jedis直连
        jedis = JedisConnectionFacotry.getJedis();
        jedis.auth("password");
        jedis.select(0);//选择库0
    }

    @Test
    void testJedisString(){
        String result = jedis.set("name","Tom");
        System.out.println("result = " + result);
        String name = jedis.get("name");
        System.out.println("name = " + name);
    }

    @Test
    void testJedisList(){
        jedis.lpush("mylist", "apple", "banana", "cherry");
        // 获取列表中的所有元素
        List<String> myList = jedis.lrange("mylist", 0, -1); // 获取所有元素
        System.out.println("myList = " + myList);
        // 从列表中弹出一个元素
        String poppedElement = jedis.lpop("mylist");
        // 获取更新后的列表
        myList = jedis.lrange("mylist", 0, -1);
        System.out.println("Updated myList = " + myList);
    }


    @Test
    void testJedisHash(){
        jedis.hset("user:1","name","Jack");
        jedis.hset("user:1","age","12");
        Map<String,String> map = jedis.hgetAll("user:1");
        System.out.println("map = " + map);
    }

    @Test
    void testJedisSet(){
        jedis.sadd("set", "apple", "banana", "cherry");
        // 获取集合中的所有元素
        Set<String> mySet = jedis.smembers("set");
        System.out.println("Set = " + mySet);
        // 测试是否存在某个元素
        boolean isMember = jedis.sismember("set", "banana");
    }


    @AfterEach
    void closeJedis(){
        if(jedis!=null){
            jedis.close();
        }
    }
}
