package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.shoppingCart.ShoppingCartDto;
import ru.yandex.practicum.dto.shoppingStore.QuantityState;
import ru.yandex.practicum.dto.shoppingStore.SetProductQuantityStateRequest;
import ru.yandex.practicum.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.mapper.WarehouseMapper;
import ru.yandex.practicum.model.WarehouseProduct;
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
        Map<UUID, Integer> productsInCart = shoppingCartDto.getProducts();
        List<WarehouseProduct> warehouseProductsList = warehouseRepository.findAllById(productsInCart.keySet());
        log.info("Products from the basket available in stock: {}", warehouseProductsList);

        Map<UUID, WarehouseProduct> warehouseProductsMap = warehouseProductsList.stream()
                .collect(Collectors.toMap(WarehouseProduct::getProductId, Function.identity()));
        log.info("Creating a Map from products available in stock: {}", warehouseProductsMap);

        checkAvailabilityProductsInWarehouse(productsInCart.keySet(), warehouseProductsMap.keySet());
        checkQuantityProductsInWarehouse(productsInCart, warehouseProductsMap);

        return bookingProducts(warehouseProductsList);
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

    private void checkAvailabilityProductsInWarehouse(Set<UUID> productsInCart, Set<UUID> productsInWarehouse) {
        productsInCart.removeAll(productsInWarehouse);
        log.info("Products that are not in stock: {}", productsInCart);

        if (!productsInCart.isEmpty()) {
            throw new NoSpecifiedProductInWarehouseException("There are no products in stock with the following IDs: " + productsInCart);
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

    private BookedProductsDto bookingProducts(List<WarehouseProduct> products) {
        BookedProductsDto result = new BookedProductsDto(0.0, 0.0, false);
        for (WarehouseProduct product : products) {
            result.setDeliveryVolume(result.getDeliveryVolume() + product.getWeight());
            result.setDeliveryVolume(result.getDeliveryVolume()
                    + product.getWidth() * product.getHeight() * product.getDepth());
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
}