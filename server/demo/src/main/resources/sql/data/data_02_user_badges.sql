USE cloudstack_blog;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE user_badges;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO user_badges (id, user_id, badge_name, badge_icon, badge_desc, obtained_at)
VALUES
(1, 1, 'Founder', NULL, '站点创建者', NOW());


-- 追加样例
INSERT INTO user_badges (id, user_id, badge_name, badge_icon, badge_desc, obtained_at)
VALUES
(2, 2, 'Contributor', NULL, '活跃贡献者', NOW()),
(3, 3, 'Rising Star', NULL, '新星作者', NOW()),
(4, 13, 'QA Master', NULL, '测试专家', NOW());
