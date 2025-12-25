<template>
  <aside class="blog-sidebar">
    <!-- 作者信息卡片 -->
    <div v-if="showAuthorCard" class="sidebar-card author-card">
      <div class="author-header">
        <img src="@/assets/blog/images/user.jpg" alt="头像" class="author-avatar" />
        <div class="author-info">
          <h3 class="author-name">CloudStack用户</h3>
          <p class="author-bio">专注技术分享与学习</p>
        </div>
      </div>
      <div class="author-stats">
        <div class="stat-item">
          <span class="stat-value">128</span>
          <span class="stat-label">文章</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">2.5k</span>
          <span class="stat-label">关注者</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">8.6k</span>
          <span class="stat-label">获赞</span>
        </div>
      </div>
      <button class="follow-btn">
        <img src="@/assets/blog/icons/plus.svg" alt="关注" class="btn-icon" />
        <span>关注</span>
      </button>
    </div>
    
    <!-- 热门标签 -->
    <div class="sidebar-card">
      <div class="card-header">
        <img src="@/assets/blog/icons/trending.svg" alt="热门标签" class="header-icon" />
        <h3 class="card-title">热门标签</h3>
      </div>
      <div class="tags-list">
        <span v-for="tag in hotTags" :key="tag.name" class="tag-item" @click="handleTagClick(tag.name)">
          {{ tag.name }}
          <span class="tag-count">{{ tag.count }}</span>
        </span>
      </div>
    </div>
    
    <!-- 热门文章 -->
    <div class="sidebar-card">
      <div class="card-header">
        <img src="@/assets/blog/icons/zap.svg" alt="热门文章" class="header-icon" />
        <h3 class="card-title">热门文章</h3>
      </div>
      <ul class="hot-posts">
        <li v-for="(post, index) in hotPosts" :key="post.id" class="hot-post-item">
          <span class="post-rank" :class="{ 'top': index < 3 }">{{ index + 1 }}</span>
          <div class="post-info">
            <router-link :to="`${postRouteBase}/${post.id}`" class="post-title">{{ post.title }}</router-link>
            <span class="post-views">
              <img src="@/assets/blog/icons/eye.svg" alt="阅读" class="view-icon" />
              {{ post.views }}
            </span>
          </div>
        </li>
      </ul>
    </div>
    
    <!-- 文章归档 -->
    <div v-if="showArchive" class="sidebar-card">
      <div class="card-header">
        <img src="@/assets/blog/icons/book.svg" alt="文章归档" class="header-icon" />
        <h3 class="card-title">文章归档</h3>
      </div>
      <ul class="archive-list">
        <li v-for="archive in archives" :key="archive.month" class="archive-item">
          <router-link :to="archiveTo(archive.month)" class="archive-link">
            <span class="archive-month">{{ archive.label }}</span>
            <span class="archive-count">{{ archive.count }}篇</span>
          </router-link>
        </li>
      </ul>
    </div>
    
    <!-- 友情链接 -->
    <div class="sidebar-card">
      <div class="card-header">
        <img src="@/assets/blog/icons/share.svg" alt="友情链接" class="header-icon" />
        <h3 class="card-title">友情链接</h3>
      </div>
      <div class="friend-links">
        <a v-for="link in friendLinks" :key="link.name" :href="link.url" target="_blank" class="friend-link">
          {{ link.name }}
        </a>
      </div>
    </div>
  </aside>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue'
import { useRouter } from 'vue-router'

export default defineComponent({
  name: 'BlogSidebar',
  props: {
    showAuthorCard: {
      type: Boolean,
      default: true
    },
    showArchive: {
      type: Boolean,
      default: true
    },
    postRouteBase: {
      type: String,
      default: '/blog/post'
    },
    tagRoutePath: {
      type: String,
      default: '/blog/search'
    },
    tagQueryKey: {
      type: String,
      default: 'tag'
    }
  },
  setup(props) {
    const router = useRouter()
    
    const hotTags = ref([
      { name: 'Vue.js', count: 128 },
      { name: 'TypeScript', count: 96 },
      { name: 'React', count: 87 },
      { name: 'Node.js', count: 76 },
      { name: 'Python', count: 65 },
      { name: '前端开发', count: 54 },
      { name: '后端开发', count: 48 },
      { name: '人工智能', count: 42 }
    ])
    
    const hotPosts = ref([
      { id: 1, title: 'Vue 3.0 新特性详解与实践指南', views: '2.3k' },
      { id: 2, title: 'TypeScript 高级类型系统深入理解', views: '1.8k' },
      { id: 3, title: '前端性能优化的20个实用技巧', views: '1.5k' },
      { id: 4, title: 'Node.js 微服务架构设计实践', views: '1.2k' },
      { id: 5, title: 'React Hooks 最佳实践总结', views: '980' }
    ])
    
    const archives = ref([
      { month: '2025-12', label: '2025年12月', count: 12 },
      { month: '2025-11', label: '2025年11月', count: 18 },
      { month: '2025-10', label: '2025年10月', count: 15 },
      { month: '2025-09', label: '2025年9月', count: 22 },
      { month: '2025-08', label: '2025年8月', count: 16 }
    ])
    
    const friendLinks = ref([
      { name: 'CloudStack Pro', url: '#' },
      { name: 'GitHub', url: 'https://github.com' },
      { name: 'Vue.js', url: 'https://vuejs.org' },
      { name: 'MDN Web Docs', url: 'https://developer.mozilla.org' }
    ])
    
    const handleTagClick = (tagName: string) => {
      router.push({ path: props.tagRoutePath, query: { [props.tagQueryKey]: tagName } })
    }

    const archiveTo = (month: string) => {
      return `/archive/${month}`
    }
    
    return {
      hotTags,
      hotPosts,
      archives,
      friendLinks,
      handleTagClick,
      archiveTo
    }
  }
})
</script>

<style scoped>
.blog-sidebar {
  width: 320px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.sidebar-card {
  background: var(--card-bg);
  border-radius: var(--border-radius-md);
  padding: 20px;
  box-shadow: var(--shadow-sm);
}

/* 作者卡片 */
.author-card {
  text-align: center;
}

.author-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.author-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 3px solid var(--border-light);
}

.author-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-dark);
  margin-bottom: 4px;
}

.author-bio {
  font-size: 13px;
  color: var(--text-muted);
}

.author-stats {
  display: flex;
  justify-content: space-around;
  padding: 16px 0;
  border-top: 1px solid var(--border-light);
  border-bottom: 1px solid var(--border-light);
  margin-bottom: 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-dark);
}

.stat-label {
  font-size: 12px;
  color: var(--text-muted);
}

.follow-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 10px;
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
  color: white;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  transition: var(--transition-default);
}

.follow-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(58, 156, 255, 0.3);
}

.btn-icon {
  width: 16px;
  height: 16px;
  filter: brightness(0) invert(1);
}

/* 通用卡片头部 */
.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-light);
}

.header-icon {
  width: 20px;
  height: 20px;
  opacity: 0.7;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-dark);
}

/* 热门标签 */
.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: var(--border-light);
  color: var(--text-light);
  font-size: 13px;
  border-radius: 16px;
  cursor: pointer;
  transition: var(--transition-default);
}

.tag-item:hover {
  background: linear-gradient(120deg, rgba(58, 156, 255, 0.1), rgba(168, 213, 255, 0.2));
  color: var(--primary-color);
}

.tag-count {
  font-size: 11px;
  color: var(--text-muted);
}

/* 热门文章 */
.hot-posts {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hot-post-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.post-rank {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--border-light);
  color: var(--text-muted);
  font-size: 12px;
  font-weight: 600;
  border-radius: 6px;
  flex-shrink: 0;
}

.post-rank.top {
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
  color: white;
}

.post-info {
  flex: 1;
  min-width: 0;
}

.post-title {
  display: block;
  font-size: 14px;
  color: var(--text-dark);
  line-height: 1.5;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.post-title:hover {
  color: var(--primary-color);
}

.post-views {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-muted);
}

.view-icon {
  width: 12px;
  height: 12px;
  opacity: 0.5;
}

/* 文章归档 */
.archive-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.archive-item {
  display: block;
}

.archive-link {
  display: flex;
  justify-content: space-between;
  padding: 8px 12px;
  border-radius: 8px;
  transition: var(--transition-default);
}

.archive-link:hover {
  background: var(--border-light);
}

.archive-month {
  font-size: 14px;
  color: var(--text-dark);
}

.archive-count {
  font-size: 13px;
  color: var(--text-muted);
}

/* 友情链接 */
.friend-links {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.friend-link {
  padding: 6px 14px;
  background: var(--border-light);
  color: var(--text-light);
  font-size: 13px;
  border-radius: 6px;
  transition: var(--transition-default);
}

.friend-link:hover {
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
  color: white;
}

@media (max-width: 1024px) {
  .blog-sidebar {
    width: 280px;
  }
}

@media (max-width: 768px) {
  .blog-sidebar {
    width: 100%;
  }
}
</style>
