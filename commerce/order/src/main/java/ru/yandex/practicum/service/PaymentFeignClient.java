package ru.yandex.practicum.service;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.api.payment.PaymentApi;

@FeignClient(name = "payment")
public interface PaymentFeignClient extends PaymentApi {
}
