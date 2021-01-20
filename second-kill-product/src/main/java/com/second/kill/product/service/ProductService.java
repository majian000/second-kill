package com.second.kill.product.service;

import com.second.kill.product.entity.Product;

import java.util.List;

public interface ProductService {

    public List<Product> queryAllList(Product queryModel);

}
