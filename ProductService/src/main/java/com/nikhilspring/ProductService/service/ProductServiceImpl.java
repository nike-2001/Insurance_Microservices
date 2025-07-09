package com.nikhilspring.ProductService.service;

import com.nikhilspring.ProductService.entity.Product;
import com.nikhilspring.ProductService.exception.ProductServiceCustomException;
import com.nikhilspring.ProductService.model.ProductRequest;
import com.nikhilspring.ProductService.model.ProductResponse;
import com.nikhilspring.ProductService.repository.ProductRepository;
import com.nikhilspring.ProductService.validation.ProductValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    @CacheEvict(value = {"products", "product-by-id"}, allEntries = true)
    public long addProduct(ProductRequest productRequest) {
        // Validate product request using utility
        ProductValidationUtil.validateProductRequest(productRequest);
        
        // Check if product with same name and type already exists (more specific check)
        Optional<Product> existingProductByType = productRepository.findByProductNameAndType(
            productRequest.getProductName(), 
            productRequest.getProductType()
        );
        if (existingProductByType.isPresent()) {
            Product product = existingProductByType.get();
            throw new ProductServiceCustomException(
                "Product with name '" + productRequest.getProductName() + "' and type '" + 
                productRequest.getProductType() + "' already exists with ID: " + product.getProductId(),
                "DUPLICATE_PRODUCT"
            );
        }
        
        Product product = Product.builder()
                .productName(productRequest.getProductName())
                .productType(productRequest.getProductType())
                .coverageType(productRequest.getCoverageType())
                .minPremium(productRequest.getMinPremium())
                .maxCoverage(productRequest.getMaxCoverage())
                .description(productRequest.getDescription())
                .isActive(productRequest.isActive())
                .build();
        product = productRepository.save(product);
        return product.getProductId();
    }

    @Override
    @Cacheable(value = "product-by-id", key = "#productId")
    public ProductResponse getProductById(long productId) {
        // Validate product ID using utility
        ProductValidationUtil.validateProductId(productId);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductServiceCustomException("Product not found", "NOT_FOUND"));
        return toResponse(product);
    }

    @Override
    @Cacheable(value = "products", key = "'all-products'")
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "products", key = "'type-' + #productType")
    public List<ProductResponse> getProductsByType(String productType) {
        if (productType == null || productType.trim().isEmpty()) {
            throw new ProductServiceCustomException("Product type cannot be null or empty", "INVALID_PRODUCT_TYPE");
        }
        return productRepository.findByProductType(productType).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = {"products", "product-by-id"}, allEntries = true)
    public void updateProduct(long productId, ProductRequest productRequest) {
        // Validate product ID and request using utility
        ProductValidationUtil.validateProductId(productId);
        ProductValidationUtil.validateProductRequest(productRequest);
        
        // Check if product exists
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductServiceCustomException("Product not found", "NOT_FOUND"));
        
        // Update the product fields
        existingProduct.setProductName(productRequest.getProductName());
        existingProduct.setProductType(productRequest.getProductType());
        existingProduct.setCoverageType(productRequest.getCoverageType());
        existingProduct.setMinPremium(productRequest.getMinPremium());
        existingProduct.setMaxCoverage(productRequest.getMaxCoverage());
        existingProduct.setDescription(productRequest.getDescription());
        existingProduct.setActive(productRequest.isActive());
        productRepository.save(existingProduct);
    }

    @Override
    @CacheEvict(value = {"products", "product-by-id"}, allEntries = true)
    public void deleteProduct(long productId) {
        // Validate product ID using utility
        ProductValidationUtil.validateProductId(productId);
        
        // Check if product exists before deleting
        if (!productRepository.existsById(productId)) {
            throw new ProductServiceCustomException("Product not found", "NOT_FOUND");
        }
        
        productRepository.deleteById(productId);
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .productType(product.getProductType())
                .coverageType(product.getCoverageType())
                .minPremium(product.getMinPremium())
                .maxCoverage(product.getMaxCoverage())
                .description(product.getDescription())
                .isActive(product.isActive())
                .build();
    }
}



