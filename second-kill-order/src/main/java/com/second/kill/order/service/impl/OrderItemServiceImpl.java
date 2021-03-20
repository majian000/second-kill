package com.second.kill.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.second.kill.common.vo.ResultVO;
import com.second.kill.order.entity.Order;
import com.second.kill.order.entity.OrderItem;
import com.second.kill.order.mapper.OrderItemMapper;
import com.second.kill.order.mapper.OrderMapper;
import com.second.kill.order.service.OrderItemService;
import com.second.kill.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Random;

@Service
public class OrderItemServiceImpl implements OrderItemService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private OrderItemService orderItemService;


    public OrderItem findByOrderNo(String orderNo){
        return orderItemMapper.findByOrderNo(orderNo);
    }


    @Override
    public int create(OrderItem orderItem) {
        return orderItemMapper.insert(orderItem);
    }

    @Transactional
    @Override
    public OrderItem createOrderItem(String skuId,Order order) {
        OrderItem orderItemPersistence = orderItemService.findByOrderNo(order.getOrderNo());

        if(orderItemPersistence==null) {
            OrderItem orderItem = new OrderItem();
            orderItem.setCreateDate(new Date());
            orderItem.setUserId(order.getUserId());
            orderItem.setOrderId(order.getId());
            orderItem.setOrderNo(order.getOrderNo());
            orderItem.setSkuId(Long.parseLong(skuId));
            orderItem.setDeliveryStatus(0);
            orderItem.setProductNum(1);
            orderItem.setOrderItemAmount(0D);
            orderItem.setDeliveryMoney(0D);
            orderItem.setProductPrice(0D);
            orderItem.setSellerStatus(0);
            orderItem.setBuyerStatus(0);

            orderItemService.create(orderItem);

            if (orderItem.getId() == null) {
                logger.info("下单失败: param:" + JSONObject.toJSON(order));
                throw new IllegalArgumentException("创建订单失败");
            }
            orderItemPersistence = orderItem;
        }

        if(new Random().nextInt(100)%2==0)
        {
            throw new IllegalArgumentException("手动测试异常");
        }
        return orderItemPersistence;
    }
}
