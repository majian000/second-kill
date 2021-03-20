package com.second.kill.product.kafka.scheduler;

import com.alibaba.fastjson.JSONObject;
import com.second.kill.common.message.MessageTopicConstant;
import com.second.kill.common.message.product.InventoryReductionMessage;
import com.second.kill.common.persistence.entity.EventProcess;
import com.second.kill.common.persistence.service.EventProcessService;
import com.second.kill.product.service.ProductSkuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;


/**
 * 处理所有执行本地事务失败的消息
 */
@Component
@EnableScheduling
public class MessageProcessScheduler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private EventProcessService eventProcessService;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private ProductSkuService productSkuService;




    /**
     * 每1分钟重新扫描一次本地看有没有执行失败的事务
     */
    @Scheduled(cron = "0 0/1 * * * ? ")
    public void resend()
    {
        logger.info("处理执行本地事务失败的消息 开始=====================");
        EventProcess query = new EventProcess();
        query.setStatus((short)0);
        List<EventProcess> eventProcesses =  eventProcessService.queryList(query);
        if(!CollectionUtils.isEmpty(eventProcesses))
        {
            for(EventProcess eventProcess : eventProcesses)
            {
                logger.info("本地事务重新执行 "+ eventProcess.getType()+" 内容:"+ eventProcess.getPayload());
                //扣库存
                if(eventProcess.getType().equals(MessageTopicConstant.sk_inventory_reduction.name())) {
                    try {
                        InventoryReductionMessage inventoryReductionMessage = JSONObject.parseObject(eventProcess.getPayload(), InventoryReductionMessage.class);
                        productSkuService.inventoryReduction(Long.parseLong(inventoryReductionMessage.getSkuId()));
                        eventProcess.setStatus((short)1); //已处理
                        eventProcessService.updateStatus(eventProcess);
                    }catch(Exception e)
                    {
                        logger.warn(e.getMessage(),e);
                    }
                }
            }

        }
        logger.info("处理执行本地事务失败的消息 结束=====================");
    }

}
