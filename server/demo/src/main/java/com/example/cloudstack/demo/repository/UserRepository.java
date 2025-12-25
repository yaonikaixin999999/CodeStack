package com.example.cloudstack.demo.repository;

import com.example.cloudstack.demo.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.nickname LIKE %:keyword%")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.status = :status")
    Page<User> findByStatus(@Param("status") Integer status, Pageable pageable);

    /**
     * 根据用户名或昵称搜索用户（不分页）
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword1% OR u.nickname LIKE %:keyword2%")
    List<User> findByUsernameContainingOrNicknameContaining(
            @Param("keyword1") String keyword1,
            @Param("keyword2") String keyword2);
}
