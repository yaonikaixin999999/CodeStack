USE cloudstack_blog;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE post_drafts;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO post_drafts (id, user_id, title, content, created_at, updated_at)
VALUES
(1, 2, '草稿示例', '这是一篇草稿内容', NOW(), NOW());


-- 追加样例
INSERT INTO post_drafts (id, user_id, title, content, created_at, updated_at)
VALUES
(2, 13, '移动端性能笔记', '整理 Flutter 与原生性能对比要点', NOW(), NOW()),
(3, 14, '安全检查清单', '准备发布 Web 安全检查清单', NOW(), NOW());
