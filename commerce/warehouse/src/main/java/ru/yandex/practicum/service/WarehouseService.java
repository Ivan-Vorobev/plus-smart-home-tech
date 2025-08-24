package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.shoppingCart.ShoppingCartDto;
import ru.yandex.practicum.dto.shoppingStore.QuantityState;
import ru.yandex.practicum.dto.shoppingStore.SetProductQuantityStateRequest;
import ru.yandex.practicum.dto.warehouse.*;
import ru.yandex.practicum.exception.*;
import ru.yandex.practicum.mapper.WarehouseMapper;
import ru.yandex.practicum.model.OrderBooking;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.repository.OrderBookingRepository;
import ru.yandex.practicum.repository.WarehouseRepository;

import java.security.SecureRandom;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseService {
    private final WarehouseRepository warehouseRepository;
    private final ShoppingStoreFeignClient storeFeignClient;
    private final OrderBookingRepository bookingRepository;
    private final OrderFeignClient orderFeignClient;

    public void addNewProductToWarehouse(NewProductInWarehouseRequest newProductInWarehouseRequest) {
        UUID productId = UUID.fromString(newProductInWarehouseRequest.getProductId());

        if (warehouseRepository.findById(productId).isPresent()) {
            throw new SpecifiedProductAlreadyInWarehouseException("The product with id: " + productId + " is already in stock");
        }

        WarehouseProduct newProduct = WarehouseMapper.mapToWarehouseProduct(newProductInWarehouseRequest);
        log.info("Saving to a new product");
        warehouseRepository.save(newProduct);
    }

    public BookedProductsDto checkProductQuantityInWarehouse(ShoppingCartDto shoppingCartDto) {
        log.debug("Checking the sufficient number of items for the basket: {}", shoppingCartDto.getShoppingCartId());
        Map<UUID, Integer> productsInCart = shoppingCartDto.getProducts();
        List<WarehouseProduct> warehouseProductsList = warehouseRepository.findAllById(productsInCart.keySet());
        log.info("Products from the basket available in stock: {}", warehouseProductsList);

        Map<UUID, WarehouseProduct> warehouseProductsMap = warehouseProductsList.stream()
                .collect(Collectors.toMap(WarehouseProduct::getProductId, Function.identity()));
        log.info("Creating a Map from products available in stock: {}", warehouseProductsMap);

        checkAvailabilityProductsInWarehouse(productsInCart.keySet(), warehouseProductsMap.keySet());
        checkQuantityProductsInWarehouse(productsInCart, warehouseProductsMap);

        return bookingProducts(productsInCart, warehouseProductsMap);
    }

    public void addProductInWarehouse(AddProductToWarehouseRequest addProductRequest) {
        UUID productId = UUID.fromString(addProductRequest.getProductId());
        WarehouseProduct product = warehouseRepository.findById(productId).orElseThrow(
                () -> new NoSpecifiedProductInWarehouseException("Product with id: " + productId + " not in stock"));
        log.info("Product from the warehouse: {}", product);

        product.setQuantity(product.getQuantity() + addProductRequest.getQuantity());
        product = warehouseRepository.save(product);
        log.info("Updated product: {}", product);
        setProductQuantityState(product);
    }

    public AddressDto getAddressWarehouse() {
        String[] addresses = new String[]{"ADDRESS_1", "ADDRESS_2"};
        String currentAddresses = addresses[Random.from(new SecureRandom()).nextInt(0, 1)];

        return AddressDto.builder()
                .country(currentAddresses)
                .city(currentAddresses)
                .street(currentAddresses)
                .house(currentAddresses)
                .flat(currentAddresses)
                .build();
    }

    public BookedProductsDto assemblingProductsForOrder(AssemblyProductsForOrderRequest assemblyRequest) {
        if (assemblyRequest == null) {
            throw new IllegalArgumentException("AssemblyProductsForOrderRequest must not be null");
        }
        Map<UUID, Integer> productsInRequest = assemblyRequest.getProducts();
        log.info("Products from AssemblyProductsForOrderRequest: {}", productsInRequest);

        List<WarehouseProduct> warehouseProductsList = warehouseRepository.findAllById(productsInRequest.keySet());
        log.info("Products from the order are available in stock: {}", warehouseProductsList);
        Map<UUID, WarehouseProduct> warehouseProductsMap = warehouseProductsList.stream()
                .collect(Collectors.toMap(WarehouseProduct::getProductId, Function.identity()));
        log.info("Creating a Map from products available in stock: {}", warehouseProductsMap);

        try {
            checkAvailabilityProductsInWarehouse(productsInRequest.keySet(), warehouseProductsMap.keySet());
            checkQuantity(productsInRequest, warehouseProductsMap);
        } catch (NoSpecifiedProductInWarehouseException | ProductInShoppingCartLowQuantityInWarehouse e) {
            orderFeignClient.assembleOrderFailed(assemblyRequest.getOrderId());
            throw e;
        }

        List<WarehouseProduct> products = changeQuantityProductsInWarehouse(productsInRequest, warehouseProductsMap);
        setProductQuantityState(products);

        BookedProductsDto bookedDto = bookingProducts(productsInRequest, warehouseProductsMap);
        OrderBooking orderBooking = WarehouseMapper.mapToOrderBooking(assemblyRequest, bookedDto);
        orderBooking = bookingRepository.save(orderBooking);
        log.info("Saving booking: {}", orderBooking);
        return bookedDto;
    }

    public void returnProductsToWarehouse(Map<UUID, Integer> returnedProducts) {
        if (returnedProducts == null || returnedProducts.isEmpty()) {
            throw new IllegalArgumentException("Items for return must not be null or empty");
        }
        List<WarehouseProduct> warehouseProductsList = warehouseRepository.findAllById(returnedProducts.keySet());
        log.info("Products from the order are available in stock: {}", warehouseProductsList);
        Map<UUID, WarehouseProduct> warehouseProductsMap = warehouseProductsList.stream()
                .collect(Collectors.toMap(WarehouseProduct::getProductId, Function.identity()));
        log.info("Creating a Map from products available in stock: {}", warehouseProductsMap);

        checkAvailabilityProductsInWarehouse(returnedProducts.keySet(), warehouseProductsMap.keySet());

        for (UUID id : returnedProducts.keySet()) {
            Integer returnedQuantity = returnedProducts.get(id);
            if (returnedQuantity == null || returnedQuantity <= 0) {
                throw new IllegalArgumentException("The returned quantity of the product must be non-null, less than or equal to zero");
            }
            WarehouseProduct product = warehouseProductsMap.get(id);
            product.setQuantity(product.getQuantity() + returnedQuantity);
            log.info("Returned product: {}", product);
        }

        List<WarehouseProduct> products = warehouseRepository.saveAll(warehouseProductsMap.values());
        log.info("Updated products: {}", products);

        setProductQuantityState(products);
    }

    public void shippedProductsToWarehouse(ShippedToDeliveryRequest deliveryRequest) {
        OrderBooking orderBooking = bookingRepository.findById(deliveryRequest.getOrderId()).orElseThrow(() ->
                new NoOrderFoundException("No booking with the specified order id was found: " + deliveryRequest.getOrderId()));
        log.info("Old OrderBooking: {}", orderBooking);
        orderBooking.setDeliveryId(deliveryRequest.getDeliveryId());
        orderBooking = bookingRepository.save(orderBooking);
        log.info("Updating OrderBooking: {}", orderBooking);
    }

    private void checkQuantity(Map<UUID, Integer> productsInRequest, Map<UUID, WarehouseProduct> warehouseProductsMap) {
        List<UUID> notAvailabilityProducts = new ArrayList<>();
        for (UUID id : productsInRequest.keySet()) {
            if (productsInRequest.get(id) > warehouseProductsMap.get(id).getQuantity()) {
                notAvailabilityProducts.add(id);
            }
        }
        log.info("Products that are not enough in stock: {}", notAvailabilityProducts);
        if (!notAvailabilityProducts.isEmpty()) {
            throw new ProductInShoppingCartLowQuantityInWarehouse("There are not enough products in stock with the following IDs: "
                    + notAvailabilityProducts);
        }
    }

    private List<WarehouseProduct> changeQuantityProductsInWarehouse(
            Map<UUID, Integer> productsInRequest,
            Map<UUID, WarehouseProduct> warehouseProductsMap) {
        for (UUID id : productsInRequest.keySet()) {
            Integer newQuantity = warehouseProductsMap.get(id).getQuantity() - productsInRequest.get(id);
            warehouseProductsMap.get(id).setQuantity(newQuantity);
            log.info("Setting a new product quantity: {}", warehouseProductsMap.get(id));
        }
        List<WarehouseProduct> productList = warehouseRepository.saveAll(warehouseProductsMap.values());
        log.info("Products with updated quantity in stock: {}", productList);
        return productList;
    }

    private void checkAvailabilityProductsInWarehouse(Set<UUID> productsInCart, Set<UUID> productsInWarehouse) {
        productsInCart.removeAll(productsInWarehouse);
        log.info("Products that are not in stock: {}", productsInCart);

        if (!productsInCart.isEmpty()) {
            throw new ProductInShoppingCartNotInWarehouse("There are no products in stock with the following IDs: " + productsInCart);
        }
    }

    private void checkQuantityProductsInWarehouse(Map<UUID, Integer> productsInCart, Map<UUID, WarehouseProduct> warehouseProductsMap) {
        List<UUID> notAvailabilityProducts = new ArrayList<>();
        for (UUID id : productsInCart.keySet()) {
            if (productsInCart.get(id) < warehouseProductsMap.get(id).getQuantity()) {
                notAvailabilityProducts.add(id);
            }
        }
        log.info("Products that are missing from the warehouse: {}", notAvailabilityProducts);

        if (!notAvailabilityProducts.isEmpty()) {
            throw new ProductInShoppingCartLowQuantityInWarehouse("There are not enough products in stock with the following IDs: "
                    + notAvailabilityProducts);
        }
    }

    private BookedProductsDto bookingProducts(
            Map<UUID, Integer> productsInRequest,
            Map<UUID, WarehouseProduct> warehouseProductsMap) {
        BookedProductsDto result = new BookedProductsDto(0.0, 0.0, false);
        for (UUID id : productsInRequest.keySet()) {
            Integer quantity = productsInRequest.get(id);
            WarehouseProduct product = warehouseProductsMap.get(id);

            result.setDeliveryWeight(product.getWeight() * quantity + result.getDeliveryWeight());
            result.setDeliveryVolume(product.getDepth() * product.getWidth() * product.getHeight() * quantity
                    + result.getDeliveryVolume());
            if (product.getFragile()) {
                result.setFragile(true);
            }
        }
        log.info("General information about the order: {}", result);
        return result;
    }

    private void setProductQuantityState(WarehouseProduct product) {
        Integer quantity = product.getQuantity();
        QuantityState quantityState;
        if (quantity == 0) {
            quantityState = QuantityState.ENDED;
        } else if (quantity < 10) {
            quantityState = QuantityState.FEW;
        } else if (quantity < 100) {
            quantityState = QuantityState.ENOUGH;
        } else {
            quantityState = QuantityState.MANY;
        }

        SetProductQuantityStateRequest payload = new SetProductQuantityStateRequest(product.getProductId(), quantityState);
        log.info("Process updating the quantity of a product in the ShoppingStore: {}", payload);

        try {
            storeFeignClient.setProductQuantityState(payload.getProductId(), payload.getQuantityState());
        } catch (Exception ignored) {
        }
        log.info("Updating the quantity of a product in the ShoppingStore");
    }

    private void setProductQuantityState(List<WarehouseProduct> products) {
        products.forEach(this::setProductQuantityState);
    }
}