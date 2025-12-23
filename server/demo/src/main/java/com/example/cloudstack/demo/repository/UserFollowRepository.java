package com.example.cloudstack.demo.repository;

import com.example.cloudstack.demo.model.entity.UserFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户关注数据访问接口
 */
@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {

    Optional<UserFollow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);

    // 查询用户的关注列表
    Page<UserFollow> findByFollowerId(Long followerId, Pageable pageable);

    // 查询用户的粉丝列表
    Page<UserFollow> findByFollowingId(Long followingId, Pageable pageable);

    // 统计关注数
    long countByFollowerId(Long followerId);

    // 统计粉丝数
    long countByFollowingId(Long followingId);

    // 查询是否互相关注
    @Query("SELECT CASE WHEN COUNT(uf) > 0 THEN true ELSE false END FROM UserFollow uf WHERE uf.followerId = :userId AND uf.followingId = :targetUserId AND EXISTS (SELECT 1 FROM UserFollow uf2 WHERE uf2.followerId = :targetUserId AND uf2.followingId = :userId)")
    boolean isMutualFollow(@Param("userId") Long userId, @Param("targetUserId") Long targetUserId);
}
