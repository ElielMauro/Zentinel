package com.zentinel.demo.config;

import com.zentinel.demo.security.AlmacenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AlmacenInterceptor almacenInterceptor;

    public WebConfig(AlmacenInterceptor almacenInterceptor) {
        this.almacenInterceptor = almacenInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(almacenInterceptor);
    }
}
