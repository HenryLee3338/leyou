package com.leyou.gateway.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

//在网关处配置跨域访问

@Configuration
@EnableConfigurationProperties(CORSProperties.class)
public class GlobalCORSConfig {

    @Autowired
    private CORSProperties corsProperties;

    @Bean
    public CorsFilter corsFilter() {
        //1.添加CORS配置信息
        CorsConfiguration config = new CorsConfiguration();
        //1) 允许的域,不要写*，否则cookie就无法使用了
//        config.addAllowedOrigin("http://manage.leyou.com");
//        config.addAllowedOrigin("http://www.leyou.com");
//        config.addAllowedOrigin("http://127.0.0.1:9001");

        //从配置文件读取跨域域名的集合
//        List<String> allowedOrigins = corsProperties.getAllowedOrigins();
//        for (String allowedOrigin : allowedOrigins) {
//            config.addAllowedOrigin(allowedOrigin);
//        }

        corsProperties.getAllowedOrigins().forEach(config::addAllowedOrigin);


        //2) 是否发送Cookie信息
//        config.setAllowCredentials(true);
        config.setAllowCredentials(corsProperties.getAllowedCredentials());


        //3) 允许的请求方式
//        config.addAllowedMethod("OPTIONS");
//        config.addAllowedMethod("HEAD");
//        config.addAllowedMethod("GET");
//        config.addAllowedMethod("PUT");
//        config.addAllowedMethod("POST");
//        config.addAllowedMethod("DELETE");

        corsProperties.getAllowedMethods().forEach(config::addAllowedMethod);


        // 4）允许的头信息
//        config.addAllowedHeader("*");

        corsProperties.getAllowedHeaders().forEach(config::addAllowedHeader);


        // 5）有效期 单位秒
//        config.setMaxAge(3600L);

        config.setMaxAge(corsProperties.getMaxAge());


        //2.添加映射路径，我们拦截一切请求
//        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
//        configSource.registerCorsConfiguration("/**", config);

        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
        configSource.registerCorsConfiguration(corsProperties.getFilterPath(),config);


        //3.返回新的CORSFilter
        return new CorsFilter(configSource);
    }
}