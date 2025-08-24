package ru.yandex.practicum.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.dto.payment.PaymentState;
import ru.yandex.practicum.dto.shoppingStore.ProductDto;
import ru.yandex.practicum.exception.NoOrderFoundException;
import ru.yandex.practicum.exception.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.mapper.PaymentMapper;
import ru.yandex.practicum.model.PaymentEntity;
import ru.yandex.practicum.repository.PaymentRepository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderFeignClient orderFeign;
    private final ShoppingStoreFeignClient storeFeign;

    public PaymentDto createPaymentOrder(OrderDto orderDto) {
        if (orderDto.getTotalPrice() == null || orderDto.getDeliveryPrice() == null || orderDto.getProductPrice() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Insufficient data to pay for the order");
        }
        PaymentEntity paymentEntity = PaymentMapper.mapToPaymentEntity(orderDto);
        Optional<PaymentEntity> oldPaymentEntityOpt = paymentRepository.findByOrderId(orderDto.getOrderId());
        if (oldPaymentEntityOpt.isPresent()) {
            log.info("Old PaymentEntity: {}", oldPaymentEntityOpt.get());
            paymentEntity.setPaymentId(oldPaymentEntityOpt.get().getPaymentId());
        }

        paymentEntity.setPaymentState(PaymentState.PENDING);
        paymentEntity = paymentRepository.save(paymentEntity);
        log.info("Saved payment: {}", paymentEntity);

        return PaymentMapper.mapToPaymentDto(paymentEntity);
    }

    public Double calculateProductCost(OrderDto orderDto) {
        Map<UUID, Integer> products = orderDto.getProducts();
        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("The list of products should not be null or empty");
        }
        double productCost = 0.0;
        for (UUID productId : products.keySet()) {
            ProductDto product;
            try {
                product = storeFeign.findProductById(productId.toString());
                log.info("We find the product in the store: {}", product);
            } catch (FeignException e) {
                throw new NotEnoughInfoInOrderToCalculateException(e.getMessage());
            }
            productCost += product.getPrice() * products.get(productId);
        }
        log.info("The cost of the products in the order: {}", productCost);
        return productCost;
    }

    public Double calculateTotalCost(OrderDto orderDto) {
        if (orderDto.getProductPrice() == null || orderDto.getDeliveryPrice() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("There is not enough data to calculate the full cost of the order");
        }
        Double totalCost = orderDto.getProductPrice() * 1.1 + orderDto.getDeliveryPrice();
        log.info("The total cost of the order: {}", totalCost);
        return totalCost;
    }

    public void setPaymentFailed(UUID paymentId) {
        if (paymentId == null) {
            throw new IllegalArgumentException("The payment ID cannot be null");
        }
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoOrderFoundException("he payment with the Id was not found: " + paymentId));
        log.info("We find the required payment: {}", payment);
        payment.setPaymentState(PaymentState.FAILED);

        try {
            OrderDto dto = orderFeign.setPaymentFailed(payment.getOrderId());
            log.info("Updating order status: {}", dto);
        } catch (FeignException e) {
            throw new NoOrderFoundException(e.getMessage());
        }
        payment = paymentRepository.save(payment);
        log.info("Updating payment status: {}", payment);
    }

    public void payOrder(UUID paymentId) {
        if (paymentId == null) {
            throw new IllegalArgumentException("The payment ID cannot be null");
        }
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoOrderFoundException("The payment with the Id was not found: " + paymentId));
        log.info("Search required payment: {}", payment);
        payment.setPaymentState(PaymentState.SUCCESS);

        try {
            OrderDto dto = orderFeign.payOrder(payment.getOrderId());
            log.info("Updating order status: {}", dto);
        } catch (FeignException e) {
            throw new NoOrderFoundException(e.getMessage());
        }
        payment = paymentRepository.save(payment);
        log.info("Updating payment status: {}", payment);
    }
}