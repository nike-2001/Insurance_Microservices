package com.nikhilspring.ProductService.service;

import com.nikhilspring.ProductService.model.ProductRequest;
import com.nikhilspring.ProductService.model.ProductResponse;

import java.util.List;

public interface ProductService {
    long addProduct(ProductRequest productRequest);

    ProductResponse getProductById(long productId);

    List<ProductResponse> getAllProducts();

    List<ProductResponse> getProductsByType(String productType);

    void updateProduct(long productId, ProductRequest productRequest);

    void deleteProduct(long productId);
}