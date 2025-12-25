-- ============================================
-- CloudStack Blog 数据库架构（来自 sql.sql）
-- ============================================

-- 先删库再建库（仅开发环境）
DROP DATABASE IF EXISTS cloudstack_blog;
CREATE DATABASE IF NOT EXISTS cloudstack_blog
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE cloudstack_blog;

-- 先清空旧表（避免外键冲突）
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `user_badges`;
DROP TABLE IF EXISTS `user_follows`;
DROP TABLE IF EXISTS `categories`;
DROP TABLE IF EXISTS `tags`;
DROP TABLE IF EXISTS `posts`;
DROP TABLE IF EXISTS `post_tags`;
DROP TABLE IF EXISTS `comments`;
DROP TABLE IF EXISTS `likes`;
DROP TABLE IF EXISTS `bookmarks`;
DROP TABLE IF EXISTS `bookmark_folders`;
DROP TABLE IF EXISTS `view_history`;
DROP TABLE IF EXISTS `notifications`;
DROP TABLE IF EXISTS `search_history`;
DROP TABLE IF EXISTS `hot_searches`;
DROP TABLE IF EXISTS `post_drafts`;
DROP TABLE IF EXISTS `system_configs`;
DROP TABLE IF EXISTS `operation_logs`;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(加密存储)',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `bio` TEXT DEFAULT NULL COMMENT '个人简介',
    `profession` VARCHAR(100) DEFAULT NULL COMMENT '职业',
    `location` VARCHAR(100) DEFAULT NULL COMMENT '所在地',
    `website` VARCHAR(255) DEFAULT NULL COMMENT '个人网站',
    `level` TINYINT UNSIGNED DEFAULT 1 COMMENT '用户等级',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    `role` VARCHAR(20) DEFAULT 'user' COMMENT '角色: user-普通用户, admin-管理员',
    `is_admin` TINYINT(1) DEFAULT 0 COMMENT '是否管理员: 1-是, 0-否',
    `last_login_at` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(45) DEFAULT NULL COMMENT '最后登录IP',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `user_badges` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `badge_name` VARCHAR(50) NOT NULL COMMENT '徽章名称',
    `badge_icon` VARCHAR(255) DEFAULT NULL COMMENT '徽章图标',
    `badge_desc` VARCHAR(255) DEFAULT NULL COMMENT '徽章描述',
    `obtained_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '获得时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    CONSTRAINT `fk_user_badges_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户徽章表';

CREATE TABLE IF NOT EXISTS `user_follows` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `follower_id` BIGINT UNSIGNED NOT NULL COMMENT '关注者ID',
    `following_id` BIGINT UNSIGNED NOT NULL COMMENT '被关注者ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_follow_relation` (`follower_id`, `following_id`),
    KEY `idx_follower_id` (`follower_id`),
    KEY `idx_following_id` (`following_id`),
    CONSTRAINT `fk_follows_follower` FOREIGN KEY (`follower_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_follows_following` FOREIGN KEY (`following_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注关系表';

CREATE TABLE IF NOT EXISTS `categories` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `slug` VARCHAR(50) NOT NULL COMMENT '分类别名(URL友好)',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '分类描述',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '分类图标',
    `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '分类封面图',
    `parent_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '父分类ID',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `post_count` INT UNSIGNED DEFAULT 0 COMMENT '文章数量(冗余字段)',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_slug` (`slug`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章分类表';

CREATE TABLE IF NOT EXISTS `tags` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `slug` VARCHAR(50) NOT NULL COMMENT '标签别名(URL友好)',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '标签描述',
    `color` VARCHAR(20) DEFAULT '#3a9cff' COMMENT '标签颜色',
    `post_count` INT UNSIGNED DEFAULT 0 COMMENT '文章数量(冗余字段)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    UNIQUE KEY `uk_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

CREATE TABLE IF NOT EXISTS `posts` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '文章ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '作者ID',
    `category_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '分类ID',
    `title` VARCHAR(200) NOT NULL COMMENT '文章标题',
    `slug` VARCHAR(200) DEFAULT NULL COMMENT '文章别名(URL友好)',
    `excerpt` VARCHAR(500) DEFAULT NULL COMMENT '文章摘要',
    `content` LONGTEXT NOT NULL COMMENT '文章内容(Markdown/HTML)',
    `content_html` LONGTEXT DEFAULT NULL COMMENT '文章内容(渲染后的HTML)',
    `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图片URL',
    `word_count` INT UNSIGNED DEFAULT 0 COMMENT '字数统计',
    `read_time` INT UNSIGNED DEFAULT 0 COMMENT '预计阅读时间(分钟)',
    `view_count` INT UNSIGNED DEFAULT 0 COMMENT '浏览量',
    `like_count` INT UNSIGNED DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT UNSIGNED DEFAULT 0 COMMENT '评论数',
    `bookmark_count` INT UNSIGNED DEFAULT 0 COMMENT '收藏数',
    `share_count` INT UNSIGNED DEFAULT 0 COMMENT '分享数',
    `is_top` TINYINT DEFAULT 0 COMMENT '是否置顶: 0-否, 1-是',
    `is_featured` TINYINT DEFAULT 0 COMMENT '是否精选: 0-否, 1-是',
    `is_original` TINYINT DEFAULT 1 COMMENT '是否原创: 0-转载, 1-原创',
    `source_url` VARCHAR(500) DEFAULT NULL COMMENT '转载来源URL',
    `allow_comment` TINYINT DEFAULT 1 COMMENT '是否允许评论: 0-否, 1-是',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-草稿, 1-已发布, 2-待审核, 3-已下架',
    `published_at` DATETIME DEFAULT NULL COMMENT '发布时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间(软删除)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_slug` (`slug`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_published_at` (`published_at`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_view_count` (`view_count`),
    KEY `idx_like_count` (`like_count`),
    FULLTEXT KEY `ft_title_content` (`title`, `excerpt`),
    CONSTRAINT `fk_posts_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_posts_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

CREATE TABLE IF NOT EXISTS `post_tags` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `post_id` BIGINT UNSIGNED NOT NULL COMMENT '文章ID',
    `tag_id` BIGINT UNSIGNED NOT NULL COMMENT '标签ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_tag` (`post_id`, `tag_id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_tag_id` (`tag_id`),
    CONSTRAINT `fk_post_tags_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_post_tags_tag` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签关联表';

CREATE TABLE IF NOT EXISTS `comments` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `post_id` BIGINT UNSIGNED NOT NULL COMMENT '文章ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '评论者ID',
    `parent_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '父评论ID(用于回复)',
    `reply_to_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '回复目标用户ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `like_count` INT UNSIGNED DEFAULT 0 COMMENT '点赞数',
    `reply_count` INT UNSIGNED DEFAULT 0 COMMENT '回复数',
    `is_top` TINYINT DEFAULT 0 COMMENT '是否置顶: 0-否, 1-是',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-待审核, 1-已发布, 2-已删除',
    `ip_address` VARCHAR(45) DEFAULT NULL COMMENT 'IP地址',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '浏览器信息',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_created_at` (`created_at`),
    CONSTRAINT `fk_comments_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_comments_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_comments_parent` FOREIGN KEY (`parent_id`) REFERENCES `comments` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_comments_reply_user` FOREIGN KEY (`reply_to_user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

CREATE TABLE IF NOT EXISTS `likes` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `target_id` BIGINT UNSIGNED NOT NULL COMMENT '目标ID',
    `target_type` ENUM('post', 'comment') NOT NULL COMMENT '目标类型: post-文章, comment-评论',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_id`, `target_type`),
    KEY `idx_target` (`target_id`, `target_type`),
    KEY `idx_user_id` (`user_id`),
    CONSTRAINT `fk_likes_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞表';

CREATE TABLE IF NOT EXISTS `bookmarks` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `post_id` BIGINT UNSIGNED NOT NULL COMMENT '文章ID',
    `folder_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '收藏夹ID',
    `note` VARCHAR(255) DEFAULT NULL COMMENT '收藏备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_post` (`user_id`, `post_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_folder_id` (`folder_id`),
    CONSTRAINT `fk_bookmarks_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_bookmarks_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

CREATE TABLE IF NOT EXISTS `bookmark_folders` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '收藏夹ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `name` VARCHAR(50) NOT NULL COMMENT '收藏夹名称',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '收藏夹描述',
    `is_public` TINYINT DEFAULT 0 COMMENT '是否公开: 0-私密, 1-公开',
    `post_count` INT UNSIGNED DEFAULT 0 COMMENT '文章数量',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    CONSTRAINT `fk_folders_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏夹表';

CREATE TABLE IF NOT EXISTS `view_history` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '用户ID(可为空,游客)',
    `post_id` BIGINT UNSIGNED NOT NULL COMMENT '文章ID',
    `ip_address` VARCHAR(45) DEFAULT NULL COMMENT 'IP地址',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '浏览器信息',
    `referer` VARCHAR(500) DEFAULT NULL COMMENT '来源页面',
    `read_duration` INT UNSIGNED DEFAULT 0 COMMENT '阅读时长(秒)',
    `read_progress` TINYINT UNSIGNED DEFAULT 0 COMMENT '阅读进度(%)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_created_at` (`created_at`),
    CONSTRAINT `fk_view_history_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_view_history_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='浏览历史表';

CREATE TABLE IF NOT EXISTS `notifications` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '接收用户ID',
    `sender_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '发送用户ID',
    `type` VARCHAR(50) NOT NULL COMMENT '通知类型: like, comment, follow, system等',
    `title` VARCHAR(200) DEFAULT NULL COMMENT '通知标题',
    `content` TEXT DEFAULT NULL COMMENT '通知内容',
    `target_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '目标ID',
    `target_type` VARCHAR(50) DEFAULT NULL COMMENT '目标类型: post, comment等',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
    `read_at` DATETIME DEFAULT NULL COMMENT '阅读时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_type` (`type`),
    KEY `idx_is_read` (`is_read`),
    KEY `idx_created_at` (`created_at`),
    CONSTRAINT `fk_notifications_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_notifications_sender` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

CREATE TABLE IF NOT EXISTS `search_history` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '用户ID',
    `keyword` VARCHAR(100) NOT NULL COMMENT '搜索关键词',
    `result_count` INT UNSIGNED DEFAULT 0 COMMENT '搜索结果数',
    `ip_address` VARCHAR(45) DEFAULT NULL COMMENT 'IP地址',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '搜索时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_keyword` (`keyword`),
    KEY `idx_created_at` (`created_at`),
    CONSTRAINT `fk_search_history_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索历史表';

CREATE TABLE IF NOT EXISTS `hot_searches` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `keyword` VARCHAR(100) NOT NULL COMMENT '搜索关键词',
    `search_count` INT UNSIGNED DEFAULT 0 COMMENT '搜索次数',
    `is_recommended` TINYINT DEFAULT 0 COMMENT '是否推荐: 0-否, 1-是',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_keyword` (`keyword`),
    KEY `idx_search_count` (`search_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='热门搜索词表';

CREATE TABLE IF NOT EXISTS `post_drafts` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '草稿ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `post_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联文章ID(编辑已发布文章时)',
    `title` VARCHAR(200) DEFAULT NULL COMMENT '标题',
    `content` LONGTEXT DEFAULT NULL COMMENT '内容',
    `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图',
    `category_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '分类ID',
    `tags` JSON DEFAULT NULL COMMENT '标签(JSON数组)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_updated_at` (`updated_at`),
    CONSTRAINT `fk_drafts_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_drafts_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章草稿表';

CREATE TABLE IF NOT EXISTS `system_configs` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT DEFAULT NULL COMMENT '配置值',
    `config_type` VARCHAR(20) DEFAULT 'string' COMMENT '配置类型: string, number, boolean, json',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '配置描述',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

CREATE TABLE IF NOT EXISTS `operation_logs` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作用户ID',
    `action` VARCHAR(100) NOT NULL COMMENT '操作类型',
    `target_type` VARCHAR(50) DEFAULT NULL COMMENT '目标类型',
    `target_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '目标ID',
    `content` TEXT DEFAULT NULL COMMENT '操作内容/描述',
    `old_value` JSON DEFAULT NULL COMMENT '原值(JSON)',
    `new_value` JSON DEFAULT NULL COMMENT '新值(JSON)',
    `ip_address` VARCHAR(45) DEFAULT NULL COMMENT 'IP地址',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '浏览器信息',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_action` (`action`),
    KEY `idx_target` (`target_type`, `target_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ============================================
-- 创建视图 (用于常用查询)
-- ============================================

-- 文章列表视图(包含作者信息)
CREATE OR REPLACE VIEW `v_post_list` AS
SELECT 
    p.id,
    p.title,
    p.slug,
    p.excerpt,
    p.cover_image,
    p.view_count,
    p.like_count,
    p.comment_count,
    p.bookmark_count,
    p.is_top,
    p.is_featured,
    p.status,
    p.published_at,
    p.created_at,
    c.id AS category_id,
    c.name AS category_name,
    c.slug AS category_slug,
    u.id AS author_id,
    u.username AS author_username,
    u.nickname AS author_nickname,
    u.avatar AS author_avatar
FROM `posts` p
LEFT JOIN `categories` c ON p.category_id = c.id
LEFT JOIN `users` u ON p.user_id = u.id
WHERE p.deleted_at IS NULL;

-- 用户统计视图
CREATE OR REPLACE VIEW `v_user_stats` AS
SELECT 
    u.id AS user_id,
    u.username,
    u.nickname,
    u.avatar,
    (SELECT COUNT(*) FROM posts WHERE user_id = u.id AND status = 1 AND deleted_at IS NULL) AS post_count,
    (SELECT COUNT(*) FROM user_follows WHERE following_id = u.id) AS follower_count,
    (SELECT COUNT(*) FROM user_follows WHERE follower_id = u.id) AS following_count,
    (SELECT COALESCE(SUM(like_count), 0) FROM posts WHERE user_id = u.id AND status = 1) AS total_likes,
    (SELECT COALESCE(SUM(view_count), 0) FROM posts WHERE user_id = u.id AND status = 1) AS total_views
FROM `users` u;
