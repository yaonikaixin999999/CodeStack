package com.example.cloudstack.demo.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分类DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String icon;
    private String coverImage;
    private Long parentId;
    private Integer sortOrder;
    private Integer postCount;

    // 子分类
    private List<CategoryDTO> children;
}
