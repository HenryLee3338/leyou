package com.leyou.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


import java.util.List;

//和yml配置文件里的跨域属性相对应的实体类

@Data
@ConfigurationProperties(prefix = "ly.cors")
public class CORSProperties {
    private List<String> allowedOrigins;
    private Boolean allowedCredentials;
    private List<String> allowedMethods;
    private List<String> allowedHeaders;
    private Long maxAge;
    private String filterPath;
}