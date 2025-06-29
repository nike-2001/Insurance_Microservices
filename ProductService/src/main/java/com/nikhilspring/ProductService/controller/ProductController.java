package com.nikhilspring.ProductService.controller;

import com.nikhilspring.ProductService.model.ProductRequest;
import com.nikhilspring.ProductService.model.ProductResponse;
import com.nikhilspring.ProductService.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "ProductService",
            "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }

    @PostMapping
    public ResponseEntity<Long> addProduct(@Valid @RequestBody ProductRequest productRequest) {
        return new ResponseEntity<>(productService.addProduct(productRequest), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("id") long productId) {
        return new ResponseEntity<>(productService.getProductById(productId), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return new ResponseEntity<>(productService.getAllProducts(), HttpStatus.OK);
    }

    @GetMapping("/type/{productType}")
    public ResponseEntity<List<ProductResponse>> getProductsByType(@PathVariable("productType") String productType) {
        return new ResponseEntity<>(productService.getProductsByType(productType), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateProduct(@PathVariable("id") long productId, @Valid @RequestBody ProductRequest productRequest) {
        productService.updateProduct(productId, productRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") long productId) {
        productService.deleteProduct(productId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/validate/{id}")
    public ResponseEntity<Void> validateProduct(@PathVariable("id") long productId) {
        productService.getProductById(productId); // This will throw exception if product doesn't exist
        return new ResponseEntity<>(HttpStatus.OK);
    }
}