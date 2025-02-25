package com.gxz;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

public class JedisTest {
    private Jedis jedis;

    @BeforeEach
    void setJedis(){
        new Jedis("218.194.61.139",9872);
        jedis.auth("password");
        jedis.select(0);//选择库0
    }

    @Test
    void testJedis(){
        String result = jedis.set("name","Tom");
        System.out.println("result = " + result);
        String name = jedis.get("name");
        System.out.println("name = " + name);
    }

    @AfterEach
    void closeJedis(){
        if(jedis!=null){
            jedis.close();
        }
    }
}
