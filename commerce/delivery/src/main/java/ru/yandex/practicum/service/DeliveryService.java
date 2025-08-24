package ru.yandex.practicum.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.delivery.DeliveryState;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.warehouse.ShippedToDeliveryRequest;
import ru.yandex.practicum.exception.NoDeliveryFoundException;
import ru.yandex.practicum.exception.NoOrderFoundException;
import ru.yandex.practicum.exception.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.mapper.DeliveryMapper;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Delivery;
import ru.yandex.practicum.repository.AddressRepository;
import ru.yandex.practicum.repository.DeliveryRepository;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final AddressRepository addressRepository;
    private final OrderFeignClient orderFeign;
    private final WarehouseFeignClient warehouseFeign;

    private static final Double BASE_COST = 5.0;
    private static final Double WAREHOUSE_ADDRESS_2_SURCHARGE = 2.0;
    private static final Double FRAGILE_SURCHARGE = 0.2;
    private static final Double WEIGHT_SURCHARGE = 0.3;
    private static final Double VOLUME_SURCHARGE = 0.2;
    private static final Double ADDRESS_DELIVERY_SURCHARGE = 0.2;

    public DeliveryDto createDelivery(DeliveryDto deliveryDto) {
        Delivery delivery = DeliveryMapper.mapToDelivery(deliveryDto);
        Optional<Delivery> deliveryOpt = deliveryRepository.findByOrderId(deliveryDto.getOrderId());
        if (deliveryOpt.isPresent()) {
            log.info("Old Delivery: {}", deliveryOpt.get());
            delivery.setDeliveryId(deliveryOpt.get().getDeliveryId());
        } else {
            log.info("There is no delivery for the specified order, we are creating a new one");
            delivery.setDeliveryId(null);
        }

        delivery.setFromAddress(findOrCreateAddress(delivery.getFromAddress()));
        if (checkEqualsAddress(delivery.getFromAddress(), delivery.getToAddress())) {
            delivery.setToAddress(delivery.getFromAddress());
            log.info("The warehouse address and the delivery address match");
        } else {
            delivery.setToAddress(findOrCreateAddress(delivery.getToAddress()));
        }

        delivery.setDeliveryState(DeliveryState.CREATED);
        delivery = deliveryRepository.save(delivery);
        log.info("Saving delivery: {}", delivery);
        return DeliveryMapper.mapToDeliveryDto(delivery);
    }

    public Double calculateDelivery(OrderDto orderDto) {
        if (!validOrder(orderDto)) {
            throw new NotEnoughInfoInOrderToCalculateException("There is not enough data to calculate the shipping cost");
        }
        Delivery delivery = findDeliveryById(orderDto.getDeliveryId());
        Address fromAddress = delivery.getFromAddress();
        Address toAddress = delivery.getToAddress();
        double cost = BASE_COST;

        cost += switch (fromAddress.getCity()) {
            case "ADDRESS_1" -> cost;
            case "ADDRESS_2" -> cost * WAREHOUSE_ADDRESS_2_SURCHARGE;
            default -> 0;
        };

        if (orderDto.getFragile()) {
            cost += cost * FRAGILE_SURCHARGE;
        }

        cost += orderDto.getDeliveryWeight() * WEIGHT_SURCHARGE;
        cost += orderDto.getDeliveryVolume() * VOLUME_SURCHARGE;
        if (!(fromAddress.getCountry().equals(toAddress.getCountry())
                && fromAddress.getCity().equals(toAddress.getCity())
                && fromAddress.getStreet().equals(toAddress.getStreet()))) {
            cost += cost * ADDRESS_DELIVERY_SURCHARGE;
        }
        log.info("Shipping cost: {}", cost);
        return cost;
    }

    public void setDeliverySuccessful(UUID deliveryId) {
        Delivery delivery = findDeliveryById(deliveryId);
        try {
            OrderDto orderDto = orderFeign.deliveryOrder(delivery.getOrderId());
            log.info("Updating the status in the order service: {}", orderDto);
        } catch (FeignException e) {
            log.info("Failure to update the status in the order service: {}", e.getMessage());
            throw new NoOrderFoundException(e.getMessage());
        }
        delivery.setDeliveryState(DeliveryState.DELIVERED);
        delivery = deliveryRepository.save(delivery);
        log.info("Updating delivery: {}", delivery);
    }

    public void setDeliveryFailed(UUID deliveryId) {
        Delivery delivery = findDeliveryById(deliveryId);
        try {
            OrderDto orderDto = orderFeign.deliveryOrderFailed(delivery.getOrderId());
            log.info("Updating the status in the order service: {}", orderDto);
        } catch (FeignException e) {
            log.info("Failure to update the status in the order service: {}", e.getMessage());
            throw new NoOrderFoundException(e.getMessage());
        }
        delivery.setDeliveryState(DeliveryState.FAILED);
        delivery = deliveryRepository.save(delivery);
        log.info("Updating delivery: {}", delivery);
    }

    public void pickOrderForDelivery(UUID deliveryId) {
        Delivery delivery = findDeliveryById(deliveryId);
        ShippedToDeliveryRequest shippedRequest = ShippedToDeliveryRequest.builder()
                .deliveryId(deliveryId)
                .orderId(delivery.getOrderId())
                .build();

        try {
            warehouseFeign.shippedProductsToWarehouse(shippedRequest);
            log.info("We update the status in the warehouse and send the order for delivery: {}", shippedRequest);
        } catch (FeignException e) {
            log.info("Failure to update the status in the warehouse service: {}", e.getMessage());
            throw new NoOrderFoundException(e.getMessage());
        }
        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        delivery = deliveryRepository.save(delivery);
        log.info("Updating delivery: {}", delivery);
    }

    private Address findOrCreateAddress(Address address) {
        Optional<Address> addressOpt = addressRepository.findByCountryAndCityAndStreetAndHouseAndFlat(
                address.getCountry(), address.getCity(), address.getStreet(), address.getHouse(), address.getFlat());
        if (addressOpt.isPresent()) {
            log.info("Old address: {}", addressOpt.get());
            return addressOpt.get();
        }
        Address result = addressRepository.save(address);
        log.info("Creating a new address: {}", result);
        return result;
    }

    private Boolean checkEqualsAddress(Address a1, Address a2) {
        return Objects.equals(a1.getCountry(), a2.getCountry()) &&
                Objects.equals(a1.getCity(), a2.getCity()) &&
                Objects.equals(a1.getStreet(), a2.getStreet()) &&
                Objects.equals(a1.getHouse(), a2.getHouse()) &&
                Objects.equals(a1.getFlat(), a2.getFlat());
    }

    private Delivery findDeliveryById(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NoDeliveryFoundException("Delivery with id not found: " + deliveryId));
        log.info("We find the delivery: {}", delivery);
        return delivery;
    }

    private Boolean validOrder(OrderDto dto) {
        return dto.getDeliveryWeight() != null
                && dto.getDeliveryVolume() != null
                && dto.getFragile() != null;
    }
}