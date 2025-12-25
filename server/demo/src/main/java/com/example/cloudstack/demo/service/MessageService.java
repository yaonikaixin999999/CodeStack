package com.example.cloudstack.demo.service;

import com.example.cloudstack.demo.dto.common.PageResponse;
import com.example.cloudstack.demo.model.entity.Message;
import com.example.cloudstack.demo.model.entity.User;
import com.example.cloudstack.demo.repository.MessageRepository;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 消息服务 - 简化版（无已读未读功能）
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    /**
     * 发送私信
     */
    @Transactional
    public MessageDTO sendMessage(Long senderId, Long receiverId, String content) {
        if (senderId.equals(receiverId)) {
            throw new RuntimeException("不能给自己发送私信");
        }

        // 验证接收者是否存在
        if (!userRepository.existsById(receiverId)) {
            throw new RuntimeException("接收者不存在");
        }

        // 创建消息
        Message message = Message.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .build();

        message = messageRepository.save(message);
        return convertToDTO(message, senderId);
    }

    /**
     * 获取会话列表
     */
    public List<ConversationDTO> getConversations(Long userId) {
        List<Message> latestMessages = messageRepository.findConversationList(userId);

        return latestMessages.stream()
                .map(msg -> {
                    Long otherUserId = msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId();
                    User otherUser = userRepository.findById(otherUserId).orElse(null);

                    return ConversationDTO.builder()
                            .otherUserId(otherUserId)
                            .lastMessageContent(truncateContent(msg.getContent()))
                            .lastMessageAt(msg.getCreatedAt())
                            .otherUser(otherUser != null ? UserDTO.builder()
                                    .id(otherUser.getId())
                                    .username(otherUser.getUsername())
                                    .nickname(otherUser.getNickname())
                                    .avatar(otherUser.getAvatar())
                                    .build() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取与指定用户的消息列表
     */
    public List<MessageDTO> getMessages(Long currentUserId, Long otherUserId) {
        List<Message> messages = messageRepository.findMessagesBetweenUsers(currentUserId, otherUserId);
        return messages.stream()
                .map(m -> convertToDTO(m, currentUserId))
                .collect(Collectors.toList());
    }

    /**
     * 获取与指定用户的消息列表（分页）
     */
    public PageResponse<MessageDTO> getMessagesPaged(Long currentUserId, Long otherUserId, int page, int size) {
        Page<Message> messages = messageRepository.findMessagesBetweenUsersPaged(
                currentUserId, otherUserId, PageRequest.of(page, size));

        List<MessageDTO> dtos = messages.getContent().stream()
                .map(m -> convertToDTO(m, currentUserId))
                .collect(Collectors.toList());

        return PageResponse.of(dtos, page, size, messages.getTotalElements());
    }

    /**
     * 删除与指定用户的会话（删除所有消息）
     */
    @Transactional
    public void deleteConversation(Long currentUserId, Long otherUserId) {
        messageRepository.deleteMessagesBetweenUsers(currentUserId, otherUserId);
    }

    /**
     * 删除单条消息
     */
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("消息不存在"));

        // 只能删除自己发送的消息
        if (!message.getSenderId().equals(userId)) {
            throw new RuntimeException("只能删除自己发送的消息");
        }

        messageRepository.delete(message);
    }

    /**
     * 搜索用户（根据用户名或昵称）
     */
    public List<UserDTO> searchUsers(String keyword, Long currentUserId) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // 在 UserRepository 中搜索
        List<User> users = userRepository.findByUsernameContainingOrNicknameContaining(
                keyword.trim(), keyword.trim());

        return users.stream()
                .filter(u -> !u.getId().equals(currentUserId)) // 排除自己
                .limit(10) // 限制结果数量
                .map(u -> UserDTO.builder()
                        .id(u.getId())
                        .username(u.getUsername())
                        .nickname(u.getNickname())
                        .avatar(u.getAvatar())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 截断消息内容
     */
    private String truncateContent(String content) {
        if (content == null)
            return "";
        return content.length() > 50 ? content.substring(0, 50) + "..." : content;
    }

    /**
     * 转换消息为DTO
     */
    private MessageDTO convertToDTO(Message message, Long currentUserId) {
        User sender = userRepository.findById(message.getSenderId()).orElse(null);

        return MessageDTO.builder()
                .id(message.getId())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .isMine(message.getSenderId().equals(currentUserId))
                .sender(sender != null ? UserDTO.builder()
                        .id(sender.getId())
                        .username(sender.getUsername())
                        .nickname(sender.getNickname())
                        .avatar(sender.getAvatar())
                        .build() : null)
                .build();
    }

    // ============== DTO 定义 ==============

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConversationDTO {
        private Long otherUserId;
        private String lastMessageContent;
        private LocalDateTime lastMessageAt;
        private UserDTO otherUser;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MessageDTO {
        private Long id;
        private String content;
        private LocalDateTime createdAt;
        private Boolean isMine;
        private UserDTO sender;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserDTO {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SendMessageRequest {
        private Long receiverId;
        private String content;
    }
}
