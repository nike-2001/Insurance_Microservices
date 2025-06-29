package com.nikhilspring.PolicyService;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class PolicyServiceConfig {

    @Bean
    public ServiceInstanceListSupplier supplier() {
        return new TestServiceInstanceListSupplier();
    }
}

