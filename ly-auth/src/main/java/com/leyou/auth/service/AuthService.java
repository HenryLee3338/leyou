package com.leyou.auth.service;

import com.leyou.common.auth.entity.UserInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface AuthService {
    void login(String username, String password, HttpServletResponse response);

    UserInfo verify(HttpServletRequest request,HttpServletResponse response);

    void logout(HttpServletRequest request, HttpServletResponse response);
}
