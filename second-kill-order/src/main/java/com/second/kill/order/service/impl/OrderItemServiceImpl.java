package com.second.kill.order.service.impl;

import com.second.kill.order.entity.Order;
import com.second.kill.order.entity.OrderItem;
import com.second.kill.order.mapper.OrderItemMapper;
import com.second.kill.order.mapper.OrderMapper;
import com.second.kill.order.service.OrderItemService;
import com.second.kill.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderItemServiceImpl implements OrderItemService {

    @Autowired
    private OrderItemMapper orderItemMapper;


    public OrderItem findByOrderNo(String orderNo){
        return orderItemMapper.findByOrderNo(orderNo);
    }


    @Override
    public int create(OrderItem orderItem) {
        return orderItemMapper.insert(orderItem);
    }
}
