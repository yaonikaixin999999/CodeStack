package com.example.cloudstack.demo.repository;

import com.example.cloudstack.demo.model.entity.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 标签数据访问接口
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    Optional<Tag> findBySlug(String slug);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    @Query("SELECT t FROM Tag t ORDER BY t.postCount DESC")
    List<Tag> findHotTags(Pageable pageable);

    @Query("""
            SELECT t, COUNT(p)
            FROM Post p
            JOIN p.tags t
            WHERE p.status = 1
              AND p.deletedAt IS NULL
            GROUP BY t
            ORDER BY COUNT(p) DESC
            """)
    List<Object[]> findHotTagsWithPostCount(Pageable pageable);

    List<Tag> findByNameContaining(String keyword);
}
