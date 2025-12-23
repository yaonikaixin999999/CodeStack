package com.example.cloudstack.demo.controller;

import com.example.cloudstack.demo.dto.ApiResponse;
import com.example.cloudstack.demo.dto.common.PageResponse;
import com.example.cloudstack.demo.dto.post.PostListDTO;
import com.example.cloudstack.demo.dto.tag.TagDTO;
import com.example.cloudstack.demo.service.PostService;
import com.example.cloudstack.demo.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签控制器
 */
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;
    private final PostService postService;

    /**
     * 获取所有标签
     * GET /api/tags
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TagDTO>>> getAllTags() {
        try {
            List<TagDTO> tags = tagService.getAllTags();
            return ResponseEntity.ok(ApiResponse.success(tags));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取热门标签
     * GET /api/tags/hot
     */
    @GetMapping("/hot")
    public ResponseEntity<ApiResponse<List<TagDTO>>> getHotTags(
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<TagDTO> tags = tagService.getHotTags(limit);
            return ResponseEntity.ok(ApiResponse.success(tags));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 搜索标签
     * GET /api/tags/search?keyword=xxx
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<TagDTO>>> searchTags(@RequestParam String keyword) {
        try {
            List<TagDTO> tags = tagService.searchTags(keyword);
            return ResponseEntity.ok(ApiResponse.success(tags));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取标签详情
     * GET /api/tags/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TagDTO>> getTag(@PathVariable Long id) {
        try {
            TagDTO tag = tagService.getTag(id);
            return ResponseEntity.ok(ApiResponse.success(tag));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 根据 slug 获取标签
     * GET /api/tags/slug/{slug}
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<TagDTO>> getTagBySlug(@PathVariable String slug) {
        try {
            TagDTO tag = tagService.getTagBySlug(slug);
            return ResponseEntity.ok(ApiResponse.success(tag));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取标签下的文章
     * GET /api/tags/{id}/posts
     */
    @GetMapping("/{id}/posts")
    public ResponseEntity<ApiResponse<PageResponse<PostListDTO>>> getTagPosts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestAttribute(value = "userId", required = false) Long currentUserId) {
        try {
            PageResponse<PostListDTO> posts = postService.getPostsByTag(id, page, size, currentUserId);
            return ResponseEntity.ok(ApiResponse.success(posts));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 根据 slug 获取标签下的文章
     * GET /api/tags/slug/{slug}/posts
     */
    @GetMapping("/slug/{slug}/posts")
    public ResponseEntity<ApiResponse<PageResponse<PostListDTO>>> getTagPostsBySlug(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestAttribute(value = "userId", required = false) Long currentUserId) {
        try {
            TagDTO tag = tagService.getTagBySlug(slug);
            PageResponse<PostListDTO> posts = postService.getPostsByTag(tag.getId(), page, size, currentUserId);
            return ResponseEntity.ok(ApiResponse.success(posts));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
