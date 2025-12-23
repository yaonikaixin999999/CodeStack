package com.example.cloudstack.demo.repository;

import com.example.cloudstack.demo.model.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 文章数据访问接口
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findBySlug(String slug);

    // 查询已发布的文章
    @Query("SELECT p FROM Post p WHERE p.status = 1 AND p.deletedAt IS NULL ORDER BY p.isTop DESC, p.publishedAt DESC")
    Page<Post> findPublishedPosts(Pageable pageable);

    // 根据用户ID查询文章
    Page<Post> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);

    // 根据分类ID查询文章
    @Query("SELECT p FROM Post p WHERE p.categoryId = :categoryId AND p.status = 1 AND p.deletedAt IS NULL")
    Page<Post> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    // 搜索文章
    @Query("SELECT p FROM Post p WHERE (p.title LIKE %:keyword% OR p.excerpt LIKE %:keyword%) AND p.status = 1 AND p.deletedAt IS NULL")
    Page<Post> searchPosts(@Param("keyword") String keyword, Pageable pageable);

    // 查询精选文章
    @Query("SELECT p FROM Post p WHERE p.isFeatured = true AND p.status = 1 AND p.deletedAt IS NULL ORDER BY p.publishedAt DESC")
    List<Post> findFeaturedPosts(Pageable pageable);

    // 查询热门文章(按浏览量)
    @Query("SELECT p FROM Post p WHERE p.status = 1 AND p.deletedAt IS NULL ORDER BY p.viewCount DESC")
    List<Post> findHotPosts(Pageable pageable);

    // 查询用户的草稿
    @Query("SELECT p FROM Post p WHERE p.userId = :userId AND p.status = 0 AND p.deletedAt IS NULL")
    Page<Post> findDraftsByUserId(@Param("userId") Long userId, Pageable pageable);

    // 增加浏览量
    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") Long postId);

    // 增加点赞数
    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + :delta WHERE p.id = :postId")
    void updateLikeCount(@Param("postId") Long postId, @Param("delta") int delta);

    // 增加评论数
    @Modifying
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + :delta WHERE p.id = :postId")
    void updateCommentCount(@Param("postId") Long postId, @Param("delta") int delta);

    // 增加收藏数
    @Modifying
    @Query("UPDATE Post p SET p.bookmarkCount = p.bookmarkCount + :delta WHERE p.id = :postId")
    void updateBookmarkCount(@Param("postId") Long postId, @Param("delta") int delta);

    // 统计用户文章数
    @Query("SELECT COUNT(p) FROM Post p WHERE p.userId = :userId AND p.status = 1 AND p.deletedAt IS NULL")
    long countByUserId(@Param("userId") Long userId);

    // 根据标签查询文章
    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE t.id = :tagId AND p.status = 1 AND p.deletedAt IS NULL")
    Page<Post> findByTagId(@Param("tagId") Long tagId, Pageable pageable);
}
