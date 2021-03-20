package com.second.kill.common.feign.fallback.product;

import com.alibaba.fastjson.JSONObject;
import com.second.kill.common.vo.ResultListVO;
import com.second.kill.common.vo.ResultObjectVO;
import com.second.kill.common.feign.service.product.FeignProductSkuService;
import feign.hystrix.FallbackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 商品服务
 */
@Component
public class FeignProductSkuServiceFallbackFactory implements FallbackFactory<FeignProductSkuService> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public FeignProductSkuService create(Throwable throwable) {
        logger.warn(throwable.getMessage(),throwable);
        return new FeignProductSkuService(){
            @Override
            public ResultListVO queryShelvesList(Map<String, Object> paramMap) {
                ResultListVO resultListVO = new ResultListVO();
                if(paramMap==null)
                {
                    resultListVO.setCode(ResultObjectVO.FAILD);
                    resultListVO.setMsg("超时重试");
                    return resultListVO;
                }
                logger.warn("查询上架商品列表服务失败 params:"+JSONObject.toJSON(paramMap));
                resultListVO.setCode(ResultObjectVO.FAILD);
                resultListVO.setMsg("超时重试");
                return resultListVO;
            }

            @Override
            public ResultObjectVO inventoryReduction(Map<String, Object> paramMap) {
                ResultObjectVO resultObjectVO = new ResultObjectVO();
                if(paramMap==null)
                {
                    resultObjectVO.setCode(ResultObjectVO.FAILD);
                    resultObjectVO.setMsg("超时重试");
                    return resultObjectVO;
                }
                logger.warn("扣库存服务 params:"+JSONObject.toJSON(paramMap));
                resultObjectVO.setCode(ResultObjectVO.FAILD);
                resultObjectVO.setMsg("购买失败,请重试!");
                return resultObjectVO;
            }

            @Override
            public ResultObjectVO refershStock(Map<String, Object> paramMap) {
                ResultObjectVO resultObjectVO = new ResultObjectVO();
                if(paramMap==null)
                {
                    resultObjectVO.setCode(ResultObjectVO.FAILD);
                    resultObjectVO.setMsg("请重试");
                    return resultObjectVO;
                }
                logger.warn("设置库存服务失败 params:"+JSONObject.toJSON(paramMap));
                resultObjectVO.setCode(ResultObjectVO.FAILD);
                resultObjectVO.setMsg("设置库存服务失败");
                return resultObjectVO;
            }


        };
    }
}
