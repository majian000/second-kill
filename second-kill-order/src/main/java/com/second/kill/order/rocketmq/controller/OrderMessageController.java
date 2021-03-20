package com.second.kill.order.rocketmq.controller;

import com.alibaba.fastjson.JSONObject;
import com.second.kill.common.lock.RedisLock;
import com.second.kill.common.rocketmq.message.order.CreateOrderMessage;
import com.second.kill.common.util.RedisStock;
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
     * @param createOrderMessage
     * @return
     */
    @RequestMapping(method= RequestMethod.POST,value="/postCreateOrderMessage",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResultObjectVO postCreateOrderMessage(@RequestBody CreateOrderMessage createOrderMessage)
    {

        ResultObjectVO resultObjectVO = new ResultObjectVO<>();

        if(createOrderMessage==null||createOrderMessage.getGlobalTransactionId()==null)
        {
            logger.info("没有开启全局事务: param:"+ JSONObject.toJSON(createOrderMessage));
            resultObjectVO.setCode(ResultVO.FAILD);
            resultObjectVO.setMsg("没有找到商品!");
            return resultObjectVO;
        }
        if(createOrderMessage==null||createOrderMessage.getSkuId()==null)
        {
            logger.info("没有找到商品: param:"+ JSONObject.toJSON(createOrderMessage));
            resultObjectVO.setCode(ResultVO.FAILD);
            resultObjectVO.setMsg("没有找到商品!");
            return resultObjectVO;
        }
        if(createOrderMessage==null||createOrderMessage.getAppId()==null)
        {
            logger.info("没有找到应用: param:"+ JSONObject.toJSON(createOrderMessage));
            resultObjectVO.setCode(ResultVO.FAILD);
            resultObjectVO.setMsg("没有找到应用!");
            return resultObjectVO;
        }
        if(createOrderMessage==null||createOrderMessage.getUserId()==null)
        {
            logger.info("没有找到用户: param:"+ JSONObject.toJSON(createOrderMessage));
            resultObjectVO.setCode(ResultVO.FAILD);
            resultObjectVO.setMsg("没有找到用户!");
            return resultObjectVO;
        }
        String skuId = createOrderMessage.getSkuId();
        String userId =createOrderMessage.getUserId();
        String appId= createOrderMessage.getAppId();


        try {

            JSONObject jsonObject =new JSONObject();
            jsonObject.put("createOrderMessage",createOrderMessage);
            String jsonString = jsonObject.toJSONString();
            Message<String> message = MessageBuilder.withPayload(jsonString).build();
            //发送创建订单消息,通知各个服务
            rocketMQTemplate.sendMessageInTransaction("sk_mq_order_group_create_order",
                    "topic_create_order",message,null);


            resultObjectVO.setCode(ResultVO.SUCCESS);
            resultObjectVO.setMsg("下单成功!");


            logger.info("创建订单: param:" + JSONObject.toJSON(createOrderMessage));

        }catch(Exception e)
        {
            redisLock.unLock(RedisStock.getGlobalSecondKillKey(appId,skuId), userId);
            logger.warn(e.getMessage(),e);

            resultObjectVO.setCode(ResultVO.FAILD);
            resultObjectVO.setMsg("下单失败!");
        }
        return resultObjectVO;
    }

}
