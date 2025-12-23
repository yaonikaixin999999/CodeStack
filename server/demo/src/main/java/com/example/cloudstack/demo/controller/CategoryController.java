package com.example.cloudstack.demo.controller;

import com.example.cloudstack.demo.dto.category.CategoryDTO;
import com.example.cloudstack.demo.dto.ApiResponse;
import com.example.cloudstack.demo.dto.common.PageResponse;
import com.example.cloudstack.demo.dto.post.PostListDTO;
import com.example.cloudstack.demo.service.CategoryService;
import com.example.cloudstack.demo.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final PostService postService;

    /**
     * 获取所有分类
     * GET /api/categories
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories() {
        try {
            List<CategoryDTO> categories = categoryService.getAllCategories();
            return ResponseEntity.ok(ApiResponse.success(categories));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取分类详情
     * GET /api/categories/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategory(@PathVariable Long id) {
        try {
            CategoryDTO category = categoryService.getCategory(id);
            return ResponseEntity.ok(ApiResponse.success(category));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 根据 slug 获取分类
     * GET /api/categories/slug/{slug}
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategoryBySlug(@PathVariable String slug) {
        try {
            CategoryDTO category = categoryService.getCategoryBySlug(slug);
            return ResponseEntity.ok(ApiResponse.success(category));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取分类下的文章
     * GET /api/categories/{id}/posts
     */
    @GetMapping("/{id}/posts")
    public ResponseEntity<ApiResponse<PageResponse<PostListDTO>>> getCategoryPosts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestAttribute(value = "userId", required = false) Long currentUserId) {
        try {
            PageResponse<PostListDTO> posts = postService.getPostsByCategory(id, page, size, currentUserId);
            return ResponseEntity.ok(ApiResponse.success(posts));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 根据 slug 获取分类下的文章
     * GET /api/categories/slug/{slug}/posts
     */
    @GetMapping("/slug/{slug}/posts")
    public ResponseEntity<ApiResponse<PageResponse<PostListDTO>>> getCategoryPostsBySlug(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestAttribute(value = "userId", required = false) Long currentUserId) {
        try {
            CategoryDTO category = categoryService.getCategoryBySlug(slug);
            PageResponse<PostListDTO> posts = postService.getPostsByCategory(category.getId(), page, size,
                    currentUserId);
            return ResponseEntity.ok(ApiResponse.success(posts));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
