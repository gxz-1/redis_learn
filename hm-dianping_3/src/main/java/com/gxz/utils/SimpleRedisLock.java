package com.gxz.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Data
@AllArgsConstructor
public class SimpleRedisLock implements ILock{

    private static final String LOCK_PREFIX = "lock:";
    private String lockName;//根据不同的业务定义不同的锁
    private String threadId;//作为value
    private StringRedisTemplate redisTemplate;
    @Override
    public boolean tryLock(long timeoutSec) {
        //value=uuid+当前线程的id, 用于区分不同线程创建的锁，避免误删其他线程的锁
        //其中uuid为了避免不同jvm的线程id可能相同的情况
        String threadId = UUID.randomUUID()+ ":" + Thread.currentThread().getId();
        this.threadId=threadId;
        //用redis设置锁:set lock:user  thread1  EX 30  NX
        Boolean success = redisTemplate.opsForValue().setIfAbsent(LOCK_PREFIX + lockName, threadId,
                timeoutSec, TimeUnit.SECONDS);
//        //由Boolean拆箱返回boolean，有success为空指针的风险
//        return success;
        return Boolean.TRUE.equals(success);
    }

    @Override
    public void unlock() {
        //判断是否为同一把锁
        String s = redisTemplate.opsForValue().get(LOCK_PREFIX + lockName);
        if(s.equals(threadId)){
            //释放锁：del lock:user
            redisTemplate.delete(LOCK_PREFIX + lockName);
        }
        //TODO：存在 判断锁 和 释放锁 的并发问题 -> 判断和释放锁必须原子性
    }
}
