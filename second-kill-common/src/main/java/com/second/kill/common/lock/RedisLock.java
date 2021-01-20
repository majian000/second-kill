package com.second.kill.common.lock;

public interface RedisLock {

    /**
     * 默认key有效期毫秒
     */
    public static long DEFAULT_MILLISECOND=5000;

    /**
     * 拿锁重试次数
     */
    public static long DEFAULT_TRY_COUNT=1000;

    public boolean lock(String lockKey,String lockValue);

    public boolean lock(String lockKey,String lockValue,long millisecond);

    public void unLock(String lockKey,String lockValue);
}
