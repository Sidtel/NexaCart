package com.ecommerce.payment.kafka;

import com.ecommerce.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer that listens to order events and triggers payment processing.
 * Implements at-least-once delivery semantics.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(
            topics = "order-events",
            groupId = "payment-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeOrderEvent(OrderEvent event) {
        log.info("Received order event: orderId={}, eventType={}", event.getOrderId(), event.getEventType());

        if ("ORDER_PLACED".equals(event.getEventType())) {
            try {
                paymentService.processPayment(event);
            } catch (Exception e) {
                log.error("Error processing payment for order {}: {}", event.getOrderId(), e.getMessage());
                // In production: implement dead letter queue or retry mechanism
            }
        }
    }
}
