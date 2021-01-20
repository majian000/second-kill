package com.second.kill.web.rocketmq.listener;


import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


/**
 * 收到创建订单完成的消息 在这里打印日志
 */
@Component
@RocketMQMessageListener(consumerGroup = "sk_mq_order_group_create_order",topic = "topic_create_order")
public class CreateOrderFinishListener  implements RocketMQListener<String> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onMessage(String message) {
        logger.info("创建订单完成:{}",message);

    }
}
