package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.shoppingCart.ShoppingCartApi;
import ru.yandex.practicum.dto.shoppingCart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.shoppingCart.ShoppingCartDto;
import ru.yandex.practicum.service.ShoppingCartService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ShoppingCartController implements ShoppingCartApi {
    private final ShoppingCartService cartService;

    @Override
    public ShoppingCartDto addProductInShoppingCart(String username, Map<UUID, Integer> productsMap) {
        return cartService.addProductInShoppingCart(username, productsMap);
    }

    @Override
    public ShoppingCartDto getUserShoppingCart(String username) {
        return cartService.getUserShoppingCart(username);
    }

    @Override
    public void deactivateUserShoppingCart(String username) {
        cartService.deactivateUserShoppingCart(username);
    }

    @Override
    public ShoppingCartDto removeProductFromShoppingCart(String username, List<UUID> productsId) {
        return cartService.removeProductFromShoppingCart(username, productsId);
    }

    @Override
    public ShoppingCartDto changeProductQuantityInShoppingCart(String username,
                                                               ChangeProductQuantityRequest changeProductQuantityRequest) {
        return cartService.changeProductQuantityInShoppingCart(username, changeProductQuantityRequest);
    }
}
