package com.example.cloudstack.demo.service;

import com.example.cloudstack.demo.dto.tag.TagDTO;
import com.example.cloudstack.demo.model.entity.Tag;
import com.example.cloudstack.demo.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签服务
 */
@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    /**
     * 获取所有标签
     */
    public List<TagDTO> getAllTags() {
        return tagRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取热门标签
     */
    public List<TagDTO> getHotTags(int limit) {
        return tagRepository.findHotTags(PageRequest.of(0, limit)).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取标签详情
     */
    public TagDTO getTag(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("标签不存在"));
        return convertToDTO(tag);
    }

    /**
     * 根据 slug 获取标签
     */
    public TagDTO getTagBySlug(String slug) {
        Tag tag = tagRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("标签不存在"));
        return convertToDTO(tag);
    }

    /**
     * 搜索标签
     */
    public List<TagDTO> searchTags(String keyword) {
        return tagRepository.findByNameContaining(keyword).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 创建标签
     */
    @Transactional
    public TagDTO createTag(String name, String description, String color) {
        if (tagRepository.existsByName(name)) {
            throw new RuntimeException("标签已存在");
        }

        String slug = name.toLowerCase().replaceAll("[^a-z0-9\\u4e00-\\u9fa5]", "-");
        if (tagRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        Tag tag = Tag.builder()
                .name(name)
                .slug(slug)
                .description(description)
                .color(color != null ? color : "#3a9cff")
                .build();

        tag = tagRepository.save(tag);
        return convertToDTO(tag);
    }

    private TagDTO convertToDTO(Tag tag) {
        return TagDTO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .slug(tag.getSlug())
                .description(tag.getDescription())
                .color(tag.getColor())
                .postCount(tag.getPostCount())
                .build();
    }
}
