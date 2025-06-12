package com.nikhilspring.ProductService.service;

import com.nikhilspring.ProductService.model.ProductRequest;
import com.nikhilspring.ProductService.model.ProductResponse;

public interface ProductService {
    long addProduct(ProductRequest productRequest);

    ProductResponse getProductById(long productId);

    void reduceQuantity(long productId, long quantity);
}