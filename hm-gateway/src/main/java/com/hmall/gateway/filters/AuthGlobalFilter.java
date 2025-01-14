package com.hmall.gateway.filters;

import cn.hutool.core.text.AntPathMatcher;
import com.hmall.common.exception.UnauthorizedException;
import com.hmall.gateway.config.AuthProperties;
import com.hmall.gateway.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {
    private final AuthProperties authProperties;
    private final JwtTool jwtTool;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1.获取请求
        ServerHttpRequest request = exchange.getRequest();
        //2.放行那些路径
        if(isExclude(request.getPath().toString())){
            //放行
            return chain.filter(exchange);
        }
        List<String> headers = request.getHeaders().get("Authorization");
        //3.获取与效验token
        String token = null;
        if (headers != null && !headers.isEmpty()) {
            token = headers.get(0);
        }
        Long userId = null;
        try {
            userId = jwtTool.parseToken(token);
        }
        catch (UnauthorizedException e) {
            ServerHttpResponse response = exchange.getResponse();
            //设置状态码为401
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        //4.传递用户信息
        String userInfo = userId.toString();
        ServerWebExchange swe = exchange.mutate()
                .request(builder -> builder.header("user-info", userInfo))
                .build();

        //5.放行
        return chain.filter(swe);
    }

    private boolean isExclude(String path) {
        for (String pathPatter : authProperties.getExcludePaths()) {
            if (antPathMatcher.match(pathPatter, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
