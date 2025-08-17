package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.shoppingStore.ProductCategory;
import ru.yandex.practicum.dto.shoppingStore.ProductDto;
import ru.yandex.practicum.dto.shoppingStore.ProductState;
import ru.yandex.practicum.dto.shoppingStore.SetProductQuantityStateRequest;
import ru.yandex.practicum.exception.ProductNotFoundException;
import ru.yandex.practicum.mapper.ShoppingStoreMapper;
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.repository.ShoppingStoreRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingStoreService {
    private final ShoppingStoreRepository storeRepository;

    public ProductDto createProduct(ProductDto productDto) {
        if (productDto.getProductId() != null) {
            throw new IllegalArgumentException("When creating a new product, the 'productId' field must be null.");
        }

        Product product = ShoppingStoreMapper.mapToProduct(productDto);
        product = storeRepository.save(product);

        log.info("Saved a new product in the database: {}", product);
        return ShoppingStoreMapper.mapToProductDto(product);
    }

    public ProductDto findProductById(String productId) {
        Product product = getProduct(UUID.fromString(productId));
        return ShoppingStoreMapper.mapToProductDto(product);
    }

    public List<ProductDto> findAllByProductCategory(ProductCategory productCategory, Pageable pageable) {
        List<Product> products = storeRepository.findAllByProductCategory(productCategory, pageable);
        if (products.isEmpty()) {
            throw new ProductNotFoundException("No products found with the category: " + productCategory);
        }
        return ShoppingStoreMapper.mapToProductDto(products);
    }

    public ProductDto updateProduct(ProductDto productDto) {
        if (productDto.getProductId() == null || productDto.getProductId().isBlank()) {
            throw new IllegalArgumentException("When updating a product, the 'productId' field should not be null or empty");
        }

        UUID productId = UUID.fromString(productDto.getProductId());

        Product productOld = getProduct(productId);
        log.info("We got the old product: {}", productOld);

        Product productNew = ShoppingStoreMapper.mapToProduct(productDto);
        productNew.setProductId(productId);

        Product result = storeRepository.save(productNew);
        log.info("Saved a new product: {}", result);
        return ShoppingStoreMapper.mapToProductDto(result);
    }

    public Boolean removeProductFromStore(UUID productId) {
        Product product = getProduct(productId);

        if (!product.getProductState().equals(ProductState.DEACTIVATE)) {
            product.setProductState(ProductState.DEACTIVATE);
            product = storeRepository.save(product);
            log.info("Delete product: {}", product);
        }
        return product.getProductState().equals(ProductState.DEACTIVATE);
    }

    public Boolean setProductQuantityState(SetProductQuantityStateRequest quantityStateRequest) {
        Product product = getProduct(quantityStateRequest.getProductId());

        if (!product.getQuantityState().equals(quantityStateRequest.getQuantityState())) {
            product.setQuantityState(quantityStateRequest.getQuantityState());
            product = storeRepository.save(product);
            log.info("Product with updated QuantityState: {}", product);
        }
        return product.getQuantityState().equals(quantityStateRequest.getQuantityState());
    }

    private Product getProduct(UUID id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with id: " + id + " not found"));
    }
}