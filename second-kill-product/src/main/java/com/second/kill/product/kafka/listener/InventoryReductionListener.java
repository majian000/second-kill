package com.second.kill.product.kafka.listener;

import com.alibaba.fastjson.JSONObject;
import com.second.kill.common.lock.RedisLock;
import com.second.kill.common.message.MessageTopicConstant;
import com.second.kill.common.message.product.InventoryReductionMessage;
import com.second.kill.common.persistence.entity.EventProcess;
import com.second.kill.common.persistence.service.EventProcessService;
import com.second.kill.common.util.RedisStock;
import com.second.kill.product.service.ProductSkuService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 秒杀模块 扣库存处理
 */
@Component
public class InventoryReductionListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RedisLock redisLock;

    @Autowired
    private ProductSkuService productSkuService;

    @Autowired
    private EventProcessService eventProcessService;


    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private KafkaTemplate kafkaTemplate;




    @KafkaListener(topics = {"sk_inventory_reduction"}, groupId = "sk_message_group")
    public void reciveMessage(ConsumerRecord<String, String> record) {
        System.out.println("收到扣库存消息:" + record);
        String messageJsonString = record.value();
        if (StringUtils.isEmpty(messageJsonString)) {
            throw new IllegalArgumentException("消息无效");
        }
        InventoryReductionMessage inventoryReductionMessage = JSONObject.parseObject(messageJsonString,InventoryReductionMessage.class);
        String skuId =inventoryReductionMessage.getSkuId();
        String appId = inventoryReductionMessage.getAppId();
        String userId =inventoryReductionMessage.getUserId();
        try{
            //保存待处理消息
            EventProcess eventProcess = new EventProcess();
            eventProcess.setCreateDate(new Date());
            eventProcess.setBusinessId(inventoryReductionMessage.getOrderNo());
            eventProcess.setRemark("秒杀模块扣库存");
            eventProcess.setTableName("sk_product_sku");
            eventProcess.setTransactionId(inventoryReductionMessage.getGlobalTransactionId());
            eventProcess.setPayload(messageJsonString);
            eventProcess.setStatus((short)0); //待处理
            eventProcess.setType(MessageTopicConstant.sk_inventory_reduction.name());
            eventProcessService.insert(eventProcess);

            //扣库存
            productSkuService.inventoryReduction(Long.parseLong(skuId));

            //修改为已处理
            eventProcess.setStatus((short)1); //已处理
            eventProcessService.updateStatus(eventProcess);

        }catch(Exception e)
        {
            //释放全局锁
            redisLock.unLock(RedisStock.getGlobalSecondKillKey(appId,skuId), userId);
            logger.warn(e.getMessage(),e);
        }



    }


}
