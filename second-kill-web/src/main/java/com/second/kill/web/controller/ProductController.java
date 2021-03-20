package com.second.kill.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.second.kill.common.feign.service.order.FeignOrderService;
import com.second.kill.common.feign.service.product.FeignProductSkuService;
import com.second.kill.common.lock.RedisLock;
import com.second.kill.common.message.MessageTopicConstant;
import com.second.kill.common.persistence.entity.EventProcess;
import com.second.kill.common.persistence.service.EventProcessService;
import com.second.kill.common.util.RedisStock;
import com.second.kill.common.vo.ResultObjectVO;
import com.second.kill.common.vo.ResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final Logger logger = LoggerFactory.getLogger(getClass());


    @Autowired
    private FeignProductSkuService feignProductSkuService;

    @Autowired
    private FeignOrderService feignOrderService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisLock redisLock;

    @Autowired
    private EventProcessService eventProcessService;


    @RequestMapping(value="/buy",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResultObjectVO buy(@RequestBody Map<String,Object> paramMap){
        ResultObjectVO resultObjectVO = new ResultObjectVO();
        logger.info("删减库存: param:"+ JSONObject.toJSON(paramMap));
        if(paramMap==null||paramMap.get("skuId")==null)
        {
            logger.info("没有找到商品: param:"+ JSONObject.toJSON(paramMap));
            resultObjectVO.setCode(ResultVO.FAILD);
            resultObjectVO.setMsg("没有找到商品!");
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
        String appId= "second_kill";
        String orderNo= UUID.randomUUID().toString().replace("-","");
        String globalTransactionId = UUID.randomUUID().toString().replace("-","");
        String productBuyKey = RedisStock.getProductBuyKey(appId,skuId);
        boolean lockStatus = redisLock.lock(productBuyKey,userId);
        while(!lockStatus)
        {
            lockStatus = redisLock.lock(productBuyKey,userId);
        }
        try {

            //扣库存
            paramMap.put("appId", appId);
            paramMap.put("orderNo", orderNo);
            resultObjectVO = feignProductSkuService.inventoryReduction(paramMap);
            if (resultObjectVO.getCode().intValue() == ResultVO.SUCCESS.intValue()) {
                //扣库存成功后创建订单
                resultObjectVO = feignOrderService.create(paramMap);
                //扣库存回滚
                if (resultObjectVO.getCode().intValue() == ResultVO.FAILD.intValue()) {
                    ResultObjectVO refershStockResult = feignProductSkuService.refershStock(paramMap);
                    if (refershStockResult.getCode().intValue() == ResultVO.FAILD.intValue()) {
                        //创建本地补偿事务消息
                        EventProcess eventProcess = new EventProcess();
                        eventProcess.setCreateDate(new Date());
                        eventProcess.setBusinessId(skuId);
                        eventProcess.setRemark("购买模块扣库存");
                        eventProcess.setTableName("sk_product_sku");
                        eventProcess.setTransactionId(globalTransactionId);
                        eventProcess.setPayload(JSONObject.toJSONString(paramMap));
                        eventProcess.setStatus((short) 0); //待处理
                        eventProcess.setType(MessageTopicConstant.inventory_reduction.name());
                        eventProcessService.insert(eventProcess);

                    }
                }
            }

            redisLock.unLock(productBuyKey,userId);

        }catch(Exception e)
        {
            logger.warn(e.getMessage(),e);
            redisLock.unLock(productBuyKey,userId);
        }

        return resultObjectVO;
    }



}
