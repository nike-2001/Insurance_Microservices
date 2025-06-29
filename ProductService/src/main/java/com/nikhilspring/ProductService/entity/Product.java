package com.nikhilspring.ProductService.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Table(name = "PRODUCTS")
@Data // Generates all the getter setter methods
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // How to generate (increment) the primary key values
    private long productId;

    @Column(name = "PRODUCT_NAME")
    private String productName;

    @Column(name = "PRODUCT_TYPE")
    private String productType;

    @Column(name = "COVERAGE_TYPE")
    private String coverageType;

    @Column(name = "MIN_PREMIUM")
    private Long minPremium;

    @Column(name = "MAX_COVERAGE")
    private Long maxCoverage;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "IS_ACTIVE", columnDefinition = "TINYINT(1) DEFAULT 1")
    private boolean isActive;

    // Custom getter to avoid Lombok issues with boolean fields starting with 'is'
    public boolean isActive() {
        return isActive;
    }

    // Custom setter
    public void setActive(boolean active) {
        this.isActive = active;
    }
}
