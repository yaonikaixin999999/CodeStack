USE cloudstack_blog;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO users (id, username, email, password, nickname, avatar, bio, profession, location, website, level, status, role, is_admin, created_at, updated_at)
VALUES
(1, 'admin', 'admin@example.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5XKxUY8Qq1aE/VYRqQGL2ZXl/IXmG', '管理员', NULL, '站点管理员', '站点管理员', '北京', NULL, 10, 1, 'admin', 1, NOW(), NOW()),
(2, 'user', 'user@example.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5XKxUY8Qq1aE/VYRqQGL2ZXl/IXmG', '演示用户', NULL, '演示账号', '产品经理', '上海', NULL, 3, 1, 'user', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  email=VALUES(email),
  password=VALUES(password),
  nickname=VALUES(nickname),
  role=VALUES(role),
  is_admin=VALUES(is_admin),
  updated_at=NOW();


-- 从 sql.sql 迁移的示例数据
INSERT INTO `users` (
    `username`, `email`, `password`, `nickname`, `role`, `is_admin`, `status`
) VALUES (
    'admin', 'admin@example.com', 'admin', 'Administrator', 'admin', 1, 1
)
ON DUPLICATE KEY UPDATE
    `role` = VALUES(`role`),
    `is_admin` = VALUES(`is_admin`);
INSERT INTO users (id, username, email, password, nickname, avatar, bio, profession, location, website, level, status, role, is_admin, created_at, updated_at) VALUES
(3, 'frontend_dev', 'frontend@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '前端小王', 'https://api.dicebear.com/7.x/avataaars/svg?seed=frontend', '专注于前端开发，Vue/React全栈工程师', '前端工程师', '北京', 'https://github.com/frontend_dev', 5, 1, 'user', 0, '2024-01-15 10:30:00', NOW()),

(4, 'java_master', 'java@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'Java大神', 'https://api.dicebear.com/7.x/avataaars/svg?seed=java', '10年Java开发经验，专注于微服务架构', '高级Java工程师', '上海', 'https://github.com/java_master', 8, 1, 'user', 0, '2024-02-20 14:20:00', NOW()),

(5, 'ai_researcher', 'ai@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'AI研究员', 'https://api.dicebear.com/7.x/avataaars/svg?seed=ai', '人工智能与机器学习研究者，热爱探索新技术', 'AI工程师', '深圳', 'https://github.com/ai_researcher', 6, 1, 'user', 0, '2024-03-10 09:15:00', NOW()),

(6, 'devops_ninja', 'devops@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '运维忍者', 'https://api.dicebear.com/7.x/avataaars/svg?seed=devops', 'DevOps工程师，专注于CI/CD和云原生技术', 'DevOps工程师', '杭州', 'https://github.com/devops_ninja', 7, 1, 'user', 0, '2024-04-05 16:45:00', NOW()),

(7, 'database_guru', 'db@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '数据库专家', 'https://api.dicebear.com/7.x/avataaars/svg?seed=database', '数据库架构师，精通MySQL/PostgreSQL/MongoDB', '数据库工程师', '广州', 'https://github.com/database_guru', 9, 1, 'user', 0, '2024-01-28 11:00:00', NOW()),

(8, 'fullstack_coder', 'fullstack@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '全栈小李', 'https://api.dicebear.com/7.x/avataaars/svg?seed=fullstack', '全栈开发者，什么都会一点点', '全栈工程师', '成都', 'https://github.com/fullstack_coder', 4, 1, 'user', 0, '2024-05-12 08:30:00', NOW()),

(9, 'tech_blogger', 'blogger@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '技术博主', 'https://api.dicebear.com/7.x/avataaars/svg?seed=blogger', '喜欢写技术文章，分享学习心得', '技术写作者', '南京', 'https://github.com/tech_blogger', 6, 1, 'user', 0, '2024-06-08 13:20:00', NOW()),

(10, 'cloud_architect', 'cloud@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '云架构师', 'https://api.dicebear.com/7.x/avataaars/svg?seed=cloud', '专注于云计算和分布式系统架构设计', '云架构师', '武汉', 'https://github.com/cloud_architect', 10, 1, 'user', 0, '2024-02-14 15:10:00', NOW()),

(11, 'security_expert', 'security@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '安全专家', 'https://api.dicebear.com/7.x/avataaars/svg?seed=security', '网络安全工程师，专注于应用安全和渗透测试', '安全工程师', '西安', 'https://github.com/security_expert', 7, 1, 'user', 0, '2024-03-25 10:05:00', NOW()),

(12, 'mobile_dev', 'mobile@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '移动端小陈', 'https://api.dicebear.com/7.x/avataaars/svg?seed=mobile', 'iOS/Android双端开发，Flutter爱好者', '移动开发工程师', '重庆', 'https://github.com/mobile_dev', 5, 1, 'user', 0, '2024-04-18 09:40:00', NOW()) ON DUPLICATE KEY UPDATE id=id;


-- 追加样例
INSERT INTO users (id, username, email, password, nickname, avatar, bio, profession, location, website, level, status, role, is_admin, created_at, updated_at)
VALUES
(13, 'qa_tester', 'qa@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '测试工程师', 'https://api.dicebear.com/7.x/avataaars/svg?seed=qa', '关注质量与自动化测试', '测试工程师', '苏州', 'https://github.com/qa_tester', 4, 1, 'user', 0, '2024-07-10 09:00:00', NOW()),
(14, 'data_engineer', 'data@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '数据工程师', 'https://api.dicebear.com/7.x/avataaars/svg?seed=data', '数据平台与ETL开发', '数据工程师', '合肥', 'https://github.com/data_engineer', 6, 1, 'user', 0, '2024-07-18 10:30:00', NOW()),
(15, 'product_owner', 'product@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '产品负责人', 'https://api.dicebear.com/7.x/avataaars/svg?seed=product', '关注用户体验与需求管理', '产品经理', '南京', 'https://github.com/product_owner', 5, 1, 'user', 0, '2024-07-25 14:10:00', NOW())
ON DUPLICATE KEY UPDATE id=id;
