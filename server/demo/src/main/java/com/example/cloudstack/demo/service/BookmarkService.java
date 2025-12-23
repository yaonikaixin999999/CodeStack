package com.example.cloudstack.demo.service;

import com.example.cloudstack.demo.dto.common.PageResponse;
import com.example.cloudstack.demo.dto.post.PostListDTO;
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
 * 收藏服务
 */
@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 收藏文章
     */
    @Transactional
    public boolean bookmark(Long userId, Long postId, Long folderId, String note) {
        if (!postRepository.existsById(postId)) {
            throw new RuntimeException("文章不存在");
        }

        if (bookmarkRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new RuntimeException("已经收藏过了");
        }

        Bookmark bookmark = Bookmark.builder()
                .userId(userId)
                .postId(postId)
                .folderId(folderId)
                .note(note)
                .build();
        bookmarkRepository.save(bookmark);

        postRepository.updateBookmarkCount(postId, 1);

        return true;
    }

    /**
     * 取消收藏
     */
    @Transactional
    public boolean unbookmark(Long userId, Long postId) {
        if (!bookmarkRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new RuntimeException("尚未收藏");
        }

        bookmarkRepository.deleteByUserIdAndPostId(userId, postId);
        postRepository.updateBookmarkCount(postId, -1);

        return true;
    }

    /**
     * 获取用户收藏列表
     */
    public PageResponse<PostListDTO> getUserBookmarks(Long userId, int page, int size) {
        Page<Bookmark> bookmarks = bookmarkRepository.findByUserId(userId, PageRequest.of(page, size));

        List<PostListDTO> posts = bookmarks.getContent().stream()
                .map(bookmark -> {
                    Post post = postRepository.findById(bookmark.getPostId()).orElse(null);
                    if (post == null || post.getDeletedAt() != null) {
                        return null;
                    }
                    return convertToListDTO(post);
                })
                .filter(p -> p != null)
                .collect(Collectors.toList());

        return PageResponse.of(posts, page, size, bookmarks.getTotalElements());
    }

    /**
     * 检查是否已收藏
     */
    public boolean isBookmarked(Long userId, Long postId) {
        return bookmarkRepository.existsByUserIdAndPostId(userId, postId);
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
                .publishedAt(post.getPublishedAt())
                .createdAt(post.getCreatedAt())
                .build();

        userRepository.findById(post.getUserId()).ifPresent(author -> {
            dto.setAuthor(PostListDTO.AuthorDTO.builder()
                    .id(author.getId())
                    .username(author.getUsername())
                    .nickname(author.getNickname())
                    .avatar(author.getAvatar())
                    .build());
        });

        if (post.getCategoryId() != null) {
            categoryRepository.findById(post.getCategoryId()).ifPresent(category -> {
                dto.setCategory(PostListDTO.CategoryDTO.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .slug(category.getSlug())
                        .build());
            });
        }

        return dto;
    }
}
