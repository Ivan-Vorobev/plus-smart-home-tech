package ru.yandex.practicum.service;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.api.delivery.DeliveryApi;

@FeignClient(name = "delivery")
public interface DeliveryFeignClient extends DeliveryApi {
}
