package com.second.kill.web.service.impl;

import com.second.kill.common.feign.service.product.FeignProductMessageService;
import com.second.kill.common.vo.ResultObjectVO;
import com.second.kill.common.vo.ResultVO;
import com.second.kill.common.feign.service.order.FeignOrderMessageService;
import com.second.kill.common.feign.service.product.FeignProductSkuService;
import com.second.kill.web.service.SecondKillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SecondKillServiceImpl implements SecondKillService {

    @Autowired
    private FeignProductMessageService feignProductMessageService;

    @Autowired
    private FeignOrderMessageService feignOrderService;

    @Autowired
    private StringRedisTemplate redisTemplate;



    @Override
    public ResultVO secondKill(String stockKey,Integer stock,Map<String, Object> queryMap) {
        ResultObjectVO resultObjectVO = new ResultObjectVO();

        feignProductMessageService.postInventoryReductionMessage(queryMap);
        //减库存
        //resultObjectVO = feignProductSkuService.inventoryReduction(queryMap);
        //if (resultObjectVO.getCode().intValue() == ResultVO.SUCCESS) {

            //int i=1/0;
            //创建订单
            //feignOrderService.postCreateOrderMessage(queryMap);

        //}

        return resultObjectVO;
    }
}
