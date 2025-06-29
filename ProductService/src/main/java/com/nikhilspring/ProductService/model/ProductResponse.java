package com.nikhilspring.ProductService.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponse {
    private long productId;
    private String productName;
    private String productType;
    private String coverageType;
    private Long minPremium;
    private Long maxCoverage;
    private String description;
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