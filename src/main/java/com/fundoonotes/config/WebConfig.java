
package com.fundoonotes.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Logging and Debugging - Web Configuration
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Registers the request logging interceptor with Spring MVC.
 * Applies request logging to the application's HTTP endpoints.
 */

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RequestLoggingInterceptor requestLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(requestLoggingInterceptor);
    }
}