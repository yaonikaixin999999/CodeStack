package com.example.cloudstack.demo.dto.tag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagDTO {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String color;
    private Integer postCount;
}
