package com.ecommerce.order.service;

import com.ecommerce.order.dto.ProductDTO;
import com.ecommerce.order.exception.ServiceCommunicationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * REST client for communicating with Product Service.
 * Implements circuit-breaker-ready patterns for resilient service-to-service calls.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceClient {

    private final WebClient productServiceClient;

    public ProductDTO getProduct(String productId) {
        log.debug("Fetching product details for ID: {}", productId);
        try {
            return productServiceClient.get()
                    .uri("/products/{id}", productId)
                    .retrieve()
                    .bodyToMono(ProductDTO.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            log.error("Product not found: {}", productId);
            throw new ServiceCommunicationException("Product not found: " + productId);
        } catch (Exception e) {
            log.error("Error communicating with Product Service: {}", e.getMessage());
            throw new ServiceCommunicationException("Failed to communicate with Product Service: " + e.getMessage());
        }
    }

    public boolean checkAvailability(String productId, int quantity) {
        log.debug("Checking availability for product {}: quantity {}", productId, quantity);
        try {
            Boolean available = productServiceClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/products/{id}/availability")
                            .queryParam("quantity", quantity)
                            .build(productId))
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            return Boolean.TRUE.equals(available);
        } catch (Exception e) {
            log.error("Error checking product availability: {}", e.getMessage());
            throw new ServiceCommunicationException("Failed to check product availability: " + e.getMessage());
        }
    }

    public void reduceStock(String productId, int quantity) {
        log.debug("Reducing stock for product {}: quantity {}", productId, quantity);
        try {
            productServiceClient.post()
                    .uri("/products/inventory/reduce")
                    .bodyValue(new InventoryRequest(productId, quantity))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            log.error("Error reducing stock: {}", e.getMessage());
            throw new ServiceCommunicationException("Failed to reduce stock: " + e.getMessage());
        }
    }

    private record InventoryRequest(String productId, int quantity) {}
}
