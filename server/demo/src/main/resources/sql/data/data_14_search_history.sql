USE cloudstack_blog;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE search_history;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO search_history (id, user_id, keyword, created_at)
VALUES
(1, 2, 'Vue3', NOW());


-- 追加样例
INSERT INTO search_history (id, user_id, keyword, created_at)
VALUES
(2, 13, 'Flutter optimization', NOW()),
(3, 14, 'Web security', NOW()),
(4, 2, 'GraphQL', NOW());
