package com.second.kill.web.rocketmq.listener;


import com.alibaba.fastjson.JSONObject;
import com.second.kill.common.rocketmq.message.order.CreateOrderMessage;
import com.second.kill.common.rocketmq.message.product.InventoryReductionMessage;
import com.second.kill.common.feign.service.order.FeignOrderMessageService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 收到减少库存完成的消息 在这里打印日志
 */
@Component
@RocketMQMessageListener(consumerGroup = "sk_mq_product_group_inventory_reduction",topic = "topic_inventory_reduction")
public class InventoryReductionFinishListener implements RocketMQListener<String> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private FeignOrderMessageService feignOrderService;

    @Override
    public void onMessage(String message) {
        logger.info("减少库存完成:{}",message);
        JSONObject jsonObject = JSONObject.parseObject(message);
        InventoryReductionMessage inventoryReductionMessage = JSONObject.parseObject(jsonObject.getString("inventoryReductionMessage"), InventoryReductionMessage.class);

        CreateOrderMessage createOrderMessage = new CreateOrderMessage();
        BeanUtils.copyProperties(inventoryReductionMessage,createOrderMessage);
        feignOrderService.postCreateOrderMessage(createOrderMessage);

    }
}
