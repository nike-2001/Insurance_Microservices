package com.nikhilspring.ClaimService.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import feign.FeignException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.IOException;

@Configuration
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // Add JWT token to Feign requests if available
            if (SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken) {
                JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
                requestTemplate.header("Authorization", "Bearer " + jwtToken.getToken().getTokenValue());
            }
        };
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            if (response.status() == 404) {
                try {
                    return new FeignException.NotFound(
                        "Resource not found: " + methodKey,
                        response.request(),
                        response.body().asInputStream().readAllBytes(),
                        null
                    );
                } catch (IOException e) {
                    return new FeignException.NotFound(
                        "Resource not found: " + methodKey,
                        response.request(),
                        new byte[0],
                        null
                    );
                }
            }
            return FeignException.errorStatus(methodKey, response);
        };
    }
} 