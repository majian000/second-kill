package com.second.kill.common.util;

public class RedisStock {

    public static String getStockKey(String appId,String skuId)
    {
        return appId+"_product_"+skuId+"_stock";
    }


    /**
     * 商品秒杀活动
     * @param appId
     * @param skuId
     * @return
     */
    public static String getProductActivityKey(String appId,String skuId)
    {
        return appId+"_product_"+skuId+"_activity";
    }

    /**
     * 秒杀活动全局锁
     * @param appId
     * @param skuId
     * @return
     */
    public static String getGlobalSecondKillKey(String appId,String skuId)
    {
        return  appId+"_sk_service_"+skuId;
    }



    /**
     * 商品购买锁
     * @param appId
     * @param skuId
     * @return
     */
    public static String getProductBuyKey(String appId,String skuId)
    {
        return  appId+"_product_buy_service_"+skuId;
    }


}
