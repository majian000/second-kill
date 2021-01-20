package com.second.kill.product.mapper;

import com.second.kill.product.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;


@Mapper
public interface ProductMapper {

    public List<Product> queryAllList(Product queryModel);

}
