package com.gxz.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;


@Data
@AllArgsConstructor
public class SimpleRedisLock implements ILock{

    private static final String LOCK_PREFIX = "lock:";
    private String lockName;//根据不同的业务定义不同的锁
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean tryLock(long timeoutSec) {
        long threadId = Thread.currentThread().getId();//获取当前线程的id
        //用redis设置锁:set lock:user  thread1  EX 30  NX
        Boolean success = redisTemplate.opsForValue().setIfAbsent(LOCK_PREFIX + lockName, "" + threadId,
                timeoutSec, TimeUnit.SECONDS);
//        //由Boolean拆箱返回boolean，有success为空指针的风险
//        return success;
        return Boolean.TRUE.equals(success);
    }

    @Override
    public void unlock() {
        //释放锁：del lock:user
        redisTemplate.delete(LOCK_PREFIX + lockName);
    }
}
