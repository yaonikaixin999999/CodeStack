-- 示例用户数据
-- 密码统一为: password123 (BCrypt加密)
-- BCrypt hash for 'password123': $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH

-- ========================================
-- 用户数据 (user_id 从 3 开始，因为 1 和 2 已存在)
-- ========================================

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

(12, 'mobile_dev', 'mobile@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '移动端小陈', 'https://api.dicebear.com/7.x/avataaars/svg?seed=mobile', 'iOS/Android双端开发，Flutter爱好者', '移动开发工程师', '重庆', 'https://github.com/mobile_dev', 5, 1, 'user', 0, '2024-04-18 09:40:00', NOW());


-- ========================================
-- 评论数据 (针对上面创建的15篇文章)
-- 分两批插入：先插入主评论，再插入回复评论
-- ========================================

-- 第一批：主评论 (parent_id = NULL)
INSERT INTO comments (post_id, user_id, parent_id, reply_to_user_id, content, like_count, reply_count, is_top, status, ip_address, created_at, updated_at) VALUES
-- 文章1: Vue 3 Composition API 完全指南
(1, 3, NULL, NULL, '这篇文章写得太详细了！Composition API确实比Options API灵活很多，特别是在处理复杂逻辑的时候。', 15, 2, 0, 1, '192.168.1.101', '2024-08-16 10:30:00', NOW()),
(1, 4, NULL, NULL, '作为后端开发，最近在学Vue3，这篇文章帮助很大，感谢分享！', 8, 1, 0, 1, '192.168.1.102', '2024-08-16 14:20:00', NOW()),
-- 文章2: TypeScript 高级类型技巧
(2, 3, NULL, NULL, 'TypeScript的类型系统真的很强大，这些高级技巧在实际项目中用处很大。', 12, 1, 0, 1, '192.168.1.106', '2024-09-11 08:45:00', NOW()),
(2, 8, NULL, NULL, '泛型约束那部分讲得很清楚，终于理解了extends的用法。', 9, 0, 0, 1, '192.168.1.107', '2024-09-11 15:30:00', NOW()),
(2, 7, NULL, NULL, '建议作者再写一篇关于TypeScript装饰器的文章，期待！', 7, 0, 0, 1, '192.168.1.109', '2024-09-12 16:00:00', NOW()),
-- 文章3: CSS Grid 布局实战
(3, 8, NULL, NULL, 'Grid布局真的比Flexbox更适合做复杂的二维布局，文章中的实战案例很有参考价值。', 11, 1, 0, 1, '192.168.1.110', '2024-08-26 09:30:00', NOW()),
(3, 12, NULL, NULL, '移动端适配那部分正是我需要的，minmax函数确实很好用！', 8, 0, 0, 1, '192.168.1.111', '2024-08-26 14:15:00', NOW()),
-- 文章4: React Hooks 深度解析
(4, 3, NULL, NULL, '虽然我主要用Vue，但React的Hooks设计思想确实值得学习，很多理念是相通的。', 10, 2, 0, 1, '192.168.1.113', '2024-10-06 10:00:00', NOW()),
(4, 8, NULL, NULL, 'useEffect的依赖数组那部分讲得很透彻，避免了很多闭包陷阱。', 13, 1, 0, 1, '192.168.1.114', '2024-10-06 15:45:00', NOW()),
-- 文章5: Spring Boot 3.0 新特性
(5, 4, NULL, NULL, '作为Java开发，Spring Boot 3的这些新特性太棒了，原生编译支持期待已久！', 18, 2, 1, 1, '192.168.1.118', '2024-07-21 08:30:00', NOW()),
(5, 10, NULL, NULL, 'GraalVM原生镜像确实能大幅减少启动时间和内存占用，很适合云原生场景。', 14, 1, 0, 1, '192.168.1.119', '2024-07-21 13:00:00', NOW()),
-- 文章6: RESTful API 设计最佳实践
(6, 4, NULL, NULL, '这篇关于API设计的文章很实用，版本控制和错误处理的建议采纳了！', 16, 1, 0, 1, '192.168.1.123', '2024-08-06 10:15:00', NOW()),
(6, 3, NULL, NULL, '作为前端，最怕后端API设计不规范，这篇文章应该让所有后端同学看看。', 12, 1, 0, 1, '192.168.1.124', '2024-08-06 16:30:00', NOW()),
-- 文章7: MySQL 性能优化实战
(7, 7, NULL, NULL, '作为DBA，这篇文章的优化建议都很实用，特别是索引优化那部分。', 20, 2, 1, 1, '192.168.1.127', '2024-09-21 09:00:00', NOW()),
(7, 4, NULL, NULL, '慢查询分析工具介绍得很详细，EXPLAIN的用法学到了新东西。', 15, 1, 0, 1, '192.168.1.128', '2024-09-21 14:30:00', NOW()),
-- 文章8: Docker 从入门到实践
(8, 6, NULL, NULL, '终于有一篇把Docker讲清楚的文章了，从基础到实践都覆盖了。', 17, 2, 1, 1, '192.168.1.132', '2024-07-06 08:45:00', NOW()),
(8, 4, NULL, NULL, 'Dockerfile的最佳实践部分很有用，镜像体积优化学到了不少技巧。', 11, 1, 0, 1, '192.168.1.133', '2024-07-06 13:20:00', NOW()),
-- 文章9: ChatGPT API 开发实战
(9, 5, NULL, NULL, '这篇ChatGPT API教程太及时了，正好在做智能客服项目，参考价值很高！', 22, 2, 1, 1, '192.168.1.137', '2024-10-16 10:30:00', NOW()),
(9, 3, NULL, NULL, 'Token计算和成本控制那部分很实用，API费用优化是个大问题。', 16, 1, 0, 1, '192.168.1.138', '2024-10-16 15:00:00', NOW()),
-- 文章10: 机器学习入门指南
(10, 5, NULL, NULL, '这篇机器学习入门写得很友好，没有太多数学公式，容易理解。', 19, 2, 0, 1, '192.168.1.142', '2024-09-01 08:30:00', NOW()),
(10, 7, NULL, NULL, '数据预处理那部分讲得很详细，这是很多教程忽略但又很重要的环节。', 14, 1, 0, 1, '192.168.1.143', '2024-09-01 13:15:00', NOW()),
-- 文章11: Prompt Engineering 提示词工程
(11, 5, NULL, NULL, '提示词工程现在太重要了，这篇文章的技巧总结得很系统！', 25, 2, 1, 1, '192.168.1.147', '2024-11-06 10:00:00', NOW()),
(11, 9, NULL, NULL, 'Few-shot学习的例子很有启发，原来上下文学习这么有效。', 18, 1, 0, 1, '192.168.1.148', '2024-11-06 14:30:00', NOW()),
-- 文章12: Redis 缓存实战
(12, 7, NULL, NULL, '缓存穿透、击穿、雪崩的解决方案讲得很清楚，面试必考题！', 21, 2, 1, 1, '192.168.1.152', '2024-10-26 09:15:00', NOW()),
(12, 4, NULL, NULL, 'Redis的数据结构应用场景总结得很好，不同场景选不同结构。', 15, 1, 0, 1, '192.168.1.153', '2024-10-26 14:00:00', NOW()),
-- 文章13: MongoDB 实战指南
(13, 7, NULL, NULL, 'MongoDB的聚合管道讲得很详细，这是它最强大的特性之一。', 14, 1, 0, 1, '192.168.1.157', '2024-08-31 08:45:00', NOW()),
(13, 4, NULL, NULL, '什么时候用MongoDB什么时候用MySQL，这篇文章给了很好的参考。', 11, 1, 0, 1, '192.168.1.158', '2024-08-31 13:30:00', NOW()),
-- 文章14: GitHub Actions CI/CD
(14, 6, NULL, NULL, '从Jenkins迁移到GitHub Actions后，配置简单太多了，这篇教程很实用！', 19, 2, 1, 1, '192.168.1.161', '2024-11-16 10:30:00', NOW()),
(14, 4, NULL, NULL, 'Matrix策略可以并行测试多个版本，节省很多时间。', 13, 1, 0, 1, '192.168.1.162', '2024-11-16 15:15:00', NOW()),
-- 文章15: Kubernetes 入门与实践
(15, 6, NULL, NULL, 'K8s入门文章写得很清晰，概念解释到位，适合初学者！', 23, 2, 1, 1, '192.168.1.166', '2024-12-06 09:00:00', NOW()),
(15, 10, NULL, NULL, 'Deployment、Service、Ingress的关系终于搞明白了，感谢！', 17, 1, 0, 1, '192.168.1.167', '2024-12-06 14:20:00', NOW());

-- 获取刚插入的主评论的起始ID（假设数据库为空时从1开始）
-- 第二批：回复评论 (需要引用上面插入的主评论ID)
-- 注意：以下parent_id基于主评论按顺序插入后的ID

SET @base_id = (SELECT MIN(id) FROM comments);

INSERT INTO comments (post_id, user_id, parent_id, reply_to_user_id, content, like_count, reply_count, is_top, status, ip_address, created_at, updated_at) VALUES
-- 回复文章1的评论 (主评论ID: @base_id+0, @base_id+1)
(1, 8, @base_id+0, 3, '同意！setup语法糖也很好用，代码更简洁了。', 5, 0, 0, 1, '192.168.1.103', '2024-08-16 16:45:00', NOW()),
(1, 9, @base_id+0, 3, '我觉得响应式系统是Vue3最大的改进，ref和reactive用起来很直观。', 3, 0, 0, 1, '192.168.1.104', '2024-08-17 09:15:00', NOW()),
(1, 5, @base_id+1, 4, '后端转前端+1，Vue3的学习曲线还是比较友好的。', 4, 0, 0, 1, '192.168.1.105', '2024-08-17 11:30:00', NOW()),
-- 回复文章2的评论 (主评论ID: @base_id+2)
(2, 10, @base_id+2, 3, '条件类型配合infer用来做类型推断特别好用，可以写出很多实用的工具类型。', 6, 0, 0, 1, '192.168.1.108', '2024-09-12 10:20:00', NOW()),
-- 回复文章3的评论 (主评论ID: @base_id+5)
(3, 3, @base_id+5, 8, '我们项目里Grid和Flexbox混用，效果很好，各有各的适用场景。', 4, 0, 0, 1, '192.168.1.112', '2024-08-27 11:00:00', NOW()),
-- 回复文章4的评论 (主评论ID: @base_id+7, @base_id+8)
(4, 5, @base_id+7, 3, 'Vue的Composition API和React Hooks确实有异曲同工之妙。', 5, 0, 0, 1, '192.168.1.115', '2024-10-07 09:20:00', NOW()),
(4, 9, @base_id+7, 3, '学会了Hooks再去看Vue3的响应式原理，理解更深刻了。', 4, 0, 0, 1, '192.168.1.116', '2024-10-07 14:30:00', NOW()),
(4, 10, @base_id+8, 8, '自定义Hooks复用逻辑确实很优雅，比之前的HOC和render props好用多了。', 6, 0, 0, 1, '192.168.1.117', '2024-10-08 11:15:00', NOW()),
-- 回复文章5的评论 (主评论ID: @base_id+9, @base_id+10)
(5, 6, @base_id+9, 4, 'Docker镜像从几百MB减少到几十MB，部署效率提升明显！', 9, 0, 0, 1, '192.168.1.120', '2024-07-22 10:45:00', NOW()),
(5, 7, @base_id+9, 4, '迁移到Jakarta EE需要注意包名的变化，javax改jakarta。', 7, 0, 0, 1, '192.168.1.121', '2024-07-22 15:20:00', NOW()),
(5, 11, @base_id+10, 10, '原生编译后的安全性也得到了提升，攻击面减小了。', 5, 0, 0, 1, '192.168.1.122', '2024-07-23 09:00:00', NOW()),
-- 回复文章6的评论 (主评论ID: @base_id+11, @base_id+12)
(6, 10, @base_id+11, 4, 'HATEOAS那部分讲得很好，虽然实际项目用得不多，但理念值得学习。', 6, 0, 0, 1, '192.168.1.125', '2024-08-07 11:00:00', NOW()),
(6, 8, @base_id+12, 3, '哈哈确实，好的API设计能减少很多前后端沟通成本。', 4, 0, 0, 1, '192.168.1.126', '2024-08-07 14:45:00', NOW()),
-- 回复文章7的评论 (主评论ID: @base_id+13, @base_id+14)
(7, 10, @base_id+13, 7, '大表分区那部分很有参考价值，我们线上系统正好需要这个方案。', 8, 0, 0, 1, '192.168.1.129', '2024-09-22 10:20:00', NOW()),
(7, 6, @base_id+13, 7, '加上监控告警，性能问题可以提前发现，运维起来省心很多。', 6, 0, 0, 1, '192.168.1.130', '2024-09-22 15:10:00', NOW()),
(7, 11, @base_id+14, 4, '建议补充一下连接池的配置优化，HikariCP的参数调优也很重要。', 5, 0, 0, 1, '192.168.1.131', '2024-09-23 11:30:00', NOW()),
-- 回复文章8的评论 (主评论ID: @base_id+15, @base_id+16)
(8, 8, @base_id+15, 6, '多阶段构建确实能大幅减小镜像体积，生产环境必备！', 7, 0, 0, 1, '192.168.1.134', '2024-07-07 10:00:00', NOW()),
(8, 10, @base_id+15, 6, '建议再出一篇Docker Compose编排的文章，多容器协作场景也很常见。', 9, 0, 0, 1, '192.168.1.135', '2024-07-07 15:30:00', NOW()),
(8, 12, @base_id+16, 4, '前端项目用Docker部署确实方便，Nginx镜像配合多阶段构建很好用。', 4, 0, 0, 1, '192.168.1.136', '2024-07-08 09:15:00', NOW()),
-- 回复文章9的评论 (主评论ID: @base_id+17, @base_id+18)
(9, 9, @base_id+17, 5, 'Function Calling功能真的很强大，可以让AI调用自定义函数了。', 10, 0, 0, 1, '192.168.1.139', '2024-10-17 11:20:00', NOW()),
(9, 8, @base_id+17, 5, '流式响应那部分代码可以直接用，用户体验提升很多！', 8, 0, 0, 1, '192.168.1.140', '2024-10-17 14:45:00', NOW()),
(9, 10, @base_id+18, 3, '建议加上缓存策略，重复问题可以直接返回缓存，省钱又快。', 6, 0, 0, 1, '192.168.1.141', '2024-10-18 09:30:00', NOW()),
-- 回复文章10的评论 (主评论ID: @base_id+19, @base_id+20)
(10, 9, @base_id+19, 5, '建议推荐一些数据集，方便初学者实践练习。', 7, 0, 0, 1, '192.168.1.144', '2024-09-02 10:40:00', NOW()),
(10, 4, @base_id+19, 5, '看完这篇后去学了sklearn，确实如文章所说，API设计很友好。', 5, 0, 0, 1, '192.168.1.145', '2024-09-02 15:00:00', NOW()),
(10, 8, @base_id+20, 7, '特征工程那章可以再展开讲讲，这块对模型效果影响很大。', 6, 0, 0, 1, '192.168.1.146', '2024-09-03 11:25:00', NOW()),
-- 回复文章11的评论 (主评论ID: @base_id+21, @base_id+22)
(11, 3, @base_id+21, 5, 'Chain of Thought那部分太棒了，让AI分步思考确实效果更好。', 12, 0, 0, 1, '192.168.1.149', '2024-11-07 09:45:00', NOW()),
(11, 8, @base_id+21, 5, '提示词模板那个仓库收藏了，日常工作中很实用！', 9, 0, 0, 1, '192.168.1.150', '2024-11-07 15:20:00', NOW()),
(11, 10, @base_id+22, 9, '结合RAG做检索增强，效果更上一层楼，期待作者的续篇。', 7, 0, 0, 1, '192.168.1.151', '2024-11-08 11:00:00', NOW()),
-- 回复文章12的评论 (主评论ID: @base_id+23, @base_id+24)
(12, 6, @base_id+23, 7, '分布式锁那部分可以再详细讲讲Redisson的实现，更可靠一些。', 8, 0, 0, 1, '192.168.1.154', '2024-10-27 10:30:00', NOW()),
(12, 10, @base_id+23, 7, 'Redis集群和哨兵模式也可以补充一下，高可用很重要。', 6, 0, 0, 1, '192.168.1.155', '2024-10-27 15:45:00', NOW()),
(12, 11, @base_id+24, 4, '建议加上Redis的安全配置，线上环境安全第一。', 5, 0, 0, 1, '192.168.1.156', '2024-10-28 11:20:00', NOW()),
-- 回复文章13的评论 (主评论ID: @base_id+25, @base_id+26)
(13, 5, @base_id+25, 7, '日志存储用MongoDB确实合适，schema-free的特性很方便。', 6, 0, 0, 1, '192.168.1.159', '2024-09-01 10:15:00', NOW()),
(13, 10, @base_id+26, 4, '索引策略那部分很重要，MongoDB的索引设计和关系型有区别。', 5, 0, 0, 1, '192.168.1.160', '2024-09-01 15:00:00', NOW()),
-- 回复文章14的评论 (主评论ID: @base_id+27, @base_id+28)
(14, 3, @base_id+27, 6, '缓存action用起来后，依赖安装时间从5分钟降到30秒！', 9, 0, 0, 1, '192.168.1.163', '2024-11-17 09:00:00', NOW()),
(14, 10, @base_id+27, 6, 'Self-hosted runner在私有环境部署很方便，成本也低。', 7, 0, 0, 1, '192.168.1.164', '2024-11-17 14:30:00', NOW()),
(14, 11, @base_id+28, 4, 'Secrets管理那部分可以再详细讲讲，安全存储敏感信息很重要。', 5, 0, 0, 1, '192.168.1.165', '2024-11-18 11:45:00', NOW()),
-- 回复文章15的评论 (主评论ID: @base_id+29, @base_id+30)
(15, 4, @base_id+29, 6, 'HPA自动扩缩容那部分很实用，根据负载自动调整副本数。', 10, 0, 0, 1, '192.168.1.168', '2024-12-07 10:30:00', NOW()),
(15, 7, @base_id+29, 6, 'ConfigMap和Secret的使用也可以补充一下，配置管理很重要。', 8, 0, 0, 1, '192.168.1.169', '2024-12-07 15:45:00', NOW()),
(15, 11, @base_id+30, 10, 'RBAC权限控制那部分希望能详细讲讲，生产环境安全很重要。', 6, 0, 0, 1, '192.168.1.170', '2024-12-08 11:00:00', NOW());


-- ========================================
-- 更新文章的评论数量 (可选，如果数据库没有触发器自动更新)
-- ========================================

UPDATE posts SET comment_count = 5 WHERE id = 1;
UPDATE posts SET comment_count = 4 WHERE id = 2;
UPDATE posts SET comment_count = 3 WHERE id = 3;
UPDATE posts SET comment_count = 5 WHERE id = 4;
UPDATE posts SET comment_count = 5 WHERE id = 5;
UPDATE posts SET comment_count = 4 WHERE id = 6;
UPDATE posts SET comment_count = 5 WHERE id = 7;
UPDATE posts SET comment_count = 5 WHERE id = 8;
UPDATE posts SET comment_count = 5 WHERE id = 9;
UPDATE posts SET comment_count = 5 WHERE id = 10;
UPDATE posts SET comment_count = 5 WHERE id = 11;
UPDATE posts SET comment_count = 5 WHERE id = 12;
UPDATE posts SET comment_count = 4 WHERE id = 13;
UPDATE posts SET comment_count = 5 WHERE id = 14;
UPDATE posts SET comment_count = 5 WHERE id = 15;
