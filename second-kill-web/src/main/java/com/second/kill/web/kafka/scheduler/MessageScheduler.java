package com.second.kill.web.kafka.scheduler;

import com.alibaba.fastjson.JSONObject;
import com.second.kill.common.message.MessageTopicConstant;
import com.second.kill.common.message.order.CreateOrderMessage;
import com.second.kill.common.message.product.InventoryReductionMessage;
import com.second.kill.common.persistence.entity.EventPublish;
import com.second.kill.common.persistence.service.EventPublishService;
import com.second.kill.common.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;


/**
 * 重发所有发送失败的消息
 */
@Component
@EnableScheduling
public class MessageScheduler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private EventPublishService eventPublishService;

    @Autowired
    private KafkaTemplate kafkaTemplate;




    /**
     * 每两分钟重发一次消息
     */
    @Scheduled(cron = "0 0/2 * * * ? ")
    public void resend()
    {
        logger.info("消息重发 开始=====================");
        //查询五分钟之前发送失败的所有消息
        List<EventPublish> eventPublishes =  eventPublishService.queryFaildListByBefore(DateUtils.advanceSecond(new Date(),60*5));
        if(!CollectionUtils.isEmpty(eventPublishes))
        {
            for(EventPublish eventPublish : eventPublishes)
            {
                logger.info("消息重发 "+ eventPublish.getType()+" 内容:"+ eventPublish.getPayload());
                if(eventPublish.getType().equals(MessageTopicConstant.sk_inventory_reduction.name())) {
                    InventoryReductionMessage inventoryReductionMessage = JSONObject.parseObject(eventPublish.getPayload(),InventoryReductionMessage.class);
                    inventoryReductionMessage.setLocalTransactionMessageId(String.valueOf(eventPublish.getId()));
                    kafkaTemplate.send(eventPublish.getType(), JSONObject.toJSONString(inventoryReductionMessage));
                }else if(eventPublish.getType().equals(MessageTopicConstant.sk_create_order.name())) {
                    CreateOrderMessage createOrderMessage = JSONObject.parseObject(eventPublish.getPayload(),CreateOrderMessage.class);
                    createOrderMessage.setLocalTransactionMessageId(String.valueOf(eventPublish.getId()));
                    kafkaTemplate.send(eventPublish.getType(), JSONObject.toJSONString(createOrderMessage));
                }
            }

        }
        logger.info("消息重发 结束=====================");
    }

}
