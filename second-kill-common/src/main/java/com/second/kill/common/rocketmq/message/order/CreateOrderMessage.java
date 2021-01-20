package com.second.kill.common.rocketmq.message.order;

import lombok.Data;

import java.util.Map;

/**
 * 创建订单消息
 */
@Data
public class CreateOrderMessage {

    Map<String,Object> paramMap;

}
