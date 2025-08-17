package ru.yandex.practicum.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import ru.yandex.practicum.dto.shoppingStore.PageProductDto;
import ru.yandex.practicum.dto.shoppingStore.ProductDto;
import ru.yandex.practicum.dto.shoppingStore.SortListDto;
import ru.yandex.practicum.model.Product;

import java.util.List;

@Slf4j
public class ShoppingStoreMapper {
    public static Product mapToProduct(ProductDto productDto) {
        return Product.builder()
                .productName(productDto.getProductName())
                .description(productDto.getDescription())
                .imageSrc(productDto.getImageSrc())
                .quantityState(productDto.getQuantityState())
                .productState(productDto.getProductState())
                .productCategory(productDto.getProductCategory())
                .price(productDto.getPrice())
                .build();
    }

    public static ProductDto mapToProductDto(Product product) {
        return ProductDto.builder()
                .productId(product.getProductId().toString())
                .productName(product.getProductName())
                .description(product.getDescription())
                .imageSrc(product.getImageSrc())
                .quantityState(product.getQuantityState())
                .productState(product.getProductState())
                .productCategory(product.getProductCategory())
                .price(product.getPrice())
                .build();
    }

    public static List<ProductDto> mapToProductDto(List<Product> products) {
        return products.stream().map(ShoppingStoreMapper::mapToProductDto).toList();
    }

    public static PageProductDto mapToProductPageDto(Page<Product> products) {
        return PageProductDto.builder()
                .content(products.getContent().stream()
                        .map(ShoppingStoreMapper::mapToProductDto)
                        .toList()
                )
                .sort(products.getSort().stream()
                        .map(s -> SortListDto.builder()
                                .direction(s.getDirection().toString())
                                .property(s.getProperty())
                                .build()
                        )
                        .toList()
                )
                .build();
    }
}
