package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.delivery.DeliveryApi;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.service.DeliveryService;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DeliveryController implements DeliveryApi {
    private final DeliveryService deliveryService;

    @Override
    public DeliveryDto createDelivery(DeliveryDto deliveryDto) {
        return deliveryService.createDelivery(deliveryDto);
    }

    @Override
    public Double calculateDelivery(OrderDto orderDto) {
        return deliveryService.calculateDelivery(orderDto);
    }

    @Override
    public void setDeliverySuccessful(UUID deliveryId) {
        deliveryService.setDeliverySuccessful(deliveryId);
    }

    @Override
    public void setDeliveryFailed(UUID deliveryId) {
        deliveryService.setDeliveryFailed(deliveryId);
    }

    @Override
    public void pickOrderForDelivery(UUID deliveryId) {
        deliveryService.pickOrderForDelivery(deliveryId);
    }
}
