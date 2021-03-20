package com.second.kill.web.scheduler;

import com.alibaba.fastjson.JSONObject;
import com.second.kill.common.feign.service.product.FeignProductSkuService;
import com.second.kill.common.message.MessageTopicConstant;
import com.second.kill.common.persistence.entity.EventProcess;
import com.second.kill.common.persistence.service.EventProcessService;
import com.second.kill.common.vo.ResultObjectVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;


/**
 * 处理所有feign调用失败的消息
 */
@Component
@EnableScheduling
public class FeignMessageProcessScheduler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private EventProcessService eventProcessService;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private FeignProductSkuService feignProductSkuService;




    /**
     * 每1分钟重新扫描一次本地看有没有执行失败的事务
     */
    @Scheduled(cron = "0 0/1 * * * ? ")
    public void resend()
    {
        logger.info("处理远程调用失败的消息 开始=====================");
        EventProcess query = new EventProcess();
        query.setStatus((short)0);
        List<EventProcess> eventProcesses =  eventProcessService.queryList(query);
        if(!CollectionUtils.isEmpty(eventProcesses))
        {
            for(EventProcess eventProcess : eventProcesses)
            {
                //扣库存
                if(eventProcess.getType().equals(MessageTopicConstant.inventory_reduction.name())) {
                    logger.info("远程服务重新调用 "+ eventProcess.getType()+" 内容:"+ eventProcess.getPayload());
                    ResultObjectVO resultObjectVO = feignProductSkuService.refershStock(JSONObject.parseObject(eventProcess.getPayload(), HashMap.class));
                    if(resultObjectVO.getCode().intValue()==ResultObjectVO.SUCCESS.intValue())
                    {
                        eventProcess.setStatus((short)1);
                        eventProcessService.updateStatus(eventProcess);
                    }
                }
            }

        }
        logger.info("处理远程失败的消息 结束=====================");
    }

}
