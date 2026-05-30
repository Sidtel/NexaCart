package com.ecommerce.order.service;

import com.ecommerce.order.dto.*;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.exception.OrderProcessingException;
import com.ecommerce.order.exception.ResourceNotFoundException;
import com.ecommerce.order.kafka.OrderEvent;
import com.ecommerce.order.kafka.OrderEventProducer;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Core order processing service.
 * Orchestrates the order flow: validation → creation → event publishing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;
    private final OrderEventProducer orderEventProducer;

    public OrderResponse placeOrder(OrderRequest request) {
        log.info("Processing order for user: {}", request.getUserId());

        // Step 1: Validate product availability and build order items
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            // Validate availability via Product Service REST call
            boolean available = productServiceClient.checkAvailability(
                    itemRequest.getProductId(), itemRequest.getQuantity());

            if (!available) {
                throw new OrderProcessingException(
                        "Product unavailable or insufficient stock: " + itemRequest.getProductId());
            }

            // Fetch product details for price calculation
            ProductDTO product = productServiceClient.getProduct(itemRequest.getProductId());

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .subtotal(subtotal)
                    .build();

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(subtotal);
        }

        // Step 2: Create and persist the order
        Order order = Order.builder()
                .userId(request.getUserId())
                .items(orderItems)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("Order created with ID: {}", savedOrder.getId());

        // Step 3: Reduce inventory
        for (OrderItemRequest itemRequest : request.getItems()) {
            productServiceClient.reduceStock(itemRequest.getProductId(), itemRequest.getQuantity());
        }

        // Step 4: Publish Kafka event for downstream services
        OrderEvent event = OrderEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(savedOrder.getId())
                .userId(savedOrder.getUserId())
                .items(savedOrder.getItems())
                .totalAmount(savedOrder.getTotalAmount())
                .shippingAddress(savedOrder.getShippingAddress())
                .eventType("ORDER_PLACED")
                .timestamp(LocalDateTime.now())
                .build();

        orderEventProducer.publishOrderEvent(event);

        return mapToResponse(savedOrder);
    }

    public OrderResponse getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        return mapToResponse(order);
    }

    public List<OrderResponse> getOrdersByUserId(String userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orders.stream().map(this::mapToResponse).toList();
    }

    public void updateOrderStatus(String orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        order.setStatus(status);
        orderRepository.save(order);
        log.info("Order {} status updated to: {}", orderId, status);
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .items(order.getItems())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .paymentId(order.getPaymentId())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
