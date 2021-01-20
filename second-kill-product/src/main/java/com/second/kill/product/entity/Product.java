package com.second.kill.product.entity;

import lombok.Data;

import java.util.Date;

/**
 * 商品
 *
 * @author majian
 */
@Data
public class Product {
    private Long id; //主键
    private Integer categoryId; //所属类别
    private String name; //商品名称
    private String attributes; //商品所有属性
    private Date create_date; //创建时间

}
