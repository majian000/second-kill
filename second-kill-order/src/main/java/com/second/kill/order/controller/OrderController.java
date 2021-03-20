package com.second.kill.order.controller;

import com.alibaba.fastjson.JSONObject;
import com.second.kill.common.lock.RedisLock;
import com.second.kill.common.vo.ResultListVO;
import com.second.kill.common.vo.ResultObjectVO;
import com.second.kill.common.vo.ResultVO;
import com.second.kill.order.entity.Order;
import com.second.kill.order.entity.OrderItem;
import com.second.kill.order.service.OrderItemService;
import com.second.kill.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final Logger logger = LoggerFactory.getLogger(getClass());


    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisLock redisLock;



    /**
     * 创建订单
     */
    @RequestMapping(value="/create",produces = "application/json;charset=UTF-8")
    public ResultObjectVO create(@RequestParam Map<String,Object> paramMap){

        ResultObjectVO resultObjectVO = new ResultObjectVO(ResultVO.FAILD,"请重试");
        if(paramMap!=null) {
            if(paramMap.get("skuId")==null)
            {
                resultObjectVO.setCode(ResultObjectVO.FAILD);
                resultObjectVO.setMsg("没有找到商品");
                return resultObjectVO;
            }
            if(paramMap.get("userId")==null)
            {
                resultObjectVO.setCode(ResultObjectVO.FAILD);
                resultObjectVO.setMsg("没有找到用户");
                return resultObjectVO;
            }
            Order order = orderService.createOrder(paramMap);
            if(order.getId()==null)
            {
                logger.warn("订单创建失败 param:"+JSONObject.toJSONString(paramMap));
                resultObjectVO.setCode(ResultObjectVO.FAILD);
                resultObjectVO.setMsg("订单创建失败");
                return resultObjectVO;
            }
            logger.info("保存订单 提交本地事务{}", JSONObject.toJSON(order));
            OrderItem orderItem = orderItemService.createOrderItem(String.valueOf(paramMap.get("skuId")),order);
            if(orderItem.getId()==null)
            {
                logger.warn("子订单创建失败 param:"+JSONObject.toJSONString(paramMap));
                resultObjectVO.setCode(ResultObjectVO.FAILD);
                resultObjectVO.setMsg("订单创建失败");
                return resultObjectVO;
            }
            logger.info("保存子订单 提交本地事务{}",JSONObject.toJSON(orderItem));

            resultObjectVO.setCode(ResultObjectVO.SUCCESS);
            resultObjectVO.setMsg("订单创建完成");
            return resultObjectVO;

        }
        return resultObjectVO;
    }



}
