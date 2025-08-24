package ru.yandex.practicum.mapper;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.model.PaymentEntity;

@Slf4j
public class PaymentMapper {
    public static PaymentEntity mapToPaymentEntity(OrderDto orderDto) {
        return PaymentEntity.builder()
                .orderId(orderDto.getOrderId())
                .totalPrice(orderDto.getTotalPrice())
                .deliveryPrice(orderDto.getDeliveryPrice())
                .productPrice(orderDto.getProductPrice())
                .build();
    }

    public static PaymentDto mapToPaymentDto(PaymentEntity entity) {
        return PaymentDto.builder()
                .paymentId(entity.getPaymentId())
                .totalPayment(entity.getTotalPrice())
                .deliveryTotal(entity.getDeliveryPrice())
                .feeTotal(entity.getTotalPrice() - entity.getDeliveryPrice() - entity.getProductPrice())
                .build();
    }
}