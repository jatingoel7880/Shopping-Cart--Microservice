package com.jatin.ms.OrderService.service;

import com.jatin.ms.OrderService.model.OrderRequest;
import com.jatin.ms.OrderService.model.OrderResponse;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);

    OrderResponse getOrderDetails(long orderId);
}
