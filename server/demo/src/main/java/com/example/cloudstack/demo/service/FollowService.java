package com.example.cloudstack.demo.service;

import com.example.cloudstack.demo.dto.common.PageResponse;
import com.example.cloudstack.demo.dto.user.UserDTO;
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
 * 关注服务
 */
@Service
@RequiredArgsConstructor
public class FollowService {

    private final UserFollowRepository userFollowRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    /**
     * 关注用户
     */
    @Transactional
    public boolean follow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new RuntimeException("不能关注自己");
        }

        if (!userRepository.existsById(followingId)) {
            throw new RuntimeException("用户不存在");
        }

        if (userFollowRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new RuntimeException("已经关注了");
        }

        UserFollow follow = UserFollow.builder()
                .followerId(followerId)
                .followingId(followingId)
                .build();
        userFollowRepository.save(follow);

        // 发送通知
        notificationService.sendNotification(
                followingId,
                followerId,
                "follow",
                "关注了你",
                "有人关注了你",
                followerId,
                "user");

        return true;
    }

    /**
     * 取消关注
     */
    @Transactional
    public boolean unfollow(Long followerId, Long followingId) {
        if (!userFollowRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new RuntimeException("尚未关注");
        }

        userFollowRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);

        return true;
    }

    /**
     * 获取关注列表
     */
    public PageResponse<UserDTO> getFollowing(Long userId, int page, int size, Long currentUserId) {
        Page<UserFollow> follows = userFollowRepository.findByFollowerId(userId, PageRequest.of(page, size));

        List<UserDTO> users = follows.getContent().stream()
                .map(follow -> {
                    User user = userRepository.findById(follow.getFollowingId()).orElse(null);
                    if (user == null)
                        return null;
                    return convertToDTO(user, currentUserId);
                })
                .filter(u -> u != null)
                .collect(Collectors.toList());

        return PageResponse.of(users, page, size, follows.getTotalElements());
    }

    /**
     * 获取粉丝列表
     */
    public PageResponse<UserDTO> getFollowers(Long userId, int page, int size, Long currentUserId) {
        Page<UserFollow> follows = userFollowRepository.findByFollowingId(userId, PageRequest.of(page, size));

        List<UserDTO> users = follows.getContent().stream()
                .map(follow -> {
                    User user = userRepository.findById(follow.getFollowerId()).orElse(null);
                    if (user == null)
                        return null;
                    return convertToDTO(user, currentUserId);
                })
                .filter(u -> u != null)
                .collect(Collectors.toList());

        return PageResponse.of(users, page, size, follows.getTotalElements());
    }

    /**
     * 检查是否已关注
     */
    public boolean isFollowing(Long followerId, Long followingId) {
        return userFollowRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    /**
     * 检查是否互相关注
     */
    public boolean isMutualFollow(Long userId, Long targetUserId) {
        return userFollowRepository.isMutualFollow(userId, targetUserId);
    }

    private UserDTO convertToDTO(User user, Long currentUserId) {
        UserDTO dto = UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .level(user.getLevel())
                .build();

        // 统计信息
        dto.setPostCount(postRepository.countByUserId(user.getId()));
        dto.setFollowerCount(userFollowRepository.countByFollowingId(user.getId()));
        dto.setFollowingCount(userFollowRepository.countByFollowerId(user.getId()));

        // 关注状态
        if (currentUserId != null && !currentUserId.equals(user.getId())) {
            dto.setIsFollowed(userFollowRepository.existsByFollowerIdAndFollowingId(currentUserId, user.getId()));
        }

        return dto;
    }
}
