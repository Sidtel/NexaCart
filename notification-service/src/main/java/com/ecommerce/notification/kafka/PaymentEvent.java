package com.ecommerce.notification.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
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
