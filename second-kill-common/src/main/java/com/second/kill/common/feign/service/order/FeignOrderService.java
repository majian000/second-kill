package com.second.kill.common.feign.service.order;

import com.second.kill.common.feign.fallback.order.FeignOrderServiceFallbackFactory;
import com.second.kill.common.feign.fallback.product.FeignProductSkuServiceFallbackFactory;
import com.second.kill.common.vo.ResultListVO;
import com.second.kill.common.vo.ResultObjectVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(value = "second-kill-order",path = "/second-kill-order/order",fallbackFactory = FeignOrderServiceFallbackFactory.class)
public interface FeignOrderService {


    @RequestMapping(value = "/create",method= RequestMethod.POST,produces = "application/json;charset=UTF-8")
    public ResultObjectVO create(@RequestParam Map<String, Object> paramMap);


}
