package ru.yandex.practicum.mapper;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Delivery;

@Slf4j
public class DeliveryMapper {
    public static Delivery mapToDelivery(DeliveryDto dto) {
        return Delivery.builder()
                .deliveryId(dto.getDeliveryId())
                .fromAddress(mapToAddress(dto.getFromAddress()))
                .toAddress(mapToAddress(dto.getToAddress()))
                .orderId(dto.getOrderId())
                .deliveryState(dto.getDeliveryState())
                .build();
    }

    public static DeliveryDto mapToDeliveryDto(Delivery delivery) {
        return DeliveryDto.builder()
                .deliveryId(delivery.getDeliveryId())
                .fromAddress(mapToAddressDto(delivery.getFromAddress()))
                .toAddress(mapToAddressDto(delivery.getToAddress()))
                .orderId(delivery.getOrderId())
                .deliveryState(delivery.getDeliveryState())
                .build();
    }

    public static Address mapToAddress(AddressDto dto) {
        return Address.builder()
                .country(dto.getCountry())
                .city(dto.getCity())
                .street(dto.getStreet())
                .house(dto.getHouse())
                .flat(dto.getFlat())
                .build();
    }

    public static AddressDto mapToAddressDto(Address address) {
        return AddressDto.builder()
                .country(address.getCountry())
                .city(address.getCity())
                .street(address.getStreet())
                .house(address.getHouse())
                .flat(address.getFlat())
                .build();
    }
}
