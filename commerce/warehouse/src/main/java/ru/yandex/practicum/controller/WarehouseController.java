package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.warehouse.WarehouseApi;
import ru.yandex.practicum.dto.shoppingCart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.*;
import ru.yandex.practicum.service.WarehouseService;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WarehouseController implements WarehouseApi {
    private final WarehouseService warehouseService;

    @Override
    public void addNewProductToWarehouse(NewProductInWarehouseRequest newProductInWarehouseRequest) {
        warehouseService.addNewProductToWarehouse(newProductInWarehouseRequest);
    }

    @Override
    public BookedProductsDto checkProductQuantityInWarehouse(ShoppingCartDto shoppingCartDto) {
        return warehouseService.checkProductQuantityInWarehouse(shoppingCartDto);
    }

    @Override
    public void addProductInWarehouse(AddProductToWarehouseRequest addProductToWarehouseRequest) {
        warehouseService.addProductInWarehouse(addProductToWarehouseRequest);
    }

    @Override
    public AddressDto getAddressWarehouse() {
        return warehouseService.getAddressWarehouse();
    }


    @Override
    public BookedProductsDto assemblingProductsForOrder(AssemblyProductsForOrderRequest assemblyRequest) {
        return warehouseService.assemblingProductsForOrder(assemblyRequest);
    }

    @Override
    public void returnProductsToWarehouse(Map<UUID, Integer> returnedProducts) {
        warehouseService.returnProductsToWarehouse(returnedProducts);
    }

    @Override
    public void shippedProductsToWarehouse(ShippedToDeliveryRequest deliveryRequest) {
        warehouseService.shippedProductsToWarehouse(deliveryRequest);
    }
}
