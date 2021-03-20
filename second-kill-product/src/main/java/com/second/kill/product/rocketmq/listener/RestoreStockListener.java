package com.second.kill.product.rocketmq.listener;

import com.alibaba.fastjson.JSONObject;
import com.second.kill.common.lock.RedisLock;
import com.second.kill.common.persistence.entity.MQTransaction;
import com.second.kill.common.persistence.service.MQTransactionService;
import com.second.kill.common.rocketmq.message.product.InventoryReductionMessage;
import com.second.kill.common.rocketmq.message.product.RestoreStockMessage;
import com.second.kill.common.util.RedisStock;
import com.second.kill.product.service.ProductSkuService;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 收到消息后 开始还原库存(下游服务失败)
 */
@Component
@RocketMQTransactionListener(txProducerGroup = "sk_mq_product_group_restore_stock")
public class RestoreStockListener implements RocketMQLocalTransactionListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProductSkuService productSkuService;


    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MQTransactionService mqTransactionService;


    @Autowired
    private RedisLock redisLock;

    private final String OPREMARK="恢复商品库存";


    /**
     * 减少库存发送消息成功后调用这个方法
     * @param message
     * @param object
     * @return
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object object) {
        String jsonString = new String((byte[]) message.getPayload());
        try {
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            RestoreStockMessage restoreStockMessage = JSONObject.parseObject(jsonObject.getString("restoreStockMessage"), RestoreStockMessage.class);
            String skuId =restoreStockMessage.getSkuId();
            String appId = restoreStockMessage.getAppId();
            String userId = restoreStockMessage.getUserId();

            try {

                int row = productSkuService.restoreStock(Long.parseLong(skuId));

                String stockKey = RedisStock.getStockKey(appId, skuId);
                Integer stock = Integer.parseInt(String.valueOf(redisTemplate.opsForValue().get(stockKey)));
                redisTemplate.opsForValue().set(stockKey, String.valueOf(stock.longValue() + 1));
                logger.info("恢复库存 提交本地事务{}", jsonString);


                //创建本地事务记录,用于回查判断
                MQTransaction mqTransaction = new MQTransaction();
                mqTransaction.setCreateDate(new Date());
                mqTransaction.setBusinessId(userId);
                mqTransaction.setRemark(OPREMARK);
                mqTransaction.setTableName("sk_product_sku");
                mqTransaction.setTransactionId(restoreStockMessage.getGlobalTransactionId());

                mqTransactionService.insert(mqTransaction);


                //如果下游服务异常 恢复库存之后 将释放全局锁
                redisLock.unLock(RedisStock.getGlobalSecondKillKey(appId,skuId), userId);
            }catch(Exception e)
            {
                redisLock.unLock(RedisStock.getGlobalSecondKillKey(appId,skuId), userId);
                throw e;
            }
            return RocketMQLocalTransactionState.COMMIT;
        }catch(Exception e)
        {
            logger.info("恢复库存 回滚本地事务 {}",jsonString);
            logger.warn(e.getMessage(),e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }


    /**
     * 消息轮训回查 不断检查消息 直到消息成功为止
     * @param message
     * @return
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {

        String messageString = new String((byte[]) message.getPayload());
        try {
            logger.info("恢复库存 回查消息{}", messageString);
            JSONObject jsonObject = JSONObject.parseObject(messageString);
            RestoreStockMessage restoreStockMessage = JSONObject.parseObject(jsonObject.getString("restoreStockMessage"), RestoreStockMessage.class);
            //查询本地事务
            MQTransaction queryMQTransaction = new MQTransaction();
            queryMQTransaction.setTableName("sk_product_sku");
            queryMQTransaction.setBusinessId(restoreStockMessage.getUserId());
            queryMQTransaction.setRemark(OPREMARK);
            queryMQTransaction.setTransactionId(restoreStockMessage.getGlobalTransactionId());
            List<MQTransaction> mqTransactionList = mqTransactionService.queryList(queryMQTransaction);
            if (CollectionUtils.isEmpty(mqTransactionList)) {

                //扣库存 保存事务记录
                String skuId =restoreStockMessage.getSkuId();
                String appId = restoreStockMessage.getAppId();
                String userId = restoreStockMessage.getUserId();


                int row = productSkuService.restoreStock(Long.parseLong(skuId));

                String stockKey = RedisStock.getStockKey(appId, skuId);
                Integer stock = Integer.parseInt(String.valueOf(redisTemplate.opsForValue().get(stockKey)));
                redisTemplate.opsForValue().set(stockKey, String.valueOf(stock.longValue() + 1));

                //创建本地事务记录,用于回查判断
                MQTransaction mqTransaction = new MQTransaction();
                mqTransaction.setCreateDate(new Date());
                mqTransaction.setBusinessId(userId);
                mqTransaction.setRemark(OPREMARK);
                mqTransaction.setTableName("sk_product_sku");
                mqTransaction.setTransactionId(restoreStockMessage.getGlobalTransactionId());

                mqTransactionService.insert(mqTransaction);

                return RocketMQLocalTransactionState.COMMIT;
            }
        }catch(Exception e)
        {
            logger.info("[减少库存] 回查消息 发生异常 {}",messageString);
            logger.warn(e.getMessage(),e);

            return RocketMQLocalTransactionState.ROLLBACK;

        }
        return RocketMQLocalTransactionState.ROLLBACK;

    }

}
