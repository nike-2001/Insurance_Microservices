package com.nikhilspring.ProductService.repository;

import com.nikhilspring.ProductService.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Check if product exists by name
    @Query("SELECT p FROM Product p WHERE p.productName = :productName")
    Optional<Product> findByProductName(@Param("productName") String productName);
    
    // Check if product exists by name (case insensitive)
    @Query("SELECT p FROM Product p WHERE LOWER(p.productName) = LOWER(:productName)")
    Optional<Product> findByProductNameIgnoreCase(@Param("productName") String productName);
    
    // Check if product exists by name and type
    @Query("SELECT p FROM Product p WHERE p.productName = :productName AND p.productType = :productType")
    Optional<Product> findByProductNameAndType(@Param("productName") String productName, @Param("productType") String productType);
    
    // Get all products by type
    @Query("SELECT p FROM Product p WHERE p.productType = :productType")
    List<Product> findByProductType(@Param("productType") String productType);
    
    // Check if product exists by ID
    boolean existsByProductId(long productId);
}
