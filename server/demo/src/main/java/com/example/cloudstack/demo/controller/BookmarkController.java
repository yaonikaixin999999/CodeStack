package com.example.cloudstack.demo.controller;

import com.example.cloudstack.demo.dto.ApiResponse;
import com.example.cloudstack.demo.dto.common.PageResponse;
import com.example.cloudstack.demo.dto.post.PostListDTO;
import com.example.cloudstack.demo.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 收藏控制器
 */
@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    /**
     * 获取用户收藏列表
     * GET /api/bookmarks
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PostListDTO>>> getBookmarks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestAttribute("userId") Long userId) {
        try {
            PageResponse<PostListDTO> bookmarks = bookmarkService.getUserBookmarks(userId, page, size);
            return ResponseEntity.ok(ApiResponse.success(bookmarks));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 检查是否已收藏
     * GET /api/bookmarks/check/{postId}
     */
    @GetMapping("/check/{postId}")
    public ResponseEntity<ApiResponse<Boolean>> checkBookmark(
            @PathVariable Long postId,
            @RequestAttribute("userId") Long userId) {
        try {
            boolean isBookmarked = bookmarkService.isBookmarked(userId, postId);
            return ResponseEntity.ok(ApiResponse.success(isBookmarked));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
