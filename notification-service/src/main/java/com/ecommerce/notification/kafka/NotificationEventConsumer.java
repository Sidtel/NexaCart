package com.ecommerce.notification.kafka;

import com.ecommerce.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer that listens to order and payment events
 * and triggers appropriate notifications.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "order-events",
            groupId = "notification-service-group",
            containerFactory = "orderEventListenerFactory"
    )
    public void consumeOrderEvent(OrderEvent event) {
        log.info("Received order event: orderId={}, type={}", event.getOrderId(), event.getEventType());

        switch (event.getEventType()) {
            case "ORDER_PLACED" -> notificationService.sendOrderConfirmation(event);
            case "ORDER_SHIPPED" -> notificationService.sendShippingNotification(event);
            case "ORDER_DELIVERED" -> notificationService.sendDeliveryNotification(event);
            default -> log.warn("Unknown order event type: {}", event.getEventType());
        }
    }

    @KafkaListener(
            topics = "payment-events",
            groupId = "notification-service-group",
            containerFactory = "paymentEventListenerFactory"
    )
    public void consumePaymentEvent(PaymentEvent event) {
        log.info("Received payment event: orderId={}, status={}", event.getOrderId(), event.getStatus());

        switch (event.getStatus()) {
            case "COMPLETED" -> notificationService.sendPaymentSuccessNotification(event);
            case "FAILED" -> notificationService.sendPaymentFailureNotification(event);
            default -> log.warn("Unknown payment status: {}", event.getStatus());
        }
    }
}
