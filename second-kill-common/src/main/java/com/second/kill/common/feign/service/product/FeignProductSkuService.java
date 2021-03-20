package com.second.kill.common.feign.service.product;

import com.second.kill.common.feign.fallback.product.FeignProductSkuServiceFallbackFactory;
import com.second.kill.common.vo.ResultListVO;
import com.second.kill.common.vo.ResultObjectVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(value = "second-kill-product",path = "/second-kill-product/productSku",fallbackFactory = FeignProductSkuServiceFallbackFactory.class)
public interface FeignProductSkuService {

    @GetMapping("/shelves/list")
    public ResultListVO queryShelvesList(Map<String,Object> paramMap);

    @RequestMapping(value = "/inventoryReduction",method= RequestMethod.POST,produces = "application/json;charset=UTF-8")
    public ResultObjectVO inventoryReduction(@RequestParam Map<String,Object> paramMap);

    @RequestMapping(value = "/refersh/stock",method= RequestMethod.POST,produces = "application/json;charset=UTF-8")
    public ResultObjectVO refershStock(@RequestParam Map<String,Object> paramMap);


}
