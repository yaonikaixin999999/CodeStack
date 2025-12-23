package com.example.cloudstack.demo.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 文章实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(unique = true, length = 200)
    private String slug;

    @Column(length = 500)
    private String excerpt;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    @Column(name = "content_html", columnDefinition = "LONGTEXT")
    private String contentHtml;

    @Column(name = "cover_image", columnDefinition = "LONGTEXT")
    private String coverImage;

    @Column(name = "word_count", columnDefinition = "INT UNSIGNED DEFAULT 0")
    @Builder.Default
    private Integer wordCount = 0;

    @Column(name = "read_time", columnDefinition = "INT UNSIGNED DEFAULT 0")
    @Builder.Default
    private Integer readTime = 0;

    @Column(name = "view_count", columnDefinition = "INT UNSIGNED DEFAULT 0")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "like_count", columnDefinition = "INT UNSIGNED DEFAULT 0")
    @Builder.Default
    private Integer likeCount = 0;

    @Column(name = "comment_count", columnDefinition = "INT UNSIGNED DEFAULT 0")
    @Builder.Default
    private Integer commentCount = 0;

    @Column(name = "bookmark_count", columnDefinition = "INT UNSIGNED DEFAULT 0")
    @Builder.Default
    private Integer bookmarkCount = 0;

    @Column(name = "share_count", columnDefinition = "INT UNSIGNED DEFAULT 0")
    @Builder.Default
    private Integer shareCount = 0;

    @Column(name = "is_top", columnDefinition = "TINYINT DEFAULT 0")
    @Builder.Default
    private Boolean isTop = false;

    @Column(name = "is_featured", columnDefinition = "TINYINT DEFAULT 0")
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "is_original", columnDefinition = "TINYINT DEFAULT 1")
    @Builder.Default
    private Boolean isOriginal = true;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "allow_comment", columnDefinition = "TINYINT DEFAULT 1")
    @Builder.Default
    private Boolean allowComment = true;

    @Column(columnDefinition = "TINYINT DEFAULT 0")
    @Builder.Default
    private Integer status = 0; // 0-草稿, 1-已发布, 2-待审核, 3-已下架

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 关联关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Category category;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();
}
