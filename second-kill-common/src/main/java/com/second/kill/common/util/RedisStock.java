package com.second.kill.common.util;

public class RedisStock {

    public static String getStockKey(String appId,String skuId)
    {
        return appId+"_product_"+skuId+"_stock";
    }


    public static String getProductActivityKey(String appId,String skuId)
    {
        return appId+"_product_"+skuId+"_activity";
    }

    public static String getGlobalSecondKillKey(String appId,String skuId)
    {
        return  appId+"_sk_service_"+skuId;
    }
}
