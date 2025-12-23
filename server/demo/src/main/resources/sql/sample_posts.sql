-- 博客系统测试文章数据
-- 执行前请确保已有用户(user_id=1或2)和分类数据

-- 清空现有文章（可选，谨慎使用）
-- TRUNCATE TABLE post_tags;
-- TRUNCATE TABLE posts;

-- ===================== 前端开发文章 (category_id = 1) =====================

INSERT INTO posts (user_id, category_id, title, slug, excerpt, content, content_html, word_count, read_time, view_count, like_count, comment_count, status, is_original, is_featured, published_at, created_at, updated_at) VALUES
(2, 1, 'Vue 3 Composition API 完全指南', 'vue3-composition-api-guide', 
'Vue 3 的 Composition API 是一种全新的组织组件逻辑的方式，本文将详细介绍其核心概念和使用方法。',
'# Vue 3 Composition API 完全指南

## 什么是 Composition API？

Composition API 是 Vue 3 引入的一种新的组织组件逻辑的方式。它允许我们使用函数来组织代码，而不是使用选项对象。

## 核心概念

### 1. setup 函数

```javascript
import { ref, reactive, computed } from ''vue''

export default {
  setup() {
    const count = ref(0)
    const state = reactive({ name: ''Vue 3'' })
    
    const doubleCount = computed(() => count.value * 2)
    
    function increment() {
      count.value++
    }
    
    return { count, state, doubleCount, increment }
  }
}
```

### 2. 响应式引用 (ref)

`ref` 用于创建一个响应式的基本类型值：

```javascript
const count = ref(0)
console.log(count.value) // 0
count.value++
console.log(count.value) // 1
```

### 3. 响应式对象 (reactive)

`reactive` 用于创建响应式对象：

```javascript
const state = reactive({
  count: 0,
  name: ''Vue''
})
state.count++ // 响应式更新
```

## 生命周期钩子

Composition API 提供了对应的生命周期钩子函数：

- `onMounted` - 组件挂载后
- `onUpdated` - 组件更新后  
- `onUnmounted` - 组件卸载后

## 总结

Composition API 让代码更易于组织和复用，特别适合大型项目。',
'<h1>Vue 3 Composition API 完全指南</h1><p>Vue 3 的 Composition API 是一种全新的组织组件逻辑的方式...</p>',
800, 4, 1256, 89, 12, 1, 1, 1, NOW(), NOW(), NOW()),

(2, 1, 'TypeScript 入门到精通', 'typescript-beginner-to-master',
'TypeScript 是 JavaScript 的超集，为 JS 添加了类型系统。本文从基础到高级，全面讲解 TypeScript。',
'# TypeScript 入门到精通

## 为什么选择 TypeScript？

TypeScript 提供了静态类型检查，能在编译时发现错误，提高代码质量。

## 基础类型

```typescript
// 基本类型
let isDone: boolean = false
let count: number = 10
let name: string = "TypeScript"

// 数组
let list: number[] = [1, 2, 3]
let list2: Array<number> = [1, 2, 3]

// 元组
let tuple: [string, number] = ["hello", 10]
```

## 接口 (Interface)

```typescript
interface User {
  id: number
  name: string
  email?: string  // 可选属性
  readonly createdAt: Date  // 只读属性
}

function createUser(user: User): User {
  return user
}
```

## 泛型 (Generics)

```typescript
function identity<T>(arg: T): T {
  return arg
}

let output = identity<string>("hello")
```

## 类型推断

TypeScript 能自动推断变量类型：

```typescript
let x = 3  // 推断为 number
let arr = [1, 2, 3]  // 推断为 number[]
```

## 实用技巧

1. 使用 `unknown` 代替 `any`
2. 使用 `const assertions`
3. 善用工具类型如 `Partial`, `Pick`, `Omit`

TypeScript 让大型项目更易维护！',
'<h1>TypeScript 入门到精通</h1><p>TypeScript 是 JavaScript 的超集...</p>',
650, 3, 2341, 156, 23, 1, 1, 1, NOW(), NOW(), NOW()),

(2, 1, 'CSS Grid 布局实战教程', 'css-grid-layout-tutorial',
'CSS Grid 是一种强大的二维布局系统，本文通过实例讲解如何使用 Grid 构建复杂布局。',
'# CSS Grid 布局实战教程

## Grid 基础

CSS Grid 是一个二维布局系统，可以同时处理行和列。

```css
.container {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: auto;
  gap: 20px;
}
```

## 常用属性

### grid-template-columns / rows

定义列和行的大小：

```css
.grid {
  grid-template-columns: 200px 1fr 2fr;
  grid-template-rows: 100px auto;
}
```

### grid-area

使用命名区域：

```css
.container {
  grid-template-areas:
    "header header header"
    "sidebar main main"
    "footer footer footer";
}

.header { grid-area: header; }
.sidebar { grid-area: sidebar; }
.main { grid-area: main; }
.footer { grid-area: footer; }
```

## 实战案例：响应式卡片布局

```css
.cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1rem;
}
```

这样就能创建自适应的卡片网格！',
'<h1>CSS Grid 布局实战教程</h1><p>CSS Grid 是一个二维布局系统...</p>',
450, 2, 876, 67, 8, 1, 1, 0, NOW(), NOW(), NOW()),

(2, 1, 'React Hooks 最佳实践', 'react-hooks-best-practices',
'React Hooks 彻底改变了我们编写 React 组件的方式，本文分享一些实用的最佳实践。',
'# React Hooks 最佳实践

## useState 使用技巧

### 函数式更新

当新状态依赖于旧状态时，使用函数式更新：

```jsx
// ❌ 不推荐
setCount(count + 1)

// ✅ 推荐
setCount(prev => prev + 1)
```

### 惰性初始化

对于复杂的初始状态计算：

```jsx
const [state, setState] = useState(() => {
  return expensiveComputation()
})
```

## useEffect 最佳实践

### 正确设置依赖数组

```jsx
useEffect(() => {
  fetchData(userId)
}, [userId])  // 只在 userId 变化时执行
```

### 清理副作用

```jsx
useEffect(() => {
  const subscription = subscribe()
  return () => subscription.unsubscribe()
}, [])
```

## 自定义 Hook

将逻辑封装成自定义 Hook：

```jsx
function useLocalStorage(key, initialValue) {
  const [value, setValue] = useState(() => {
    const item = localStorage.getItem(key)
    return item ? JSON.parse(item) : initialValue
  })
  
  useEffect(() => {
    localStorage.setItem(key, JSON.stringify(value))
  }, [key, value])
  
  return [value, setValue]
}
```

遵循这些最佳实践，让你的 React 代码更健壮！',
'<h1>React Hooks 最佳实践</h1><p>React Hooks 彻底改变了我们编写 React 组件的方式...</p>',
550, 3, 1543, 112, 19, 1, 1, 0, NOW(), NOW(), NOW());

-- ===================== 后端开发文章 (category_id = 2) =====================

INSERT INTO posts (user_id, category_id, title, slug, excerpt, content, content_html, word_count, read_time, view_count, like_count, comment_count, status, is_original, is_featured, published_at, created_at, updated_at) VALUES
(2, 2, 'Spring Boot 3.0 新特性详解', 'spring-boot-3-new-features',
'Spring Boot 3.0 带来了许多激动人心的新特性，包括对 Java 17 的支持和 GraalVM 原生镜像。',
'# Spring Boot 3.0 新特性详解

## 核心变化

### 1. Java 17 基线

Spring Boot 3.0 要求最低 Java 17：

```java
// 使用 record 类
public record User(Long id, String name, String email) {}

// 模式匹配
if (obj instanceof String s) {
    System.out.println(s.length());
}
```

### 2. Jakarta EE 9+

包名从 `javax.*` 迁移到 `jakarta.*`：

```java
// 旧版
import javax.servlet.http.HttpServletRequest;

// 新版
import jakarta.servlet.http.HttpServletRequest;
```

### 3. GraalVM 原生镜像支持

```bash
# 构建原生镜像
./mvnw -Pnative native:compile
```

## 新的可观测性

Spring Boot 3.0 引入了 Micrometer 观测 API：

```java
@Observed(name = "user.find")
public User findUser(Long id) {
    return userRepository.findById(id);
}
```

## 配置迁移

使用迁移工具检查废弃配置：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-properties-migrator</artifactId>
</dependency>
```

升级到 Spring Boot 3.0，拥抱现代 Java！',
'<h1>Spring Boot 3.0 新特性详解</h1><p>Spring Boot 3.0 带来了许多激动人心的新特性...</p>',
520, 3, 1876, 134, 21, 1, 1, 1, NOW(), NOW(), NOW()),

(2, 2, 'RESTful API 设计规范', 'restful-api-design-guide',
'良好的 API 设计是后端开发的基础，本文总结 RESTful API 设计的最佳实践。',
'# RESTful API 设计规范

## 基本原则

### 1. 使用名词而非动词

```
✅ GET /users
❌ GET /getUsers

✅ POST /users
❌ POST /createUser
```

### 2. 使用复数形式

```
✅ /users
✅ /posts
❌ /user
❌ /post
```

## HTTP 方法

| 方法 | 用途 | 示例 |
|------|------|------|
| GET | 获取资源 | GET /users |
| POST | 创建资源 | POST /users |
| PUT | 更新资源 | PUT /users/1 |
| DELETE | 删除资源 | DELETE /users/1 |
| PATCH | 部分更新 | PATCH /users/1 |

## 状态码规范

```
200 OK - 成功
201 Created - 创建成功
204 No Content - 删除成功
400 Bad Request - 请求错误
401 Unauthorized - 未认证
403 Forbidden - 无权限
404 Not Found - 资源不存在
500 Internal Server Error - 服务器错误
```

## 分页与过滤

```
GET /users?page=1&size=20&sort=createdAt,desc
GET /posts?category=tech&status=published
```

## 版本控制

```
/api/v1/users
/api/v2/users
```

遵循这些规范，构建专业的 API！',
'<h1>RESTful API 设计规范</h1><p>良好的 API 设计是后端开发的基础...</p>',
480, 2, 2156, 189, 27, 1, 1, 1, NOW(), NOW(), NOW()),

(2, 2, 'MySQL 性能优化实战', 'mysql-performance-optimization',
'数据库性能是系统性能的关键，本文分享 MySQL 优化的实用技巧。',
'# MySQL 性能优化实战

## 索引优化

### 创建合适的索引

```sql
-- 单列索引
CREATE INDEX idx_user_email ON users(email);

-- 复合索引（遵循最左前缀原则）
CREATE INDEX idx_post_user_status ON posts(user_id, status, created_at);
```

### 索引使用原则

1. 对 WHERE、ORDER BY、JOIN 的列建索引
2. 避免在索引列上使用函数
3. 避免 LIKE 以 % 开头

## 查询优化

### 使用 EXPLAIN 分析

```sql
EXPLAIN SELECT * FROM posts WHERE user_id = 1;
```

关注 `type`、`key`、`rows` 字段。

### 避免 SELECT *

```sql
-- ❌ 不推荐
SELECT * FROM users;

-- ✅ 推荐
SELECT id, name, email FROM users;
```

## 配置优化

```ini
# my.cnf
innodb_buffer_pool_size = 4G
innodb_log_file_size = 256M
max_connections = 200
```

## 慢查询日志

```sql
SET GLOBAL slow_query_log = ON;
SET GLOBAL long_query_time = 1;
```

持续优化，让数据库飞起来！',
'<h1>MySQL 性能优化实战</h1><p>数据库性能是系统性能的关键...</p>',
450, 2, 1654, 98, 15, 1, 1, 0, NOW(), NOW(), NOW()),

(2, 2, 'Docker 容器化部署指南', 'docker-containerization-guide',
'Docker 是现代应用部署的标准，本文讲解如何使用 Docker 容器化你的应用。',
'# Docker 容器化部署指南

## Dockerfile 编写

### Java 应用示例

```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Node.js 应用示例

```dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
EXPOSE 3000
CMD ["node", "server.js"]
```

## Docker Compose

```yaml
version: "3.8"
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    depends_on:
      - db
      
  db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: secret
      MYSQL_DATABASE: myapp
    volumes:
      - mysql_data:/var/lib/mysql

volumes:
  mysql_data:
```

## 常用命令

```bash
# 构建镜像
docker build -t myapp:1.0 .

# 运行容器
docker run -d -p 8080:8080 myapp:1.0

# 查看日志
docker logs -f container_id
```

容器化让部署更简单！',
'<h1>Docker 容器化部署指南</h1><p>Docker 是现代应用部署的标准...</p>',
500, 2, 1234, 87, 11, 1, 1, 0, NOW(), NOW(), NOW());

-- ===================== 人工智能文章 (category_id = 3) =====================

INSERT INTO posts (user_id, category_id, title, slug, excerpt, content, content_html, word_count, read_time, view_count, like_count, comment_count, status, is_original, is_featured, published_at, created_at, updated_at) VALUES
(2, 3, 'ChatGPT API 开发实战', 'chatgpt-api-development',
'OpenAI 的 ChatGPT API 为开发者提供了强大的 AI 能力，本文讲解如何集成和使用。',
'# ChatGPT API 开发实战

## 快速开始

### 安装 SDK

```bash
npm install openai
# 或
pip install openai
```

### 基本调用

```javascript
import OpenAI from "openai";

const openai = new OpenAI({
  apiKey: process.env.OPENAI_API_KEY
});

const completion = await openai.chat.completions.create({
  model: "gpt-4",
  messages: [
    { role: "system", content: "你是一个有帮助的助手" },
    { role: "user", content: "你好！" }
  ]
});

console.log(completion.choices[0].message.content);
```

## 高级用法

### 流式输出

```javascript
const stream = await openai.chat.completions.create({
  model: "gpt-4",
  messages: [{ role: "user", content: "写一首诗" }],
  stream: true
});

for await (const chunk of stream) {
  process.stdout.write(chunk.choices[0]?.delta?.content || "");
}
```

### Function Calling

```javascript
const tools = [{
  type: "function",
  function: {
    name: "get_weather",
    description: "获取天气信息",
    parameters: {
      type: "object",
      properties: {
        city: { type: "string", description: "城市名" }
      },
      required: ["city"]
    }
  }
}];
```

## 最佳实践

1. 使用环境变量存储 API Key
2. 实现请求重试机制
3. 控制 Token 使用量
4. 缓存常见问题的回答

让 AI 为你的应用赋能！',
'<h1>ChatGPT API 开发实战</h1><p>OpenAI 的 ChatGPT API 为开发者提供了强大的 AI 能力...</p>',
550, 3, 3256, 234, 45, 1, 1, 1, NOW(), NOW(), NOW()),

(2, 3, '机器学习入门：从零开始', 'machine-learning-from-scratch',
'机器学习是人工智能的核心，本文用通俗的语言讲解机器学习基础概念。',
'# 机器学习入门：从零开始

## 什么是机器学习？

机器学习是让计算机从数据中学习规律，而不需要显式编程。

## 三种学习类型

### 1. 监督学习

有标签数据，学习输入到输出的映射：

- 分类：预测类别（垃圾邮件检测）
- 回归：预测数值（房价预测）

### 2. 无监督学习

无标签数据，发现数据结构：

- 聚类：将相似数据分组
- 降维：减少数据维度

### 3. 强化学习

通过与环境交互学习最优策略。

## 第一个模型：线性回归

```python
from sklearn.linear_model import LinearRegression

# 准备数据
X = [[1], [2], [3], [4]]
y = [2, 4, 6, 8]

# 训练模型
model = LinearRegression()
model.fit(X, y)

# 预测
print(model.predict([[5]]))  # 输出: [10.]
```

## 模型评估

```python
from sklearn.metrics import accuracy_score, mean_squared_error

# 分类任务
accuracy = accuracy_score(y_true, y_pred)

# 回归任务
mse = mean_squared_error(y_true, y_pred)
```

## 学习路线

1. 数学基础：线性代数、概率统计
2. Python 编程
3. Scikit-learn 入门
4. 深度学习框架

开始你的 AI 之旅！',
'<h1>机器学习入门：从零开始</h1><p>机器学习是人工智能的核心...</p>',
500, 3, 2543, 178, 32, 1, 1, 1, NOW(), NOW(), NOW()),

(2, 3, 'Prompt Engineering 提示词工程', 'prompt-engineering-guide',
'好的提示词能让 AI 输出更好的结果，本文分享提示词工程的技巧。',
'# Prompt Engineering 提示词工程

## 什么是提示词工程？

提示词工程是设计和优化输入给 AI 模型的文本，以获得更好输出的技术。

## 基本技巧

### 1. 明确指令

```
❌ 写点关于 Python 的东西
✅ 请写一篇 500 字的文章，介绍 Python 在数据分析中的应用，包含代码示例
```

### 2. 提供上下文

```
你是一位有10年经验的 Python 开发者。请为初学者解释什么是装饰器。
```

### 3. 指定输出格式

```
请用以下 JSON 格式返回结果：
{
  "title": "标题",
  "summary": "摘要",
  "points": ["要点1", "要点2"]
}
```

## 高级技巧

### Few-shot Learning

提供示例让模型学习：

```
将英文翻译成中文：
Hello -> 你好
World -> 世界
Computer -> 
```

### Chain of Thought

引导逐步思考：

```
让我们一步步思考这个问题：
1. 首先...
2. 然后...
3. 最后...
```

## 常用模板

```
角色：你是一位 [专业角色]
任务：请帮我 [具体任务]
背景：[相关背景信息]
要求：[具体要求和限制]
输出格式：[期望的输出格式]
```

掌握提示词，释放 AI 潜力！',
'<h1>Prompt Engineering 提示词工程</h1><p>好的提示词能让 AI 输出更好的结果...</p>',
480, 2, 1987, 145, 28, 1, 1, 0, NOW(), NOW(), NOW());

-- ===================== 数据库文章 (category_id = 4) =====================

INSERT INTO posts (user_id, category_id, title, slug, excerpt, content, content_html, word_count, read_time, view_count, like_count, comment_count, status, is_original, is_featured, published_at, created_at, updated_at) VALUES
(2, 4, 'Redis 缓存实战指南', 'redis-cache-practice-guide',
'Redis 是最流行的缓存数据库，本文讲解 Redis 在实际项目中的应用。',
'# Redis 缓存实战指南

## 常用数据类型

### String

```redis
SET user:1:name "张三"
GET user:1:name
SETEX token:abc123 3600 "user_data"  # 带过期时间
```

### Hash

```redis
HSET user:1 name "张三" age 25
HGET user:1 name
HGETALL user:1
```

### List

```redis
LPUSH messages "msg1" "msg2"
LRANGE messages 0 -1
```

### Set

```redis
SADD tags:post:1 "java" "spring" "redis"
SMEMBERS tags:post:1
```

## 缓存策略

### Cache Aside

```java
public User getUser(Long id) {
    String key = "user:" + id;
    User user = redis.get(key);
    if (user == null) {
        user = db.findById(id);
        redis.setex(key, 3600, user);
    }
    return user;
}
```

### 缓存穿透防护

```java
// 缓存空值
if (user == null) {
    redis.setex(key, 60, "NULL");
}
```

### 缓存雪崩防护

```java
// 随机过期时间
int expire = 3600 + new Random().nextInt(600);
redis.setex(key, expire, user);
```

## 分布式锁

```java
boolean locked = redis.setnx("lock:order:" + orderId, "1", 30);
if (locked) {
    try {
        // 业务逻辑
    } finally {
        redis.del("lock:order:" + orderId);
    }
}
```

合理使用缓存，提升系统性能！',
'<h1>Redis 缓存实战指南</h1><p>Redis 是最流行的缓存数据库...</p>',
520, 3, 1876, 132, 19, 1, 1, 1, NOW(), NOW(), NOW()),

(2, 4, 'MongoDB 文档数据库入门', 'mongodb-document-database-intro',
'MongoDB 是最流行的 NoSQL 数据库，本文介绍其基本使用方法。',
'# MongoDB 文档数据库入门

## 为什么选择 MongoDB？

- 灵活的文档模型
- 水平扩展能力
- 丰富的查询语法

## 基本操作

### 插入文档

```javascript
db.users.insertOne({
  name: "张三",
  age: 25,
  email: "zhangsan@example.com",
  tags: ["developer", "blogger"]
})

db.users.insertMany([
  { name: "李四", age: 30 },
  { name: "王五", age: 28 }
])
```

### 查询文档

```javascript
// 查找所有
db.users.find()

// 条件查询
db.users.find({ age: { $gte: 25 } })

// 投影
db.users.find({}, { name: 1, age: 1 })
```

### 更新文档

```javascript
db.users.updateOne(
  { name: "张三" },
  { $set: { age: 26 } }
)

db.users.updateMany(
  { age: { $lt: 30 } },
  { $inc: { age: 1 } }
)
```

### 删除文档

```javascript
db.users.deleteOne({ name: "张三" })
db.users.deleteMany({ age: { $lt: 20 } })
```

## 索引

```javascript
db.users.createIndex({ email: 1 }, { unique: true })
db.posts.createIndex({ title: "text", content: "text" })
```

## 聚合管道

```javascript
db.orders.aggregate([
  { $match: { status: "completed" } },
  { $group: { _id: "$userId", total: { $sum: "$amount" } } },
  { $sort: { total: -1 } }
])
```

灵活的数据模型，简化开发！',
'<h1>MongoDB 文档数据库入门</h1><p>MongoDB 是最流行的 NoSQL 数据库...</p>',
480, 2, 1234, 89, 14, 1, 1, 0, NOW(), NOW(), NOW());

-- ===================== DevOps 文章 (category_id = 5) =====================

INSERT INTO posts (user_id, category_id, title, slug, excerpt, content, content_html, word_count, read_time, view_count, like_count, comment_count, status, is_original, is_featured, published_at, created_at, updated_at) VALUES
(2, 5, 'GitHub Actions CI/CD 实战', 'github-actions-cicd-practice',
'GitHub Actions 是强大的 CI/CD 工具，本文讲解如何自动化你的开发流程。',
'# GitHub Actions CI/CD 实战

## 工作流基础

创建 `.github/workflows/ci.yml`：

```yaml
name: CI

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: 18
          
      - name: Install dependencies
        run: npm ci
        
      - name: Run tests
        run: npm test
        
      - name: Build
        run: npm run build
```

## 部署工作流

```yaml
name: Deploy

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Deploy to server
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.HOST }}
          username: ${{ secrets.USERNAME }}
          key: ${{ secrets.SSH_KEY }}
          script: |
            cd /var/www/app
            git pull
            npm ci
            pm2 restart app
```

## 常用技巧

### 缓存依赖

```yaml
- uses: actions/cache@v3
  with:
    path: ~/.npm
    key: ${{ runner.os }}-node-${{ hashFiles(''**/package-lock.json'') }}
```

### 环境变量

```yaml
env:
  NODE_ENV: production
  
- name: Build
  run: npm run build
  env:
    API_KEY: ${{ secrets.API_KEY }}
```

自动化一切，提高效率！',
'<h1>GitHub Actions CI/CD 实战</h1><p>GitHub Actions 是强大的 CI/CD 工具...</p>',
500, 2, 1543, 112, 18, 1, 1, 1, NOW(), NOW(), NOW()),

(2, 5, 'Kubernetes 入门教程', 'kubernetes-getting-started',
'Kubernetes 是容器编排的事实标准，本文带你入门 K8s 的基本概念和使用。',
'# Kubernetes 入门教程

## 核心概念

- **Pod**: 最小部署单元
- **Service**: 服务发现和负载均衡
- **Deployment**: 管理 Pod 副本
- **ConfigMap/Secret**: 配置管理

## 创建 Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp
spec:
  replicas: 3
  selector:
    matchLabels:
      app: myapp
  template:
    metadata:
      labels:
        app: myapp
    spec:
      containers:
        - name: myapp
          image: myapp:1.0
          ports:
            - containerPort: 8080
```

## 创建 Service

```yaml
apiVersion: v1
kind: Service
metadata:
  name: myapp-service
spec:
  selector:
    app: myapp
  ports:
    - port: 80
      targetPort: 8080
  type: LoadBalancer
```

## 常用命令

```bash
# 应用配置
kubectl apply -f deployment.yaml

# 查看资源
kubectl get pods
kubectl get services

# 查看日志
kubectl logs pod-name

# 进入容器
kubectl exec -it pod-name -- /bin/sh

# 扩缩容
kubectl scale deployment myapp --replicas=5
```

## ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
data:
  DATABASE_URL: "mysql://db:3306"
```

K8s 让容器管理更简单！',
'<h1>Kubernetes 入门教程</h1><p>Kubernetes 是容器编排的事实标准...</p>',
480, 2, 1876, 134, 22, 1, 1, 0, NOW(), NOW(), NOW());

-- ===================== 其他分类文章 (category_id = 6) =====================

INSERT INTO posts (user_id, category_id, title, slug, excerpt, content, content_html, word_count, read_time, view_count, like_count, comment_count, status, is_original, is_featured, published_at, created_at, updated_at) VALUES
(2, 6, '程序员效率工具推荐', 'developer-productivity-tools',
'工欲善其事必先利其器，本文推荐一些提高开发效率的工具。',
'# 程序员效率工具推荐

## 编辑器 & IDE

### VS Code

最流行的代码编辑器，推荐插件：

- **GitLens** - Git 增强
- **Prettier** - 代码格式化
- **ESLint** - 代码检查
- **GitHub Copilot** - AI 编程助手

### JetBrains 全家桶

专业 IDE，智能提示无敌。

## 终端工具

### Oh My Zsh

```bash
sh -c "$(curl -fsSL https://raw.github.com/ohmyzsh/ohmyzsh/master/tools/install.sh)"
```

常用插件：
- zsh-autosuggestions
- zsh-syntax-highlighting

### Warp / iTerm2

现代化终端，更好的使用体验。

## API 测试

- **Postman** - 功能最全
- **Insomnia** - 轻量好用
- **Thunder Client** - VS Code 插件

## 效率工具

### Alfred (Mac) / uTools (跨平台)

快速启动器，工作流自动化。

### Raycast

Mac 上的 Alfred 替代品，更现代。

## 文档工具

- **Notion** - 全能笔记
- **Obsidian** - Markdown 笔记
- **Typora** - 最美 MD 编辑器

## 学习资源

- GitHub
- Stack Overflow
- MDN Web Docs

工具选得好，下班回家早！',
'<h1>程序员效率工具推荐</h1><p>工欲善其事必先利其器...</p>',
450, 2, 2345, 187, 35, 1, 1, 1, NOW(), NOW(), NOW()),

(2, 6, '技术人如何写好博客', 'how-to-write-tech-blog',
'写博客是技术人成长的重要途径，本文分享写技术博客的心得体会。',
'# 技术人如何写好博客

## 为什么要写博客？

1. **加深理解** - 教是最好的学
2. **建立影响力** - 个人品牌
3. **记录成长** - 回顾进步
4. **帮助他人** - 分享知识

## 选题技巧

### 好的选题来源

- 工作中解决的问题
- 学习新技术的笔记
- 踩过的坑和解决方案
- 技术选型的思考

### 标题很重要

```
❌ 记一次 Bug 修复
✅ 记一次内存泄漏排查：从现象到根因的完整分析
```

## 内容结构

### 推荐模板

1. **背景/问题** - 为什么写这篇
2. **解决方案** - 具体怎么做
3. **代码示例** - 可运行的代码
4. **总结思考** - 学到了什么

## 写作技巧

### 简洁清晰

- 一段话只说一件事
- 用简单的词汇
- 避免长句子

### 图文并茂

- 流程图
- 架构图
- 截图说明

### 代码规范

- 语法高亮
- 必要的注释
- 可复制运行

## 坚持是关键

- 定期更新，建立节奏
- 不求完美，先发后改
- 从短文开始，逐步提升

开始写吧，你的博客等着你！',
'<h1>技术人如何写好博客</h1><p>写博客是技术人成长的重要途径...</p>',
520, 3, 1654, 143, 27, 1, 1, 0, NOW(), NOW(), NOW());

-- 查看插入结果
SELECT id, title, category_id, status, view_count, like_count FROM posts ORDER BY id;
