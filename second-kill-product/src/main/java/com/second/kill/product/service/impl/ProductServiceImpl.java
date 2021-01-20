package com.second.kill.product.service.impl;

import com.second.kill.product.entity.Product;
import com.second.kill.product.mapper.ProductMapper;
import com.second.kill.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<Product> queryAllList(Product queryModel) {
        return productMapper.queryAllList(queryModel);
    }
}
