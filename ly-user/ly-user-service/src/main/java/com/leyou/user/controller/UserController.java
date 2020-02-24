package com.leyou.user.controller;

import com.leyou.common.exceptions.LyException;
import com.leyou.user.dto.UserDTO;
import com.leyou.user.entity.TbUser;
import com.leyou.user.service.TbUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.stream.Collectors;

@RestController
public class UserController {

    @Autowired
    private TbUserService userService;

    /**
     * 查询用户名或者手机号是否存在
     * 因为要从url中取参数，所以要加@PathVariable注解
     * @param data 用户名或者手机号
     * @param type  判断是用户名还是手机号
     * @return ResponseEntity<Boolean>
     */
    @GetMapping(value = "/check/{data}/{type}",name = "查询用户名或者手机号是否存在")
    public ResponseEntity<Boolean> check(@PathVariable("data") String data, @PathVariable("type") Integer type){
        Boolean b = userService.checkData(data,type);
        return ResponseEntity.ok(b);
    }

    /**
     * 发送短信验证码
     * @param phone 手机号码
     */
    @PostMapping(value = "/code",name = "发送短信验证码")
    public ResponseEntity<Void> sendCode(@RequestParam("phone") String phone){
        userService.sendCode(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    /**
     * 用户注册
     * 在user前加@Valid注解可以对里面的属性做正则表达式的判断，在TbUser的属性上也做了相应的注解才行
     * @param user 用户信息
     * @param code 验证码
     */
    @PostMapping(value = "/register",name = "用户注册")
    public ResponseEntity<Void> registry(@Valid TbUser user,BindingResult result, @RequestParam("code") String code){
        //如果hibernate validator验证不通过，抛一个具体的原因
        if (result.hasErrors()) {
            String errorMessage = result.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining("|"));
            throw new LyException(400, errorMessage);
        }
        userService.registry(user,code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据用户名和密码查询用户
     * @param username 用户名
     * @param password 密码
     * @return 用户信息
     */
    @GetMapping(value = "/query",name = "查询用户")
    public ResponseEntity<UserDTO> queryUserByUsernameAndPassword(@RequestParam("username") String username, @RequestParam("password") String password){
        UserDTO user= userService.queryUserByUsernameAndPassword(username, password);
        return ResponseEntity.ok(user);
    }
}
