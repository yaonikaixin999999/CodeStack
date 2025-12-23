package com.example.cloudstack.demo.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDTO {

    private Long id;
    private String title;
    private String slug;
    private String excerpt;
    private String content;
    private String contentHtml;
    private String coverImage;
    private Integer wordCount;
    private Integer readTime;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer bookmarkCount;
    private Boolean isTop;
    private Boolean isFeatured;
    private Boolean isOriginal;
    private String sourceUrl;
    private Boolean allowComment;
    private Integer status;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 作者信息
    private AuthorDTO author;

    // 分类信息
    private CategoryDTO category;

    // 标签列表
    private List<TagDTO> tags;

    // 当前用户交互状态
    private Boolean isLiked;
    private Boolean isBookmarked;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthorDTO {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
        private Integer level;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryDTO {
        private Long id;
        private String name;
        private String slug;
        private String icon;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TagDTO {
        private Long id;
        private String name;
        private String slug;
        private String color;
    }
}
