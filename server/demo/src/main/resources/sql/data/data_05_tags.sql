USE cloudstack_blog;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE tags;
SET FOREIGN_KEY_CHECKS = 1;

-- 从 sql.sql 迁移的示例数据
INSERT INTO `tags` (`name`, `slug`, `color`) VALUES
('Vue.js', 'vuejs', '#42b883'),
('React', 'react', '#61dafb'),
('TypeScript', 'typescript', '#3178c6'),
('JavaScript', 'javascript', '#f7df1e'),
('Node.js', 'nodejs', '#339933'),
('Python', 'python', '#3776ab'),
('Java', 'java', '#007396'),
('Go', 'golang', '#00add8'),
('Docker', 'docker', '#2496ed'),
('Kubernetes', 'kubernetes', '#326ce5'),
('MySQL', 'mysql', '#4479a1'),
('Redis', 'redis', '#dc382d'),
('MongoDB', 'mongodb', '#47a248'),
('Git', 'git', '#f05032'),
('Linux', 'linux', '#fcc624'),
('机器学习', 'machine-learning', '#ff6f00'),
('深度学习', 'deep-learning', '#ff6f00'),
('微服务', 'microservices', '#6db33f'),
('设计模式', 'design-patterns', '#9b59b6'),
('性能优化', 'performance', '#e74c3c') ON DUPLICATE KEY UPDATE id=id;


-- 追加样例
INSERT INTO `tags` (`name`, `slug`, `color`) VALUES
('Spring Boot', 'spring-boot', '#6db33f'),
('Flutter', 'flutter', '#02569b'),
('Swift', 'swift', '#ff9f1c'),
('Security', 'security', '#2c3e50'),
('GraphQL', 'graphql', '#e10098') ON DUPLICATE KEY UPDATE id=id;
