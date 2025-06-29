package com.nikhilspring.PolicyService.repository;

import com.nikhilspring.PolicyService.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.productId = :productId AND p.active = true")
    Optional<Product> findActiveProductById(@Param("productId") Long productId);

    @Query("SELECT p FROM Product p WHERE p.active = true")
    List<Product> findAllActiveProducts();

    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.productId = :productId AND p.active = true")
    boolean existsByProductIdAndIsActiveTrue(@Param("productId") Long productId);
} 