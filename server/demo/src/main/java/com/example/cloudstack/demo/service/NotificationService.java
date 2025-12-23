package com.example.cloudstack.demo.service;

import com.example.cloudstack.demo.dto.common.PageResponse;
import com.example.cloudstack.demo.model.entity.Notification;
import com.example.cloudstack.demo.repository.NotificationRepository;
import com.example.cloudstack.demo.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知服务
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * 发送通知
     */
    @Transactional
    public void sendNotification(Long userId, Long senderId, String type,
            String title, String content, Long targetId, String targetType) {
        Notification notification = Notification.builder()
                .userId(userId)
                .senderId(senderId)
                .type(type)
                .title(title)
                .content(content)
                .targetId(targetId)
                .targetType(targetType)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    /**
     * 获取通知列表
     */
    public PageResponse<NotificationDTO> getNotifications(Long userId, String type, int page, int size) {
        Page<Notification> notifications;

        if (type != null && !type.isEmpty()) {
            notifications = notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(
                    userId, type, PageRequest.of(page, size));
        } else {
            notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                    userId, PageRequest.of(page, size));
        }

        List<NotificationDTO> dtos = notifications.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResponse.of(dtos, page, size, notifications.getTotalElements());
    }

    /**
     * 获取未读通知数量
     */
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    /**
     * 标记单个通知为已读
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("通知不存在"));

        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }

        notificationRepository.markAsRead(notificationId);
    }

    /**
     * 标记所有通知为已读
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = NotificationDTO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .targetId(notification.getTargetId())
                .targetType(notification.getTargetType())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();

        // 发送者信息
        if (notification.getSenderId() != null) {
            userRepository.findById(notification.getSenderId()).ifPresent(user -> {
                dto.setSender(NotificationDTO.SenderDTO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .avatar(user.getAvatar())
                        .build());
            });
        }

        return dto;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NotificationDTO {
        private Long id;
        private String type;
        private String title;
        private String content;
        private Long targetId;
        private String targetType;
        private Boolean isRead;
        private LocalDateTime readAt;
        private LocalDateTime createdAt;
        private SenderDTO sender;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class SenderDTO {
            private Long id;
            private String username;
            private String nickname;
            private String avatar;
        }
    }
}
