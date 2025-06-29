package com.nikhilspring.PolicyService.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Table(name = "PRODUCTS")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long productId;

    @Column(name = "PRODUCT_NAME")
    private String productName;

    @Column(name = "PRODUCT_TYPE")
    private String productType;

    @Column(name = "COVERAGE_TYPE")
    private String coverageType;

    @Column(name = "MIN_PREMIUM")
    private long minPremium;

    @Column(name = "MAX_COVERAGE")
    private long maxCoverage;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean active = true;

    // Helper method to check if product is active
    public boolean isActive() {
        return active != null ? active : true;
    }

    // Helper method to set active status
    public void setActive(boolean active) {
        this.active = active;
    }
} 