USE cloudstack_blog;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE categories;
SET FOREIGN_KEY_CHECKS = 1;

-- 从 sql.sql 迁移的示例数据
INSERT INTO `categories` (`name`, `slug`, `description`, `icon`, `sort_order`) VALUES
('前端开发', 'frontend', '前端开发相关技术文章', 'code', 1),
('后端开发', 'backend', '后端开发相关技术文章', 'server', 2),
('人工智能', 'ai', 'AI/机器学习相关文章', 'cpu', 3),
('数据库', 'database', '数据库相关技术文章', 'database', 4),
('DevOps', 'devops', '运维与DevOps相关文章', 'terminal', 5),
('其他', 'other', '其他技术分享', 'folder', 99) ON DUPLICATE KEY UPDATE id=id;


-- 追加样例
INSERT INTO `categories` (`name`, `slug`, `description`, `icon`, `sort_order`) VALUES
('移动开发', 'mobile', '移动端开发相关内容', 'mobile', 6),
('安全', 'security', '安全与防护相关主题', 'shield', 7),
('产品', 'product', '产品与增长相关内容', 'layers', 8) ON DUPLICATE KEY UPDATE id=id;
