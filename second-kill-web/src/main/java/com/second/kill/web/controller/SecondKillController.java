package com.second.kill.web.controller;


import com.alibaba.fastjson.JSONObject;
import com.second.kill.common.feign.service.product.FeignProductMessageService;
import com.second.kill.common.lock.RedisLock;
import com.second.kill.common.util.RedisStock;
import com.second.kill.common.vo.ResultListVO;
import com.second.kill.common.vo.ResultObjectVO;
import com.second.kill.common.feign.service.order.FeignOrderMessageService;
import com.second.kill.common.feign.service.product.FeignProductSkuService;
import com.second.kill.web.service.SecondKillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/product")
public class SecondKillController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private FeignProductSkuService feignProductSkuService;

    @Autowired
    private FeignOrderMessageService feignOrderService;

    @Autowired
    private RedisLock redisLock;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private SecondKillService secondKillService;

    @Autowired
    private FeignProductMessageService feignProductMessageService;



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
        ResultObjectVO resultObjectVO = new ResultObjectVO();
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

            paramMap.put("appId",appId);
            paramMap.put("orderNo",UUID.randomUUID().toString().replace("-",""));


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
                //扣库存
                feignProductMessageService.postInventoryReductionMessage(paramMap);

            }catch (Exception e)
            {
                redisLock.unLock(lockKey, userId);
                logger.warn(e.getMessage(),e);
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
