package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

//@SpringBootApplication
//@EnableDiscoveryClient 注册中心客户端，springboot2.0以后可以省略
//@EnableCircuitBreaker  熔断
//@SpringCloudApplication中包含了上面三个注解
@SpringCloudApplication
@EnableZuulProxy
public class LyGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(LyGatewayApplication.class,args);
    }
}
