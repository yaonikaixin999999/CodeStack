package com.example.cloudstack.demo.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章列表项DTO(精简版)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostListDTO {

    private Long id;
    private String title;
    private String slug;
    private String excerpt;
    private String coverImage;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isTop;
    private Boolean isFeatured;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;

    // 作者信息
    private AuthorDTO author;

    // 分类信息
    private CategoryDTO category;

    // 标签列表
    private List<TagDTO> tags;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthorDTO {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryDTO {
        private Long id;
        private String name;
        private String slug;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TagDTO {
        private Long id;
        private String name;
        private String color;
    }
}
