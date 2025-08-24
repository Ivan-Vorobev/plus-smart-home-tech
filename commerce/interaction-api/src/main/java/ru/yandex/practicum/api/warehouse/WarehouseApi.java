package ru.yandex.practicum.api.warehouse;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.shoppingCart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.*;

import java.util.Map;
import java.util.UUID;

public interface WarehouseApi {

    @PutMapping("/api/v1/warehouse")
    void addNewProductToWarehouse(@Valid @RequestBody NewProductInWarehouseRequest newProductInWarehouseRequest);

    @PostMapping("/api/v1/warehouse/check")
    BookedProductsDto checkProductQuantityInWarehouse(@Valid @RequestBody ShoppingCartDto shoppingCartDto);

    @PostMapping("/api/v1/warehouse/add")
    void addProductInWarehouse(@Valid @RequestBody AddProductToWarehouseRequest addProductToWarehouseRequest);

    @GetMapping("/api/v1/warehouse/address")
    AddressDto getAddressWarehouse();

    @PostMapping("/api/v1/warehouse/assembly")
    BookedProductsDto assemblingProductsForOrder(@Valid @RequestBody AssemblyProductsForOrderRequest assemblyRequest);

    @PostMapping("/api/v1/warehouse/return")
    void returnProductsToWarehouse(@RequestBody Map<UUID, Integer> returnedProducts);

    @PostMapping("/api/v1/warehouse/shipped")
    void shippedProductsToWarehouse(@Valid @RequestBody ShippedToDeliveryRequest deliveryRequest);
}
