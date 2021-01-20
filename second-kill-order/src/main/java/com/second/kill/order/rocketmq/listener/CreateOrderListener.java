package com.second.kill.order.rocketmq.listener;

import com.alibaba.fastjson.JSONObject;
import com.second.kill.common.feign.service.product.FeignProductMessageService;
import com.second.kill.common.lock.RedisLock;
import com.second.kill.common.util.RedisStock;
import com.second.kill.order.entity.Order;
import com.second.kill.order.entity.OrderItem;
import com.second.kill.common.rocketmq.message.order.CreateOrderMessage;
import com.second.kill.order.service.OrderItemService;
import com.second.kill.order.service.OrderService;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


/**
 * 收到消息后 开始创建订单
 */
@Component
@RocketMQTransactionListener(txProducerGroup = "sk_mq_order_group_create_order")
public class CreateOrderListener implements RocketMQLocalTransactionListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private FeignProductMessageService feignProductMessageService;

    @Autowired
    private RedisLock redisLock;


    /**
     * 创建订单发送消息成功后调用这个方法
     * @param message
     * @param object
     * @return
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object object) {
        String jsonString = new String((byte[]) message.getPayload());
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        CreateOrderMessage createOrderMessage = JSONObject.parseObject(jsonObject.getString("createOrderMessage"), CreateOrderMessage.class);
        try {
            String skuId = String.valueOf(createOrderMessage.getParamMap().get("skuId"));
            String appId = String.valueOf(createOrderMessage.getParamMap().get("appId"));
            String userId = String.valueOf(createOrderMessage.getParamMap().get("userId"));
            String lockKey = RedisStock.getGlobalSecondKillKey(appId,skuId);

            orderService.createOrderAndOrderItem(createOrderMessage.getParamMap());
            logger.info("[保存订单] 提交本地事务{}",jsonString);

            //调用服务末尾,如果扣库存 创建订单都没有问题 删除这个全局锁
            redisLock.unLock(lockKey, userId);
            return RocketMQLocalTransactionState.COMMIT;
        }catch(Exception e)
        {
            logger.info("[保存订单失败] 还原库存  {}",jsonString);
            feignProductMessageService.postRestoreStockMessage(createOrderMessage.getParamMap());

            logger.info("[保存订单失败] 回滚本地事务  {}",jsonString);
            logger.warn(e.getMessage(),e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }


    /**
     * 消息轮训回查 不断检查消息 检查订单是否创建成功了
     * @param message
     * @return
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        String messageString = new String((byte[]) message.getPayload());
        logger.info("[保存订单] 回查消息{}",messageString);
        JSONObject jsonObject = JSONObject.parseObject(messageString);
        CreateOrderMessage createOrderMessage = JSONObject.parseObject(jsonObject.getString("createOrderMessage"), CreateOrderMessage.class);

        String skuId = String.valueOf(createOrderMessage.getParamMap().get("skuId"));
        String appId = String.valueOf(createOrderMessage.getParamMap().get("appId"));
        String userId = String.valueOf(createOrderMessage.getParamMap().get("userId"));
        String lockKey = RedisStock.getGlobalSecondKillKey(appId,skuId);


        Order order = orderService.findByOrderNo(String.valueOf(createOrderMessage.getParamMap().get("orderNo")));
        OrderItem orderItem = orderItemService.findByOrderNo(String.valueOf(createOrderMessage.getParamMap().get("orderNo")));

        if(order!=null&&orderItem!=null)
        {
            redisLock.unLock(lockKey, userId);
            return RocketMQLocalTransactionState.COMMIT;
        }

        return RocketMQLocalTransactionState.UNKNOWN;
    }

}
