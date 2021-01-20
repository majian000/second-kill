package com.second.kill.common.rocketmq.message.product;

import lombok.Data;

import java.util.Map;

/**
 * 减少库存消息
 */
@Data
public class InventoryReductionMessage {

    Map<String,Object> paramMap;

}
