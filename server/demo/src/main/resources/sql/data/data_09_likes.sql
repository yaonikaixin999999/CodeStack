USE cloudstack_blog;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE likes;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO likes (id, user_id, target_id, target_type, created_at)
VALUES
(1, 1, 1, 'post', NOW());


-- 追加样例
INSERT INTO likes (id, user_id, target_id, target_type, created_at)
VALUES
(2, 13, 16, 'post', NOW()),
(3, 14, 17, 'post', NOW()),
(4, 8, 16, 'post', NOW());
