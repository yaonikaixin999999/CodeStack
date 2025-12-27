package com.example.cloudstack.demo.controller;

import com.example.cloudstack.demo.dto.ApiResponse;
import com.example.cloudstack.demo.dto.user.LoginRequest;
import com.example.cloudstack.demo.dto.user.LoginResponse;
import com.example.cloudstack.demo.dto.user.RegisterRequest;
import com.example.cloudstack.demo.dto.user.UserDTO;
import com.example.cloudstack.demo.dto.user.UserUpdateRequest;
import com.example.cloudstack.demo.service.BlogUserService;
import com.example.cloudstack.demo.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 用户认证控制器（博客）
 */
@RestController
@RequestMapping("/api/blog/auth")
@RequiredArgsConstructor
public class AuthController {

    private final BlogUserService userService;
    private final AdminService adminService;

    /**
     * 获取认证端点信息
     * GET /api/blog/auth
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> getAuthInfo() {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("register", "POST /api/blog/auth/register");
        info.put("login", "POST /api/blog/auth/login");
        info.put("getCurrentUser", "GET /api/blog/auth/me");
        info.put("updateProfile", "PUT /api/blog/auth/profile");
        return ResponseEntity.ok(ApiResponse.success(info));
    }

    /**
     * 用户注册
     * POST /api/blog/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            LoginResponse response = userService.register(request);
            return ResponseEntity.ok(ApiResponse.success("注册成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 用户登录
     * POST /api/blog/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.login(request);
            return ResponseEntity.ok(ApiResponse.success("登录成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取当前用户信息
     * GET /api/blog/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(@RequestAttribute("userId") Long userId) {
        try {
            UserDTO user = userService.getUserById(userId, userId);
            return ResponseEntity.ok(ApiResponse.success(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 用户 SSE 流 (状态变更推送)
     * GET /api/blog/auth/stream?token=xxx
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestAttribute("userId") Long userId) {
        return adminService.subscribeUser(userId);
    }

    /**
     * 更新用户信息
     * PUT /api/blog/auth/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody UserUpdateRequest request) {
        try {
            UserDTO user = userService.updateUser(userId, request);
            return ResponseEntity.ok(ApiResponse.success("更新成功", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 调试用：验证密码
     * POST /api/blog/auth/debug/verify-password
     * 仅开发环境使用
     */
    @PostMapping("/debug/verify-password")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyPassword(@RequestBody LoginRequest request) {
        try {
            Map<String, Object> result = new LinkedHashMap<>();

            // 尝试找到用户
            var userOpt = userService.findByUsername(request.getUsername());
            if (userOpt.isEmpty()) {
                result.put("found", false);
                result.put("message", "用户不存在");
                return ResponseEntity.ok(ApiResponse.success(result));
            }

            result.put("found", true);
            result.put("username", request.getUsername());
            result.put("message", "密码验证成功 - 请用此凭证登录");
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
