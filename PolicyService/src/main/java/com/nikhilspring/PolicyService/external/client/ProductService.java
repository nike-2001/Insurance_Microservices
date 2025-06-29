package com.nikhilspring.PolicyService.external.client;

import com.nikhilspring.PolicyService.exception.CustomException;
import com.nikhilspring.PolicyService.external.response.ProductResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "PRODUCT-SERVICE/product")
public interface ProductService {

    @CircuitBreaker(name = "external", fallbackMethod = "fallback")
    @PostMapping("/validate/{id}")
    ResponseEntity<Void> validateProduct(
            @PathVariable("id") long productId
    );

    @CircuitBreaker(name = "external", fallbackMethod = "fallback")
    @GetMapping("/{id}")
    ResponseEntity<ProductResponse> getProductById(
            @PathVariable("id") long productId
    );

    default Object fallback(long productId, Exception e) {
        throw new CustomException("Product Service is not available",
                "UNAVAILABLE",
                500);
    }
}
