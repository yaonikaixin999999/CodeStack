-- ============================================
-- CloudStack Blog 私信消息系统数据库设计
-- 数据库: MySQL 8.0+
-- 创建时间: 2025-12-25
-- ============================================

USE cloudstack_blog;

-- ============================================
-- 19. 私信消息表 (private_messages)
-- ============================================
CREATE TABLE IF NOT EXISTS `private_messages` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `sender_id` BIGINT UNSIGNED NOT NULL COMMENT '发送者ID',
    `receiver_id` BIGINT UNSIGNED NOT NULL COMMENT '接收者ID',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (`id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_receiver_id` (`receiver_id`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_conversation` (`sender_id`, `receiver_id`, `created_at`),
    CONSTRAINT `fk_pm_sender` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_pm_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私信消息表';

-- ============================================
-- 私信会话视图 (用于获取会话列表)
-- ============================================
CREATE OR REPLACE VIEW `v_conversations` AS
SELECT 
    LEAST(sender_id, receiver_id) AS user1_id,
    GREATEST(sender_id, receiver_id) AS user2_id,
    MAX(id) AS last_message_id,
    MAX(created_at) AS last_message_at
FROM `private_messages`
GROUP BY LEAST(sender_id, receiver_id), GREATEST(sender_id, receiver_id);
