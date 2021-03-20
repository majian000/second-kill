package com.second.kill.common.rocketmq.message.product;

import lombok.Data;

import java.util.Map;

/**
 * 重置库存消息
 */
@Data
public class RestoreStockMessage {


    /**
     * 全局事务ID
     */
    private String globalTransactionId;

    private String orderNo;

    private String skuId;

    private String appId;

    private String userId;

}
