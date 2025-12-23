package com.example.cloudstack.demo.service;

import com.example.cloudstack.demo.model.entity.User;
import com.example.cloudstack.demo.repository.UserRepository;
import com.example.cloudstack.demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

    public String register(String username, String password, String phone) {
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

        User savedUser = userRepository.save(user);
        log.info("用户注册成功: {}", savedUser.getUsername());

        return jwtUtil.generateToken(savedUser.getId(), savedUser.getUsername(), savedUser.getIsAdmin());
    }

    public String login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        log.info("用户登录成功: {}", user.getUsername());
        return jwtUtil.generateToken(user.getId(), user.getUsername(), user.getIsAdmin());
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
}