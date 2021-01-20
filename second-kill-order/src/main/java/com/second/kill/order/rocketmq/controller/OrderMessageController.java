package com.second.kill.order.rocketmq.controller;

import com.alibaba.fastjson.JSONObject;
import com.second.kill.common.lock.RedisLock;
import com.second.kill.common.rocketmq.message.order.CreateOrderMessage;
import com.second.kill.common.vo.ResultListVO;
import com.second.kill.common.vo.ResultObjectVO;
import com.second.kill.common.vo.ResultVO;
import com.second.kill.order.entity.Order;
import com.second.kill.order.entity.OrderItem;
import com.second.kill.order.service.OrderItemService;
import com.second.kill.order.service.OrderService;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/order/message")
public class OrderMessageController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;


    @Autowired
    private RedisLock redisLock;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;



    /**
     * 创建订单
     * @param paramMap
     * @return
     */
    @RequestMapping(method= RequestMethod.POST,value="/postCreateOrderMessage",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResultObjectVO postCreateOrderMessage(@RequestParam Map<String,Object> paramMap)
    {

        ResultObjectVO resultObjectVO = new ResultObjectVO<>();
        if(paramMap==null||paramMap.get("skuId")==null)
        {
            logger.info("没有找到商品: param:"+ JSONObject.toJSON(paramMap));
            resultObjectVO.setCode(ResultVO.FAILD);
            resultObjectVO.setMsg("没有找到商品!");
            return resultObjectVO;
        }
        if(paramMap==null||paramMap.get("appId")==null)
        {
            logger.info("没有找到应用: param:"+ JSONObject.toJSON(paramMap));
            resultObjectVO.setCode(ResultVO.FAILD);
            resultObjectVO.setMsg("没有找到应用!");
            return resultObjectVO;
        }
        if(paramMap==null||paramMap.get("userId")==null)
        {
            logger.info("没有找到用户: param:"+ JSONObject.toJSON(paramMap));
            resultObjectVO.setCode(ResultVO.FAILD);
            resultObjectVO.setMsg("没有找到用户!");
            return resultObjectVO;
        }
        String skuId = String.valueOf(paramMap.get("skuId"));
        String userId = String.valueOf(paramMap.get("userId"));
        String appId= String.valueOf(paramMap.get("appId"));


        String lockKey = appId+"_order_service_"+skuId;
        boolean lockStatus = redisLock.lock(lockKey,userId);
        if(!lockStatus)
        {
            resultObjectVO.setCode(ResultObjectVO.SUCCESS);
            resultObjectVO.setMsg("超时重试");
            return resultObjectVO;
        }
        try {

            CreateOrderMessage createOrderMessage = new CreateOrderMessage();
            createOrderMessage.setParamMap(paramMap);
            JSONObject jsonObject =new JSONObject();
            jsonObject.put("createOrderMessage",createOrderMessage);
            String jsonString = jsonObject.toJSONString();
            Message<String> message = MessageBuilder.withPayload(jsonString).build();
            //发送创建订单消息,通知各个服务
            rocketMQTemplate.sendMessageInTransaction("sk_mq_order_group_create_order",
                    "topic_create_order",message,null);


            resultObjectVO.setCode(ResultVO.SUCCESS);
            resultObjectVO.setMsg("下单成功!");

            redisLock.unLock(lockKey, userId);

            logger.info("创建订单: param:" + JSONObject.toJSON(paramMap));

        }catch(Exception e)
        {
            redisLock.unLock(lockKey, userId);
            logger.warn(e.getMessage(),e);

            resultObjectVO.setCode(ResultVO.FAILD);
            resultObjectVO.setMsg("下单失败!");
        }
        return resultObjectVO;
    }

}
