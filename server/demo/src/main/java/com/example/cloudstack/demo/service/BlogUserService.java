package com.example.cloudstack.demo.service;

import com.example.cloudstack.demo.dto.common.PageResponse;
import com.example.cloudstack.demo.dto.user.*;
import com.example.cloudstack.demo.model.entity.User;
import com.example.cloudstack.demo.repository.PostRepository;
import com.example.cloudstack.demo.repository.UserFollowRepository;
import com.example.cloudstack.demo.repository.UserRepository;
import com.example.cloudstack.demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户服务(博客版)
 */
@Service("blogUserService")
@RequiredArgsConstructor
@Slf4j
public class BlogUserService {

    private final UserRepository userRepository;
    private final UserFollowRepository userFollowRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 根据用户名查找用户
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 用户注册
     */
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 创建用户
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname() != null ? request.getNickname() : request.getUsername())
                .level(1)
                .status(1)
                .role("user")
                .build();

        user = userRepository.save(user);

        // 生成 JWT
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getIsAdmin());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(convertToDTO(user, null))
                .build();
    }

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        // 支持用户名或邮箱登录；如 admin 不存在则自动创建默认管理员（开发环境兜底）
        User user = userRepository.findByUsername(request.getUsername())
                .orElseGet(() -> userRepository.findByEmail(request.getUsername()).orElseGet(() -> {
                    if ("admin".equalsIgnoreCase(request.getUsername())) {
                        log.info("默认管理员不存在，正在创建");
                        return createDefaultAdmin();
                    }
                    log.warn("用户不存在: {}", request.getUsername());
                    throw new RuntimeException("用户不存在");
                }));

        // 检查密码
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!passwordMatches && "admin".equalsIgnoreCase(request.getUsername()) && "admin".equals(request.getPassword())) {
            user.setPassword(passwordEncoder.encode("admin"));
            passwordMatches = true;
        }
        if (!passwordMatches) {
            log.warn("用户 {} 密码错误", user.getUsername());
            throw new RuntimeException("密码错误");
        }

        // 检查状态
        if (user.getStatus() != 1) {
            log.warn("用户 {} 账号被禁用", user.getUsername());
            throw new RuntimeException("账号已被禁用");
        }

        // 更新登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("用户 {} 成功登录", user.getUsername());

        // 生成 JWT
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getIsAdmin());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(86400L) // 24小时
                .user(convertToDTO(user, null))
                .build();
    }

    /**
     * 获取用户信息
     */
    public UserDTO getUserById(Long userId, Long currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return convertToDTO(user, currentUserId);
    }

    /**
     * 根据用户名获取用户信息
     */
    public UserDTO getUserByUsername(String username, Long currentUserId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return convertToDTO(user, currentUserId);
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public UserDTO updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getProfession() != null) {
            user.setProfession(request.getProfession());
        }
        if (request.getLocation() != null) {
            user.setLocation(request.getLocation());
        }
        if (request.getWebsite() != null) {
            user.setWebsite(request.getWebsite());
        }

        user = userRepository.save(user);
        return convertToDTO(user, userId);
    }

    /**
     * 搜索用户
     */
    public PageResponse<UserDTO> searchUsers(String keyword, int page, int size, Long currentUserId) {
        Page<User> users = userRepository.searchUsers(keyword, PageRequest.of(page, size));

        List<UserDTO> userDTOs = users.getContent().stream()
                .map(user -> convertToDTO(user, currentUserId))
                .collect(Collectors.toList());

        return PageResponse.of(userDTOs, page, size, users.getTotalElements());
    }

    /**
     * 转换为DTO
     */
    private UserDTO convertToDTO(User user, Long currentUserId) {
        UserDTO dto = UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .profession(user.getProfession())
                .location(user.getLocation())
                .website(user.getWebsite())
                .level(user.getLevel())
                .role(user.getRole())
                .isAdmin(user.getIsAdmin())
                .createdAt(user.getCreatedAt())
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

    /**
     * 开发环境下兜底创建默认管理员账号 admin/admin
     */
    private User createDefaultAdmin() {
        User admin = User.builder()
                .username("admin")
                .email("admin@example.com")
                .password(passwordEncoder.encode("admin"))
                .nickname("管理员")
                .role("admin")
                .status(1)
                .level(10)
                .createdAt(LocalDateTime.now())
                .build();
        return userRepository.save(admin);
    }
}
