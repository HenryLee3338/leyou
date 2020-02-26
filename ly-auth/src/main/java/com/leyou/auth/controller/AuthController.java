package com.leyou.auth.controller;

import com.leyou.auth.service.AuthService;
import com.leyou.common.auth.entity.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     */
    @PostMapping(value = "/login",name = "用户登录")
    public ResponseEntity<Void> login(@RequestParam("username") String username, @RequestParam("password") String password, HttpServletResponse response){
        authService.login(username,password,response);
        return ResponseEntity.ok().build();
    }


    /**
     * 校验用户是否登录
     * @param request 从request里获取cookie
     * @return 用户
     */
    @GetMapping(value = "/verify",name = "校验用户是否登录")
    public ResponseEntity<UserInfo> verify(HttpServletRequest request,HttpServletResponse response){
        UserInfo userInfo = authService.verify(request,response);
        return ResponseEntity.ok(userInfo);
    }


    /**
     * 退出登录
     */
    @PostMapping(value = "/logout",name = "退出登录")
    public ResponseEntity<Void> logout(HttpServletRequest request,HttpServletResponse response){
        authService.logout(request,response);
        return ResponseEntity.ok().build();
    }
}
