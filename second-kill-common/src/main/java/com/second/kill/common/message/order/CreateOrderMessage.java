package com.second.kill.common.message.order;

import lombok.Data;

import java.util.Map;

/**
 * 创建订单消息
 */
@Data
public class CreateOrderMessage {

    /**
     * 全局事务ID
     */
    private String globalTransactionId;

    private String orderNo;

    private String skuId;

    private String appId;

    private String userId;


    /**
     * 本地消息ID
     */
    private String localTransactionMessageId;

}
