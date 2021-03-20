package com.second.kill.common.feign.service.order;

import com.second.kill.common.feign.fallback.order.FeignOrderMessageServiceFallbackFactory;
import com.second.kill.common.feign.fallback.product.FeignProductSkuServiceFallbackFactory;
import com.second.kill.common.rocketmq.message.order.CreateOrderMessage;
import com.second.kill.common.vo.ResultObjectVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(value = "second-kill-order",path = "/second-kill-order/order/message",fallbackFactory = FeignOrderMessageServiceFallbackFactory.class)
public interface FeignOrderMessageService {


    @RequestMapping(value = "/postCreateOrderMessage",method= RequestMethod.POST,produces = "application/json;charset=UTF-8")
    public ResultObjectVO postCreateOrderMessage(@RequestBody CreateOrderMessage createOrderMessage);




}
