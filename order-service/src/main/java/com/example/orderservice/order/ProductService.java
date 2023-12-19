package com.example.orderservice.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.security.oauth2.server.resource.web.reactive.function.client.ServletBearerExchangeFilterFunction;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.cache.annotation.Cacheable;
import java.util.Collections;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.web.reactive.function.client.WebClient;

@RefreshScope
@Service
class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final WebClient.Builder webClientBuilder;

    @Value("${order.products-api-url}")
    private String productsApiUrl;

    ProductService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Cacheable("Products")
    public List<Product> fetchProducts() {
        if (productsApiUrl == null || productsApiUrl.isEmpty()) {
            throw new RuntimeException("order.products-api-url not set");
        }

        return  Arrays.stream(
                    Objects.requireNonNull(
                        webClientBuilder.build()
                        .get()
                        .uri(productsApiUrl)
                        .retrieve()
                        .bodyToMono(Product[].class).block()))
                    .toList();
    }
}