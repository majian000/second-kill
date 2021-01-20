package com.second.kill.product.entity;

import lombok.Data;

import java.util.Date;

/**
 * 商品SKU
 *
 * @author majian
 */
@Data
public class ProductSku {
    private Long id; //主键
    private Integer productId; //所属商品
    private String attributes; //商品所有属性
    private Double price; //价格
    private Integer stockNum; //库存数量
    private String remark; //备注
    private Short status; //是否上架 0:未上架 1:已上架
    private Date create_date; //创建时间

}
