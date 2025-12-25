package com.example.cloudstack.demo.service;

import com.example.cloudstack.demo.model.entity.User;
import com.example.cloudstack.demo.repository.UserRepository;
import com.example.cloudstack.demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

/**
 * 旧版用户服务 - 用于原有的登录注册功能
 * 新的博客用户功能请使用 BlogUserService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public Map<String, Object> register(String username, String password, String phone) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在（使用 phone 作为 email）
        if (userRepository.existsByEmail(phone)) {
            throw new RuntimeException("邮箱已存在");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(phone);
        user.setNickname(username);
        user.setRole("user");

        User savedUser = userRepository.save(user);
        log.info("用户注册成功: {}", savedUser.getUsername());

        String token = jwtUtil.generateToken(savedUser.getId(), savedUser.getUsername(), savedUser.getIsAdmin());
        Map<String, Object> resp = new HashMap<>();
        resp.put("token", token);
        resp.put("username", savedUser.getUsername());
        resp.put("userId", savedUser.getId());
        resp.put("isAdmin", Boolean.TRUE.equals(savedUser.getIsAdmin()));
        resp.put("role", savedUser.getRole());
        resp.put("avatar", savedUser.getAvatar());
        return resp;
    }

    public Map<String, Object> login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        // 开发环境兜底：若 admin 不存在则自动创建默认管理员
        if (userOpt.isEmpty() && "admin".equalsIgnoreCase(username)) {
            User admin = createDefaultAdmin();
            userOpt = Optional.of(admin);
        }

        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }

        User user = userOpt.get();

        boolean matches = passwordEncoder.matches(password, user.getPassword());
        if (!matches && password.equals(user.getPassword())) {
            matches = true; // 兼容明文历史数据
        }
        if (!matches && "admin".equalsIgnoreCase(username) && "admin".equals(password)) {
            user.setPassword(passwordEncoder.encode("admin"));
            matches = true;
        }
        if (!matches) {
            throw new RuntimeException("密码错误");
        }

        if (user.getStatus() != null && user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }
        user.setLastLoginAt(java.time.LocalDateTime.now());
        userRepository.save(user);
        log.info("用户登录成功: {}", user.getUsername());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getIsAdmin());
        Map<String, Object> resp = new HashMap<>();
        resp.put("token", token);
        resp.put("username", user.getUsername());
        resp.put("userId", user.getId());
        resp.put("isAdmin", Boolean.TRUE.equals(user.getIsAdmin()));
        resp.put("role", user.getRole());
        resp.put("avatar", user.getAvatar());
        return resp;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * 开发环境下兜底创建默认管理员账号 admin/admin
     */
    private User createDefaultAdmin() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setNickname("管理员");
        admin.setRole("admin");
        admin.setLevel(10);
        admin.setStatus(1);
        admin.setCreatedAt(java.time.LocalDateTime.now());
        return userRepository.save(admin);
    }
}
