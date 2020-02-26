package com.leyou.auth.service.impl;

import com.leyou.auth.service.AuthService;
import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.auth.utils.RsaUtils;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.CookieUtils;
import com.leyou.config.JwtProperties;
import com.leyou.user.client.UserClient;
import com.leyou.user.dto.UserDTO;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.PrivateKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private StringRedisTemplate redisTemplate;
//    private static final String USER_ROLE = "role_user";

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     */
    @Override
    public void login(String username, String password, HttpServletResponse response) {

        //1.根据username和password远程调用用户中心的微服务，得到userDTO
        UserDTO user = userClient.queryUserByUsernameAndPassword(username, password);
        //2.将userDTO转换成UserInfo
        UserInfo userInfo = BeanHelper.copyProperties(user, UserInfo.class);
        userInfo.setRole("vip");
        //3.加密
        PrivateKey privateKey = jwtProperties.getPrivateKey();
        //4.把加密后的token字符串放入到cookie
        generateTokenAndSetCookie(response, userInfo, privateKey);
    }

    /**
     * 校验用户是否登录
     * @param request 从request里获取cookie
     * @param response  用来刷新token
     * @return 用户
     */
    @Override
    public UserInfo verify(HttpServletRequest request,HttpServletResponse response) {
        try {
            //1.从cookie中获取token
            String token = CookieUtils.getCookieValue(request, jwtProperties.getUser().getCookieName());
            //2.解析token得到Payload
            Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey(), UserInfo.class);


//======================================================================================================================
            //2.5判断payload的id是否在redis的黑名单中,如果在，那就是已经退出登录
            if (redisTemplate.hasKey(payload.getId())){
                return null;
            }
//======================================================================================================================

            //3.从PayLoad中获取UserInfo
            UserInfo userInfo = payload.getUserInfo();

//======================================================================================================================
            //3.5刷新token
            //判断token是否即将过时(剩余有效时间是否低于15分钟)
            //expirationTime - 15分钟 <= 当前时间 重新生成一个token
            Date expirationTime = payload.getExpiration();
            if (new DateTime(expirationTime).minusMinutes(jwtProperties.getUser().getMinRefreshInterval()).isBeforeNow()) {
                PrivateKey privateKey = jwtProperties.getPrivateKey();
                generateTokenAndSetCookie(response, userInfo, privateKey);
            }
//======================================================================================================================
            return userInfo;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 退出登录
     */
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        //1.将token标记未已失效,在redis中做一个黑名单
        String token = CookieUtils.getCookieValue(request, jwtProperties.getUser().getCookieName());
        Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey(), UserInfo.class);
        String payloadId = payload.getId();
        long time = payload.getExpiration().getTime() - new Date().getTime();//token还有多长时间失效，毫秒
        redisTemplate.boundValueOps(payloadId).set("",time, TimeUnit.MILLISECONDS);

        //2.删除cookie
        CookieUtils.deleteCookie(jwtProperties.getUser().getCookieName(),jwtProperties.getUser().getCookieDomain(),response);


    }


//======================================================================================================================
//======================================================================================================================
    /**
     * 生成token并且设置成cookie
     */
    private void generateTokenAndSetCookie(HttpServletResponse response, UserInfo userInfo, PrivateKey privateKey) {
        String token;
        token = JwtUtils.generateTokenExpireInMinutes(userInfo, privateKey, jwtProperties.getUser().getExpire());
        //网关里又一个忽略敏感头的设置将cookie忽略，需要设置
        CookieUtils.newCookieBuilder().name(jwtProperties.getUser().getCookieName())//cookie名字
                .value(token)//cookie内容
                .domain(jwtProperties.getUser().getCookieDomain())//在哪一个页面存储cookie
                .httpOnly(true)//禁用js操作cookie,防止xss攻击
                .charset("utf-8")//设置编码，其中这里用不到中文
                .response(response)//响应
                .maxAge(60*60*24*7)//单位s，这是一星期
                .build();
    }
}
