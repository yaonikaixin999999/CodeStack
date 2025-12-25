package com.example.cloudstack.demo.repository;

import com.example.cloudstack.demo.model.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

/**
 * 评论数据访问接口
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 查询文章的顶层评论
    @Query("SELECT c FROM Comment c WHERE c.postId = :postId AND c.parentId IS NULL AND c.status = 1 ORDER BY c.isTop DESC, c.createdAt DESC")
    Page<Comment> findTopLevelCommentsByPostId(@Param("postId") Long postId, Pageable pageable);

    // 查询评论的回复
    @Query("SELECT c FROM Comment c WHERE c.parentId = :parentId AND c.status = 1 ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentId(@Param("parentId") Long parentId);

    // 查询用户的评论
    Page<Comment> findByUserIdAndStatus(Long userId, Integer status, Pageable pageable);

    // 统计文章评论数
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.postId = :postId AND c.status = 1")
    long countByPostId(@Param("postId") Long postId);

    // 增加点赞数
    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount + :delta WHERE c.id = :commentId")
    void updateLikeCount(@Param("commentId") Long commentId, @Param("delta") int delta);

    // 增加回复数
    @Modifying
    @Query("UPDATE Comment c SET c.replyCount = c.replyCount + :delta WHERE c.id = :commentId")
    void updateReplyCount(@Param("commentId") Long commentId, @Param("delta") int delta);

    List<Comment> findByStatusAndCreatedAtAfter(Integer status, LocalDateTime createdAt);
}
