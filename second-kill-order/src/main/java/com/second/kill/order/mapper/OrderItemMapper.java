package com.second.kill.order.mapper;

import com.second.kill.order.entity.Order;
import com.second.kill.order.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;


@Mapper
public interface OrderItemMapper {

    public int insert(OrderItem orderItem);


    @Select("select * from sk_order_item where order_no=#{orderNo}")
    public OrderItem findByOrderNo(String orderNo);
}
