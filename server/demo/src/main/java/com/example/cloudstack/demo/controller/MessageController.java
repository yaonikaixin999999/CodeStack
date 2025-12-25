package com.example.cloudstack.demo.controller;

import com.example.cloudstack.demo.dto.ApiResponse;
import com.example.cloudstack.demo.dto.common.PageResponse;
import com.example.cloudstack.demo.service.MessageService;
import com.example.cloudstack.demo.service.MessageService.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 私信消息控制器（简化版）
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 发送私信
     * POST /api/messages
     * Body: { receiverId: Long, content: String }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MessageDTO>> sendMessage(
            @RequestBody SendMessageRequest request,
            @RequestAttribute("userId") Long userId) {
        try {
            MessageDTO message = messageService.sendMessage(
                    userId,
                    request.getReceiverId(),
                    request.getContent());
            return ResponseEntity.ok(ApiResponse.success("发送成功", message));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取会话列表
     * GET /api/messages/conversations
     */
    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ConversationDTO>>> getConversations(
            @RequestAttribute("userId") Long userId) {
        try {
            List<ConversationDTO> conversations = messageService.getConversations(userId);
            return ResponseEntity.ok(ApiResponse.success(conversations));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取与指定用户的消息列表
     * GET /api/messages/user/{otherUserId}
     */
    @GetMapping("/user/{otherUserId}")
    public ResponseEntity<ApiResponse<List<MessageDTO>>> getMessages(
            @PathVariable Long otherUserId,
            @RequestAttribute("userId") Long userId) {
        try {
            List<MessageDTO> messages = messageService.getMessages(userId, otherUserId);
            return ResponseEntity.ok(ApiResponse.success(messages));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取与指定用户的消息列表（分页）
     * GET /api/messages/user/{otherUserId}/paged?page=0&size=50
     */
    @GetMapping("/user/{otherUserId}/paged")
    public ResponseEntity<ApiResponse<PageResponse<MessageDTO>>> getMessagesPaged(
            @PathVariable Long otherUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestAttribute("userId") Long userId) {
        try {
            PageResponse<MessageDTO> messages = messageService.getMessagesPaged(userId, otherUserId, page, size);
            return ResponseEntity.ok(ApiResponse.success(messages));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 删除与指定用户的会话
     * DELETE /api/messages/user/{otherUserId}
     */
    @DeleteMapping("/user/{otherUserId}")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(
            @PathVariable Long otherUserId,
            @RequestAttribute("userId") Long userId) {
        try {
            messageService.deleteConversation(userId, otherUserId);
            return ResponseEntity.ok(ApiResponse.successMsg("删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 删除单条消息
     * DELETE /api/messages/{messageId}
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(
            @PathVariable Long messageId,
            @RequestAttribute("userId") Long userId) {
        try {
            messageService.deleteMessage(messageId, userId);
            return ResponseEntity.ok(ApiResponse.successMsg("删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 搜索用户（用于发起新私信时选择用户）
     * GET /api/messages/search-users?keyword=xxx
     */
    @GetMapping("/search-users")
    public ResponseEntity<ApiResponse<List<UserDTO>>> searchUsers(
            @RequestParam String keyword,
            @RequestAttribute("userId") Long userId) {
        try {
            List<UserDTO> users = messageService.searchUsers(keyword, userId);
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
