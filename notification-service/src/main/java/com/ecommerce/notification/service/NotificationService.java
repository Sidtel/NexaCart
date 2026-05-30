package com.ecommerce.notification.service;

import com.ecommerce.notification.kafka.OrderEvent;
import com.ecommerce.notification.kafka.PaymentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Notification service that simulates sending emails and SMS.
 * In production, this would integrate with SendGrid, Twilio, AWS SES, etc.
 */
@Slf4j
@Service
public class NotificationService {

    public void sendOrderConfirmation(OrderEvent event) {
        log.info("=== SENDING ORDER CONFIRMATION ===");
        log.info("To: User {}", event.getUserId());
        log.info("Subject: Order Confirmed - #{}", event.getOrderId());
        log.info("Body: Your order #{} has been placed successfully. Total: ${}",
                event.getOrderId(), event.getTotalAmount());
        log.info("Items: {}", event.getItems().size());

        // Simulate email sending
        simulateEmailSend(event.getUserId(), "Order Confirmation",
                "Your order #" + event.getOrderId() + " has been placed.");

        // Simulate SMS sending
        simulateSmsSend(event.getUserId(),
                "Order #" + event.getOrderId() + " confirmed. Total: $" + event.getTotalAmount());
    }

    public void sendShippingNotification(OrderEvent event) {
        log.info("=== SENDING SHIPPING NOTIFICATION ===");
        log.info("To: User {}", event.getUserId());
        log.info("Subject: Order Shipped - #{}", event.getOrderId());

        simulateEmailSend(event.getUserId(), "Order Shipped",
                "Your order #" + event.getOrderId() + " has been shipped.");
        simulateSmsSend(event.getUserId(),
                "Order #" + event.getOrderId() + " shipped. Track your delivery.");
    }

    public void sendDeliveryNotification(OrderEvent event) {
        log.info("=== SENDING DELIVERY NOTIFICATION ===");
        log.info("To: User {}", event.getUserId());
        log.info("Subject: Order Delivered - #{}", event.getOrderId());

        simulateEmailSend(event.getUserId(), "Order Delivered",
                "Your order #" + event.getOrderId() + " has been delivered.");
    }

    public void sendPaymentSuccessNotification(PaymentEvent event) {
        log.info("=== SENDING PAYMENT SUCCESS NOTIFICATION ===");
        log.info("To: User {}", event.getUserId());
        log.info("Subject: Payment Successful for Order #{}", event.getOrderId());
        log.info("Amount: ${}, Transaction: {}", event.getAmount(), event.getTransactionId());

        simulateEmailSend(event.getUserId(), "Payment Successful",
                "Payment of $" + event.getAmount() + " for order #" + event.getOrderId() +
                " was successful. Transaction ID: " + event.getTransactionId());
    }

    public void sendPaymentFailureNotification(PaymentEvent event) {
        log.info("=== SENDING PAYMENT FAILURE NOTIFICATION ===");
        log.info("To: User {}", event.getUserId());
        log.info("Subject: Payment Failed for Order #{}", event.getOrderId());

        simulateEmailSend(event.getUserId(), "Payment Failed",
                "Payment for order #" + event.getOrderId() + " failed. Please retry.");
        simulateSmsSend(event.getUserId(),
                "Payment failed for order #" + event.getOrderId() + ". Please update payment method.");
    }

    private void simulateEmailSend(String userId, String subject, String body) {
        log.info("[EMAIL] To: {} | Subject: {} | Body: {}", userId, subject, body);
        // In production: integrate with SendGrid, AWS SES, or SMTP
    }

    private void simulateSmsSend(String userId, String message) {
        log.info("[SMS] To: {} | Message: {}", userId, message);
        // In production: integrate with Twilio, AWS SNS, or similar
    }
}
