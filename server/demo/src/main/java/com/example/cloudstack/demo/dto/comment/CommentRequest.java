package com.example.cloudstack.demo.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建评论请求
 */
@Data
public class CommentRequest {

    @NotNull(message = "文章ID不能为空")
    private Long postId;

    private Long parentId; // 回复的评论ID

    private Long replyToUserId; // 回复的用户ID

    @NotBlank(message = "评论内容不能为空")
    private String content;
}
