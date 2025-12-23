package com.example.cloudstack.demo.repository;

import com.example.cloudstack.demo.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 分类数据访问接口
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    List<Category> findByParentIdIsNullAndStatusOrderBySortOrder(Integer status);

    List<Category> findByParentIdAndStatus(Long parentId, Integer status);

    boolean existsBySlug(String slug);

    @Query("SELECT c FROM Category c WHERE c.status = 1 ORDER BY c.sortOrder ASC")
    List<Category> findAllActiveCategories();
}
