USE cloudstack_blog;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE hot_searches;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO hot_searches (id, keyword, search_count, is_recommended, updated_at)
VALUES
(1, 'Spring Boot', 100, 1, NOW()),
(2, 'Vue 3', 90, 1, NOW()),
(3, 'Docker', 80, 0, NOW()),
(4, 'Flutter', 70, 0, NOW());


-- 追加样例
INSERT INTO hot_searches (id, keyword, search_count, is_recommended, updated_at)
VALUES
(5, 'Security', 65, 0, NOW()),
(6, 'Flutter performance', 60, 0, NOW()),
(7, 'GraphQL', 55, 0, NOW());
