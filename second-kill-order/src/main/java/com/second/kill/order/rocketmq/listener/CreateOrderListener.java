package com.second.kill.order.rocketmq.listener;

import com.alibaba.fastjson.JSONObject;
import com.second.kill.common.feign.service.product.FeignProductMessageService;
import com.second.kill.common.lock.RedisLock;
import com.second.kill.common.persistence.entity.MQTransaction;
import com.second.kill.common.persistence.service.MQTransactionService;
import com.second.kill.common.rocketmq.message.product.RestoreStockMessage;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

    @Autowired
    private MQTransactionService mqTransactionService;


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
            String skuId = createOrderMessage.getSkuId();
            String appId = createOrderMessage.getAppId();
            String userId = createOrderMessage.getUserId();

            Map<String,Object> paramMap=new HashMap<String,Object>();
            paramMap.put("skuId",createOrderMessage.getSkuId());
            paramMap.put("orderNo",createOrderMessage.getOrderNo());
            paramMap.put("userId",createOrderMessage.getUserId());

            Order order = orderService.createOrder(paramMap);
            if(order.getId()==null)
            {
                throw new IllegalArgumentException("订单创建失败");
            }
            logger.info("保存订单 提交本地事务{}",JSONObject.toJSON(order));

            OrderItem orderItem = orderItemService.createOrderItem(skuId,order);
            if(orderItem.getId()==null)
            {
                throw new IllegalArgumentException("子订单创建失败");
            }
            logger.info("保存子订单 提交本地事务{}",JSONObject.toJSON(orderItem));


            //创建本地事务记录,用于回查判断
            MQTransaction mqTransaction = new MQTransaction();
            mqTransaction.setCreateDate(new Date());
            mqTransaction.setBusinessId(String.valueOf(order.getId()));
            mqTransaction.setRemark("保存订单");
            mqTransaction.setTableName("sk_order");
            mqTransaction.setTransactionId(createOrderMessage.getGlobalTransactionId());

            mqTransactionService.insert(mqTransaction);



            mqTransaction = new MQTransaction();
            mqTransaction.setCreateDate(new Date());
            mqTransaction.setBusinessId(String.valueOf(orderItem.getId()));
            mqTransaction.setRemark("保存子订单");
            mqTransaction.setTableName("sk_order_item");
            mqTransaction.setTransactionId(createOrderMessage.getGlobalTransactionId());

            mqTransactionService.insert(mqTransaction);

            //调用服务末尾,如果扣库存 创建订单都没有问题 删除这个全局锁
            redisLock.unLock(RedisStock.getGlobalSecondKillKey(appId,skuId), userId);
            return RocketMQLocalTransactionState.COMMIT;
        }catch(Exception e)
        {
            //删除主订单
            orderService.deleteByOrderNo(createOrderMessage.getOrderNo());

            logger.info("保存订单或子订单失败 还原库存  {}",jsonString);
            RestoreStockMessage restoreStockMessage = new RestoreStockMessage();
            BeanUtils.copyProperties(createOrderMessage,restoreStockMessage);
            feignProductMessageService.postRestoreStockMessage(restoreStockMessage);

            logger.info("保存订单或子订单失败 回滚本地事务  {}",jsonString);
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
        try {
            logger.info("保存订单和子订单 回查消息{}", messageString);
            JSONObject jsonObject = JSONObject.parseObject(messageString);
            CreateOrderMessage createOrderMessage = JSONObject.parseObject(jsonObject.getString("createOrderMessage"), CreateOrderMessage.class);
            //查询本地事务
            MQTransaction queryMQTransaction = new MQTransaction();
            queryMQTransaction.setTableName("sk_order");
            queryMQTransaction.setRemark("保存订单");
            queryMQTransaction.setTransactionId(createOrderMessage.getGlobalTransactionId());
            List<MQTransaction> mqTransactionList = mqTransactionService.queryList(queryMQTransaction);
            Order order=null;
            //重试 创建订单
            if (CollectionUtils.isEmpty(mqTransactionList)) {

                Order orderPersistence = orderService.findByOrderNo(createOrderMessage.getOrderNo());
                if(orderPersistence==null)
                {
                    Map<String,Object> paramMap=new HashMap<String,Object>();
                    paramMap.put("skuId",createOrderMessage.getSkuId());
                    paramMap.put("orderNo",createOrderMessage.getOrderNo());
                    paramMap.put("userId",createOrderMessage.getUserId());

                    order = orderService.createOrder(paramMap);
                    if(order.getId()==null)
                    {
                        throw new IllegalArgumentException("订单创建失败");
                    }

                    //创建本地事务记录,用于回查判断
                    MQTransaction mqTransaction = new MQTransaction();
                    mqTransaction.setCreateDate(new Date());
                    mqTransaction.setBusinessId(String.valueOf(order.getId()));
                    mqTransaction.setRemark("保存订单");
                    mqTransaction.setTableName("sk_order");
                    mqTransaction.setTransactionId(createOrderMessage.getGlobalTransactionId());

                    mqTransactionService.insert(mqTransaction);
                }

            }

            mqTransactionList = mqTransactionService.queryList(queryMQTransaction);
            //重试 创建子订单
            if (CollectionUtils.isEmpty(mqTransactionList)) {

                OrderItem orderItemPersistence = orderItemService.findByOrderNo(createOrderMessage.getOrderNo());
                if(orderItemPersistence==null)
                {
                    OrderItem orderItem = orderItemService.createOrderItem(createOrderMessage.getSkuId(),order);
                    if(orderItem.getId()==null)
                    {
                        throw new IllegalArgumentException("子订单创建失败");
                    }

                    //创建本地事务记录,用于回查判断
                    MQTransaction mqTransaction = new MQTransaction();
                    mqTransaction.setCreateDate(new Date());
                    mqTransaction.setBusinessId(String.valueOf(orderItem.getId()));
                    mqTransaction.setRemark("保存子订单");
                    mqTransaction.setTableName("sk_order_item");
                    mqTransaction.setTransactionId(createOrderMessage.getGlobalTransactionId());

                    mqTransactionService.insert(mqTransaction);
                }

            }

            //释放全局锁
            redisLock.unLock(RedisStock.getGlobalSecondKillKey(createOrderMessage.getAppId(),createOrderMessage.getSkuId()), createOrderMessage.getUserId());
            return RocketMQLocalTransactionState.COMMIT;
        }catch(Exception e)
        {
            logger.info("保存订单和子订单 回查消息 发生异常 {}",messageString);
            logger.warn(e.getMessage(),e);
        }
        return RocketMQLocalTransactionState.ROLLBACK;
    }

}
