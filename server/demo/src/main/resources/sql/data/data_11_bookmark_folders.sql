USE cloudstack_blog;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE bookmark_folders;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO bookmark_folders (id, user_id, name, description, is_public, post_count, created_at, updated_at)
VALUES
(1, 1, '默认收藏夹', '示例收藏夹', 0, 1, NOW(), NOW());


-- 追加样例
INSERT INTO bookmark_folders (id, user_id, name, description, is_public, post_count, created_at, updated_at)
VALUES
(2, 14, '安全资料', 'Web 安全相关收藏', 0, 1, NOW(), NOW()),
(3, 13, '移动开发', '移动端优化相关文章', 0, 1, NOW(), NOW());
