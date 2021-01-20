package com.second.kill.product.mapper;

import com.second.kill.product.entity.ProductSku;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;


@Mapper
public interface ProductSkuMapper {

    public List<ProductSku> queryList(Map<String,Object> queryMap);

    public ProductSku queryBySkuIdForUpdate(Long skuId);

    public int inventoryReduction(Long skuId);

    public int restoreStock(Long skuId);

}
