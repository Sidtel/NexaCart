package com.ecommerce.order.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer for publishing order events.
 * Publishes to 'order-events' topic for downstream consumers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventProducer {

    private static final String TOPIC = "order-events";

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void publishOrderEvent(OrderEvent event) {
        log.info("Publishing order event: orderId={}, eventType={}", event.getOrderId(), event.getEventType());

        CompletableFuture<SendResult<String, OrderEvent>> future =
                kafkaTemplate.send(TOPIC, event.getOrderId(), event);

        future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                log.error("Failed to publish order event for orderId={}: {}",
                        event.getOrderId(), throwable.getMessage());
            } else {
                log.info("Order event published successfully: orderId={}, partition={}, offset={}",
                        event.getOrderId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
