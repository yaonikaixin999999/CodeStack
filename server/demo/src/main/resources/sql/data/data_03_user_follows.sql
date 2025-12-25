USE cloudstack_blog;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE user_follows;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO user_follows (id, follower_id, following_id, created_at)
VALUES
(1, 2, 1, NOW());


-- 追加样例
INSERT INTO user_follows (id, follower_id, following_id, created_at)
VALUES
(2, 3, 2, NOW()),
(3, 4, 1, NOW()),
(4, 13, 2, NOW()),
(5, 14, 3, NOW()),
(6, 15, 1, NOW());
