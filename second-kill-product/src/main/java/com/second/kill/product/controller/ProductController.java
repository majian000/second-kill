package com.second.kill.product.controller;

import com.second.kill.common.lock.RedisLock;
import com.second.kill.common.vo.ResultListVO;
import com.second.kill.product.entity.Product;
import com.second.kill.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisLock redisLock;

    @RequestMapping(value="/list",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResultListVO queryList(){
        ResultListVO<Product> resultListVO = new ResultListVO<>();
        resultListVO.setData(productService.queryAllList(null));
        return resultListVO;
    }



//
//    @RequestMapping(value="/buy",produces = "application/json;charset=UTF-8")
//    public void buy(){
//        redisTemplate.setKeySerializer(new StringRedisSerializer());
//        redisTemplate.setValueSerializer(new StringRedisSerializer());
//        redisTemplate.opsForValue().set("product_stock","1000");
//        redisTemplate.opsForValue().getOperations().delete("sk_product_lock");
//        //TODO 增加人数控制,比如允许500人抢到 那么其余的人将不进入这个方法
//        int threadCount=1000;
//        Thread[] threads = new Thread[threadCount];
//        for (int k = 0; k < threadCount; k++) {
//            threads[k] = new Thread("thread"+k) {
//                @Override
//                public void run() {
//                    while(true) {
//                        try {
//                            redisLock.lock("sk_product_lock", String.valueOf(this.hashCode()));
//                            Integer stock = Integer.parseInt(String.valueOf(redisTemplate.opsForValue().get("product_stock")));
//
//
//                            System.out.println("thread hascode :" + this.getName()+ " current stock:" + stock);
//                            if(stock<=0) {
//                                redisLock.unLock("sk_product_lock", String.valueOf(this.hashCode()));
//                                return;
//                            }
//                            redisTemplate.opsForValue().set("product_stock", String.valueOf(stock.longValue() - 1));
//
//                            redisLock.unLock("sk_product_lock", String.valueOf(this.hashCode()));
//                        }catch(Exception e)
//                        {
//                            redisLock.unLock("sk_product_lock", String.valueOf(this.hashCode()));
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            };
//        }
//        for (int i = 0; i < threadCount; i++) {
//            threads[i].start();
//        }
//    }



}
