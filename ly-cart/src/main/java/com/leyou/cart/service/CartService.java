package com.leyou.cart.service;

import com.leyou.cart.entity.Cart;

import java.util.List;

public interface CartService {
    void addCartToRedis(String userId, Cart cart);

    List<Cart> findCartListFromRedis(String userId);

    void updateCartNum(String userId, Long id, Integer num);

    void deleteCartFromRedis(String userId, Long id);

    void addCartListToRedis(String userId, List<Cart> cartList);
}
