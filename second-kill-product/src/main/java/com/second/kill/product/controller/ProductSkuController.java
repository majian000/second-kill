package com.second.kill.product.controller;

import com.alibaba.fastjson.JSONObject;
import com.second.kill.common.lock.RedisLock;
import com.second.kill.common.util.RedisStock;
import com.second.kill.common.vo.ResultListVO;
import com.second.kill.common.vo.ResultObjectVO;
import com.second.kill.common.vo.ResultVO;
import com.second.kill.product.entity.ProductSku;
import com.second.kill.product.service.ProductSkuService;
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




    /**
     * 查询所有上架商品
     * @param paramMap
     * @return
     */
    @RequestMapping(value="/shelves/list",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResultListVO queryShelvesList(Map<String,Object> paramMap)
    {
        if(paramMap==null)
        {
            paramMap = new HashMap<String,Object>();
        }
        paramMap.put("status",1);
        ResultListVO<ProductSku> resultListVO = new ResultListVO<>();
        resultListVO.setData(productSkuService.queryList(paramMap));
        return resultListVO;
    }




    /**
     * 还原库存
     * @param paramMap
     * @return
     */
    @RequestMapping(value="/refersh/stock",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResultObjectVO refershStock(@RequestParam Map<String,Object> paramMap)
    {
        ResultObjectVO resultObjectVO = new ResultObjectVO();
        if(paramMap==null||paramMap.get("appId")==null)
        {
            logger.info("没有找到应用: param:"+ JSONObject.toJSON(paramMap));
            resultObjectVO.setCode(ResultVO.FAILD);
            resultObjectVO.setMsg("没有找到应用!");
            return resultObjectVO;
        }
        if(paramMap==null||paramMap.get("skuId")==null)
        {
            logger.info("没有找到skuId: param:"+ JSONObject.toJSON(paramMap));
            resultObjectVO.setCode(ResultVO.FAILD);
            resultObjectVO.setMsg("没有找到skuId!");
            return resultObjectVO;
        }

        productSkuService.restoreStock(Long.parseLong(String.valueOf(paramMap.get("skuId"))));

        return resultObjectVO;
    }



    /**
     * 删减库存
     * @param paramMap
     * @return
     */
    @RequestMapping(method= RequestMethod.POST,value="/inventoryReduction",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResultObjectVO inventoryReduction(@RequestParam Map<String,Object> paramMap)
    {

        logger.info("删减库存: param:"+ JSONObject.toJSON(paramMap));
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

        try {

            //扣库存
            int row = productSkuService.inventoryReduction(Long.parseLong(skuId));
            if (row <= 0) {
                logger.info("没有库存了 param:" + JSONObject.toJSON(paramMap));
                resultObjectVO.setCode(ResultVO.FAILD);
                resultObjectVO.setMsg("没有库存了!");
            } else {
                logger.info("减库存: param:" + JSONObject.toJSON(paramMap));
            }
        }catch(Exception e)
        {
            logger.warn(e.getMessage(),e);
            resultObjectVO.setCode(ResultVO.FAILD);
            resultObjectVO.setMsg("扣库存失败!");
        }
        return resultObjectVO;
    }






}
