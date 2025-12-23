package com.example.cloudstack.demo.service;

import com.example.cloudstack.demo.dto.category.CategoryDTO;
import com.example.cloudstack.demo.model.entity.Category;
import com.example.cloudstack.demo.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 获取所有分类(树形结构)
     */
    public List<CategoryDTO> getAllCategories() {
        List<Category> rootCategories = categoryRepository.findByParentIdIsNullAndStatusOrderBySortOrder(1);
        return rootCategories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取分类详情
     */
    public CategoryDTO getCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        return convertToDTO(category);
    }

    /**
     * 根据 slug 获取分类
     */
    public CategoryDTO getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        return convertToDTO(category);
    }

    /**
     * 创建分类(管理员)
     */
    @Transactional
    public CategoryDTO createCategory(String name, String slug, String description,
            String icon, String coverImage, Long parentId, Integer sortOrder) {
        if (categoryRepository.existsBySlug(slug)) {
            throw new RuntimeException("分类别名已存在");
        }

        Category category = Category.builder()
                .name(name)
                .slug(slug)
                .description(description)
                .icon(icon)
                .coverImage(coverImage)
                .parentId(parentId)
                .sortOrder(sortOrder != null ? sortOrder : 0)
                .status(1)
                .build();

        category = categoryRepository.save(category);
        return convertToDTO(category);
    }

    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .icon(category.getIcon())
                .coverImage(category.getCoverImage())
                .parentId(category.getParentId())
                .sortOrder(category.getSortOrder())
                .postCount(category.getPostCount())
                .build();

        // 加载子分类
        List<Category> children = categoryRepository.findByParentIdAndStatus(category.getId(), 1);
        if (!children.isEmpty()) {
            dto.setChildren(children.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
