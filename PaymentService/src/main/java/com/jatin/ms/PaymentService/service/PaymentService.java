package com.jatin.ms.PaymentService.service;

import com.jatin.ms.PaymentService.model.PaymentRequest;
import com.jatin.ms.PaymentService.model.PaymentResponse;

public interface PaymentService {
    Long doPayment(PaymentRequest paymentRequest);

    PaymentResponse getPaymentDetailsByOrderId(String orderId);
}
