package com.itdev.finalproject.config;

import com.itdev.finalproject.dto.kafka.EventKafkaMessage;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<Long, EventKafkaMessage> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9092");
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                LongSerializer.class);
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JacksonJsonSerializer.class);
        configProps.put(
                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,
                true);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<Long, EventKafkaMessage> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
