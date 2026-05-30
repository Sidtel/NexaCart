package com.ecommerce.payment.service;

import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.entity.PaymentStatus;
import com.ecommerce.payment.kafka.OrderEvent;
import com.ecommerce.payment.kafka.PaymentEvent;
import com.ecommerce.payment.kafka.PaymentEventProducer;
import com.ecommerce.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

/**
 * Payment processing service.
 * Simulates payment gateway interaction with configurable success rate.
 * In production, this would integrate with Stripe, PayPal, or similar providers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;
    private final Random random = new Random();

    // Simulated success rate: 90% success
    private static final double SUCCESS_RATE = 0.9;

    public void processPayment(OrderEvent orderEvent) {
        log.info("Processing payment for order: {}", orderEvent.getOrderId());

        // Check for idempotency - prevent duplicate payment processing
        if (paymentRepository.findByOrderId(orderEvent.getOrderId()).isPresent()) {
            log.warn("Payment already exists for order: {}. Skipping.", orderEvent.getOrderId());
            return;
        }

        // Create payment record
        Payment payment = Payment.builder()
                .orderId(orderEvent.getOrderId())
                .userId(orderEvent.getUserId())
                .amount(orderEvent.getTotalAmount())
                .status(PaymentStatus.PROCESSING)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // Simulate payment processing delay
        simulateProcessingDelay();

        // Simulate payment gateway response
        boolean paymentSuccess = random.nextDouble() < SUCCESS_RATE;

        if (paymentSuccess) {
            savedPayment.setStatus(PaymentStatus.COMPLETED);
            savedPayment.setTransactionId(UUID.randomUUID().toString());
            savedPayment.setProcessedAt(LocalDateTime.now());
            log.info("Payment successful for order: {}", orderEvent.getOrderId());
        } else {
            savedPayment.setStatus(PaymentStatus.FAILED);
            savedPayment.setFailureReason("Payment declined by gateway (simulated)");
            savedPayment.setProcessedAt(LocalDateTime.now());
            log.warn("Payment failed for order: {}", orderEvent.getOrderId());
        }

        paymentRepository.save(savedPayment);

        // Publish payment completion event
        PaymentEvent paymentEvent = PaymentEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .paymentId(savedPayment.getId())
                .orderId(savedPayment.getOrderId())
                .userId(savedPayment.getUserId())
                .amount(savedPayment.getAmount())
                .status(savedPayment.getStatus().name())
                .transactionId(savedPayment.getTransactionId())
                .eventType("PAYMENT_COMPLETED")
                .timestamp(LocalDateTime.now())
                .build();

        paymentEventProducer.publishPaymentEvent(paymentEvent);
    }

    private void simulateProcessingDelay() {
        try {
            Thread.sleep(1000 + random.nextInt(2000)); // 1-3 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
