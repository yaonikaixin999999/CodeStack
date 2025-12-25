USE cloudstack_blog;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE view_history;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO view_history (id, user_id, post_id, ip_address, user_agent, read_duration, read_progress, created_at)
VALUES
(1, 2, 1, '127.0.0.1', 'Mozilla/5.0', 120, 80, NOW());


-- 追加样例
INSERT INTO view_history (id, user_id, post_id, ip_address, user_agent, read_duration, read_progress, created_at)
VALUES
(2, 13, 16, '192.168.1.210', 'Mozilla/5.0', 180, 90, NOW()),
(3, 14, 17, '192.168.1.211', 'Mozilla/5.0', 150, 75, NOW());
