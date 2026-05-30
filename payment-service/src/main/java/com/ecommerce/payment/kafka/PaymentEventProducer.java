package com.ecommerce.payment.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventProducer {

    private static final String TOPIC = "payment-events";

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public void publishPaymentEvent(PaymentEvent event) {
        log.info("Publishing payment event: paymentId={}, status={}", event.getPaymentId(), event.getStatus());

        kafkaTemplate.send(TOPIC, event.getOrderId(), event)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Failed to publish payment event: {}", throwable.getMessage());
                    } else {
                        log.info("Payment event published: partition={}, offset={}",
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
