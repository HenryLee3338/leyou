package com.leyou.gateway.filters;

import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.FORM_BODY_WRAPPER_FILTER_ORDER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

@Component
@EnableConfigurationProperties(FilterProperties.class)
public class AuthFilter extends ZuulFilter {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private FilterProperties filterProperties;

    @Override
    public String filterType() {//过滤的类型 有前置，后置，进行中，异常
//        return "pre";
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {//过滤器的执行顺序 值越小越靠前执行
        return FORM_BODY_WRAPPER_FILTER_ORDER - 1 ;
//        return 0;
    }

    @Override
    public boolean shouldFilter() {//是否要进入到run方法中，true进入，false不进入
        //设置白名单，直接放行
        List<String> allowPaths = filterProperties.getAllowPaths();
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String uri = request.getRequestURI();
        for (String allowPath : allowPaths) {
            //判断当前uri是否以allowPath:  /api/user/check 开头
            if (uri.startsWith(allowPath)){
                return false;
            }
        }
        return true;
    }

    @Override
    public Object run() throws ZuulException {//业务实现
        //1.获取token
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String token = CookieUtils.getCookieValue(request, jwtProperties.getUser().getCookieName());
        //2.解密
        try {
            Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey(), UserInfo.class);
            UserInfo userInfo = payload.getUserInfo();
            Long userId = userInfo.getId();//把这个userId发送到微服务当中
            ctx.addZuulRequestHeader("USER_ID",userId.toString());//通过网关得请求头发送userId
            String role = userInfo.getRole();//角色
            //todo 虽然能解析到用户，但是这里还应该根据用户角色判断角色是否能进入相关微服务当中
        } catch (Exception e) {
            //出现异常意味着没有解析成功，不能进入微服务
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(HttpStatus.SC_FORBIDDEN);
            e.printStackTrace();
        }
        return null;
    }
}
