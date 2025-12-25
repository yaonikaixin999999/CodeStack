USE cloudstack_blog;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE notifications;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO notifications (id, user_id, sender_id, type, title, content, target_id, target_type, is_read, created_at)
VALUES
(1, 1, 2, 'comment', '有新评论', 'user 对你的文章发表评论', 1, 'post', 0, NOW());


-- 追加样例
INSERT INTO notifications (id, user_id, sender_id, type, title, content, target_id, target_type, is_read, created_at)
VALUES
(2, 13, 1, 'system', '系统提示', '欢迎完善个人资料', NULL, NULL, 0, NOW()),
(3, 14, 13, 'like', '收到点赞', '你的文章收到新的点赞', 17, 'post', 0, NOW());
