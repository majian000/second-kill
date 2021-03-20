package com.second.kill.common.feign.fallback.order;

import com.alibaba.fastjson.JSONObject;
import com.second.kill.common.feign.service.order.FeignOrderService;
import com.second.kill.common.feign.service.product.FeignProductSkuService;
import com.second.kill.common.vo.ResultListVO;
import com.second.kill.common.vo.ResultObjectVO;
import feign.hystrix.FallbackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 订单服务
 */
@Component
public class FeignOrderServiceFallbackFactory implements FallbackFactory<FeignOrderService> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public FeignOrderService create(Throwable throwable) {

        logger.warn(throwable.getMessage(),throwable);
        return new FeignOrderService(){

            @Override
            public ResultObjectVO create(Map<String, Object> paramMap) {
                ResultObjectVO resultObjectVO = new ResultObjectVO();
                if(paramMap==null)
                {
                    resultObjectVO.setCode(ResultObjectVO.FAILD);
                    resultObjectVO.setMsg("请重试");
                    return resultObjectVO;
                }
                logger.warn("订单创建失败 params:"+JSONObject.toJSON(paramMap));
                resultObjectVO.setCode(ResultObjectVO.FAILD);
                resultObjectVO.setMsg("订单创建失败");
                return resultObjectVO;
            }
        };
    }
}
