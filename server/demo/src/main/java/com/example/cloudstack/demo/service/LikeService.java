package com.example.cloudstack.demo.service;

import com.example.cloudstack.demo.model.entity.*;
import com.example.cloudstack.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 点赞服务
 */
@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;

    /**
     * 点赞文章
     */
    @Transactional
    public boolean likePost(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("文章不存在"));

        // 检查是否已点赞
        if (likeRepository.existsByUserIdAndTargetIdAndTargetType(userId, postId, Like.TargetType.post)) {
            throw new RuntimeException("已经点赞过了");
        }

        // 创建点赞记录
        Like like = Like.builder()
                .userId(userId)
                .targetId(postId)
                .targetType(Like.TargetType.post)
                .build();
        likeRepository.save(like);

        // 更新文章点赞数
        postRepository.updateLikeCount(postId, 1);

        // 发送通知
        if (!post.getUserId().equals(userId)) {
            notificationService.sendNotification(
                    post.getUserId(),
                    userId,
                    "like",
                    "点赞了你的文章",
                    "有人点赞了你的文章《" + post.getTitle() + "》",
                    postId,
                    "post");
        }

        return true;
    }

    /**
     * 取消点赞文章
     */
    @Transactional
    public boolean unlikePost(Long userId, Long postId) {
        if (!likeRepository.existsByUserIdAndTargetIdAndTargetType(userId, postId, Like.TargetType.post)) {
            throw new RuntimeException("尚未点赞");
        }

        likeRepository.deleteByUserIdAndTargetIdAndTargetType(userId, postId, Like.TargetType.post);
        postRepository.updateLikeCount(postId, -1);

        return true;
    }

    /**
     * 点赞评论
     */
    @Transactional
    public boolean likeComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        if (likeRepository.existsByUserIdAndTargetIdAndTargetType(userId, commentId, Like.TargetType.comment)) {
            throw new RuntimeException("已经点赞过了");
        }

        Like like = Like.builder()
                .userId(userId)
                .targetId(commentId)
                .targetType(Like.TargetType.comment)
                .build();
        likeRepository.save(like);

        commentRepository.updateLikeCount(commentId, 1);

        // 发送通知
        if (!comment.getUserId().equals(userId)) {
            notificationService.sendNotification(
                    comment.getUserId(),
                    userId,
                    "like",
                    "点赞了你的评论",
                    comment.getContent().length() > 50 ? comment.getContent().substring(0, 50) + "..."
                            : comment.getContent(),
                    commentId,
                    "comment");
        }

        return true;
    }

    /**
     * 取消点赞评论
     */
    @Transactional
    public boolean unlikeComment(Long userId, Long commentId) {
        if (!likeRepository.existsByUserIdAndTargetIdAndTargetType(userId, commentId, Like.TargetType.comment)) {
            throw new RuntimeException("尚未点赞");
        }

        likeRepository.deleteByUserIdAndTargetIdAndTargetType(userId, commentId, Like.TargetType.comment);
        commentRepository.updateLikeCount(commentId, -1);

        return true;
    }

    /**
     * 检查是否已点赞
     */
    public boolean isLiked(Long userId, Long targetId, Like.TargetType targetType) {
        return likeRepository.existsByUserIdAndTargetIdAndTargetType(userId, targetId, targetType);
    }
}
