package com.example.cloudstack.demo.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDTO {

    private Long id;
    private Long postId;
    private Long parentId;
    private String content;
    private Integer likeCount;
    private Integer replyCount;
    private Boolean isTop;
    private LocalDateTime createdAt;

    // 评论者信息
    private UserDTO user;

    // 回复目标用户(如果是回复)
    private UserDTO replyToUser;

    // 子评论列表(仅顶层评论有)
    private List<CommentDTO> replies;

    // 当前用户是否点赞
    private Boolean isLiked;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserDTO {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
        private Integer level;
    }
}
