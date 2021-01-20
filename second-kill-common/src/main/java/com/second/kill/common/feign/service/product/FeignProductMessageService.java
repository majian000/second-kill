package com.second.kill.common.feign.service.product;

import com.second.kill.common.feign.fallback.product.FeignProductSkuServiceFallbackFactory;
import com.second.kill.common.vo.ResultObjectVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(value = "second-kill-product",path = "/second-kill-product/product/message",fallbackFactory = FeignProductSkuServiceFallbackFactory.class)
public interface FeignProductMessageService {


    @RequestMapping(value = "/postInventoryReductionMessage",method= RequestMethod.POST,produces = "application/json;charset=UTF-8")
    public ResultObjectVO postInventoryReductionMessage(@RequestParam Map<String, Object> paramMap);


    @RequestMapping(value = "/postRestoreStockMessage",method= RequestMethod.POST,produces = "application/json;charset=UTF-8")
    public ResultObjectVO postRestoreStockMessage(@RequestParam Map<String, Object> paramMap);



}
