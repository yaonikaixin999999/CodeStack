package com.example.cloudstack.demo.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 创建/更新文章请求
 */
@Data
public class PostRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题最多200个字符")
    private String title;

    @Size(max = 500, message = "摘要最多500个字符")
    private String excerpt;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String coverImage;

    private Long categoryId;

    private List<Long> tagIds;

    private List<String> newTags; // 新建的标签名称

    private Boolean isOriginal = true;

    private String sourceUrl;

    private Boolean allowComment = true;

    private Integer status = 0; // 0-草稿, 1-发布
}
