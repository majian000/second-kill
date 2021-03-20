package com.second.kill.web.app.config;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaCustomerConfig {
    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String bootstrapServersConfig;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;

    @Value("${spring.kafka.consumer.enable-auto-commit}")
    private String enableAutoCommit;

    @Value("${spring.kafka.consumer.key-deserializer}")
    private String keySerializer;

    @Value("${spring.kafka.consumer.value-deserializer}")
    private String valueSerializer;




    @Bean
    public KafkaListenerContainerFactory<?> batchFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new
                ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(consumerConfigs()));
        factory.setBatchListener(true); // 开启批量监听
        return factory;
    }


    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> configs = new HashMap<>();

        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrapServersConfig);
        configs.put(ConsumerConfig.GROUP_ID_CONFIG,groupId);
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,autoOffsetReset);
        configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,enableAutoCommit);
        //设置序列化
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, keySerializer);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,valueSerializer);
        return configs;
    }


}
