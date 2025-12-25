USE cloudstack_blog;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE post_tags;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO post_tags (id, post_id, tag_id, created_at)
VALUES
(1, 1, 1, NOW()),
(2, 2, 2, NOW());


-- 追加样例
INSERT INTO post_tags (id, post_id, tag_id, created_at)
VALUES
(3, 16, 22, NOW()),
(4, 16, 23, NOW()),
(5, 17, 24, NOW());
