package com.leyou.config;

import com.leyou.common.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;

@Data
@Slf4j
@Component
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties implements InitializingBean {

    private String pubKeyPath;
    private String priKeyPath;

    private PrivateKey privateKey; //根据文件路径生成
    private PublicKey publicKey;

    private UserTokenProperties user = new UserTokenProperties();

    @Override
    public void afterPropertiesSet() throws Exception {//pubKeyPath，priKeyPath赋值成功后才能执行该方法
        publicKey = RsaUtils.getPublicKey(pubKeyPath);
        privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }



    @Data
    public class UserTokenProperties {
        /**
         * token过期时长
         */
        private int expire;
        /**
         * 存放token的cookie名称
         */
        private String cookieName;
        /**
         * 存放token的cookie的domain
         */
        private String cookieDomain;

        /**
         * 最小刷新时间，有效时间低于这个时间就刷新Token
         */
        private Integer minRefreshInterval;
    }
}