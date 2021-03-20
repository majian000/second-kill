package com.second.kill.product.rocketmq.controller;

import com.alibaba.fastjson.JSONObject;
import com.second.kill.common.lock.RedisLock;
import com.second.kill.common.rocketmq.message.order.CreateOrderMessage;
import com.second.kill.common.rocketmq.message.product.InventoryReductionMessage;
import com.second.kill.common.rocketmq.message.product.RestoreStockMessage;
import com.second.kill.common.util.RedisStock;
import com.second.kill.common.vo.ResultListVO;
import com.second.kill.common.vo.ResultObjectVO;
import com.second.kill.common.vo.ResultVO;
import com.second.kill.product.entity.ProductSku;
import com.second.kill.product.service.ProductSkuService;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product/message")
public class ProductMessageController {

    private final Logger logger = LoggerFactory.getLogger(getClass());


    @Autowired
    private RedisLock redisLock;


    @Autowired
    private RocketMQTemplate rocketMQTemplate;



    /**
     * 减少库存消息
     * @param inventoryReductionMessage
     * @return
     */
    @RequestMapping(method= RequestMethod.POST,value="/postInventoryReductionMessage",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResultObjectVO postInventoryReductionMessage(@RequestBody InventoryReductionMessage inventoryReductionMessage)
    {

        ResultObjectVO resultObjectVO = new ResultObjectVO<>();
        if(inventoryReductionMessage==null||inventoryReductionMessage.getGlobalTransactionId()==null)
        {
            logger.info("没有开启全局事务: param:"+ JSONObject.toJSON(inventoryReductionMessage));
            resultObjectVO.setCode(ResultVO.FAILD);
            resultObjectVO.setMsg("没有找到商品!");
            return resultObjectVO;
        }
        if(inventoryReductionMessage==null||inventoryReductionMessage.getSkuId()==null)
        {
            logger.info("没有找到商品: param:"+ JSONObject.toJSON(inventoryReductionMessage));
            resultObjectVO.setCode(ResultVO.FAILD);
            resultObjectVO.setMsg("没有找到商品!");
            return resultObjectVO;
        }
        if(inventoryReductionMessage==null||inventoryReductionMessage.getAppId()==null)
        {
            logger.info("没有找到应用: param:"+ JSONObject.toJSON(inventoryReductionMessage));
            resultObjectVO.setCode(ResultVO.FAILD);
            resultObjectVO.setMsg("没有找到应用!");
            return resultObjectVO;
        }
        if(inventoryReductionMessage==null||inventoryReductionMessage.getUserId()==null)
        {
            logger.info("没有找到用户: param:"+ JSONObject.toJSON(inventoryReductionMessage));
            resultObjectVO.setCode(ResultVO.FAILD);
            resultObjectVO.setMsg("没有找到用户!");
            return resultObjectVO;
        }
        String skuId =inventoryReductionMessage.getSkuId();
        String userId = inventoryReductionMessage.getUserId();
        String appId= inventoryReductionMessage.getAppId();


        try {


            JSONObject jsonObject =new JSONObject();
            jsonObject.put("inventoryReductionMessage",inventoryReductionMessage);
            String jsonString = jsonObject.toJSONString();
            Message<String> message = MessageBuilder.withPayload(jsonString).build();
            //发送创建订单消息,通知各个服务

            rocketMQTemplate.sendMessageInTransaction("sk_mq_product_group_inventory_reduction",
                    "topic_inventory_reduction",message,null);


            resultObjectVO.setCode(ResultVO.SUCCESS);
            resultObjectVO.setMsg("下单成功!");


            logger.info("减少库存: param:" + JSONObject.toJSON(inventoryReductionMessage));

        }catch(Exception e)
        {
            redisLock.unLock(RedisStock.getGlobalSecondKillKey(appId,skuId), userId);
            logger.warn(e.getMessage(),e);

            resultObjectVO.setCode(ResultVO.FAILD);
            resultObjectVO.setMsg("下单失败!");
        }
        return resultObjectVO;
    }





    /**
     * 下游服务失败 还原库存
     * @param restoreStockMessage
     * @return
     */
    @RequestMapping(method= RequestMethod.POST,value="/postRestoreStockMessage",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResultObjectVO postRestoreStockMessage(@RequestBody RestoreStockMessage restoreStockMessage)
    {

        ResultObjectVO resultObjectVO = new ResultObjectVO<>();
        if(restoreStockMessage==null||restoreStockMessage.getGlobalTransactionId()==null)
        {
            logger.info("没有开启全局事务: param:"+ JSONObject.toJSON(restoreStockMessage));
            resultObjectVO.setCode(ResultVO.FAILD);
            resultObjectVO.setMsg("没有找到商品!");
            return resultObjectVO;
        }
        if(restoreStockMessage==null||restoreStockMessage.getSkuId()==null)
        {
            logger.info("没有找到商品: param:"+ JSONObject.toJSON(restoreStockMessage));
            resultObjectVO.setCode(ResultVO.FAILD);
            resultObjectVO.setMsg("没有找到商品!");
            return resultObjectVO;
        }
        if(restoreStockMessage==null||restoreStockMessage.getAppId()==null)
        {
            logger.info("没有找到应用: param:"+ JSONObject.toJSON(restoreStockMessage));
            resultObjectVO.setCode(ResultVO.FAILD);
            resultObjectVO.setMsg("没有找到应用!");
            return resultObjectVO;
        }
        if(restoreStockMessage==null||restoreStockMessage.getUserId()==null)
        {
            logger.info("没有找到用户: param:"+ JSONObject.toJSON(restoreStockMessage));
            resultObjectVO.setCode(ResultVO.FAILD);
            resultObjectVO.setMsg("没有找到用户!");
            return resultObjectVO;
        }
        String skuId = restoreStockMessage.getSkuId();
        String userId = restoreStockMessage.getUserId();
        String appId= restoreStockMessage.getAppId();


        try {

            JSONObject jsonObject =new JSONObject();
            jsonObject.put("restoreStockMessage",restoreStockMessage);
            String jsonString = jsonObject.toJSONString();
            Message<String> message = MessageBuilder.withPayload(jsonString).build();
            //发送消息,通知本地监听器执行恢复库存
            rocketMQTemplate.sendMessageInTransaction("sk_mq_product_group_restore_stock",
                    "topic_restore_stock",message,null);

            resultObjectVO.setCode(ResultVO.SUCCESS);
            resultObjectVO.setMsg("下单成功!");

            logger.info("还原库存: param:" + JSONObject.toJSON(restoreStockMessage));

        }catch(Exception e)
        {
            redisLock.unLock(RedisStock.getGlobalSecondKillKey(appId,skuId), userId);
            logger.warn(e.getMessage(),e);

            resultObjectVO.setCode(ResultVO.FAILD);
            resultObjectVO.setMsg("还原库存失败!");
        }
        return resultObjectVO;
    }


}
