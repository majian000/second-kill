package com.second.kill.web.app.config;


import com.second.kill.web.kafka.callback.SendCallback;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;

@Configuration
public class KafkaProducerConfig {
    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String bootstrapServersConfig;

    @Value("${spring.kafka.producer.retries}")
    private String retries;

    @Value("${spring.kafka.producer.batch-size}")
    private String batchSize;

    @Value("${spring.kafka.producer.buffer-memory}")
    private String bufferMemory;

    @Value("${spring.kafka.producer.properties.max.requst.size}")
    private String maxRequstSize;

    @Value("${spring.kafka.producer.key-serializer}")
    private String keySerializer;

    @Value("${spring.kafka.producer.value-serializer}")
    private String valueSerializer;

    @Autowired
    private SendCallback sendCallback;




    @Bean
    public KafkaTemplate kafkaTemplate(){
        HashMap<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrapServersConfig);
        configs.put(ProducerConfig.RETRIES_CONFIG,retries);
        configs.put(ProducerConfig.BATCH_SIZE_CONFIG,batchSize);
        configs.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG,maxRequstSize);
        //设置序列化
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,valueSerializer);
        //设置自定义分区
        DefaultKafkaProducerFactory producerFactory = new DefaultKafkaProducerFactory(configs);
        KafkaTemplate kafkaTemplate = new KafkaTemplate(producerFactory);
        //设置消息发送回调
        kafkaTemplate.setProducerListener(sendCallback);
        return kafkaTemplate;
    }


}
