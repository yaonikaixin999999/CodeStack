package com.example.cloudstack.demo.controller;

import com.example.cloudstack.demo.dto.comment.CommentDTO;
import com.example.cloudstack.demo.dto.comment.CommentRequest;
import com.example.cloudstack.demo.dto.ApiResponse;
import com.example.cloudstack.demo.dto.common.PageResponse;
import com.example.cloudstack.demo.service.CommentService;
import com.example.cloudstack.demo.service.LikeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 评论控制器
 */
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final LikeService likeService;

    /**
     * 获取评论端点信息
     * GET /api/comments
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> getCommentInfo() {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("getPostComments", "GET /api/comments/post/{postId}");
        info.put("createComment", "POST /api/comments");
        info.put("getComment", "GET /api/comments/{id}");
        info.put("updateComment", "PUT /api/comments/{id}");
        info.put("deleteComment", "DELETE /api/comments/{id}");
        return ResponseEntity.ok(ApiResponse.success(info));
    }

    /**
     * 获取文章评论
     * GET /api/comments/post/{postId}
     */
    @GetMapping("/post/{postId}")
    public ResponseEntity<ApiResponse<PageResponse<CommentDTO>>> getPostComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestAttribute(value = "userId", required = false) Long currentUserId) {
        try {
            PageResponse<CommentDTO> comments = commentService.getPostComments(postId, page, size, currentUserId);
            return ResponseEntity.ok(ApiResponse.success(comments));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 创建评论
     * POST /api/comments
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CommentDTO>> createComment(
            @Valid @RequestBody CommentRequest request,
            @RequestAttribute("userId") Long userId,
            HttpServletRequest httpRequest) {
        try {
            String ipAddress = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            CommentDTO comment = commentService.createComment(userId, request, ipAddress, userAgent);
            return ResponseEntity.ok(ApiResponse.success("评论成功", comment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 删除评论
     * DELETE /api/comments/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        try {
            commentService.deleteComment(id, userId);
            return ResponseEntity.ok(ApiResponse.successMsg("删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 点赞评论
     * POST /api/comments/{id}/like
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponse<Void>> likeComment(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        try {
            likeService.likeComment(userId, id);
            return ResponseEntity.ok(ApiResponse.successMsg("点赞成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 取消点赞评论
     * DELETE /api/comments/{id}/like
     */
    @DeleteMapping("/{id}/like")
    public ResponseEntity<ApiResponse<Void>> unlikeComment(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        try {
            likeService.unlikeComment(userId, id);
            return ResponseEntity.ok(ApiResponse.successMsg("取消点赞成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }
}
