package com.jatin.ms.OrderService.service;

import com.jatin.ms.OrderService.entity.Order;
import com.jatin.ms.OrderService.exception.CustomException;
import com.jatin.ms.OrderService.external.client.PaymentService;
import com.jatin.ms.OrderService.external.client.ProductService;
import com.jatin.ms.OrderService.external.request.PaymentRequest;
import com.jatin.ms.OrderService.external.response.PaymentResponse;
import com.jatin.ms.OrderService.model.OrderRequest;
import com.jatin.ms.OrderService.model.OrderResponse;
import com.jatin.ms.OrderService.repository.OrderRepository;
//import com.jatin.ms.OrderService.external.response.ProductResponse;
import com.jatin.ms.ProductService.model.ProductResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public long placeOrder(OrderRequest orderRequest) {

        //Order Entity-> Save the data with status order created
        //Call the Product Service to block the products (reduce the quantity)
        //Payment service to complete the payment and if payment is successful need to mark the order as complete
        //else cancelled

        log.info("Placing Order Request: {}", orderRequest);

        productService.reduceQuantity(orderRequest.getProductId(), orderRequest.getQuantity());
        log.info("Creating Order with Status CREATED");
        Order order = Order.builder()
                .amount(orderRequest.getTotalAmount())
                .orderStatus("CREATED")
                .productId(orderRequest.getProductId())
                .orderDate(Instant.now())
                .quantity(orderRequest.getQuantity())
                .build();

        order = orderRepository.save(order);

        log.info("Calling payment service to complete the payment");
        PaymentRequest paymentRequest=PaymentRequest.builder()
                .orderId(order.getId())
                .paymentMode(orderRequest.getPaymentMode())
                .amount(orderRequest.getTotalAmount())
                .build();

        String orderStatus= null;
        try {
            paymentService.doPayment(paymentRequest);
            log.info("Payment done successfully. Changing the order status to PLACED");
            orderStatus="Placed";
        } catch (Exception e){
            log.error("Error occurred in payment. Changing order status to PAYMENT_FAILED");
            orderStatus="PAYMENT_FAILED";
        }

        order.setOrderStatus(orderStatus);
        orderRepository.save(order);

        log.info("Order Placed successfully with order Id: {}", order.getId());
        return order.getId();
    }

    @Override
    public OrderResponse getOrderDetails(long orderId) {
        log.info("Get Order details for Order Id:{}", orderId);

        Order order= orderRepository.findById(orderId)
                .orElseThrow(()-> new CustomException("Order not found for the order id:" + orderId,
                        "NOT_FOUND", 404));

        log.info("Invoking Product Service to fetch the product for id: {} ", order.getProductId());
        log.info("Invoking Product Service to fetch the product for id: {} ", order.getProductId());
        ProductResponse productResponse=restTemplate.getForObject("http://PRODUCT-SERVICE/product/" + order.getProductId(),ProductResponse.class);

        log.info("Getting payment information from the payment service");
        PaymentResponse paymentResponse=restTemplate.getForObject("http://PAYMENT-SERVICE/payment/order/"+ order.getId(),PaymentResponse.class);


        OrderResponse.ProductDetails productDetails= OrderResponse.ProductDetails.builder()
                .productName(productResponse.getProductName())
                .productId(productResponse.getProductId())
                .build();

        OrderResponse.PaymentDetails paymentDetails=OrderResponse.PaymentDetails.builder()
                .paymentId(paymentResponse.getPaymentId())
                .paymentMode(paymentResponse.getPaymentMode())
                .paymentDate(paymentResponse.getPaymentDate())
                .paymentStatus(paymentResponse.getStatus())
                .build();

        OrderResponse orderResponse= OrderResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getOrderStatus())
                .orderDate(order.getOrderDate())
                .amount(order.getAmount())
                .productDetails(productDetails)
                .paymentDetails(paymentDetails)
                .build();


        return orderResponse;
    }
}

