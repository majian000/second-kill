package com.second.kill.common.util;


/**
 * 单例对象
 * @author majian
 */

public class Singleton {

    private static volatile Singleton redisLock = null;


    public static Singleton instance()
    {
        if(redisLock==null) {
            synchronized (Singleton.class) {
                if (redisLock == null) {
                    redisLock = new Singleton();
                }
            }
        }
        return redisLock;
    }



//    public static void main(String[] args) throws InterruptedException {
//        for (int k = 0; k < 100; k++) {
//            System.out.println("第" + (k + 1) + "次");
//            Thread[] threads = new Thread[10];
//            for (int i = 0; i < 10; i++) {
//                threads[i] = new Thread(() -> {
//                    Singleton singletonThread = Singleton.instance();
//                    //打印线程获取的hash值，用来判断返回的是否是同一单例
//                    System.out.println(Thread.currentThread().getName() + ":" + singletonThread.hashCode());
//
//                });
//
//            }
//
//            for (int i = 0; i < 10; i++) {
//                threads[i].start();
//            }
//            for (int i = 0; i < 10; i++) {
//                threads[i].join();
//            }
//        }
//    }
}
