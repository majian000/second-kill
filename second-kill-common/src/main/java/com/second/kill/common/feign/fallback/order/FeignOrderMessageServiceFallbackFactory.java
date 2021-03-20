package com.second.kill.common.feign.fallback.order;

import com.alibaba.fastjson.JSONObject;
import com.second.kill.common.rocketmq.message.order.CreateOrderMessage;
import com.second.kill.common.vo.ResultObjectVO;
import com.second.kill.common.feign.service.order.FeignOrderMessageService;
import feign.hystrix.FallbackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FeignOrderMessageServiceFallbackFactory implements FallbackFactory<FeignOrderMessageService> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public FeignOrderMessageService create(Throwable throwable) {
        logger.warn(throwable.getMessage(),throwable);
        return new FeignOrderMessageService(){

            @Override
            public ResultObjectVO postCreateOrderMessage(CreateOrderMessage createOrderMessage) {
                ResultObjectVO resultObjectVO = new ResultObjectVO();
                if(createOrderMessage==null)
                {
                    resultObjectVO.setCode(ResultObjectVO.FAILD);
                    resultObjectVO.setMsg("超时重试");
                    return resultObjectVO;
                }
                logger.warn("下单服务失败 params:"+JSONObject.toJSON(createOrderMessage));
                resultObjectVO.setCode(ResultObjectVO.FAILD);
                resultObjectVO.setMsg("超时重试");
                return resultObjectVO;
            }
        };
    }
}
