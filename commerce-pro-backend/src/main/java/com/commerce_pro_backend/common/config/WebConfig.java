package com.commerce_pro_backend.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web configuration for customizing Spring MVC behavior.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configure pageable resolver to use 0-based page numbers
     * (Spring default is 0-based, Angular Material is 0-based)
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();
        resolver.setOneIndexedParameters(false); // 0-based pagination
        resolver.setMaxPageSize(100);
        resolvers.add(resolver);
    }
}
