package com.second.kill.common.lock.thread;

import com.second.kill.common.lock.RedisLock;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

@Data
public class RedisLockThread extends Thread {

    private String lockKey;

    private boolean loop=true;

    private RedisTemplate redisTemplate;


    @Override
    public void run() {
        try {
            if (redisTemplate != null) {
                while(loop) {
                    redisTemplate.expire(lockKey, RedisLock.DEFAULT_MILLISECOND, TimeUnit.SECONDS);
                    sleep(2000);
                }
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
