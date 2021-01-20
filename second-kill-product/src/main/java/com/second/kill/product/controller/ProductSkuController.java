package com.second.kill.product.controller;

import com.alibaba.fastjson.JSONObject;
import com.second.kill.common.lock.RedisLock;
import com.second.kill.common.rocketmq.message.order.CreateOrderMessage;
import com.second.kill.common.util.RedisStock;
import com.second.kill.common.vo.ResultListVO;
import com.second.kill.common.vo.ResultObjectVO;
import com.second.kill.common.vo.ResultVO;
import com.second.kill.product.entity.ProductSku;
import com.second.kill.product.service.ProductSkuService;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/productSku")
public class ProductSkuController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProductSkuService productSkuService;

    @Autowired
    private RedisLock redisLock;

    @Autowired
    private StringRedisTemplate redisTemplate;



    /**
     * 查询所有上架商品
     * @param queryMap
     * @return
     */
    @RequestMapping(value="/shelves/list",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResultListVO queryShelvesList(Map<String,Object> queryMap)
    {
        if(queryMap==null)
        {
            queryMap = new HashMap<String,Object>();
        }
        queryMap.put("status",1);
        ResultListVO<ProductSku> resultListVO = new ResultListVO<>();
        resultListVO.setData(productSkuService.queryList(queryMap));
        return resultListVO;
    }




    /**
     * 设置商品库存
     * @param queryMap
     * @return
     */
    @RequestMapping(value="/refersh/stock",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResultObjectVO refershStock(@RequestParam Map<String,Object> queryMap)
    {
        ResultObjectVO resultObjectVO = new ResultObjectVO();
        if(queryMap==null||queryMap.get("appId")==null)
        {
            logger.info("没有找到应用: param:"+ JSONObject.toJSON(queryMap));
            resultObjectVO.setCode(ResultVO.FAILD);
            resultObjectVO.setMsg("没有找到应用!");
            return resultObjectVO;
        }
        if(queryMap==null||queryMap.get("skuId")==null)
        {
            logger.info("没有找到skuId: param:"+ JSONObject.toJSON(queryMap));
            resultObjectVO.setCode(ResultVO.FAILD);
            resultObjectVO.setMsg("没有找到skuId!");
            return resultObjectVO;
        }

        String appId= String.valueOf(queryMap.get("appId"));

        Map<String,Object> productSkuQueryMap = new HashMap<String,Object>();
        productSkuQueryMap.put("status",1);
        productSkuQueryMap.put("id",queryMap.get("skuId"));
        List<ProductSku> productSkuList = productSkuService.queryList(productSkuQueryMap);
        if(!CollectionUtils.isEmpty(productSkuList))
        {

            for(ProductSku productSku:productSkuList)
            {
                if(productSku!=null&&productSku.getId()!=null) {
                    String productSkuStockKey = RedisStock.getStockKey(appId,String.valueOf(productSku.getId()));
                    redisTemplate.opsForValue().set(productSkuStockKey,String.valueOf(productSku.getStockNum()));
                }
            }
        }
        return resultObjectVO;
    }



    /**
     * 删减库存
     * @param queryMap
     * @return
     */
    @RequestMapping(method= RequestMethod.POST,value="/inventoryReduction",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResultObjectVO inventoryReduction(@RequestParam Map<String,Object> queryMap)
    {

        ResultObjectVO resultObjectVO = new ResultObjectVO<>();
        if(queryMap==null||queryMap.get("skuId")==null)
        {
            logger.info("没有找到商品: param:"+ JSONObject.toJSON(queryMap));
            resultObjectVO.setCode(ResultVO.FAILD);
            resultObjectVO.setMsg("没有找到商品!");
            return resultObjectVO;
        }
        if(queryMap==null||queryMap.get("appId")==null)
        {
            logger.info("没有找到应用: param:"+ JSONObject.toJSON(queryMap));
            resultObjectVO.setCode(ResultVO.FAILD);
            resultObjectVO.setMsg("没有找到应用!");
            return resultObjectVO;
        }
        if(queryMap==null||queryMap.get("userId")==null)
        {
            logger.info("没有找到用户: param:"+ JSONObject.toJSON(queryMap));
            resultObjectVO.setCode(ResultVO.FAILD);
            resultObjectVO.setMsg("没有找到用户!");
            return resultObjectVO;
        }
        String skuId = String.valueOf(queryMap.get("skuId"));
        String userId = String.valueOf(queryMap.get("userId"));
        String appId= String.valueOf(queryMap.get("appId"));

        String lockKey = appId+"_product_service_"+skuId;
        boolean lockStatus = redisLock.lock(lockKey,userId);
        if(!lockStatus)
        {
            resultObjectVO.setCode(ResultObjectVO.SUCCESS);
            resultObjectVO.setMsg("超时重试");
            return resultObjectVO;
        }
        try {

            //扣库存
            int row = productSkuService.inventoryReduction(Long.parseLong(skuId));
            if (row <= 0) {
                logger.info("没有库存了 param:" + JSONObject.toJSON(queryMap));
                resultObjectVO.setCode(ResultVO.FAILD);
                resultObjectVO.setMsg("没有库存了!");
            } else {
                logger.info("减库存: param:" + JSONObject.toJSON(queryMap));
            }
            redisLock.unLock(lockKey, userId);
        }catch(Exception e)
        {
            redisLock.unLock(lockKey, userId);
            logger.warn(e.getMessage(),e);

            resultObjectVO.setCode(ResultVO.FAILD);
            resultObjectVO.setMsg("扣库存失败!");
        }
        return resultObjectVO;
    }






}
