package com.opentext.guesstheword.config;

import com.opentext.guesstheword.model.Role;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor(null)).addPathPatterns("/");
        registry.addInterceptor(new AuthInterceptor(Role.PLAYER)).addPathPatterns("/play", "/api/game/**");
        registry.addInterceptor(new AuthInterceptor(Role.ADMIN)).addPathPatterns("/admin");
    }
}
