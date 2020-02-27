package com.leyou.cart.controller;

import com.leyou.cart.entity.Cart;
import com.leyou.cart.entity.UserHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class CartController {

    /**
     * 添加购物车
     * @param cart 因为参数是一个json对象，所以要加@RequestBody才能接收到
     */
    @PostMapping(name = "添加购物车")
    public ResponseEntity<Void> addCart(HttpServletRequest request, @RequestBody Cart cart){
        //用户id获取用户名
//        String userId = request.getHeader("USER_ID");
        String userId = UserHolder.getUserId();
        return ResponseEntity.ok().build();
    }
}
