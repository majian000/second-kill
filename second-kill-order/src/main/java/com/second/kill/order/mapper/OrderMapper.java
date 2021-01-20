package com.second.kill.order.mapper;

import com.second.kill.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface OrderMapper {

    public int insert(Order order);


    @Select("select * from sk_order where order_no=#{orderNo}")
    public Order findByOrderNo(String orderNo);
}
