package com.second.kill.common.feign.fallback.product;

import com.alibaba.fastjson.JSONObject;
import com.second.kill.common.feign.service.product.FeignProductMessageService;
import com.second.kill.common.feign.service.product.FeignProductSkuService;
import com.second.kill.common.rocketmq.message.product.InventoryReductionMessage;
import com.second.kill.common.rocketmq.message.product.RestoreStockMessage;
import com.second.kill.common.vo.ResultListVO;
import com.second.kill.common.vo.ResultObjectVO;
import feign.hystrix.FallbackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 商品消息服务
 */
@Component
public class FeignProductMessageServiceFallbackFactory implements FallbackFactory<FeignProductMessageService> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public FeignProductMessageService create(Throwable throwable) {
        logger.warn(throwable.getMessage(),throwable);
        return new FeignProductMessageService(){
            @Override
            public ResultObjectVO postInventoryReductionMessage(InventoryReductionMessage inventoryReductionMessage) {
                ResultObjectVO resultObjectVO = new ResultObjectVO();
                if(inventoryReductionMessage==null)
                {
                    resultObjectVO.setCode(ResultObjectVO.FAILD);
                    resultObjectVO.setMsg("超时重试");
                    return resultObjectVO;
                }
                logger.warn("下单服务失败 params:"+JSONObject.toJSON(inventoryReductionMessage));
                resultObjectVO.setCode(ResultObjectVO.FAILD);
                resultObjectVO.setMsg("超时重试");
                return resultObjectVO;
            }

            @Override
            public ResultObjectVO postRestoreStockMessage(RestoreStockMessage restoreStockMessage) {
                ResultObjectVO resultObjectVO = new ResultObjectVO();
                if(restoreStockMessage==null)
                {
                    resultObjectVO.setCode(ResultObjectVO.FAILD);
                    resultObjectVO.setMsg("超时重试");
                    return resultObjectVO;
                }
                logger.warn("下单服务失败 params:"+JSONObject.toJSON(restoreStockMessage));
                resultObjectVO.setCode(ResultObjectVO.FAILD);
                resultObjectVO.setMsg("超时重试");
                return resultObjectVO;
            }
        };
    }
}
