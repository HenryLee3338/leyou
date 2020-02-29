package com.leyou.cart.service.impl;

import com.leyou.cart.entity.Cart;
import com.leyou.common.auth.entity.UserHolder;
import com.leyou.cart.service.CartService;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static String prefix = "ly:cart";

    /**
     * 添加购物车到redis中
     * @param userId 用户id
     * @param cart 购物车对象
     */
    @Override
    public void addCartToRedis(String userId, Cart cart) {
        //添加购物车之前判断redis中是否已经有当前商品了
        BoundHashOperations<String, String, String> hashOperations = redisTemplate.boundHashOps(prefix + userId);
        addCart(cart, hashOperations);
    }

    /**
     * 登录时向redis中批量添加购物车
     * @param userId 用户id
     * @param cartList 购物车集合
     */
    @Override
    public void addCartListToRedis(String userId, List<Cart> cartList) {
        //判断redis中是否有此商品，有的话数量合并，没有的话直接放入redis
        BoundHashOperations<String, String, String> hashOperations = redisTemplate.boundHashOps(prefix + userId);
        for (Cart cart : cartList) {
            addCart(cart,hashOperations);
        }
    }

    /**
     * 从redis中查取对应用户的购物车
     * @param userId 用户id
     * @return 购物车集合
     */
    @Override
    public List<Cart> findCartListFromRedis(String userId) {
        BoundHashOperations<String, String, String> hashOperations = redisTemplate.boundHashOps(prefix + userId);
        //获取BoundHash中的所有value值
        List<String> values = hashOperations.values();
        //判断是否有值
        if (CollectionUtils.isEmpty(values)){
            throw new LyException(ExceptionEnum.CARTS_NOT_FOUND);
        }
        List<Cart> cartList = values.stream().map(value -> JsonUtils.toBean(value, Cart.class)).collect(Collectors.toList());
        return cartList;
    }

    /**
     *增加减少购物车商品数量
     * @param id skuId
     * @param num 数量
     */
    @Override
    public void updateCartNum(String userId, Long id, Integer num) {
        //1.先将对应的cart从redis取出来
        BoundHashOperations<String, String, String> hashOperations = redisTemplate.boundHashOps(prefix + userId);
        String cartStr = hashOperations.get(id.toString());
        Cart cart = JsonUtils.toBean(cartStr, Cart.class);
        //2.更改数量
        cart.setNum(num);
        //3.再次放回redis中
        hashOperations.put(id.toString(),JsonUtils.toString(cart));
    }

    /**
     * 删除购物车中的某一件商品
     */
    @Override
    public void deleteCartFromRedis(String userId, Long id) {
        redisTemplate.boundHashOps(prefix + userId).delete(id.toString());
    }

//======================================================================================================================
    /**
     * 判断redis中是否有此商品，有的话数量合并，没有的话直接放入redis
     * @param cart 购物车对象
     * @param hashOperations 操作redis的对象
     */
    private void addCart(Cart cart, BoundHashOperations<String, String, String> hashOperations) {
        String cartStr = hashOperations.get(cart.getSkuId().toString());
        if (StringUtils.isNotBlank(cartStr)){
            //如果有，将cartStr转换为对象，数量累加，如果没有，直接存入
            Cart cart1 = JsonUtils.toBean(cartStr, Cart.class);
            cart.setNum(cart1.getNum() + cart.getNum());
        }
       //将购物车放入redis中
        hashOperations.put(cart.getSkuId().toString(),JsonUtils.toString(cart));
    }
}
