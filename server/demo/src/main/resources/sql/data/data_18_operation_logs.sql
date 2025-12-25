USE cloudstack_blog;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE operation_logs;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO operation_logs (id, user_id, action, target_type, target_id, content, ip_address, user_agent, created_at)
VALUES
(1, 1, 'login', 'user', 1, '管理员登录', '127.0.0.1', 'Mozilla/5.0', NOW()),
(2, 2, 'create_post', 'post', 2, '发布新文章', '127.0.0.1', 'Mozilla/5.0', NOW()),
(3, 3, 'update_profile', 'user', 3, '更新个人资料', '127.0.0.1', 'Mozilla/5.0', NOW());


-- 追加样例
INSERT INTO operation_logs (id, user_id, action, target_type, target_id, content, ip_address, user_agent, created_at)
VALUES
(4, 13, 'create_post', 'post', 16, '发布 Flutter 性能优化文章', '192.168.1.220', 'Mozilla/5.0', NOW()),
(5, 14, 'create_post', 'post', 17, '发布 Web 安全入门文章', '192.168.1.221', 'Mozilla/5.0', NOW()),
(6, 1, 'publish_notice', 'system', NULL, '发布系统公告', '192.168.1.222', 'Mozilla/5.0', NOW());
