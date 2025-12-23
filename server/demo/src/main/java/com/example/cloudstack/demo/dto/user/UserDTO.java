package com.example.cloudstack.demo.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户信息DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String avatar;
    private String bio;
    private String profession;
    private String location;
    private String website;
    private Integer level;
    private String role;
    private Boolean isAdmin;
    private LocalDateTime createdAt;

    // 统计信息
    private Long postCount;
    private Long followerCount;
    private Long followingCount;
    private Long totalLikes;
    private Long totalViews;

    // 关注状态(当前用户是否关注)
    private Boolean isFollowed;
}
