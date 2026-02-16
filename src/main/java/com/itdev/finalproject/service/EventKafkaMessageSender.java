package com.itdev.finalproject.service;

import com.itdev.finalproject.dto.kafka.EventKafkaMessage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventKafkaMessageSender {

    private final KafkaTemplate<Long, EventKafkaMessage> kafkaTemplate;

    public EventKafkaMessageSender(KafkaTemplate<Long, EventKafkaMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(EventKafkaMessage event) {
        if (!event.fieldChanges().isEmpty()) {
            System.err.println("Sending Kafka: " + event);
            kafkaTemplate.send(
                    "event-topic",
                    event.eventId(),
                    event
            );
        }
    }
}
