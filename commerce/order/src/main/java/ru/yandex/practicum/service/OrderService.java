package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.delivery.DeliveryState;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.OrderState;
import ru.yandex.practicum.dto.order.ProductReturnRequest;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.exception.NoOrderFoundException;
import ru.yandex.practicum.exception.NotAssembledOrderException;
import ru.yandex.practicum.exception.NotAuthorizedUserException;
import ru.yandex.practicum.exception.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.mapper.OrderMapper;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.repository.OrderRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final WarehouseFeignClient warehouseFeign;
    private final PaymentFeignClient paymentFeign;
    private final DeliveryFeignClient deliveryFeign;

    public OrderDto createOrder(String username, CreateNewOrderRequest newOrderRequest) {
        if (newOrderRequest == null) {
            throw new IllegalArgumentException("CreateNewOrderRequest should not be null");
        }
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException("Username must not be null or empty");
        }
        BookedProductsDto bookedProductsDto = warehouseFeign.checkProductQuantityInWarehouse(newOrderRequest.getShoppingCart());
        log.info("Checking the goods in the warehouse: {}", bookedProductsDto);

        Order order = OrderMapper.mapToOrder(username, newOrderRequest, bookedProductsDto);
        order = orderRepository.save(order);
        log.info("Saved order: {}", order);

        Double productCost = paymentFeign.calculateProductCost(OrderMapper.mapToOrderDto(order));
        log.info("The cost of the products in the order: {}", productCost);
        order.setProductPrice(productCost);

        DeliveryDto deliveryDto = createDeliveryOrder(order.getOrderId(), newOrderRequest.getDeliveryAddress());
        order.setDeliveryId(deliveryDto.getDeliveryId());
        order = orderRepository.save(order);
        log.info("Adding deliveryId to the order: {}", order);
        return OrderMapper.mapToOrderDto(order);
    }

    public List<OrderDto> getUserOrders(String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException("Username must not be null or empty");
        }
        List<Order> orders = orderRepository.findAllByUsername(username);
        log.info("User Orders: {}", orders);

        return OrderMapper.mapToOrderDto(orders);
    }

    public OrderDto calculateTotalCost(UUID orderId) {
        Order order = findOrderById(orderId);
        if (order.getDeliveryPrice() == null || order.getProductPrice() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("There is not enough data to calculate the full cost of the order");
        }
        Double totalCost = paymentFeign.calculateTotalCost(OrderMapper.mapToOrderDto(order));
        log.info("The full cost of the order: {}", totalCost);
        order.setTotalPrice(totalCost);
        order = orderRepository.save(order);
        log.info("Updating order: {}", order);
        return OrderMapper.mapToOrderDto(order);
    }

    public OrderDto calculateDelivery(UUID orderId) {
        Order order = findOrderById(orderId);
        Double cost = deliveryFeign.calculateDelivery(OrderMapper.mapToOrderDto(order));
        order.setDeliveryPrice(cost);
        order = orderRepository.save(order);
        log.info("Updating order: {}", order);
        return OrderMapper.mapToOrderDto(order);
    }

    public OrderDto returnOrder(ProductReturnRequest returnRequest) {
        if (returnRequest.getProducts().isEmpty()) {
            throw new IllegalArgumentException("The list of returned products should not be empty");
        }
        Order order = findOrderById(returnRequest.getOrderId());
        order.setState(OrderState.PRODUCT_RETURNED);
        order = orderRepository.save(order);
        log.info("Updating order status: {}", order);

        warehouseFeign.returnProductsToWarehouse(returnRequest.getProducts());
        log.info("We return the products to the warehouse");
        return OrderMapper.mapToOrderDto(order);
    }

    public OrderDto payOrder(UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.PAID);
        order = orderRepository.save(order);
        log.info("Updating order status: {}", order);
        return OrderMapper.mapToOrderDto(order);
    }

    public OrderDto setPaymentFailed(UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.PAYMENT_FAILED);
        order = orderRepository.save(order);
        log.info("Updating order status: {}", order);
        return OrderMapper.mapToOrderDto(order);
    }

    public OrderDto deliveryOrder(UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.DELIVERED);
        order = orderRepository.save(order);
        log.info("Updating order status: {}", order);
        return OrderMapper.mapToOrderDto(order);
    }

    public OrderDto deliveryOrderFailed(UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.DELIVERY_FAILED);
        order = orderRepository.save(order);
        log.info("Updating order status: {}", order);
        return OrderMapper.mapToOrderDto(order);
    }

    public OrderDto completedOrder(UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.COMPLETED);
        order = orderRepository.save(order);
        log.info("Updating order status: {}", order);
        return OrderMapper.mapToOrderDto(order);
    }

    public OrderDto assembleOrder(UUID orderId) {
        Order order = findOrderById(orderId);
        AssemblyProductsForOrderRequest assemblyRequest = new AssemblyProductsForOrderRequest(orderId, order.getProducts());
        log.info("Request for an assembly for a warehouse: {}", assemblyRequest);
        BookedProductsDto bookedDto = warehouseFeign.assemblingProductsForOrder(assemblyRequest);
        log.info("The warehouse's response to the assembly: {}", bookedDto);
        order.setState(OrderState.ASSEMBLED);
        order = orderRepository.save(order);
        log.info("Updating order: {}", order);

        order = createPaymentOrder(order);

        return OrderMapper.mapToOrderDto(order);
    }

    public OrderDto assembleOrderFailed(UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.ASSEMBLY_FAILED);
        order = orderRepository.save(order);
        log.info("Updating order: {}", order);
        return OrderMapper.mapToOrderDto(order);
    }

    private Order findOrderById(UUID orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("The OrderID must not be null");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("The order with the id was not found: " + orderId));
        log.info("We find the right order: {}", order);
        return order;
    }

    private Order createPaymentOrder(Order order) {
        if (!(order.getState().equals(OrderState.ASSEMBLED))) {
            throw new NotAssembledOrderException("The order has not been assembled in the warehouse yet");
        }
        if (order.getTotalPrice() == null || order.getDeliveryPrice() == null || order.getProductPrice() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Insufficient data to pay for the order");
        }
        PaymentDto paymentDto = paymentFeign.createPaymentOrder(OrderMapper.mapToOrderDto(order));
        log.info("Created a payment in the payment service: {}", paymentDto);
        order.setState(OrderState.ON_PAYMENT);
        order.setPaymentId(paymentDto.getPaymentId());
        order = orderRepository.save(order);
        log.info("Updating order status: {}", order);
        return order;
    }

    private DeliveryDto createDeliveryOrder(UUID orderId, AddressDto toAddressDelivery) {
        AddressDto fromAddressDelivery = warehouseFeign.getAddressWarehouse();
        DeliveryDto dto = DeliveryDto.builder()
                .fromAddress(fromAddressDelivery)
                .toAddress(toAddressDelivery)
                .orderId(orderId)
                .deliveryState(DeliveryState.CREATED)
                .build();

        DeliveryDto newDeliveryDto = deliveryFeign.createDelivery(dto);
        log.info("Creating order delivery in the order service: {}", newDeliveryDto);
        return newDeliveryDto;
    }
}