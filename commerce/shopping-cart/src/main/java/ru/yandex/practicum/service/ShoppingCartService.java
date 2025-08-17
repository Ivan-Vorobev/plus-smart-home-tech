package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.shoppingCart.CartState;
import ru.yandex.practicum.dto.shoppingCart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.shoppingCart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.exception.NotAuthorizedUserException;
import ru.yandex.practicum.mapper.ShoppingCartMapper;
import ru.yandex.practicum.model.ShoppingCart;
import ru.yandex.practicum.repository.ShoppingCartRepository;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingCartService {
    private final ShoppingCartRepository cartRepository;
    private final WarehouseFeignClient warehouseFeignClient;

    public ShoppingCartDto addProductInShoppingCart(String username, Map<UUID, Integer> productsMap) {
        checkUsername(username);

        if (productsMap == null || productsMap.isEmpty()) {
            throw new IllegalArgumentException("productsMap cannot be null or empty");
        }

        ShoppingCart shoppingCart = getActiveShoppingCartByUserName(username);
        shoppingCart.getProducts().putAll(productsMap);
        log.info("Added new product items to the cart: {}", shoppingCart);

        BookedProductsDto bookedProductsDto = warehouseFeignClient
                .checkProductQuantityInWarehouse(ShoppingCartMapper.mapToShoppingCartDto(shoppingCart));
        log.info("Checked the availability of products in stock: {}", bookedProductsDto);

        shoppingCart = cartRepository.save(shoppingCart);
        log.info("Saved the bucket in the database: {}", shoppingCart);

        return ShoppingCartMapper.mapToShoppingCartDto(shoppingCart);
    }

    public ShoppingCartDto getUserShoppingCart(String username) {
        checkUsername(username);
        ShoppingCart shoppingCart = getActiveShoppingCartByUserName(username);
        return ShoppingCartMapper.mapToShoppingCartDto(shoppingCart);
    }

    public void deactivateUserShoppingCart(String username) {
        checkUsername(username);

        ShoppingCart shoppingCart = getActiveShoppingCartByUserName(username);
        shoppingCart.setCartState(CartState.DEACTIVATE);
        shoppingCart = cartRepository.save(shoppingCart);

        log.info("The shopping cart has been deactivated {}", shoppingCart);
    }

    public ShoppingCartDto removeProductFromShoppingCart(String username, List<UUID> productsId) {
        checkUsername(username);

        if (productsId == null || productsId.isEmpty()) {
            throw new IllegalArgumentException("The list of deleted products should not be null or empty");
        }

        ShoppingCart shoppingCart = getActiveShoppingCartByUserName(username);
        if (shoppingCart.getProducts().isEmpty()) {
            throw new NoProductsInShoppingCartException("The cart is already empty");
        }

        for (UUID id : productsId) {
            shoppingCart.getProducts().remove(id);
        }

        shoppingCart = cartRepository.save(shoppingCart);
        log.info("Updated shopping cart: {}", shoppingCart);

        return ShoppingCartMapper.mapToShoppingCartDto(shoppingCart);
    }

    public ShoppingCartDto changeProductQuantityInShoppingCart(String username,
                                                               ChangeProductQuantityRequest changeQuantityRequest) {
        checkUsername(username);
        ShoppingCart shoppingCart = getActiveShoppingCartByUserName(username);

        if (!shoppingCart.getProducts().containsKey(changeQuantityRequest.getProductId())) {
            throw new NoProductsInShoppingCartException("Cart not found productId: " + changeQuantityRequest.getProductId());
        }

        shoppingCart.getProducts().put(changeQuantityRequest.getProductId(), changeQuantityRequest.getNewQuantity());
        BookedProductsDto bookedProductsDto = warehouseFeignClient
                .checkProductQuantityInWarehouse(ShoppingCartMapper.mapToShoppingCartDto(shoppingCart));
        log.info("Check warehouse quantity: {}", bookedProductsDto);

        shoppingCart = cartRepository.save(shoppingCart);
        log.info("Updated cart: {}", shoppingCart);

        return ShoppingCartMapper.mapToShoppingCartDto(shoppingCart);
    }

    private ShoppingCart getActiveShoppingCartByUserName(String username) {
        Optional<ShoppingCart> searchShoppingCart = cartRepository.findByUsernameAndCartState(username, CartState.ACTIVE);

        ShoppingCart shoppingCart;
        if (searchShoppingCart.isEmpty()) {
            log.info("User {} does not have active basket", username);
            shoppingCart = ShoppingCart.builder()
                    .username(username)
                    .cartState(CartState.ACTIVE)
                    .products(new HashMap<>())
                    .build();
            shoppingCart = cartRepository.save(shoppingCart);
            log.info("New cart {} for user: {}", shoppingCart, username);
        } else {
            shoppingCart = searchShoppingCart.get();
            log.info("Searched cart {} for user: {}", shoppingCart, username);
        }

        return shoppingCart;
    }

    private void checkUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException("The username cannot be null or empty");
        }
    }
}
