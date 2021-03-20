package com.second.kill.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.second.kill.common.lock.RedisLock;
import com.second.kill.common.vo.ResultObjectVO;
import com.second.kill.common.vo.ResultVO;
import com.second.kill.order.entity.Order;
import com.second.kill.order.entity.OrderItem;
import com.second.kill.order.mapper.OrderMapper;
import com.second.kill.order.service.OrderItemService;
import com.second.kill.order.service.OrderService;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {


    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RedisLock redisLock;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemService orderItemService;

    @Resource
    private RocketMQTemplate rocketMQTemplate;


    @Override
    public int create(Order order) {
        return orderMapper.insert(order);
    }

    @Override
    public int deleteByOrderNo(String orderNo) {
        return orderMapper.deleteByOrderNo(orderNo);
    }

    @Transactional
    @Override
    public Order createOrder(Map<String, Object> paramMap) {
        String skuId = String.valueOf(paramMap.get("skuId"));
        String userId = String.valueOf(paramMap.get("userId"));
        String orderNo =String.valueOf(paramMap.get("orderNo"));


        Order orderPersistence = orderMapper.findByOrderNo(orderNo);

        if(orderPersistence==null) {
            //订单创建
            Order order = new Order();
            order.setCreateDate(new Date());
            order.setUserId(Long.parseLong(userId));
            order.setPayType(-1);
            order.setOrderNo(orderNo);
            order.setPayStatus(0);  //支付状态
            order.setOrderAmount(0D); //商品总金额
            order.setTotalAmount(0D); //商品最终金额(折扣算完)
            order.setTradeStatus(0); //交易状态
            order.setPayAmount(0D);

            this.create(order);
            if (order.getId() == null) {
                logger.info("下单失败: param:" + JSONObject.toJSON(paramMap));
                throw new IllegalArgumentException("创建订单失败");
            }

            orderPersistence = order;
        }
        return orderPersistence;
    }

    @Transactional
    @Override
    public ResultObjectVO createOrderAndOrderItem(Map<String,Object> paramMap) {


        ResultObjectVO resultObjectVO = new ResultObjectVO<>();
        String skuId = String.valueOf(paramMap.get("skuId"));
        String userId = String.valueOf(paramMap.get("userId"));
        String orderNo =String.valueOf(paramMap.get("orderNo"));


        Order orderPersistence = orderMapper.findByOrderNo(orderNo);

        if(orderPersistence==null) {
            //订单创建
            Order order = new Order();
            order.setCreateDate(new Date());
            order.setUserId(Long.parseLong(userId));
            order.setPayType(-1);
            order.setOrderNo(orderNo);
            order.setPayStatus(0);  //支付状态
            order.setOrderAmount(0D); //商品总金额
            order.setTotalAmount(0D); //商品最终金额(折扣算完)
            order.setTradeStatus(0); //交易状态
            order.setPayAmount(0D);

            this.create(order);
            if (order.getId() == null) {

                logger.info("下单失败: param:" + JSONObject.toJSON(paramMap));
                resultObjectVO.setCode(ResultVO.FAILD);
                resultObjectVO.setMsg("下单失败!");
                return resultObjectVO;
            }

            orderPersistence = order;
        }



        OrderItem orderItemPersistence = orderItemService.findByOrderNo(orderNo);

        if(orderItemPersistence==null) {
            OrderItem orderItem = new OrderItem();
            orderItem.setCreateDate(new Date());
            orderItem.setUserId(Long.parseLong(userId));
            orderItem.setOrderId(orderPersistence.getId());
            orderItem.setOrderNo(orderNo);
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

                logger.info("下单失败: param:" + JSONObject.toJSON(paramMap));
                resultObjectVO.setCode(ResultVO.FAILD);
                resultObjectVO.setMsg("下单失败!");
                return resultObjectVO;
            }
        }

//        if(new Random().nextInt(100)%2==0)
//        {
//            throw new IllegalArgumentException("手动测试异常");
//        }

        return resultObjectVO;
    }

    @Override
    public Order findByOrderNo(String orderNo) {
        return orderMapper.findByOrderNo(orderNo);
    }
}
