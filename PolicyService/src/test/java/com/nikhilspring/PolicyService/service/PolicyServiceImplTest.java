package com.nikhilspring.PolicyService.service;

import com.nikhilspring.PolicyService.entity.Policy;
import com.nikhilspring.PolicyService.exception.CustomException;
import com.nikhilspring.PolicyService.external.client.PaymentService;
import com.nikhilspring.PolicyService.external.client.ProductService;
import com.nikhilspring.PolicyService.external.request.PaymentRequest;
import com.nikhilspring.PolicyService.external.response.PaymentResponse;
import com.nikhilspring.PolicyService.external.response.ProductResponse;
import com.nikhilspring.PolicyService.model.PolicyRequest;
import com.nikhilspring.PolicyService.model.PaymentMode;
import com.nikhilspring.PolicyService.model.PolicyResponse;
import com.nikhilspring.PolicyService.repository.PolicyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
public class PolicyServiceImplTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private ProductService productService;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    PolicyService policyService = new PolicyServiceImpl();

    @DisplayName("Get Policy - Success Scenario")
    @Test
    void test_When_Policy_Success() {
        // Mocking
        Policy policy = getMockPolicy();
        when(policyRepository.findById(anyLong()))
                .thenReturn(Optional.of(policy));

        when(productService.getProductById(anyLong()))
                .thenReturn(new ResponseEntity<>(getMockProductResponse(), HttpStatus.OK));

        // Actual method calling
        PolicyResponse policyResponse = policyService.getPolicyDetails(1);

        // Verification
        verify(policyRepository, times(1)).findById(anyLong());
        verify(productService, times(1)).getProductById(anyLong());

        // Assert
        assertNotNull(policyResponse);
        assertEquals(policy.getId(), policyResponse.getPolicyId());
    }

    @DisplayName("Get Policy - Failure Scenario")
    @Test
    void test_When_Get_Policy_NOT_FOUND_then_Not_Found() {
        when(policyRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(null));

        CustomException exception =
                assertThrows(CustomException.class,
                        () -> policyService.getPolicyDetails(1));

        assertEquals("NOT_FOUND", exception.getErrorCode());
        assertEquals(404, exception.getStatus());

        verify(policyRepository, times(1))
                .findById(anyLong());
    }

    @DisplayName("Issue Policy - Success Scenario")
    @Test
    void test_When_Issue_Policy_Success() {
        Policy policy = getMockPolicy();
        PolicyRequest policyRequest = getMockPolicyRequest();

        when(policyRepository.save(any(Policy.class)))
                .thenReturn(policy);
        when(productService.validateProduct(anyLong()))
                .thenReturn(new ResponseEntity<Void>(HttpStatus.OK));
        when(productService.getProductById(anyLong()))
                .thenReturn(new ResponseEntity<>(getMockProductResponse(), HttpStatus.OK));
        when(paymentService.processPayment(any(PaymentRequest.class)))
                .thenReturn(new ResponseEntity<Long>(1L, HttpStatus.OK));

        long policyId = policyService.issuePolicy(policyRequest);

        verify(policyRepository, times(1))
                .save(any());
        verify(productService, times(1))
                .validateProduct(anyLong());
        verify(productService, times(1))
                .getProductById(anyLong());

        assertEquals(policy.getId(), policyId);
    }

    @DisplayName("Issue Policy - Payment Failed Scenario")
    @Test
    void test_when_Issue_Policy_Payment_Fails_then_Policy_Pending() {
        Policy policy = getMockPolicy();
        PolicyRequest policyRequest = getMockPolicyRequest();

        when(policyRepository.save(any(Policy.class)))
                .thenReturn(policy);
        when(productService.validateProduct(anyLong()))
                .thenReturn(new ResponseEntity<Void>(HttpStatus.OK));
        when(productService.getProductById(anyLong()))
                .thenReturn(new ResponseEntity<>(getMockProductResponse(), HttpStatus.OK));
        when(paymentService.processPayment(any(PaymentRequest.class)))
                .thenThrow(new RuntimeException());

        long policyId = policyService.issuePolicy(policyRequest);

        verify(policyRepository, times(1))
                .save(any());
        verify(productService, times(1))
                .validateProduct(anyLong());
        verify(productService, times(1))
                .getProductById(anyLong());

        assertEquals(policy.getId(), policyId);
    }

    private PolicyRequest getMockPolicyRequest() {
        return PolicyRequest.builder()
                .customerId(1)
                .productId(1)
                .paymentMode(PaymentMode.CASH)
                .premiumAmount(100)
                .coverageAmount(10000)
                .policyStartDate(Instant.now().plusSeconds(86400)) // Future date
                .policyEndDate(Instant.now().plusSeconds(31536000))
                .build();
    }

    private Policy getMockPolicy() {
        return Policy.builder()
                .policyStatus("ACTIVE")
                .policyStartDate(Instant.now())
                .id(1)
                .premiumAmount(100)
                .coverageAmount(10000)
                .productId(2)
                .customerId(1)
                .policyNumber("POL-123456")
                .createdDate(Instant.now())
                .updatedDate(Instant.now())
                .build();
    }

    private ProductResponse getMockProductResponse() {
        return ProductResponse.builder()
                .productId(2)
                .productName("Health Product")
                .productType("Health")
                .coverageType("Comprehensive")
                .minPremium(100)
                .maxCoverage(10000)
                .description("Test product")
                .isActive(true)
                .build();
    }

    private PaymentResponse getMockPaymentResponse() {
        return PaymentResponse.builder()
                .paymentId(1)
                .paymentDate(Instant.now())
                .paymentMode(PaymentMode.CASH)
                .amount(200)
                .policyId(1)
                .status("SUCCESS")
                .paymentType("PREMIUM")
                .referenceNumber("REF-123456")
                .build();
    }
}