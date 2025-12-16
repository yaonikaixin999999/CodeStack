package com.example.cloudstack.demo.service;

import com.example.cloudstack.demo.model.User;
import com.example.cloudstack.demo.model.repository.UserRepository;
import com.example.cloudstack.demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
import java.util.Optional;

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

        // 检查手机号是否已存在
        if (userRepository.existsByPhone(phone)) {
            throw new RuntimeException("手机号已存在");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setPhone(phone);
        // user.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd
        // HH:mm:ss")));
        // user.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd
        // HH:mm:ss")));
        // user.setStatus("active");

        User savedUser = userRepository.save(user);
        log.info("用户注册成功: {}", savedUser.getUsername());

        return jwtUtil.generateToken(savedUser.getUsername());
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
        return jwtUtil.generateToken(user.getUsername());
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
}