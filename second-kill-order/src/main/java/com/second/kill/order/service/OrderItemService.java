package com.second.kill.order.service;

import com.second.kill.common.vo.ResultObjectVO;
import com.second.kill.order.entity.Order;
import com.second.kill.order.entity.OrderItem;

import java.util.Map;

public interface OrderItemService {

    public int create(OrderItem orderItem);


    public OrderItem findByOrderNo(String orderNo);

}
