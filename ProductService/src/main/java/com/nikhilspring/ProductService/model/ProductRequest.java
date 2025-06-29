package com.nikhilspring.ProductService.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductRequest {
    
    @NotBlank(message = "Product name cannot be null or empty")
    private String productName;
    
    @NotBlank(message = "Product type cannot be null or empty")
    private String productType;
    
    @NotBlank(message = "Coverage type cannot be null or empty")
    private String coverageType;
    
    @NotNull(message = "Minimum premium cannot be null")
    @Positive(message = "Minimum premium must be greater than 0")
    private Long minPremium;
    
    @NotNull(message = "Maximum coverage cannot be null")
    @Positive(message = "Maximum coverage must be greater than 0")
    private Long maxCoverage;
    
    @NotBlank(message = "Description cannot be null or empty")
    private String description;
    
    @Builder.Default
    private boolean isActive = true; // Default to true
    
    // Custom getter to avoid Lombok issues with boolean fields starting with 'is'
    public boolean isActive() {
        return isActive;
    }
    
    // Custom setter
    public void setActive(boolean active) {
        this.isActive = active;
    }
}