package com.example.cloudstack.demo.controller;

import com.example.cloudstack.demo.dto.ApiResponse;
import com.example.cloudstack.demo.dto.LoginRequest;
import com.example.cloudstack.demo.dto.RegisterRequest;
import com.example.cloudstack.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(originPatterns = "*", allowCredentials = "true") // 允许所有来源跨域访问
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public ApiResponse<Map<String, String>> login(@RequestBody LoginRequest request) {
        try {
            log.info("收到登录请求，用户名: {}", request.getUsername());
            String token = userService.login(request.getUsername(), request.getPassword());
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("username", request.getUsername());
            return ApiResponse.success("登录成功", response);
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ApiResponse<Map<String, String>> register(@RequestBody RegisterRequest request) {
        try {
            log.info("收到注册请求，用户名: {}", request.getUsername());
            String token = userService.register(request.getUsername(), request.getPassword(), request.getPhone());
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("username", request.getUsername());
            return ApiResponse.success("注册成功", response);
        } catch (Exception e) {
            log.error("注册失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    // 测试端点
    @GetMapping("/test")
    public String test() {
        return "Auth API is working!";
    }
}