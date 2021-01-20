package com.second.kill.product.entity;

import lombok.Data;

import java.util.Date;

/**
 * 属性键
 *
 * @author majian
 */
@Data
public class AttributeKey {
    private Long id; //主键
    private Integer categoryId; //所属类别
    private String attributeName; //属性名
    private Long attributeSort; //排序
    private Date create_date; //创建时间

}
