package com.example.cloudstack.demo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

        private final AuthInterceptor authInterceptor;
        private final AdminOnlyInterceptor adminOnlyInterceptor;

        @Override
        public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                                .allowedOriginPatterns("*")
                                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                                .allowedHeaders("*")
                                .allowCredentials(true)
                                .maxAge(3600);
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
                // 需要登录的接口
                registry.addInterceptor(authInterceptor)
                                .addPathPatterns(
                                                "/api/auth/me",
                                                "/api/auth/profile",
                                                "/api/posts", // POST 创建文章
                                                "/api/posts/*/like",
                                                "/api/posts/*/bookmark",
                                                "/api/comments", // POST 创建评论
                                                "/api/comments/*/like",
                                                "/api/users/*/follow",
                                                "/api/notifications/**",
                                                "/api/bookmarks/**")
                                .excludePathPatterns(
                                                "/api/auth/login",
                                                "/api/auth/register",
                                                "/api/posts/search",
                                                "/api/posts/hot",
                                                "/api/posts/featured",
                                                "/api/posts/slug/**",
                                                "/api/posts/user/**",
                                                "/api/categories/**",
                                                "/api/tags/**",
                                                "/api/comments/post/**",
                                                "/api/users" // GET 搜索用户
                                );
                registry.addInterceptor(adminOnlyInterceptor)
                                .addPathPatterns("/api/admin/**");
        }
}
