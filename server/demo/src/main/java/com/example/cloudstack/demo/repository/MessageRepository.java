package com.example.cloudstack.demo.repository;

import com.example.cloudstack.demo.model.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 消息数据访问接口
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * 获取两个用户之间的消息（按时间正序）
     */
    @Query("SELECT m FROM Message m WHERE " +
            "(m.senderId = :userId1 AND m.receiverId = :userId2) OR " +
            "(m.senderId = :userId2 AND m.receiverId = :userId1) " +
            "ORDER BY m.createdAt ASC")
    List<Message> findMessagesBetweenUsers(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2);

    /**
     * 获取两个用户之间的消息（分页，按时间倒序）
     */
    @Query("SELECT m FROM Message m WHERE " +
            "(m.senderId = :userId1 AND m.receiverId = :userId2) OR " +
            "(m.senderId = :userId2 AND m.receiverId = :userId1) " +
            "ORDER BY m.createdAt DESC")
    Page<Message> findMessagesBetweenUsersPaged(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2,
            Pageable pageable);

    /**
     * 获取用户的会话列表（查找与当前用户有消息往来的所有用户）
     * 返回每个会话的最新消息
     */
    @Query(value = "SELECT m.* FROM private_messages m " +
            "INNER JOIN (" +
            "  SELECT " +
            "    CASE WHEN sender_id < receiver_id THEN sender_id ELSE receiver_id END as user1, " +
            "    CASE WHEN sender_id < receiver_id THEN receiver_id ELSE sender_id END as user2, " +
            "    MAX(id) as max_id " +
            "  FROM private_messages " +
            "  WHERE sender_id = :userId OR receiver_id = :userId " +
            "  GROUP BY user1, user2" +
            ") latest ON m.id = latest.max_id " +
            "ORDER BY m.created_at DESC", nativeQuery = true)
    List<Message> findConversationList(@Param("userId") Long userId);

    /**
     * 获取与指定用户的最新消息
     */
    @Query("SELECT m FROM Message m WHERE " +
            "(m.senderId = :userId1 AND m.receiverId = :userId2) OR " +
            "(m.senderId = :userId2 AND m.receiverId = :userId1) " +
            "ORDER BY m.createdAt DESC LIMIT 1")
    Message findLatestMessageBetweenUsers(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2);

    /**
     * 删除两个用户之间的所有消息
     */
    @Query("DELETE FROM Message m WHERE " +
            "(m.senderId = :userId1 AND m.receiverId = :userId2) OR " +
            "(m.senderId = :userId2 AND m.receiverId = :userId1)")
    void deleteMessagesBetweenUsers(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2);
}
