package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.payment.PaymentApi;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.service.PaymentService;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {
    private final PaymentService paymentService;

    @Override
    public PaymentDto createPaymentOrder(OrderDto orderDto) {
        return paymentService.createPaymentOrder(orderDto);
    }

    @Override
    public Double calculateProductCost(OrderDto orderDto) {
        return paymentService.calculateProductCost(orderDto);
    }

    @Override
    public Double calculateTotalCost(OrderDto orderDto) {
        return paymentService.calculateTotalCost(orderDto);
    }

    @Override
    public void setPaymentFailed(UUID paymentId) {
        paymentService.setPaymentFailed(paymentId);
    }

    @Override
    public void payOrder(UUID paymentId) {
        paymentService.payOrder(paymentId);
    }
}
