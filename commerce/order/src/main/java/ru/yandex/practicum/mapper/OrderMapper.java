package ru.yandex.practicum.mapper;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.OrderState;
import ru.yandex.practicum.dto.shoppingCart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.model.Order;

import java.util.List;
import java.util.UUID;

@Slf4j
public class OrderMapper {
    public static Order mapToOrder(
            String username,
            CreateNewOrderRequest newOrderRequest,
            BookedProductsDto bookedProducts) {
        ShoppingCartDto shoppingCart = newOrderRequest.getShoppingCart();
        return Order.builder()
                .username(username)
                .shoppingCartId(UUID.fromString(shoppingCart.getShoppingCartId()))
                .products(shoppingCart.getProducts())
                .state(OrderState.NEW)
                .deliveryWeight(bookedProducts.getDeliveryWeight())
                .deliveryVolume(bookedProducts.getDeliveryVolume())
                .fragile(bookedProducts.getFragile())
                .build();
    }

    public static OrderDto mapToOrderDto(Order order) {
        return OrderDto.builder()
                .orderId(order.getOrderId())
                .username(order.getUsername())
                .shoppingCartId(order.getShoppingCartId())
                .products(order.getProducts())
                .paymentId(order.getPaymentId())
                .deliveryId(order.getDeliveryId())
                .state(order.getState())
                .deliveryWeight(order.getDeliveryWeight())
                .deliveryVolume(order.getDeliveryVolume())
                .fragile(order.getFragile())
                .totalPrice(order.getTotalPrice())
                .deliveryPrice(order.getDeliveryPrice())
                .productPrice(order.getProductPrice())
                .build();
    }

    public static List<OrderDto> mapToOrderDto(List<Order> orders) {
        return orders.stream().map(OrderMapper::mapToOrderDto).toList();
    }
}
