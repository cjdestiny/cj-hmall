package com.hmall.api.config;

import com.hmall.common.utils.UserContext;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;

public class DefaultFeignConfig {
    @Bean
    public Logger.Level defaultFeignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor UserInfoInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                if (UserContext.getUser() != null) {
                    requestTemplate.header("user-info", UserContext.getUser().toString());
                }
            }
        };
    }
}
