package com.example.cloudstack.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.cloudstack.demo.model.entity.User;
import com.example.cloudstack.demo.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证拦截器 - 用于需要登录的接口
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // OPTIONS 请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        Long userId = (Long) request.getAttribute("userId");

        if (userId == null) {
            sendUnauthorizedResponse(response, "请先登录");
            return false;
        }

        // 再次检查用户状态，避免被封禁/禁言后仍可用旧 token 访问
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            sendUnauthorizedResponse(response, "用户不存在或已删除");
            return false;
        }

        Integer status = user.getStatus() == null ? 0 : user.getStatus();
        // -1 封禁：所有接口拒绝
        if (status == -1) {
            sendForbiddenResponse(response, "账号已被封禁");
            return false;
        }
        // 0 禁言：仅允许只读请求（GET/HEAD/OPTIONS），写操作拦截
        if (status == 0) {
            String method = request.getMethod() != null ? request.getMethod().toUpperCase() : "";
            boolean isReadOnly = "GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method);
            if (!isReadOnly) {
                sendForbiddenResponse(response, "账号被禁言，暂时无法发布/评论等操作");
                return false;
            }
        }

        return true;
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        result.put("code", 401);

        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    private void sendForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        result.put("code", 403);

        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
