USE cloudstack_blog;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE system_configs;
SET FOREIGN_KEY_CHECKS = 1;

-- 从 sql.sql 迁移的示例数据
INSERT INTO `system_configs` (`config_key`, `config_value`, `config_type`, `description`) VALUES
('site_name', 'CloudStack Blog', 'string', '网站名称'),
('site_description', '分享技术 · 记录成长 · 连接世界', 'string', '网站描述'),
('posts_per_page', '10', 'number', '每页文章数'),
('allow_register', 'true', 'boolean', '是否允许注册'),
('allow_comment', 'true', 'boolean', '是否允许评论'),
('comment_audit', 'false', 'boolean', '评论是否需要审核'),
('hot_search_count', '10', 'number', '热门搜索显示数量') ON DUPLICATE KEY UPDATE id=id;


-- 追加样例
INSERT INTO `system_configs` (`config_key`, `config_value`, `config_type`, `description`) VALUES
('site_theme', 'light', 'string', '站点主题'),
('upload_max_size', '2', 'number', '上传大小限制(MB)'),
('enable_two_factor', 'false', 'boolean', '是否开启二次验证') ON DUPLICATE KEY UPDATE id=id;
