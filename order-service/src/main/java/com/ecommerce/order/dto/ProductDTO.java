package com.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO representing product data received from Product Service.
 * Used for service-to-service communication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private String id;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private int stock;
    private String imageUrl;
    private boolean active;
}
