package com.nikhilspring.OrderService;

import com.nikhilspring.OrderService.external.intercept.RestTemplateInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@SpringBootApplication
@EnableFeignClients
public class OrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderServiceApplication.class, args);
	}


	@Bean
	@LoadBalanced
	public RestTemplate restTemplate(RestTemplateInterceptor restTemplateInterceptor) {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setInterceptors(List.of(restTemplateInterceptor));
		return restTemplate;
	}

}