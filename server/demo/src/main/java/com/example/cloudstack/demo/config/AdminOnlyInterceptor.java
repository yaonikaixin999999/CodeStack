package com.example.cloudstack.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

@Component
public class AdminOnlyInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    public AdminOnlyInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        Long userId = (Long) request.getAttribute("userId");
        Boolean isAdmin = (Boolean) request.getAttribute("isAdmin");
        if (userId == null || isAdmin == null || !isAdmin) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "需要管理员权限");
            result.put("code", 403);
            response.getWriter().write(objectMapper.writeValueAsString(result));
            return false;
        }
        return true;
    }
}
