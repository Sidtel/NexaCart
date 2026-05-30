package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.exception.InsufficientStockException;
import com.ecommerce.product.exception.ResourceNotFoundException;
import com.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * Service layer for product catalog operations.
 * Handles CRUD, pagination, filtering, and inventory management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse createProduct(ProductRequest request) {
        log.info("Creating product: {}", request.getName());

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .price(request.getPrice())
                .stock(request.getStock())
                .imageUrl(request.getImageUrl())
                .active(true)
                .build();

        Product saved = productRepository.save(product);
        log.info("Product created with ID: {}", saved.getId());
        return mapToResponse(saved);
    }

    public ProductResponse updateProduct(String id, ProductRequest request) {
        log.info("Updating product: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setImageUrl(request.getImageUrl());

        Product updated = productRepository.save(product);
        log.info("Product updated: {}", id);
        return mapToResponse(updated);
    }

    public void deleteProduct(String id) {
        log.info("Deleting product: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        // Soft delete - mark as inactive
        product.setActive(false);
        productRepository.save(product);
        log.info("Product soft-deleted: {}", id);
    }

    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        return mapToResponse(product);
    }

    public Page<ProductResponse> getAllProducts(int page, int size, String sortBy, String direction,
                                                String category, String search) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> products;

        if (category != null && search != null) {
            products = productRepository.findByCategoryAndNameContainingIgnoreCase(category, search, pageable);
        } else if (category != null) {
            products = productRepository.findByActiveTrueAndCategory(category, pageable);
        } else if (search != null) {
            products = productRepository.findByNameContainingIgnoreCase(search, pageable);
        } else {
            products = productRepository.findByActiveTrue(pageable);
        }

        return products.map(this::mapToResponse);
    }

    public void reduceStock(String productId, int quantity) {
        log.info("Reducing stock for product {}: quantity {}", productId, quantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (product.getStock() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for product: " + product.getName() +
                    ". Available: " + product.getStock() + ", Requested: " + quantity);
        }

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
        log.info("Stock reduced for product {}: new stock = {}", productId, product.getStock());
    }

    public boolean checkAvailability(String productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
        return product.getStock() >= quantity && product.isActive();
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .active(product.isActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
