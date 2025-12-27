package com.example.cloudstack.demo.controller;

import com.example.cloudstack.demo.dto.ApiResponse;
import com.example.cloudstack.demo.service.AdminService;
import com.example.cloudstack.demo.service.MessageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final MessageService messageService;

    public AdminController(AdminService adminService, MessageService messageService) {
        this.adminService = adminService;
        this.messageService = messageService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dashboard() {
        Map<String, Object> data = adminService.getDashboard(20);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/charts")
    public ResponseEntity<ApiResponse<Map<String, Object>>> charts(
            @RequestParam(defaultValue = "5") Integer postDays,
            @RequestParam(defaultValue = "5") Integer commentDays,
            @RequestParam(defaultValue = "10") Integer loginDays) {
        Map<String, Object> data = adminService.getCharts(postDays, commentDays, loginDays);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listPosts(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(ApiResponse.success(adminService.listPosts(status, limit, q)));
    }

    @GetMapping("/comments")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listComments(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(ApiResponse.success(adminService.listComments(status, limit)));
    }

    @PostMapping("/posts/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approvePost(@PathVariable Long id) {
        adminService.approvePost(id);
        return ResponseEntity.ok(ApiResponse.successMsg("已通过"));
    }

    @PostMapping("/posts/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectPost(@PathVariable Long id) {
        adminService.rejectPost(id);
        return ResponseEntity.ok(ApiResponse.successMsg("已驳回"));
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        adminService.deletePost(id);
        return ResponseEntity.ok(ApiResponse.successMsg("已删除"));
    }

    @PostMapping("/comments/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveComment(@PathVariable Long id) {
        adminService.approveComment(id);
        return ResponseEntity.ok(ApiResponse.successMsg("已通过"));
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long id) {
        adminService.deleteComment(id);
        return ResponseEntity.ok(ApiResponse.successMsg("已删除"));
    }

    @PostMapping("/announcements")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createAnnouncement(
            @RequestBody Map<String, String> payload,
            @RequestAttribute("userId") Long userId) {
        String title = payload != null ? payload.get("title") : null;
        String content = payload != null ? payload.get("content") : null;
        String type = payload != null ? payload.get("type") : null;
        var ann = adminService.createAnnouncement(userId, title, content, type);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", ann.getId());
        result.put("title", ann.getTitle());
        result.put("content", ann.getContent());
        result.put("type", ann.getType());
        result.put("time", ann.getCreatedAt());
        return ResponseEntity.ok(ApiResponse.success("发布成功", result));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listUsers(@RequestParam Map<String, Object> params) {
        return ResponseEntity.ok(ApiResponse.success(adminService.listUsers(params)));
    }

    @PostMapping("/users/{id}/mute")
    public ResponseEntity<ApiResponse<Void>> muteUser(@PathVariable Long id) {
        adminService.muteUser(id);
        return ResponseEntity.ok(ApiResponse.successMsg("已禁言"));
    }

    @PostMapping("/users/{id}/ban")
    public ResponseEntity<ApiResponse<Void>> banUser(@PathVariable Long id) {
        adminService.banUser(id);
        return ResponseEntity.ok(ApiResponse.successMsg("已封禁"));
    }

    @PostMapping("/users/{id}/unban")
    public ResponseEntity<ApiResponse<Void>> unbanUser(@PathVariable Long id) {
        adminService.unbanUser(id);
        return ResponseEntity.ok(ApiResponse.successMsg("已解封"));
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadAvatar(@RequestParam("file") MultipartFile file,
            @RequestAttribute("userId") Long userId) throws Exception {
        String url = adminService.uploadAvatar(file, userId);
        return ResponseEntity.ok(ApiResponse.success(url));
    }

    @GetMapping("/stream")
    public SseEmitter stream() {
        return adminService.subscribe();
    }
}
