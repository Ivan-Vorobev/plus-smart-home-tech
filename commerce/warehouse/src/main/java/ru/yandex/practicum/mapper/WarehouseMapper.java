package ru.yandex.practicum.mapper;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.dto.warehouse.DimensionDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.model.WarehouseProduct;

import java.util.UUID;

@Slf4j
public class WarehouseMapper {
    public static WarehouseProduct mapToWarehouseProduct(NewProductInWarehouseRequest newProductRequest) {
        DimensionDto dimension = newProductRequest.getDimension();
        return WarehouseProduct.builder()
                .productId(UUID.fromString(newProductRequest.getProductId()))
                .fragile(newProductRequest.getFragile())
                .width(dimension.getWidth())
                .height(dimension.getHeight())
                .depth(dimension.getDepth())
                .weight(newProductRequest.getWeight())
                .quantity(0)
                .build();
    }
}
