package com.second.kill.web.kafka.callback;

import com.alibaba.fastjson.JSONObject;
import com.second.kill.common.message.MessageTopicConstant;
import com.second.kill.common.message.order.CreateOrderMessage;
import com.second.kill.common.message.product.InventoryReductionMessage;
import com.second.kill.common.persistence.entity.EventPublish;
import com.second.kill.common.persistence.service.EventPublishService;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.stereotype.Component;

@Component
public class SendCallback implements ProducerListener<String, Object> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private EventPublishService eventPublishService;

    @Override
    public void onSuccess(String topic, Integer partition, String key, Object value, RecordMetadata recordMetadata) {
        logger.info(" send kafka message success topic:"+topic+" msgContent:"+String.valueOf(value));
        String messageJson= String.valueOf(value);
        //扣库存成功,修改本地消息状态为已发送
        if(topic.equals(MessageTopicConstant.sk_inventory_reduction.name()))
        {
            InventoryReductionMessage inventoryReductionMessage = JSONObject.parseObject(messageJson,InventoryReductionMessage.class);
            EventPublish eventPublish = eventPublishService.findById(Long.parseLong(inventoryReductionMessage.getLocalTransactionMessageId()));
            eventPublish.setStatus((short)1); //已发送
            eventPublishService.updateStatus(eventPublish);
        }else if(topic.equals(MessageTopicConstant.sk_create_order.name()))
        {
            CreateOrderMessage createOrderMessage = JSONObject.parseObject(messageJson,CreateOrderMessage.class);
            EventPublish eventPublish = eventPublishService.findById(Long.parseLong(createOrderMessage.getLocalTransactionMessageId()));
            eventPublish.setStatus((short)1); //已发送
            eventPublishService.updateStatus(eventPublish);
        }
    }

    @Override
    public void onError(String topic, Integer partition, String key, Object value, Exception exception) {
        logger.warn(" send kafka message error topic:"+topic+" msgContent:"+String.valueOf(value));

        //扣库存失败,记录失败消息
        if(topic.equals(MessageTopicConstant.sk_inventory_reduction.name()))
        {
            logger.warn("resend kafka message  topic:"+topic+" msgContent:"+String.valueOf(value));

        }
    }

    @Override
    public boolean isInterestedInSuccess() {
        return true;
    }
}
