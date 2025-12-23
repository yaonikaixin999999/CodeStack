package com.example.cloudstack.demo.controller;

import com.example.cloudstack.demo.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 首页控制器 - 返回 API 状态信息
 */
@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> home() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("name", "CloudStack Blog API");
        info.put("version", "1.0.0");
        info.put("status", "running");
        info.put("time", LocalDateTime.now().toString());

        Map<String, String> endpoints = new LinkedHashMap<>();
        endpoints.put("认证", "/api/blog/auth/");
        endpoints.put("文章", "/api/posts/");
        endpoints.put("评论", "/api/comments/");
        endpoints.put("分类", "/api/categories/");
        endpoints.put("标签", "/api/tags/");
        endpoints.put("通知", "/api/notifications/");
        endpoints.put("收藏", "/api/bookmarks/");

        info.put("endpoints", endpoints);

        return ResponseEntity.ok(ApiResponse.success(info));
    }

    @GetMapping("/api")
    public ResponseEntity<ApiResponse<Map<String, Object>>> api() {
        return home();
    }
}
