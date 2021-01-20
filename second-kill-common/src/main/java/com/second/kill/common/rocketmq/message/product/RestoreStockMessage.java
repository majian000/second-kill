package com.second.kill.common.rocketmq.message.product;

import lombok.Data;

import java.util.Map;

/**
 * 重置库存消息
 */
@Data
public class RestoreStockMessage {

    Map<String,Object> paramMap;

}
