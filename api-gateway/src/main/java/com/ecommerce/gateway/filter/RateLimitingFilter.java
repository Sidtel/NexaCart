package com.ecommerce.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory rate limiting filter.
 * In production, use Redis-based rate limiting (e.g., Spring Cloud Gateway Redis RateLimiter).
 */
@Slf4j
@Component
public class RateLimitingFilter implements GlobalFilter, Ordered {

    private static final int MAX_REQUESTS_PER_SECOND = 100;
    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientIp = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";

        AtomicInteger count = requestCounts.computeIfAbsent(clientIp, k -> new AtomicInteger(0));

        if (count.incrementAndGet() > MAX_REQUESTS_PER_SECOND) {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        }

        // Reset counter periodically (simplified - in production use sliding window)
        if (count.get() == 1) {
            Mono.delay(java.time.Duration.ofSeconds(1))
                    .subscribe(l -> requestCounts.remove(clientIp));
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1; // Execute before other filters
    }
}
