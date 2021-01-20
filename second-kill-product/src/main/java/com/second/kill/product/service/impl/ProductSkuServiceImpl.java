package com.second.kill.product.service.impl;

import com.second.kill.product.entity.Product;
import com.second.kill.product.entity.ProductSku;
import com.second.kill.product.mapper.ProductMapper;
import com.second.kill.product.mapper.ProductSkuMapper;
import com.second.kill.product.service.ProductService;
import com.second.kill.product.service.ProductSkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ProductSkuServiceImpl implements ProductSkuService {

    @Autowired
    private ProductSkuMapper productSkuMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public List<ProductSku> queryList(Map<String,Object> queryMap) {
        return productSkuMapper.queryList(queryMap);
    }



    @Transactional
    @Override
    public int inventoryReduction(Long skuId) {
        //锁住这条记录
        ProductSku productSku = productSkuMapper.queryBySkuIdForUpdate(skuId);
        if(productSku.getStockNum()>0) {
            return productSkuMapper.inventoryReduction(skuId);
        }
        return 0;
    }




    @Transactional
    @Override
    public int restoreStock(Long skuId) {
        //锁住这条记录
        ProductSku productSku = productSkuMapper.queryBySkuIdForUpdate(skuId);
        return productSkuMapper.restoreStock(skuId);
    }
}
