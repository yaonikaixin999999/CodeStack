package com.example.cloudstack.demo.repository;

import com.example.cloudstack.demo.model.entity.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 收藏数据访问接口
 */
@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Optional<Bookmark> findByUserIdAndPostId(Long userId, Long postId);

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    void deleteByUserIdAndPostId(Long userId, Long postId);

    Page<Bookmark> findByUserId(Long userId, Pageable pageable);

    Page<Bookmark> findByUserIdAndFolderId(Long userId, Long folderId, Pageable pageable);

    long countByUserId(Long userId);
}
