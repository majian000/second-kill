package com.second.kill.order.service;

import com.second.kill.common.vo.ResultObjectVO;
import com.second.kill.order.entity.Order;

import java.util.List;
import java.util.Map;

public interface OrderService {

    public int create(Order order);

    public ResultObjectVO createOrderAndOrderItem(Map<String,Object> paramMap);

    public Order findByOrderNo(String orderNo);

}
