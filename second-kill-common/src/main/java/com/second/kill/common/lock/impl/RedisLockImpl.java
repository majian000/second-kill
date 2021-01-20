package com.second.kill.common.lock.impl;

import com.second.kill.common.lock.RedisLock;
import com.second.kill.common.lock.thread.RedisLockThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class RedisLockImpl implements RedisLock {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, RedisLockThread> threadHashMap=new ConcurrentHashMap<String,RedisLockThread>();

    @Autowired
    private StringRedisTemplate redisTemplate;



    public boolean lock(String lockKey,String lockValue)
    {
        return lock(lockKey,lockValue,RedisLock.DEFAULT_MILLISECOND);
    }


    public boolean lock(String lockKey,String lockValue,long millisecond)
    {

        int tryCount=1;
        while(true) {
            if(tryCount>=DEFAULT_TRY_COUNT)
            {
                logger.info("redis key "+lockKey+" 已存在 重试次数已到"+DEFAULT_TRY_COUNT);
                break;
            }
            tryCount++;
            //利用setnx 设置一个key
            Boolean result= redisTemplate.opsForValue().setIfAbsent(lockKey,lockValue);
            if (result!=null&&result.booleanValue()) {
                //维持key有效期
                redisTemplate.expire(lockKey,millisecond,TimeUnit.SECONDS);
                if(threadHashMap.get(lockKey+"_thread")!=null)
                {
                    threadHashMap.get(lockKey+"_thread").setLoop(false);
                }
                //维持心跳
                RedisLockThread expireThread = new RedisLockThread();
                expireThread.setLockKey(lockKey);
                threadHashMap.put(lockKey+"_thread",expireThread);
                return true;
            }
        }
        return false;
    }



    public void unLock(String lockKey,String lockValue)
    {
        if(threadHashMap.get(lockKey+"_thread")!=null)
        {
            threadHashMap.get(lockKey+"_thread").setLoop(false);
            threadHashMap.remove(lockKey+"_thread");
        }

        if(lockValue.equals(redisTemplate.opsForValue().get(lockKey)))
        {
            redisTemplate.opsForValue().getOperations().delete(lockKey);
        }
    }



}
