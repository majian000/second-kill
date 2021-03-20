package com.second.kill.web.controller;


import com.alibaba.fastjson.JSONObject;
import com.second.kill.common.lock.RedisLock;
import com.second.kill.common.message.MessageTopicConstant;
import com.second.kill.common.message.order.CreateOrderMessage;
import com.second.kill.common.message.product.InventoryReductionMessage;
import com.second.kill.common.persistence.entity.EventPublish;
import com.second.kill.common.persistence.service.EventPublishService;
import com.second.kill.common.util.RedisStock;
import com.second.kill.common.vo.ResultListVO;
import com.second.kill.common.vo.ResultObjectVO;
import com.second.kill.common.feign.service.product.FeignProductSkuService;
import com.second.kill.common.vo.ResultVO;
import com.second.kill.web.service.SecondKillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * 秒杀服务
 */
@RestController
@RequestMapping("/sk")
public class SecondKillController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private FeignProductSkuService feignProductSkuService;


    @Autowired
    private RedisLock redisLock;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private SecondKillService secondKillService;

    @Autowired
    private KafkaTemplate kafkaTemplate;


    @Autowired
    private EventPublishService eventPublishService;



    /**
     * 查询所有上架商品
     * @param queryMap
     * @return
     */
    @RequestMapping(method = RequestMethod.GET,value="/shelves/list",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResultListVO queryShelvesList(Map<String,Object> queryMap)
    {
        return feignProductSkuService.queryShelvesList(queryMap);
    }


    /**
     * 点击秒杀
     * @param paramMap
     * @return
     */
    @RequestMapping(method = RequestMethod.POST,value="/secondKill",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResultObjectVO secondKill(@RequestBody Map<String,Object> paramMap)
    {
        ResultObjectVO resultObjectVO = new ResultObjectVO(ResultVO.FAILD,"请重试");
        if(paramMap!=null) {
            logger.info("点击秒杀 param : "+ JSONObject.toJSON(paramMap));
            if(paramMap.get("skuId")==null)
            {
                resultObjectVO.setCode(ResultObjectVO.FAILD);
                resultObjectVO.setMsg("没有找到商品");
                return resultObjectVO;
            }
            if(paramMap.get("userId")==null)
            {
                resultObjectVO.setCode(ResultObjectVO.FAILD);
                resultObjectVO.setMsg("没有找到用户");
                return resultObjectVO;
            }
            String skuId = String.valueOf(paramMap.get("skuId"));
            String appId="second_kill";
            String userId=String.valueOf(paramMap.get("userId"));
            String lockKey = RedisStock.getGlobalSecondKillKey(appId,skuId);
            String stockKey = RedisStock.getStockKey(appId,skuId);
            String orderNo = UUID.randomUUID().toString().replace("-","");


            //TODO:控制访问人数,比如只允许1000人访问,其余所有人将不执行后面代码


            //判断全局状态
            String productActivityKey = RedisStock.getProductActivityKey(appId,skuId);

            Object productActivityValueObject = redisTemplate.opsForValue().get(productActivityKey);
            if(productActivityValueObject==null)
            {
                resultObjectVO.setCode(ResultObjectVO.SUCCESS);
                resultObjectVO.setMsg("活动已结束");
                return resultObjectVO;
            }
            String productActivityValue = String.valueOf(productActivityValueObject);
            if("0".equals(productActivityValue))
            {
                resultObjectVO.setCode(ResultObjectVO.SUCCESS);
                resultObjectVO.setMsg("商品已售罄");
                return resultObjectVO;
            }

            boolean lockStatus = redisLock.lock(lockKey,userId);
            if(!lockStatus)
            {
                resultObjectVO.setCode(ResultObjectVO.SUCCESS);
                resultObjectVO.setMsg("超时重试");
                return resultObjectVO;
            }

            try {
                if (redisTemplate.opsForValue().get(stockKey) == null) {
                    resultObjectVO = feignProductSkuService.refershStock(paramMap);
                }

                Integer stock = Integer.parseInt(String.valueOf(redisTemplate.opsForValue().get(stockKey)));
                if (stock <= 0) {
                    redisLock.unLock(lockKey, userId);

                    //设置结束状态
                    redisTemplate.opsForValue().set(productActivityKey,"0");

                    resultObjectVO.setCode(ResultObjectVO.SUCCESS);
                    resultObjectVO.setMsg("商品已售罄");
                    return resultObjectVO;
                }


                //扣redis库存
                redisTemplate.opsForValue().set(stockKey, String.valueOf(stock.longValue() - 1));

                logger.info("get product userId:"+userId+ " skuId:"+skuId);
                String globalTransactionId = UUID.randomUUID().toString().replace("-","");

                //异步发送消息 数据入库

                //发送扣库存消息
                InventoryReductionMessage inventoryReductionMessage = new InventoryReductionMessage();
                inventoryReductionMessage.setAppId(appId);
                inventoryReductionMessage.setUserId(userId);
                inventoryReductionMessage.setOrderNo(orderNo);
                //保存全局事务
                inventoryReductionMessage.setGlobalTransactionId(globalTransactionId);
                inventoryReductionMessage.setSkuId(skuId);

                //保存发消息记录到数据库
                EventPublish inventorReductionMessagePersistence = new EventPublish();
                inventorReductionMessagePersistence.setCreateDate(new Date());
                inventorReductionMessagePersistence.setBusinessId(orderNo);
                inventorReductionMessagePersistence.setRemark("扣库存");
                inventorReductionMessagePersistence.setTransactionId(globalTransactionId);
                inventorReductionMessagePersistence.setPayload(JSONObject.toJSONString(inventoryReductionMessage));
                inventorReductionMessagePersistence.setStatus((short)0); //待发送
                inventorReductionMessagePersistence.setType(MessageTopicConstant.sk_inventory_reduction.name());
                eventPublishService.insert(inventorReductionMessagePersistence);

                inventoryReductionMessage.setLocalTransactionMessageId(String.valueOf(inventorReductionMessagePersistence.getId()));

                kafkaTemplate.send(MessageTopicConstant.sk_inventory_reduction.name(),JSONObject.toJSONString(inventoryReductionMessage));


                //发送创建订单消息
                CreateOrderMessage createOrderMessage = new CreateOrderMessage();
                BeanUtils.copyProperties(inventoryReductionMessage,createOrderMessage);

                //保存发消息记录到数据库
                EventPublish orderMessagePersistence = new EventPublish();
                orderMessagePersistence.setCreateDate(new Date());
                orderMessagePersistence.setBusinessId(orderNo);
                orderMessagePersistence.setRemark("创建订单");
                orderMessagePersistence.setTransactionId(globalTransactionId);
                orderMessagePersistence.setPayload(JSONObject.toJSONString(createOrderMessage));
                orderMessagePersistence.setStatus((short)0); //待发送
                orderMessagePersistence.setType(MessageTopicConstant.sk_create_order.name());
                eventPublishService.insert(orderMessagePersistence);
                createOrderMessage.setLocalTransactionMessageId(String.valueOf(orderMessagePersistence.getId()));

                kafkaTemplate.send(MessageTopicConstant.sk_create_order.name(),JSONObject.toJSONString(createOrderMessage));

                redisLock.unLock(lockKey, userId);
                resultObjectVO.setMsg("恭喜抢到了");
            }catch (Exception e)
            {
                redisLock.unLock(lockKey, userId);
                logger.warn(e.getMessage(),e);
                resultObjectVO.setCode(ResultObjectVO.FAILD);
                resultObjectVO.setMsg("请重试");
            }
        }
        return resultObjectVO;
    }


    /**
     * 下单支付功能
     * @param queryMap
     * @return
     */
    @RequestMapping(method = RequestMethod.POST,value="/placeOrder",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResultObjectVO placeOrder(@RequestBody Map<String,Object> queryMap)
    {
        ResultObjectVO resultObjectVO = new ResultObjectVO();
        if(queryMap!=null) {
            if(queryMap.get("skuId")==null)
            {
                resultObjectVO.setCode(ResultObjectVO.FAILD);
                resultObjectVO.setMsg("没有找到商品");
                return resultObjectVO;
            }
        }
        return resultObjectVO;
    }

}
