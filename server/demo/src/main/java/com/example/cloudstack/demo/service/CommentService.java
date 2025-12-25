package com.example.cloudstack.demo.service;

import com.example.cloudstack.demo.dto.common.PageResponse;
import com.example.cloudstack.demo.dto.comment.*;
import com.example.cloudstack.demo.model.entity.*;
import com.example.cloudstack.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论服务
 */
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final NotificationService notificationService;

    /**
     * 创建评论
     */
    @Transactional
    public CommentDTO createComment(Long userId, CommentRequest request, String ipAddress, String userAgent) {
        // 检查文章是否存在
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("文章不存在"));

        // 检查是否允许评论
        if (!post.getAllowComment()) {
            throw new RuntimeException("该文章不允许评论");
        }

        // 如果是回复，检查父评论是否存在
        if (request.getParentId() != null) {
            if (!commentRepository.existsById(request.getParentId())) {
                throw new RuntimeException("回复的评论不存在");
            }
        }

        Comment comment = Comment.builder()
                .postId(request.getPostId())
                .userId(userId)
                .parentId(request.getParentId())
                .replyToUserId(request.getReplyToUserId())
                .content(request.getContent())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .status(1) // 直接发布
                .build();

        comment = commentRepository.save(comment);

        // 更新文章评论数
        postRepository.updateCommentCount(request.getPostId(), 1);

        // 更新父评论回复数
        if (request.getParentId() != null) {
            commentRepository.updateReplyCount(request.getParentId(), 1);
        }

        // 发送通知
        if (!post.getUserId().equals(userId)) {
            notificationService.sendNotification(
                    post.getUserId(),
                    userId,
                    "comment",
                    "评论了你的文章",
                    "有人评论了你的文章《" + post.getTitle() + "》",
                    post.getId(),
                    "post");
        }

        // 如果是回复，通知被回复的用户
        if (request.getReplyToUserId() != null && !request.getReplyToUserId().equals(userId)) {
            notificationService.sendNotification(
                    request.getReplyToUserId(),
                    userId,
                    "reply",
                    "回复了你的评论",
                    request.getContent(),
                    comment.getId(),
                    "comment");
        }

        return convertToDTO(comment, userId);
    }

    /**
     * 删除评论
     */
    @Transactional
    public void deleteComment(Long commentId, Long userId, boolean isAdmin) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        // 检查权限
        if (!isAdmin && !comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此评论");
        }

        // 软删除
        comment.setStatus(2);
        commentRepository.save(comment);

        // 更新文章评论数
        postRepository.updateCommentCount(comment.getPostId(), -1);

        // 更新父评论回复数
        if (comment.getParentId() != null) {
            commentRepository.updateReplyCount(comment.getParentId(), -1);
        }
    }

    /**
     * 获取文章评论列表
     */
    public PageResponse<CommentDTO> getPostComments(Long postId, int page, int size, Long currentUserId) {
        Page<Comment> comments = commentRepository.findTopLevelCommentsByPostId(postId, PageRequest.of(page, size));

        List<CommentDTO> commentDTOs = comments.getContent().stream()
                .map(comment -> {
                    CommentDTO dto = convertToDTO(comment, currentUserId);
                    // 加载回复
                    List<Comment> replies = commentRepository.findRepliesByParentId(comment.getId());
                    dto.setReplies(replies.stream()
                            .map(reply -> convertToDTO(reply, currentUserId))
                            .collect(Collectors.toList()));
                    return dto;
                })
                .collect(Collectors.toList());

        return PageResponse.of(commentDTOs, page, size, comments.getTotalElements());
    }

    /**
     * 转换为DTO
     */
    private CommentDTO convertToDTO(Comment comment, Long currentUserId) {
        CommentDTO dto = CommentDTO.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .parentId(comment.getParentId())
                .content(comment.getContent())
                .likeCount(comment.getLikeCount())
                .replyCount(comment.getReplyCount())
                .isTop(comment.getIsTop())
                .createdAt(comment.getCreatedAt())
                .build();

        // 评论者信息
        userRepository.findById(comment.getUserId()).ifPresent(user -> {
            dto.setAuthor(CommentDTO.UserDTO.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .avatar(user.getAvatar())
                    .level(user.getLevel())
                    .build());
        });

        // 回复目标用户
        if (comment.getReplyToUserId() != null) {
            userRepository.findById(comment.getReplyToUserId()).ifPresent(user -> {
                dto.setReplyToUser(CommentDTO.UserDTO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .avatar(user.getAvatar())
                        .level(user.getLevel())
                        .build());
            });
        }

        // 当前用户是否点赞
        if (currentUserId != null) {
            dto.setIsLiked(likeRepository.existsByUserIdAndTargetIdAndTargetType(
                    currentUserId, comment.getId(), Like.TargetType.comment));
        }

        return dto;
    }
}
