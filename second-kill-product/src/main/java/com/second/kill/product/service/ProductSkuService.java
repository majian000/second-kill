package com.second.kill.product.service;


import com.second.kill.product.entity.ProductSku;

import java.util.List;
import java.util.Map;

public interface ProductSkuService {

    public List<ProductSku> queryList(Map<String,Object> queryMap);

    /**
     * 删减库存
     * @param skuId
     * @return
     */
    public int inventoryReduction(Long skuId);



    /**
     * 恢复库存
     * @param skuId
     * @return
     */
    public int restoreStock(Long skuId);
}
