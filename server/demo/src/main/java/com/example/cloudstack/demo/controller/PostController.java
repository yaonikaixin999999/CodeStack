package com.example.cloudstack.demo.controller;

import com.example.cloudstack.demo.dto.ApiResponse;
import com.example.cloudstack.demo.dto.common.PageResponse;
import com.example.cloudstack.demo.dto.post.*;
import com.example.cloudstack.demo.service.BookmarkService;
import com.example.cloudstack.demo.service.LikeService;
import com.example.cloudstack.demo.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章控制器
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final LikeService likeService;
    private final BookmarkService bookmarkService;

    /**
     * 获取文章列表
     * GET /api/posts?page=0&size=10&categoryId=1
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PostListDTO>>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestAttribute(value = "userId", required = false) Long currentUserId) {
        try {
            PageResponse<PostListDTO> posts = postService.getPosts(page, size, categoryId, tagId, authorId, keyword,
                    sort, currentUserId);
            return ResponseEntity.ok(ApiResponse.success(posts));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取文章详情
     * GET /api/posts/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDTO>> getPost(
            @PathVariable Long id,
            @RequestAttribute(value = "userId", required = false) Long currentUserId) {
        try {
            PostDTO post = postService.getPost(id, currentUserId);
            return ResponseEntity.ok(ApiResponse.success(post));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 根据 slug 获取文章
     * GET /api/posts/slug/{slug}
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<PostDTO>> getPostBySlug(
            @PathVariable String slug,
            @RequestAttribute(value = "userId", required = false) Long currentUserId) {
        try {
            PostDTO post = postService.getPostBySlug(slug, currentUserId);
            return ResponseEntity.ok(ApiResponse.success(post));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 创建文章
     * POST /api/posts
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PostDTO>> createPost(
            @Valid @RequestBody PostRequest request,
            @RequestAttribute("userId") Long userId) {
        try {
            PostDTO post = postService.createPost(userId, request);
            return ResponseEntity.ok(ApiResponse.success("创建成功", post));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 更新文章
     * PUT /api/posts/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDTO>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest request,
            @RequestAttribute("userId") Long userId) {
        try {
            PostDTO post = postService.updatePost(id, userId, request);
            return ResponseEntity.ok(ApiResponse.success("更新成功", post));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 删除文章
     * DELETE /api/posts/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId,
            @RequestAttribute(value = "isAdmin", required = false) Boolean isAdmin) {
        try {
            postService.deletePost(id, userId, isAdmin != null && isAdmin);
            return ResponseEntity.ok(ApiResponse.successMsg("删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 搜索文章
     * GET /api/posts/search?keyword=xxx
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<PostListDTO>>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestAttribute(value = "userId", required = false) Long currentUserId) {
        try {
            PageResponse<PostListDTO> posts = postService.searchPosts(keyword, page, size, currentUserId);
            return ResponseEntity.ok(ApiResponse.success(posts));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取热门文章
     * GET /api/posts/hot
     */
    @GetMapping("/hot")
    public ResponseEntity<ApiResponse<List<PostListDTO>>> getHotPosts(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<PostListDTO> posts = postService.getHotPosts(limit);
            return ResponseEntity.ok(ApiResponse.success(posts));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取精选文章
     * GET /api/posts/featured
     */
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<PostListDTO>>> getFeaturedPosts(
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<PostListDTO> posts = postService.getFeaturedPosts(limit);
            return ResponseEntity.ok(ApiResponse.success(posts));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取用户文章
     * GET /api/posts/user/{username}
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<ApiResponse<PageResponse<PostListDTO>>> getUserPosts(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestAttribute(value = "userId", required = false) Long currentUserId) {
        try {
            PageResponse<PostListDTO> posts = postService.getPostsByUser(username, page, size, currentUserId);
            return ResponseEntity.ok(ApiResponse.success(posts));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 点赞文章
     * POST /api/posts/{id}/like
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponse<Void>> likePost(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        try {
            likeService.likePost(userId, id);
            return ResponseEntity.ok(ApiResponse.successMsg("点赞成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 取消点赞
     * DELETE /api/posts/{id}/like
     */
    @DeleteMapping("/{id}/like")
    public ResponseEntity<ApiResponse<Void>> unlikePost(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        try {
            likeService.unlikePost(userId, id);
            return ResponseEntity.ok(ApiResponse.successMsg("取消点赞成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 收藏文章
     * POST /api/posts/{id}/bookmark
     */
    @PostMapping("/{id}/bookmark")
    public ResponseEntity<ApiResponse<Void>> bookmarkPost(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId,
            @RequestParam(required = false) Long folderId,
            @RequestParam(required = false) String note) {
        try {
            bookmarkService.bookmark(userId, id, folderId, note);
            return ResponseEntity.ok(ApiResponse.successMsg("收藏成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 取消收藏
     * DELETE /api/posts/{id}/bookmark
     */
    @DeleteMapping("/{id}/bookmark")
    public ResponseEntity<ApiResponse<Void>> unbookmarkPost(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        try {
            bookmarkService.unbookmark(userId, id);
            return ResponseEntity.ok(ApiResponse.successMsg("取消收藏成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
