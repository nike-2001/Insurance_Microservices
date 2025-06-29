package com.nikhilspring.ProductService.validation;

import com.nikhilspring.ProductService.exception.ProductServiceCustomException;
import com.nikhilspring.ProductService.model.ProductRequest;

public class ProductValidationUtil {

    public static void validateProductRequest(ProductRequest productRequest) {
        if (productRequest == null) {
            throw new ProductServiceCustomException(
                "Product request cannot be null",
                "INVALID_REQUEST"
            );
        }

        if (productRequest.getProductName() == null || productRequest.getProductName().trim().isEmpty()) {
            throw new ProductServiceCustomException(
                "Product name cannot be null or empty",
                "INVALID_PRODUCT_NAME"
            );
        }

        if (productRequest.getProductType() == null || productRequest.getProductType().trim().isEmpty()) {
            throw new ProductServiceCustomException(
                "Product type cannot be null or empty",
                "INVALID_PRODUCT_TYPE"
            );
        }

        if (productRequest.getCoverageType() == null || productRequest.getCoverageType().trim().isEmpty()) {
            throw new ProductServiceCustomException(
                "Coverage type cannot be null or empty",
                "INVALID_COVERAGE_TYPE"
            );
        }

        if (productRequest.getMinPremium() <= 0) {
            throw new ProductServiceCustomException(
                "Minimum premium must be greater than 0",
                "INVALID_MIN_PREMIUM"
            );
        }

        if (productRequest.getMaxCoverage() <= 0) {
            throw new ProductServiceCustomException(
                "Maximum coverage must be greater than 0",
                "INVALID_MAX_COVERAGE"
            );
        }

        if (productRequest.getDescription() == null || productRequest.getDescription().trim().isEmpty()) {
            throw new ProductServiceCustomException(
                "Product description cannot be null or empty",
                "INVALID_DESCRIPTION"
            );
        }
    }

    public static void validateProductId(long productId) {
        if (productId <= 0) {
            throw new ProductServiceCustomException(
                "Invalid product ID: " + productId,
                "INVALID_PRODUCT_ID"
            );
        }
    }

    public static void validateProductExists(boolean exists, long productId) {
        if (!exists) {
            throw new ProductServiceCustomException(
                "Product not found with ID: " + productId,
                "PRODUCT_NOT_FOUND"
            );
        }
    }
} 