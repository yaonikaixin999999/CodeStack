package com.example.cloudstack.demo.controller;

import com.example.cloudstack.demo.dto.ApiResponse;
import com.example.cloudstack.demo.dto.common.PageResponse;
import com.example.cloudstack.demo.dto.user.UserDTO;
import com.example.cloudstack.demo.service.BlogUserService;
import com.example.cloudstack.demo.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class BlogUserController {

    private final BlogUserService blogUserService;
    private final FollowService followService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(
            @PathVariable Long id,
            @RequestAttribute(value = "userId", required = false) Long currentUserId) {
        try {
            return ResponseEntity.ok(ApiResponse.success(blogUserService.getUserById(id, currentUserId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<UserDTO>>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestAttribute(value = "userId", required = false) Long currentUserId) {
        try {
            return ResponseEntity
                    .ok(ApiResponse.success(blogUserService.searchUsers(keyword, page, size, currentUserId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/follow")
    public ResponseEntity<ApiResponse<Void>> follow(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        try {
            followService.follow(userId, id);
            return ResponseEntity.ok(ApiResponse.successMsg("关注成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/follow")
    public ResponseEntity<ApiResponse<Void>> unfollow(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        try {
            followService.unfollow(userId, id);
            return ResponseEntity.ok(ApiResponse.successMsg("取消关注成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}/following")
    public ResponseEntity<ApiResponse<PageResponse<UserDTO>>> getFollowing(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestAttribute(value = "userId", required = false) Long currentUserId) {
        try {
            return ResponseEntity.ok(ApiResponse.success(followService.getFollowing(id, page, size, currentUserId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}/followers")
    public ResponseEntity<ApiResponse<PageResponse<UserDTO>>> getFollowers(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestAttribute(value = "userId", required = false) Long currentUserId) {
        try {
            return ResponseEntity.ok(ApiResponse.success(followService.getFollowers(id, page, size, currentUserId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
