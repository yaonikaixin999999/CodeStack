USE cloudstack_blog;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE bookmarks;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO bookmarks (id, user_id, post_id, folder_id, note, created_at)
VALUES
(1, 1, 1, NULL, '收藏示例', NOW());


-- 追加样例
INSERT INTO bookmarks (id, user_id, post_id, folder_id, note, created_at)
VALUES
(2, 13, 16, 3, '性能优化收藏', NOW()),
(3, 14, 17, 2, '安全主题', NOW());
