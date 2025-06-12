package com.nikhilspring.OrderService.service;

import com.nikhilspring.OrderService.model.OrderRequest;
import com.nikhilspring.OrderService.model.OrderResponse;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);

    OrderResponse getOrderDetails(long orderId);
}