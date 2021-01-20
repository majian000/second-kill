package com.second.kill.product.entity;

import lombok.Data;

import java.util.Date;

/**
 * 商品类别
 *
 * @author majian
 */
@Data
public class Category {
    private Long id; //主键
    private Long parentId; //上级类别
    private String name; //类别名称
    private Long categorySort; //排序
    private Date create_date; //创建时间

}
