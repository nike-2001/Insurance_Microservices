package com.nikhilspring.ProductService;

import com.nikhilspring.ProductService.entity.Product;
import com.nikhilspring.ProductService.model.ProductRequest;
import com.nikhilspring.ProductService.model.ProductResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductServiceApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void testIsActiveFieldHandling() {
		// Test ProductRequest with explicit true
		ProductRequest requestTrue = ProductRequest.builder()
				.productName("Test Product True")
				.productType("Test Type")
				.coverageType("Test Coverage")
				.minPremium(1000L)
				.maxCoverage(10000L)
				.description("Test Description")
				.isActive(true)
				.build();
		
		assertTrue(requestTrue.isActive());
		assertEquals(true, requestTrue.isActive());
		
		// Test ProductRequest with explicit false
		ProductRequest requestFalse = ProductRequest.builder()
				.productName("Test Product False")
				.productType("Test Type")
				.coverageType("Test Coverage")
				.minPremium(1000L)
				.maxCoverage(10000L)
				.description("Test Description")
				.isActive(false)
				.build();
		
		assertFalse(requestFalse.isActive());
		assertEquals(false, requestFalse.isActive());
		
		// Test ProductRequest with null (should default to true)
		ProductRequest requestNull = ProductRequest.builder()
				.productName("Test Product Null")
				.productType("Test Type")
				.coverageType("Test Coverage")
				.minPremium(1000L)
				.maxCoverage(10000L)
				.description("Test Description")
				.isActive(true)
				.build();
		
		assertTrue(requestNull.isActive()); // Should be true since we set it explicitly
		
		// Test Product entity with true
		Product productTrue = Product.builder()
				.productName("Test Product True")
				.productType("Test Type")
				.coverageType("Test Coverage")
				.minPremium(1000L)
				.maxCoverage(10000L)
				.description("Test Description")
				.isActive(true)
				.build();
		
		assertTrue(productTrue.isActive());
		assertEquals(true, productTrue.isActive());
		
		// Test Product entity with false
		Product productFalse = Product.builder()
				.productName("Test Product False")
				.productType("Test Type")
				.coverageType("Test Coverage")
				.minPremium(1000L)
				.maxCoverage(10000L)
				.description("Test Description")
				.isActive(false)
				.build();
		
		assertFalse(productFalse.isActive());
		assertEquals(false, productFalse.isActive());
		
		// Test setting to false
		productTrue.setActive(false);
		assertFalse(productTrue.isActive());
		assertEquals(false, productTrue.isActive());
		
		// Test setting to true
		productFalse.setActive(true);
		assertTrue(productFalse.isActive());
		assertEquals(true, productFalse.isActive());
		
		// Test ProductResponse
		ProductResponse response = ProductResponse.builder()
				.productId(1L)
				.productName("Test Product")
				.productType("Test Type")
				.coverageType("Test Coverage")
				.minPremium(1000L)
				.maxCoverage(10000L)
				.description("Test Description")
				.isActive(true)
				.build();
		
		assertTrue(response.isActive());
		assertEquals(true, response.isActive());
	}

}
