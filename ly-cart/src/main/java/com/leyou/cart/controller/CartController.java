package com.leyou.cart.controller;

import com.leyou.cart.entity.Cart;

import com.leyou.cart.service.CartService;
import com.leyou.common.auth.entity.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class CartController {

    @Autowired
    CartService cartService;

    /**
     * 从redis获取购物车集合
     * @return 购物车集合
     */
    @GetMapping(value = "/list",name = "从redis获取当前登录人的购物车集合")
    public ResponseEntity<List<Cart>> findCartListFromRedis(){
        String userId = UserHolder.getUserId();
        List<Cart> cartList = cartService.findCartListFromRedis(userId);
        return ResponseEntity.ok(cartList);
    }

    /**
     * 登录时向redis中批量添加未登录时的购物车
     * @param cartList 购物车集合,json格式参数需要加@RequestBody
     */
    @PostMapping(value = "/list",name = "登录时向redis中批量添加购物车")
    public ResponseEntity<Void> addCartListToRedis(@RequestBody List<Cart> cartList){
        String userId = UserHolder.getUserId();
        cartService.addCartListToRedis(userId,cartList);
        return ResponseEntity.ok().build();
    }

    /**
     * 登录状态添加购物车
     * @param cart 因为参数是一个json对象，所以要加@RequestBody才能接收到
     */
    @PostMapping(name = "添加购物车")
    public ResponseEntity<Void> addCart(HttpServletRequest request, @RequestBody Cart cart){
        //用户id获取用户名
        String userId = UserHolder.getUserId();
        cartService.addCartToRedis(userId,cart);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     *增加减少购物车商品数量
     * @param id skuId
     * @param num 数量
     */
    @PutMapping(name = "增加减少数量")
    public ResponseEntity<Void> updateCartNum(@RequestParam("id") Long id, @RequestParam("num") Integer num){
        String userId = UserHolder.getUserId();
        cartService.updateCartNum(userId,id,num);
       return ResponseEntity.ok().build();
    }

    /**
     * 删除购物车中的某一件商品
     * @param id  @PathVariable 因为要从url中取参数
     */
    @DeleteMapping(value = "/{id}",name = "删除购物车中的某一件商品")
    public ResponseEntity<Void> deleteCartFromRedis(@PathVariable("id") Long id){
        String userId = UserHolder.getUserId();
        cartService.deleteCartFromRedis(userId,id);
        return ResponseEntity.ok().build();
    }

}
