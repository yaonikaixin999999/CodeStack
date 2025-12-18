<template>
  <div class="blog-profile">
    <BlogHeader />
    
    <main class="main-content">
      <!-- 用户信息卡片 -->
      <header class="profile-header">
        <div class="header-bg">
          <div class="bg-pattern"></div>
        </div>
        <div class="header-content">
          <div class="user-avatar-wrapper">
            <img src="@/assets/blog/images/user.jpg" :alt="userInfo.name" class="user-avatar" />
            <span class="user-level">Lv.{{ userInfo.level }}</span>
          </div>
          <div class="user-info">
            <div class="user-name-row">
              <h1 class="user-name">{{ userInfo.name }}</h1>
              <span class="user-badge" v-for="badge in userInfo.badges" :key="badge">{{ badge }}</span>
            </div>
            <p class="user-bio">{{ userInfo.bio }}</p>
            <div class="user-meta">
              <span class="meta-item">
                <img src="@/assets/blog/icons/category.svg" alt="职业" class="meta-icon" />
                {{ userInfo.profession }}
              </span>
              <span class="meta-item">
                <img src="@/assets/blog/icons/home.svg" alt="位置" class="meta-icon" />
                {{ userInfo.location }}
              </span>
              <span class="meta-item">
                <img src="@/assets/blog/icons/clock.svg" alt="加入时间" class="meta-icon" />
                {{ userInfo.joinDate }} 加入
              </span>
            </div>
          </div>
          <div class="user-actions" v-if="!isOwner">
            <button class="action-btn primary" :class="{ followed: isFollowed }" @click="toggleFollow">
              <img :src="isFollowed ? checkIcon : plusIcon" alt="" class="btn-icon" />
              {{ isFollowed ? '已关注' : '关注' }}
            </button>
            <button class="action-btn">
              <img src="@/assets/blog/icons/comment.svg" alt="私信" class="btn-icon" />
              私信
            </button>
          </div>
          <div class="user-actions" v-else>
            <router-link to="/profile/edit" class="action-btn">
              <img src="@/assets/blog/icons/edit.svg" alt="编辑" class="btn-icon" />
              编辑资料
            </router-link>
            <router-link to="/profile/settings" class="action-btn">
              <img src="@/assets/blog/icons/settings.svg" alt="设置" class="btn-icon" />
              设置
            </router-link>
          </div>
        </div>
      </header>
      
      <!-- 用户统计 -->
      <section class="profile-stats">
        <div class="content-container">
          <div class="stats-grid">
            <div class="stat-card">
              <span class="stat-value">{{ userInfo.stats.posts }}</span>
              <span class="stat-label">文章</span>
            </div>
            <div class="stat-card">
              <span class="stat-value">{{ userInfo.stats.followers }}</span>
              <span class="stat-label">粉丝</span>
            </div>
            <div class="stat-card">
              <span class="stat-value">{{ userInfo.stats.following }}</span>
              <span class="stat-label">关注</span>
            </div>
            <div class="stat-card">
              <span class="stat-value">{{ userInfo.stats.likes }}</span>
              <span class="stat-label">获赞</span>
            </div>
            <div class="stat-card">
              <span class="stat-value">{{ userInfo.stats.views }}</span>
              <span class="stat-label">阅读量</span>
            </div>
          </div>
        </div>
      </section>
      
      <!-- 内容区 -->
      <div class="content-container">
        <div class="main-grid">
          <section class="posts-section">
            <!-- 内容标签页 -->
            <div class="content-tabs">
              <button 
                v-for="tab in contentTabs" 
                :key="tab.id"
                class="tab-btn"
                :class="{ active: activeTab === tab.id }"
                @click="activeTab = tab.id"
              >
                <img :src="tab.icon" :alt="tab.name" class="tab-icon" />
                {{ tab.name }}
                <span class="tab-count">{{ tab.count }}</span>
              </button>
            </div>
            
            <!-- 文章列表 -->
            <div class="posts-list" v-if="activeTab === 'posts'">
              <article v-for="post in userPosts" :key="post.id" class="post-item">
                <router-link :to="`/blog/post/${post.id}`" class="post-content">
                  <div class="post-cover" v-if="post.coverImage">
                    <img :src="post.coverImage" :alt="post.title" />
                  </div>
                  <div class="post-info">
                    <h3 class="post-title">{{ post.title }}</h3>
                    <p class="post-excerpt">{{ post.excerpt }}</p>
                    <div class="post-meta">
                      <span class="meta-item">
                        <img src="@/assets/blog/icons/clock.svg" alt="时间" class="meta-icon" />
                        {{ post.createdAt }}
                      </span>
                      <span class="meta-item">
                        <img src="@/assets/blog/icons/eye.svg" alt="阅读" class="meta-icon" />
                        {{ post.views }}
                      </span>
                      <span class="meta-item">
                        <img src="@/assets/blog/icons/heart.svg" alt="点赞" class="meta-icon" />
                        {{ post.likes }}
                      </span>
                      <span class="meta-item">
                        <img src="@/assets/blog/icons/comment.svg" alt="评论" class="meta-icon" />
                        {{ post.comments }}
                      </span>
                    </div>
                  </div>
                </router-link>
              </article>
            </div>
            
            <!-- 收藏列表 -->
            <div class="bookmarks-list" v-if="activeTab === 'bookmarks'">
              <article v-for="post in bookmarks" :key="post.id" class="post-item">
                <router-link :to="`/blog/post/${post.id}`" class="post-content">
                  <div class="post-cover" v-if="post.coverImage">
                    <img :src="post.coverImage" :alt="post.title" />
                  </div>
                  <div class="post-info">
                    <h3 class="post-title">{{ post.title }}</h3>
                    <p class="post-excerpt">{{ post.excerpt }}</p>
                    <div class="post-meta">
                      <span class="meta-item author">
                        <img :src="post.author.avatar" :alt="post.author.name" class="author-avatar" />
                        {{ post.author.name }}
                      </span>
                      <span class="meta-item">
                        <img src="@/assets/blog/icons/clock.svg" alt="收藏时间" class="meta-icon" />
                        收藏于 {{ post.bookmarkedAt }}
                      </span>
                    </div>
                  </div>
                </router-link>
              </article>
            </div>
            
            <!-- 关注列表 -->
            <div class="following-list" v-if="activeTab === 'following'">
              <div v-for="user in following" :key="user.id" class="user-card">
                <img :src="user.avatar" :alt="user.name" class="user-card-avatar" />
                <div class="user-card-info">
                  <h4 class="user-card-name">{{ user.name }}</h4>
                  <p class="user-card-bio">{{ user.bio }}</p>
                  <span class="user-card-followers">{{ user.followers }} 粉丝</span>
                </div>
                <button class="follow-btn" :class="{ followed: user.isFollowed }">
                  {{ user.isFollowed ? '已关注' : '关注' }}
                </button>
              </div>
            </div>
            
            <!-- 粉丝列表 -->
            <div class="followers-list" v-if="activeTab === 'followers'">
              <div v-for="user in followers" :key="user.id" class="user-card">
                <img :src="user.avatar" :alt="user.name" class="user-card-avatar" />
                <div class="user-card-info">
                  <h4 class="user-card-name">{{ user.name }}</h4>
                  <p class="user-card-bio">{{ user.bio }}</p>
                  <span class="user-card-followers">{{ user.followers }} 粉丝</span>
                </div>
                <button class="follow-btn" :class="{ followed: user.isFollowed }">
                  {{ user.isFollowed ? '已关注' : '关注' }}
                </button>
              </div>
            </div>
          </section>
          
          <!-- 侧边栏 -->
          <aside class="profile-sidebar">
            <!-- 成就卡片 -->
            <div class="sidebar-card achievements-card">
              <h3 class="card-title">
                <img src="@/assets/blog/icons/zap.svg" alt="成就" class="title-icon" />
                成就展示
              </h3>
              <div class="achievements-grid">
                <div v-for="achievement in achievements" :key="achievement.id" class="achievement-item">
                  <div class="achievement-icon">
                    <img :src="achievement.icon" :alt="achievement.name" class="achievement-img" />
                  </div>
                  <span class="achievement-name">{{ achievement.name }}</span>
                </div>
              </div>
            </div>
            
            <!-- 技术栈 -->
            <div class="sidebar-card skills-card">
              <h3 class="card-title">
                <img src="@/assets/blog/icons/code.svg" alt="技术栈" class="title-icon" />
                技术栈
              </h3>
              <div class="skills-list">
                <div v-for="skill in skills" :key="skill.name" class="skill-item">
                  <div class="skill-header">
                    <span class="skill-name">{{ skill.name }}</span>
                    <span class="skill-level">{{ skill.level }}%</span>
                  </div>
                  <div class="skill-bar">
                    <div class="skill-progress" :style="{ width: skill.level + '%' }"></div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 常用标签 -->
            <div class="sidebar-card tags-card">
              <h3 class="card-title">
                <img src="@/assets/blog/icons/category.svg" alt="常用标签" class="title-icon" />
                常用标签
              </h3>
              <div class="tags-cloud">
                <span v-for="tag in tags" :key="tag" class="tag">{{ tag }}</span>
              </div>
            </div>
          </aside>
        </div>
      </div>
    </main>
    
    <BlogFooter />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import BlogHeader from '@/components/blog/BlogHeader.vue'
import BlogFooter from '@/components/blog/BlogFooter.vue'

import bookIcon from '@/assets/blog/icons/book.svg'
import bookmarkIcon from '@/assets/blog/icons/bookmark.svg'
import userIcon from '@/assets/blog/icons/user.svg'
import heartIcon from '@/assets/blog/icons/heart.svg'
import plusIcon from '@/assets/blog/icons/plus.svg'
import checkIcon from '@/assets/blog/icons/check.svg'
import iconAuthor from '@/assets/blog/icons/icons8-文章-96.png'
import iconTrophy from '@/assets/blog/icons/icons8-奖杯-100.png'
import iconExcellent from '@/assets/blog/icons/icons8-优秀-96.png'
import iconFire from '@/assets/blog/icons/icons8-火-96.png'
import iconDiamond from '@/assets/blog/icons/icons8-钻石-96.png'
import iconWriter from '@/assets/blog/icons/icons8-作者-100.png'

export default defineComponent({
  name: 'BlogProfile',
  components: {
    BlogHeader,
    BlogFooter
  },
  setup() {
    const route = useRoute()
    
    const isOwner = ref(true)
    const isFollowed = ref(false)
    const activeTab = ref('posts')
    
    const userInfo = ref({
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=profile',
      name: '技术小白',
      level: 5,
      badges: ['优秀作者', '技术达人'],
      bio: '全栈开发工程师，热爱技术分享，专注于 Vue.js、TypeScript 和 Node.js 的探索与实践。',
      profession: '全栈工程师',
      location: '北京',
      joinDate: '2023-01',
      stats: {
        posts: 86,
        followers: '1.2k',
        following: 128,
        likes: '3.6k',
        views: '12.5w'
      }
    })
    
    const contentTabs = ref([
      { id: 'posts', name: '文章', count: 86, icon: bookIcon },
      { id: 'bookmarks', name: '收藏', count: 32, icon: bookmarkIcon },
      { id: 'following', name: '关注', count: 128, icon: userIcon },
      { id: 'followers', name: '粉丝', count: '1.2k', icon: heartIcon }
    ])
    
    const userPosts = ref([
      {
        id: 1,
        title: 'Vue 3.0 Composition API 完全指南',
        excerpt: '深入探讨 Vue 3.0 的 Composition API，包括 setup 函数、响应式系统等核心概念。',
        coverImage: 'https://picsum.photos/300/200?random=40',
        createdAt: '2025-12-15',
        views: '2.3k',
        likes: 186,
        comments: 42
      },
      {
        id: 2,
        title: 'TypeScript 高级类型详解',
        excerpt: '深入讲解 TypeScript 的高级类型特性，包括泛型约束、条件类型等。',
        coverImage: 'https://picsum.photos/300/200?random=41',
        createdAt: '2025-12-14',
        views: '1.8k',
        likes: 142,
        comments: 35
      },
      {
        id: 3,
        title: 'Node.js 微服务架构实践',
        excerpt: '探索如何将传统的单体应用拆分为微服务架构。',
        coverImage: '',
        createdAt: '2025-12-13',
        views: '1.5k',
        likes: 98,
        comments: 28
      }
    ])
    
    const bookmarks = ref([
      {
        id: 10,
        title: 'React 18 新特性深度解析',
        excerpt: '全面了解 React 18 带来的新特性和改进。',
        coverImage: 'https://picsum.photos/300/200?random=50',
        author: { name: 'React专家', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=10' },
        bookmarkedAt: '2025-12-10'
      },
      {
        id: 11,
        title: 'Docker 容器化部署最佳实践',
        excerpt: '学习如何使用 Docker 进行应用容器化部署。',
        coverImage: 'https://picsum.photos/300/200?random=51',
        author: { name: '运维大师', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=11' },
        bookmarkedAt: '2025-12-08'
      }
    ])
    
    const following = ref([
      { id: 1, name: '代码大师', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=f1', bio: '10年全栈开发经验', followers: '5.2k', isFollowed: true },
      { id: 2, name: '前端达人', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=f2', bio: '专注前端技术分享', followers: '3.8k', isFollowed: true },
      { id: 3, name: '架构师老王', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=f3', bio: '系统架构设计', followers: '8.6k', isFollowed: true }
    ])
    
    const followers = ref([
      { id: 4, name: '小明同学', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=f4', bio: '前端开发爱好者', followers: '128', isFollowed: false },
      { id: 5, name: '技术小白', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=f5', bio: '正在学习Vue.js', followers: '56', isFollowed: true },
      { id: 6, name: '程序员阿杰', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=f6', bio: '后端开发工程师', followers: '892', isFollowed: false }
    ])
    
    const achievements = ref([
      { id: 1, name: '百篇文章', icon: iconAuthor },
      { id: 2, name: '万粉作者', icon: iconTrophy },
      { id: 3, name: '技术达人', icon: iconExcellent },
      { id: 4, name: '优质创作', icon: iconWriter },
      { id: 5, name: '热门作者', icon: iconFire },
      { id: 6, name: '社区贡献', icon: iconDiamond }
    ])
    
    const skills = ref([
      { name: 'Vue.js', level: 95 },
      { name: 'TypeScript', level: 88 },
      { name: 'Node.js', level: 82 },
      { name: 'React', level: 75 },
      { name: 'Python', level: 68 }
    ])
    
    const tags = ref([
      'Vue.js', 'TypeScript', 'JavaScript', 'Node.js', 'React',
      '前端开发', '全栈', '微服务', 'Docker', 'Git'
    ])
    
    const toggleFollow = () => {
      isFollowed.value = !isFollowed.value
    }
    
    return {
      isOwner,
      isFollowed,
      activeTab,
      userInfo,
      contentTabs,
      userPosts,
      bookmarks,
      following,
      followers,
      achievements,
      skills,
      tags,
      toggleFollow,
      plusIcon,
      checkIcon
    }
  }
})
</script>

<style scoped>
.blog-profile {
  min-height: 100vh;
  background: var(--background-gradient);
}

.main-content {
  padding-top: 64px;
}

.content-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

/* Profile Header */
.profile-header {
  position: relative;
  margin-bottom: 24px;
}

.header-bg {
  height: 100px;
  /* background: linear-gradient(120deg, #3a9cff, #a8d5ff); */
  position: relative;
  overflow: hidden;
}

.bg-pattern {
  position: absolute;
  inset: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.1'%3E%3Ccircle cx='30' cy='30' r='4'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

.header-content {
  display: flex;
  align-items: flex-end;
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  transform: translateY(-60px);
}

.user-avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.user-avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  border: 4px solid white;
  box-shadow: var(--shadow-lg);
  background: white;
}

.user-level {
  position: absolute;
  bottom: 4px;
  right: 4px;
  padding: 4px 10px;
  background: linear-gradient(120deg, #ff9f43, #ff6b6b);
  color: white;
  font-size: 12px;
  font-weight: 600;
  border-radius: 12px;
}

.user-info {
  flex: 1;
  padding-bottom: 20px;
}

.user-name-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.user-name {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-dark);
}

.user-badge {
  padding: 4px 12px;
  background: linear-gradient(120deg, rgba(58, 156, 255, 0.1), rgba(168, 213, 255, 0.2));
  color: var(--primary-color);
  font-size: 12px;
  border-radius: 12px;
}

.user-bio {
  font-size: 15px;
  color: var(--text-light);
  margin-bottom: 12px;
  max-width: 500px;
}

.user-meta {
  display: flex;
  gap: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--text-muted);
}

.meta-icon {
  width: 16px;
  height: 16px;
  opacity: 0.5;
}

.user-actions {
  display: flex;
  gap: 12px;
  padding-bottom: 20px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 24px;
  background: white;
  color: var(--text-light);
  border-radius: 24px;
  font-size: 14px;
  box-shadow: var(--shadow-sm);
  transition: var(--transition-default);
}

.action-btn:hover {
  color: var(--primary-color);
  box-shadow: var(--shadow-md);
}

.action-btn.primary {
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
  color: white;
}

.action-btn.primary:hover {
  transform: scale(1.02);
}

.action-btn.followed {
  background: white;
  color: var(--text-light);
}

.btn-icon {
  width: 16px;
  height: 16px;
}

.action-btn.primary .btn-icon {
  filter: brightness(0) invert(1);
}

/* Profile Stats */
.profile-stats {
  background: white;
  padding: 24px 0;
  box-shadow: var(--shadow-sm);
  margin-top: -36px;
  margin-bottom: 32px;
}

.stats-grid {
  display: flex;
  justify-content: center;
  gap: 48px;
}

.stat-card {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: var(--text-dark);
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: var(--text-muted);
}

/* Main Grid */
.main-grid {
  display: flex;
  gap: 32px;
  padding-bottom: 48px;
}

.posts-section {
  flex: 1;
  min-width: 0;
}

/* Content Tabs */
.content-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  padding: 8px;
  background: white;
  border-radius: 12px;
  box-shadow: var(--shadow-sm);
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: transparent;
  color: var(--text-light);
  border-radius: 8px;
  font-size: 14px;
  transition: var(--transition-default);
}

.tab-btn:hover {
  background: var(--border-light);
}

.tab-btn.active {
  background: linear-gradient(120deg, rgba(58, 156, 255, 0.1), rgba(168, 213, 255, 0.2));
  color: var(--primary-color);
}

.tab-icon {
  width: 18px;
  height: 18px;
  opacity: 0.6;
}

.tab-btn.active .tab-icon {
  opacity: 1;
}

.tab-count {
  padding: 2px 8px;
  background: var(--border-light);
  border-radius: 10px;
  font-size: 12px;
}

.tab-btn.active .tab-count {
  background: rgba(58, 156, 255, 0.2);
}

/* Posts List */
.posts-list,
.bookmarks-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-item {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  transition: var(--transition-default);
}

.post-item:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.post-content {
  display: flex;
  gap: 20px;
  padding: 20px;
}

.post-cover {
  width: 200px;
  height: 130px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.post-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.post-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.post-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-dark);
  margin-bottom: 8px;
  line-height: 1.4;
}

.post-excerpt {
  font-size: 14px;
  color: var(--text-light);
  line-height: 1.6;
  flex: 1;
  margin-bottom: 12px;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 16px;
}

.post-meta .meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-muted);
}

.post-meta .author {
  gap: 6px;
}

.author-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
}

/* User Lists */
.following-list,
.followers-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: var(--shadow-sm);
}

.user-card-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
}

.user-card-info {
  flex: 1;
  min-width: 0;
}

.user-card-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-dark);
  margin-bottom: 4px;
}

.user-card-bio {
  font-size: 14px;
  color: var(--text-light);
  margin-bottom: 4px;
}

.user-card-followers {
  font-size: 12px;
  color: var(--text-muted);
}

.follow-btn {
  padding: 8px 20px;
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
  color: white;
  border-radius: 20px;
  font-size: 13px;
  transition: var(--transition-default);
}

.follow-btn:hover {
  transform: scale(1.02);
}

.follow-btn.followed {
  background: var(--border-light);
  color: var(--text-muted);
}

/* Profile Sidebar */
.profile-sidebar {
  width: 320px;
  flex-shrink: 0;
}

.sidebar-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: var(--shadow-sm);
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-dark);
  margin-bottom: 16px;
}

.title-icon {
  width: 20px;
  height: 20px;
  opacity: 0.7;
}

/* Achievements */
.achievements-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.achievement-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 12px 8px;
  background: var(--border-light);
  border-radius: 12px;
}

.achievement-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.achievement-img {
  width: 40px;
  height: 40px;
  object-fit: contain;
}

.achievement-name {
  font-size: 11px;
  color: var(--text-muted);
  text-align: center;
}

/* Skills */
.skills-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.skill-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}

.skill-name {
  font-size: 14px;
  color: var(--text-dark);
}

.skill-level {
  font-size: 12px;
  color: var(--text-muted);
}

.skill-bar {
  height: 6px;
  background: var(--border-light);
  border-radius: 3px;
  overflow: hidden;
}

.skill-progress {
  height: 100%;
  background: linear-gradient(90deg, #3a9cff, #a8d5ff);
  border-radius: 3px;
  transition: width 0.3s ease;
}

/* Tags */
.tags-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  padding: 6px 14px;
  background: var(--border-light);
  color: var(--text-light);
  border-radius: 16px;
  font-size: 13px;
  transition: var(--transition-default);
}

.tag:hover {
  background: linear-gradient(120deg, rgba(58, 156, 255, 0.1), rgba(168, 213, 255, 0.2));
  color: var(--primary-color);
}

@media (max-width: 1024px) {
  .main-grid {
    flex-direction: column;
  }
  
  .profile-sidebar {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: center;
    text-align: center;
    transform: translateY(-40px);
  }
  
  .user-info {
    padding-bottom: 0;
  }
  
  .user-meta {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .user-actions {
    width: 100%;
    justify-content: center;
    padding-bottom: 0;
  }
  
  .stats-grid {
    flex-wrap: wrap;
    gap: 24px;
  }
  
  .content-tabs {
    overflow-x: auto;
  }
  
  .post-content {
    flex-direction: column;
  }
  
  .post-cover {
    width: 100%;
    height: 180px;
  }
}
</style>
