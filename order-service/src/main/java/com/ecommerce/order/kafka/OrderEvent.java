package com.ecommerce.order.kafka;

import com.ecommerce.order.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Kafka event published when an order is placed.
 * Consumed by Payment Service and Notification Service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    private String eventId;
    private String orderId;
    private String userId;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String eventType;
    private LocalDateTime timestamp;
}
