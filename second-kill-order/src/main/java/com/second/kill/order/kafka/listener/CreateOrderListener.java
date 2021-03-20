package com.second.kill.order.kafka.listener;

import com.alibaba.fastjson.JSONObject;
import com.second.kill.common.message.MessageTopicConstant;
import com.second.kill.common.message.order.CreateOrderMessage;
import com.second.kill.common.persistence.entity.EventProcess;
import com.second.kill.common.persistence.service.EventProcessService;
import com.second.kill.order.entity.Order;
import com.second.kill.order.entity.OrderItem;
import com.second.kill.order.service.OrderItemService;
import com.second.kill.order.service.OrderService;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 扣库存处理
 */
@Component
public class CreateOrderListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;


    @Autowired
    private EventProcessService eventProcessService;





    @KafkaListener(topics = {"sk_create_order"}, groupId = "sk_message_group")
    public void reciveMessage(ConsumerRecord<String, String> record) {
        System.out.println("收到创建订单消息:" + record);
        String messageJsonString = record.value();
        if (StringUtils.isEmpty(messageJsonString)) {
            throw new IllegalArgumentException("消息无效");
        }
        CreateOrderMessage kafkaMessage = JSONObject.parseObject(messageJsonString,CreateOrderMessage.class);
        String skuId =kafkaMessage.getSkuId();
        String appId = kafkaMessage.getAppId();
        String userId =kafkaMessage.getUserId();
        try{
            //保存待处理消息
            EventProcess eventProcess = new EventProcess();
            eventProcess.setCreateDate(new Date());
            eventProcess.setBusinessId(kafkaMessage.getOrderNo());
            eventProcess.setRemark("创建订单");
            eventProcess.setTableName("sk_order");
            eventProcess.setTransactionId(kafkaMessage.getGlobalTransactionId());
            eventProcess.setPayload(messageJsonString);
            eventProcess.setStatus((short)0); //待处理
            eventProcess.setType(MessageTopicConstant.sk_create_order.name());
            eventProcessService.insert(eventProcess);

            //创建订单

            Map<String,Object> paramMap=new HashMap<String,Object>();
            paramMap.put("skuId",kafkaMessage.getSkuId());
            paramMap.put("orderNo",kafkaMessage.getOrderNo());
            paramMap.put("userId",kafkaMessage.getUserId());

            Order order = orderService.createOrder(paramMap);
            if(order.getId()==null)
            {
                throw new IllegalArgumentException("订单创建失败");
            }
            logger.info("保存订单 提交本地事务{}",JSONObject.toJSON(order));
            OrderItem orderItem = orderItemService.createOrderItem(skuId,order);
            if(orderItem.getId()==null)
            {
                throw new IllegalArgumentException("子订单创建失败");
            }
            logger.info("保存子订单 提交本地事务{}",JSONObject.toJSON(orderItem));

            //修改为已处理
            eventProcess.setStatus((short)1); //已处理
            eventProcessService.updateStatus(eventProcess);


        }catch(Exception e)
        {
            //发送恢复库存消息

            logger.warn(e.getMessage(),e);
        }



    }


}
