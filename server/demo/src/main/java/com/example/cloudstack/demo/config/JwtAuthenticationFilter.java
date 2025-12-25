package com.example.cloudstack.demo.config;

import com.example.cloudstack.demo.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null && jwtUtil.validateToken(token)) {
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            Boolean isAdmin = jwtUtil.isAdminFromToken(token);

            // 将用户信息设置到 request 属性中
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            request.setAttribute("isAdmin", isAdmin);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        // 兼容 SSE 等无法自定义 Header 的请求，支持从查询参数中读取 token
        String tokenParam = request.getParameter("token");
        if (tokenParam != null && !tokenParam.isBlank()) {
            return tokenParam.trim();
        }
        return null;
    }
}
