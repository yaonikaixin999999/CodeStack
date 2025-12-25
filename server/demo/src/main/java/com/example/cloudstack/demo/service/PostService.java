package com.example.cloudstack.demo.service;

import com.example.cloudstack.demo.dto.common.PageResponse;
import com.example.cloudstack.demo.dto.post.*;
import com.example.cloudstack.demo.model.entity.*;
import com.example.cloudstack.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文章服务
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final LikeRepository likeRepository;
    private final BookmarkRepository bookmarkRepository;

    /**
     * 创建文章
     */
    @Transactional
    public PostDTO createPost(Long userId, PostRequest request) {
        // 验证用户存在
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Post post = Post.builder()
                .userId(userId)
                .title(request.getTitle())
                .slug(generateSlug(request.getTitle()))
                .excerpt(request.getExcerpt())
                .content(request.getContent())
                .coverImage(request.getCoverImage())
                .categoryId(request.getCategoryId())
                .isOriginal(request.getIsOriginal())
                .sourceUrl(request.getSourceUrl())
                .allowComment(request.getAllowComment())
                .status(request.getStatus())
                .wordCount(countWords(request.getContent()))
                .readTime(calculateReadTime(request.getContent()))
                .build();

        // 如果是发布状态，设置发布时间
        if (request.getStatus() == 1) {
            post.setPublishedAt(LocalDateTime.now());
        }

        post = postRepository.save(post);

        // 处理标签
        handleTags(post, request.getTagIds(), request.getNewTags());

        // 更新分类文章数
        if (request.getCategoryId() != null) {
            updateCategoryPostCount(request.getCategoryId(), 1);
        }

        return convertToDTO(post, userId);
    }

    /**
     * 更新文章
     */
    @Transactional
    public PostDTO updatePost(Long postId, Long userId, PostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("文章不存在"));

        // 检查权限
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改此文章");
        }

        Long oldCategoryId = post.getCategoryId();

        post.setTitle(request.getTitle());
        post.setExcerpt(request.getExcerpt());
        post.setContent(request.getContent());
        post.setCoverImage(request.getCoverImage());
        post.setCategoryId(request.getCategoryId());
        post.setIsOriginal(request.getIsOriginal());
        post.setSourceUrl(request.getSourceUrl());
        post.setAllowComment(request.getAllowComment());
        post.setWordCount(countWords(request.getContent()));
        post.setReadTime(calculateReadTime(request.getContent()));

        // 草稿 -> 发布
        if (post.getStatus() == 0 && request.getStatus() == 1) {
            post.setStatus(1);
            post.setPublishedAt(LocalDateTime.now());
        } else if (request.getStatus() != null) {
            post.setStatus(request.getStatus());
        }

        post = postRepository.save(post);

        // 更新标签
        post.getTags().clear();
        handleTags(post, request.getTagIds(), request.getNewTags());

        // 更新分类文章数
        if (!Objects.equals(oldCategoryId, request.getCategoryId())) {
            if (oldCategoryId != null) {
                updateCategoryPostCount(oldCategoryId, -1);
            }
            if (request.getCategoryId() != null) {
                updateCategoryPostCount(request.getCategoryId(), 1);
            }
        }

        return convertToDTO(post, userId);
    }

    /**
     * 删除文章(软删除)
     */
    @Transactional
    public void deletePost(Long postId, Long userId, boolean isAdmin) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("文章不存在"));

        // 检查权限
        if (!isAdmin && !post.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此文章");
        }

        post.setDeletedAt(LocalDateTime.now());
        postRepository.save(post);

        // 更新分类文章数
        if (post.getCategoryId() != null) {
            updateCategoryPostCount(post.getCategoryId(), -1);
        }
    }

    /**
     * 获取文章详情
     */
    @Transactional
    public PostDTO getPost(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("文章不存在"));

        if (post.getDeletedAt() != null) {
            throw new RuntimeException("文章已被删除");
        }

        // 增加浏览量
        postRepository.incrementViewCount(postId);
        post.setViewCount(post.getViewCount() + 1);

        return convertToDTO(post, currentUserId);
    }

    /**
     * 根据 slug 获取文章
     */
    @Transactional
    public PostDTO getPostBySlug(String slug, Long currentUserId) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("文章不存在"));

        if (post.getDeletedAt() != null) {
            throw new RuntimeException("文章已被删除");
        }

        // 增加浏览量
        postRepository.incrementViewCount(post.getId());
        post.setViewCount(post.getViewCount() + 1);

        return convertToDTO(post, currentUserId);
    }

    /**
     * 获取文章列表 (支持多种筛选条件)
     */
    public PageResponse<PostListDTO> getPosts(int page, int size, Long categoryId, Long tagId,
            Long authorId, String keyword, String sort, Long currentUserId) {
        Page<Post> posts;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String safeKeyword = keyword != null ? keyword.trim() : null;

        // 根据排序参数调整
        if (sort != null && !sort.isEmpty()) {
            String[] sortParts = sort.split(",");
            String sortField = sortParts[0];
            Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc")
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;
            pageRequest = PageRequest.of(page, size, Sort.by(direction, sortField));
        }

        // 根据筛选条件查询
        if (categoryId != null) {
            posts = postRepository.findByCategoryId(categoryId, pageRequest);
        } else if (tagId != null) {
            posts = postRepository.findByTagId(tagId, pageRequest);
        } else if (authorId != null) {
            posts = postRepository.findByUserIdAndDeletedAtIsNull(authorId, pageRequest);
        } else if (safeKeyword != null && !safeKeyword.isEmpty()) {
            posts = postRepository.searchPosts(safeKeyword, pageRequest);
        } else {
            posts = postRepository.findPublishedPosts(pageRequest);
        }

        List<PostListDTO> postDTOs = posts.getContent().stream()
                .map(this::convertToListDTO)
                .collect(Collectors.toList());

        return PageResponse.of(postDTOs, page, size, posts.getTotalElements());
    }

    /**
     * 获取文章列表 (简单版本，保持向后兼容)
     */
    public PageResponse<PostListDTO> getPosts(int page, int size, Long currentUserId) {
        return getPosts(page, size, null, null, null, null, null, currentUserId);
    }

    /**
     * 根据分类获取文章
     */
    public PageResponse<PostListDTO> getPostsByCategory(Long categoryId, int page, int size, Long currentUserId) {
        Page<Post> posts = postRepository.findByCategoryId(categoryId, PageRequest.of(page, size));

        List<PostListDTO> postDTOs = posts.getContent().stream()
                .map(this::convertToListDTO)
                .collect(Collectors.toList());

        return PageResponse.of(postDTOs, page, size, posts.getTotalElements());
    }

    /**
     * 根据标签获取文章
     */
    public PageResponse<PostListDTO> getPostsByTag(Long tagId, int page, int size, Long currentUserId) {
        Page<Post> posts = postRepository.findByTagId(tagId, PageRequest.of(page, size));

        List<PostListDTO> postDTOs = posts.getContent().stream()
                .map(this::convertToListDTO)
                .collect(Collectors.toList());

        return PageResponse.of(postDTOs, page, size, posts.getTotalElements());
    }

    /**
     * 根据用户获取文章
     */
    public PageResponse<PostListDTO> getPostsByUser(String username, int page, int size, Long currentUserId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Page<Post> posts = postRepository.findByUserIdAndDeletedAtIsNull(user.getId(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        List<PostListDTO> postDTOs = posts.getContent().stream()
                .map(this::convertToListDTO)
                .collect(Collectors.toList());

        return PageResponse.of(postDTOs, page, size, posts.getTotalElements());
    }

    /**
     * 搜索文章
     */
    public PageResponse<PostListDTO> searchPosts(String keyword, int page, int size, Long currentUserId) {
        String safeKeyword = keyword != null ? keyword.trim() : "";
        if (safeKeyword.isEmpty()) {
            return PageResponse.of(List.of(), page, size, 0);
        }

        Page<Post> posts = postRepository.searchPosts(safeKeyword, PageRequest.of(page, size));

        List<PostListDTO> postDTOs = posts.getContent().stream()
                .map(this::convertToListDTO)
                .collect(Collectors.toList());

        return PageResponse.of(postDTOs, page, size, posts.getTotalElements());
    }

    public PageResponse<PostListDTO> searchPostsForGlobal(String keyword, int page, int size, Long currentUserId,
            boolean isAdmin) {
        String safeKeyword = keyword != null ? keyword.trim() : "";
        if (safeKeyword.isEmpty()) {
            return PageResponse.of(List.of(), page, size, 0);
        }

        long safeUserId = currentUserId != null ? currentUserId : -1L;
        Page<Post> posts = postRepository.searchPostsForGlobal(safeKeyword, safeUserId, isAdmin,
                PageRequest.of(page, size));

        List<PostListDTO> postDTOs = posts.getContent().stream()
                .map(this::convertToListDTO)
                .collect(Collectors.toList());

        return PageResponse.of(postDTOs, page, size, posts.getTotalElements());
    }

    /**
     * 获取热门文章
     */
    public List<PostListDTO> getHotPosts(int limit) {
        return postRepository.findHotPosts(PageRequest.of(0, limit)).stream()
                .map(this::convertToListDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取精选文章
     */
    public List<PostListDTO> getFeaturedPosts(int limit) {
        return postRepository.findFeaturedPosts(PageRequest.of(0, limit)).stream()
                .map(this::convertToListDTO)
                .collect(Collectors.toList());
    }

    // ==================== 私有方法 ====================

    private String generateSlug(String title) {
        // 简单实现：使用时间戳 + 部分标题
        String base = title.length() > 20 ? title.substring(0, 20) : title;
        return base.toLowerCase().replaceAll("[^a-z0-9\\u4e00-\\u9fa5]", "-")
                + "-" + System.currentTimeMillis();
    }

    private int countWords(String content) {
        if (content == null)
            return 0;
        // 去除 HTML 标签后统计
        String text = content.replaceAll("<[^>]+>", "");
        return text.length();
    }

    private int calculateReadTime(String content) {
        int words = countWords(content);
        // 假设每分钟阅读 300 字
        return Math.max(1, words / 300);
    }

    private void handleTags(Post post, List<Long> tagIds, List<String> newTags) {
        Set<Tag> tags = new HashSet<>();

        // 处理已有标签
        if (tagIds != null && !tagIds.isEmpty()) {
            tags.addAll(tagRepository.findAllById(tagIds));
        }

        // 处理新标签
        if (newTags != null && !newTags.isEmpty()) {
            for (String tagName : newTags) {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> {
                            Tag newTag = Tag.builder()
                                    .name(tagName)
                                    .slug(tagName.toLowerCase().replaceAll("[^a-z0-9]", "-"))
                                    .build();
                            return tagRepository.save(newTag);
                        });
                tags.add(tag);
            }
        }

        post.setTags(tags);
        postRepository.save(post);

        // 更新标签文章数
        for (Tag tag : tags) {
            tag.setPostCount(tag.getPostCount() + 1);
            tagRepository.save(tag);
        }
    }

    private void updateCategoryPostCount(Long categoryId, int delta) {
        categoryRepository.findById(categoryId).ifPresent(category -> {
            category.setPostCount(Math.max(0, category.getPostCount() + delta));
            categoryRepository.save(category);
        });
    }

    private PostDTO convertToDTO(Post post, Long currentUserId) {
        PostDTO dto = PostDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .excerpt(post.getExcerpt())
                .content(post.getContent())
                .contentHtml(post.getContentHtml())
                .coverImage(post.getCoverImage())
                .wordCount(post.getWordCount())
                .readTime(post.getReadTime())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .bookmarkCount(post.getBookmarkCount())
                .isTop(post.getIsTop())
                .isFeatured(post.getIsFeatured())
                .isOriginal(post.getIsOriginal())
                .sourceUrl(post.getSourceUrl())
                .allowComment(post.getAllowComment())
                .status(post.getStatus())
                .publishedAt(post.getPublishedAt())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();

        // 作者信息
        if (post.getAuthor() != null) {
            dto.setAuthor(PostDTO.AuthorDTO.builder()
                    .id(post.getAuthor().getId())
                    .username(post.getAuthor().getUsername())
                    .nickname(post.getAuthor().getNickname())
                    .avatar(post.getAuthor().getAvatar())
                    .level(post.getAuthor().getLevel())
                    .build());
        } else {
            userRepository.findById(post.getUserId()).ifPresent(author -> {
                dto.setAuthor(PostDTO.AuthorDTO.builder()
                        .id(author.getId())
                        .username(author.getUsername())
                        .nickname(author.getNickname())
                        .avatar(author.getAvatar())
                        .level(author.getLevel())
                        .build());
            });
        }

        // 分类信息
        if (post.getCategoryId() != null) {
            categoryRepository.findById(post.getCategoryId()).ifPresent(category -> {
                dto.setCategory(PostDTO.CategoryDTO.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .slug(category.getSlug())
                        .icon(category.getIcon())
                        .build());
            });
        }

        // 标签信息
        if (post.getTags() != null && !post.getTags().isEmpty()) {
            dto.setTags(post.getTags().stream()
                    .map(tag -> PostDTO.TagDTO.builder()
                            .id(tag.getId())
                            .name(tag.getName())
                            .slug(tag.getSlug())
                            .color(tag.getColor())
                            .build())
                    .collect(Collectors.toList()));
        }

        // 当前用户交互状态
        if (currentUserId != null) {
            dto.setIsLiked(likeRepository.existsByUserIdAndTargetIdAndTargetType(
                    currentUserId, post.getId(), Like.TargetType.post));
            dto.setIsBookmarked(bookmarkRepository.existsByUserIdAndPostId(
                    currentUserId, post.getId()));
        }

        return dto;
    }

    private PostListDTO convertToListDTO(Post post) {
        PostListDTO dto = PostListDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .excerpt(post.getExcerpt())
                .coverImage(post.getCoverImage())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .isTop(post.getIsTop())
                .isFeatured(post.getIsFeatured())
                .status(post.getStatus())
                .publishedAt(post.getPublishedAt())
                .createdAt(post.getCreatedAt())
                .build();

        // 作者信息
        userRepository.findById(post.getUserId()).ifPresent(author -> {
            dto.setAuthor(PostListDTO.AuthorDTO.builder()
                    .id(author.getId())
                    .username(author.getUsername())
                    .nickname(author.getNickname())
                    .avatar(author.getAvatar())
                    .build());
        });

        // 分类信息
        if (post.getCategoryId() != null) {
            categoryRepository.findById(post.getCategoryId()).ifPresent(category -> {
                dto.setCategory(PostListDTO.CategoryDTO.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .slug(category.getSlug())
                        .build());
            });
        }

        // 标签信息
        if (post.getTags() != null && !post.getTags().isEmpty()) {
            dto.setTags(post.getTags().stream()
                    .map(tag -> PostListDTO.TagDTO.builder()
                            .id(tag.getId())
                            .name(tag.getName())
                            .color(tag.getColor())
                            .build())
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
