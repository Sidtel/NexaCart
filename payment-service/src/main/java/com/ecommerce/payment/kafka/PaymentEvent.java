package com.ecommerce.payment.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event published after payment processing completes.
 * Consumed by Notification Service and Order Service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {

    private String eventId;
    private String paymentId;
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private String status;
    private String transactionId;
    private String eventType;
    private LocalDateTime timestamp;
}
