package com.nikhilspring.PolicyService.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import com.nikhilspring.PolicyService.PolicyServiceConfig;
import com.nikhilspring.PolicyService.entity.Policy;
import com.nikhilspring.PolicyService.entity.Product;
import com.nikhilspring.PolicyService.model.PolicyRequest;
import com.nikhilspring.PolicyService.model.PolicyResponse;
import com.nikhilspring.PolicyService.model.PaymentMode;
import com.nikhilspring.PolicyService.repository.PolicyRepository;
import com.nikhilspring.PolicyService.repository.ProductRepository;
import com.nikhilspring.PolicyService.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.nio.charset.Charset.defaultCharset;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.util.StreamUtils.copyToString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest({"server.port=0"})
@EnableConfigurationProperties
@AutoConfigureMockMvc
@ContextConfiguration(classes = {PolicyServiceConfig.class})
public class PolicyControllerTest {

    @Autowired
    private PolicyService policyService;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @RegisterExtension
    static WireMockExtension wireMockServer
            = WireMockExtension.newInstance()
            .options(WireMockConfiguration
                    .wireMockConfig()
                    .port(8089))
            .build();

    private ObjectMapper objectMapper
            = new ObjectMapper()
            .findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private Long createdPolicyId;
    private Long createdProductId;

    @BeforeEach
    void setup() throws IOException {
        // Clear existing data
        policyRepository.deleteAll();
        productRepository.deleteAll();
        
        // Create test data
        createTestProduct();
        createTestPolicy();
        
        getProductDetailsResponse();
        processPayment();
        getPaymentDetails();
        validateProduct();
    }

    private void createTestProduct() {
        Product testProduct = Product.builder()
                .productName("Test Insurance")
                .productType("LIFE")
                .coverageType("COMPREHENSIVE")
                .minPremium(100)
                .maxCoverage(50000)
                .description("Test insurance product")
                .active(true)
                .build();
        Product savedProduct = productRepository.save(testProduct);
        createdProductId = savedProduct.getProductId();
    }

    private void createTestPolicy() {
        Policy testPolicy = Policy.builder()
                .policyNumber("POL001")
                .customerId(1)
                .productId(createdProductId)
                .policyStatus("ACTIVE")
                .premiumAmount(200)
                .coverageAmount(10000)
                .policyStartDate(Instant.now().plusSeconds(86400))
                .policyEndDate(Instant.now().plusSeconds(31536000))
                .createdDate(Instant.now())
                .updatedDate(Instant.now())
                .build();
        Policy savedPolicy = policyRepository.save(testPolicy);
        createdPolicyId = savedPolicy.getId();
    }

    private void validateProduct() {
        circuitBreakerRegistry.circuitBreaker("external").reset();
        wireMockServer.stubFor(post(urlMatching("/product/validate/.*"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)));
    }

    private void getPaymentDetails() throws IOException {
        circuitBreakerRegistry.circuitBreaker("external").reset();
        wireMockServer.stubFor(get(urlMatching("/payment/.*"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(
                                copyToString(
                                        PolicyControllerTest.class
                                                .getClassLoader()
                                                .getResourceAsStream("mock/GetPayment.json"),
                                        defaultCharset()
                                )
                        )));
    }

    private void processPayment() {
        wireMockServer.stubFor(post(urlEqualTo("/payment"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)));
    }

    private void getProductDetailsResponse() throws IOException {
        // GET /product/{productId} - use the created product ID
        wireMockServer.stubFor(get(urlMatching("/product/" + createdProductId))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(copyToString(
                                PolicyControllerTest.class
                                        .getClassLoader()
                                        .getResourceAsStream("mock/GetProduct.json"),
                                defaultCharset()
                        ))));
    }

    private PolicyRequest getMockPolicyRequest() {
        return PolicyRequest.builder()
                .customerId(1)
                .productId(createdProductId)
                .paymentMode(PaymentMode.CASH)
                .premiumAmount(200)
                .coverageAmount(10000)
                .policyStartDate(Instant.now().plusSeconds(86400)) // Future date
                .policyEndDate(Instant.now().plusSeconds(31536000)) // 1 year
                .build();
    }

    @Test
    public void test_WhenIssuePolicy_ProcessPayment_Success() throws Exception {
        //First Issue Policy
        // Get Policy by Policy Id from Db and check
        //Check Output

        PolicyRequest policyRequest = getMockPolicyRequest();
        MvcResult mvcResult
                = mockMvc.perform(MockMvcRequestBuilders.post("/policy/issue")
                        .with(user("customer").roles("Customer"))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(policyRequest))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String policyId = mvcResult.getResponse().getContentAsString();

        Optional<Policy> policy = policyRepository.findById(Long.valueOf(policyId));
        assertTrue(policy.isPresent());

        Policy p = policy.get();
        assertEquals(Long.parseLong(policyId), p.getId());
        assertEquals("ACTIVE", p.getPolicyStatus());
        assertEquals(policyRequest.getPremiumAmount(), p.getPremiumAmount());
        assertEquals(policyRequest.getCoverageAmount(), p.getCoverageAmount());
    }

    @Test
    public void test_WhenIssuePolicyWithWrongAccess_thenThrow403() throws Exception {
        PolicyRequest policyRequest = getMockPolicyRequest();
        MvcResult mvcResult
                = mockMvc.perform(MockMvcRequestBuilders.post("/policy/issue")
                        .with(user("admin").roles("Admin"))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(policyRequest))
                ).andExpect(MockMvcResultMatchers.status().isForbidden())
                .andReturn();
    }

    @Test
    public void test_WhenGetPolicy_Success() throws Exception {
        MvcResult mvcResult
                = mockMvc.perform(MockMvcRequestBuilders.get("/policy/" + createdPolicyId)
                        .with(user("admin").roles("Admin"))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String actualResponse = mvcResult.getResponse().getContentAsString();
        Policy policy = policyRepository.findById(createdPolicyId).get();
        String expectedResponse = getPolicyResponse(policy);

        assertEquals(expectedResponse,actualResponse);
    }

    @Test
    public void testWhen_GetPolicy_Policy_Not_Found() throws Exception {
        MvcResult mvcResult
                = mockMvc.perform(MockMvcRequestBuilders.get("/policy/999")
                        .with(user("admin").roles("Admin"))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();
    }

    private String getPolicyResponse(Policy policy) throws IOException {
        // Get the actual product from the database
        Product product = productRepository.findById(policy.getProductId()).orElse(null);
        
        PolicyResponse.ProductDetails productDetails = PolicyResponse.ProductDetails.builder()
                .productId(policy.getProductId())
                .productName(product != null ? product.getProductName() : "Unknown Product")
                .productType(product != null ? product.getProductType() : "UNKNOWN")
                .coverageType(product != null ? product.getCoverageType() : "UNKNOWN")
                .build();
        
        PolicyResponse policyResponse = PolicyResponse.builder()
                .policyId(policy.getId())
                .policyNumber(policy.getPolicyNumber())
                .policyStatus(policy.getPolicyStatus())
                .premiumAmount(policy.getPremiumAmount())
                .coverageAmount(policy.getCoverageAmount())
                .policyStartDate(policy.getPolicyStartDate())
                .policyEndDate(policy.getPolicyEndDate())
                .productDetails(productDetails)
                .build();
        
        return objectMapper.writeValueAsString(policyResponse);
    }
}