package com.jatin.ms.ProductService.service;

import com.jatin.ms.ProductService.model.ProductRequest;
import com.jatin.ms.ProductService.model.ProductResponse;

public interface ProductService {
    long addProduct(ProductRequest productRequest);

    ProductResponse getProductByID(long productId);

    void reduceQuantity(long productId, long quantity);
}
