package com.example.cloudstack.demo.repository;

import com.example.cloudstack.demo.model.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 点赞数据访问接口
 */
@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUserIdAndTargetIdAndTargetType(Long userId, Long targetId, Like.TargetType targetType);

    boolean existsByUserIdAndTargetIdAndTargetType(Long userId, Long targetId, Like.TargetType targetType);

    void deleteByUserIdAndTargetIdAndTargetType(Long userId, Long targetId, Like.TargetType targetType);

    long countByTargetIdAndTargetType(Long targetId, Like.TargetType targetType);
}
