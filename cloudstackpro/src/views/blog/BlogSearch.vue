<template>
  <div class="blog-search">
    <BlogHeader />
    
    <main class="main-content">
      <div class="content-container">
        <!-- 搜索头部 -->
        <header class="search-header">
          <div class="search-box-large">
            <img src="@/assets/blog/icons/search.svg" alt="搜索" class="search-icon" />
            <input 
              v-model="searchQuery" 
              type="text" 
              placeholder="搜索文章、标签或作者..."
              @keyup.enter="performSearch"
            />
            <button class="search-btn" @click="performSearch">搜索</button>
          </div>
          
          <div class="search-info" v-if="hasSearched">
            <span class="result-count">
              找到 <strong>{{ totalResults }}</strong> 条相关结果
            </span>
            <span class="search-time">
              耗时 {{ searchTime }}ms
            </span>
          </div>
        </header>
        
        <!-- 热门搜索 -->
        <section class="hot-search" v-if="!hasSearched">
          <h3 class="section-title">
            <img src="@/assets/blog/icons/trending.svg" alt="热门搜索" class="title-icon" />
            热门搜索
          </h3>
          <div class="hot-tags">
            <button 
              v-for="tag in hotSearchTags" 
              :key="tag"
              class="hot-tag"
              @click="searchByTag(tag)"
            >
              {{ tag }}
            </button>
          </div>
        </section>
        
        <!-- 搜索结果 -->
        <div class="main-grid" v-if="hasSearched">
          <section class="results-section">
            <!-- 筛选器 -->
            <div class="filter-bar">
              <div class="filter-tabs">
                <button 
                  v-for="tab in filterTabs" 
                  :key="tab.id"
                  class="filter-tab"
                  :class="{ active: activeFilter === tab.id }"
                  @click="activeFilter = tab.id"
                >
                  <img :src="tab.icon" :alt="tab.name" class="tab-icon" />
                  <span>{{ tab.name }}</span>
                  <span class="tab-count">{{ tab.count }}</span>
                </button>
              </div>
              
              <div class="sort-select">
                <select v-model="sortBy">
                  <option value="relevance">相关度</option>
                  <option value="latest">最新</option>
                  <option value="popular">最热</option>
                </select>
                <img src="@/assets/blog/icons/chevron-down.svg" alt="展开" class="select-icon" />
              </div>
            </div>
            
            <!-- 结果列表 -->
            <div class="results-list">
              <article v-for="result in searchResults" :key="result.id" class="result-item">
                <router-link :to="`/blog/post/${result.id}`" class="result-content">
                  <div class="result-main">
                    <h3 class="result-title" v-html="highlightText(result.title)"></h3>
                    <p class="result-excerpt" v-html="highlightText(result.excerpt)"></p>
                    <div class="result-meta">
                      <span class="meta-item">
                        <img :src="result.author.avatar" :alt="result.author.name" class="author-avatar" />
                        {{ result.author.name }}
                      </span>
                      <span class="meta-item">
                        <img src="@/assets/blog/icons/clock.svg" alt="时间" class="meta-icon" />
                        {{ result.createdAt }}
                      </span>
                      <span class="meta-item">
                        <img src="@/assets/blog/icons/eye.svg" alt="阅读" class="meta-icon" />
                        {{ result.views }}
                      </span>
                    </div>
                  </div>
                  <div class="result-cover" v-if="result.coverImage">
                    <img :src="result.coverImage" :alt="result.title" />
                  </div>
                </router-link>
              </article>
            </div>
            
            <!-- 加载更多 -->
            <div class="load-more" v-if="searchResults.length > 0">
              <button class="load-more-btn" @click="loadMore">
                <img src="@/assets/blog/icons/chevrons-down.svg" alt="加载更多" class="btn-icon" />
                加载更多
              </button>
            </div>
            
            <!-- 无结果 -->
            <div class="no-results" v-if="hasSearched && searchResults.length === 0">
              <img src="@/assets/blog/icons/search.svg" alt="无结果" class="no-results-icon" />
              <h3>未找到相关结果</h3>
              <p>换个关键词试试？</p>
            </div>
          </section>
          
          <BlogSidebar />
        </div>
        
        <!-- 推荐阅读 -->
        <section class="recommend-section" v-if="!hasSearched">
          <h3 class="section-title">
            <img src="@/assets/blog/icons/zap.svg" alt="推荐阅读" class="title-icon" />
            推荐阅读
          </h3>
          <div class="recommend-grid">
            <PostCard 
              v-for="post in recommendPosts" 
              :key="post.id" 
              :post="post"
            />
          </div>
        </section>
      </div>
    </main>
    
    <BlogFooter />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import BlogHeader from '@/components/blog/BlogHeader.vue'
import BlogFooter from '@/components/blog/BlogFooter.vue'
import BlogSidebar from '@/components/blog/BlogSidebar.vue'
import PostCard from '@/components/blog/PostCard.vue'

import bookIcon from '@/assets/blog/icons/book.svg'
import userIcon from '@/assets/blog/icons/user.svg'
import categoryIcon from '@/assets/blog/icons/category.svg'

export default defineComponent({
  name: 'BlogSearch',
  components: {
    BlogHeader,
    BlogFooter,
    BlogSidebar,
    PostCard
  },
  setup() {
    const route = useRoute()
    
    const searchQuery = ref('')
    const hasSearched = ref(false)
    const totalResults = ref(0)
    const searchTime = ref(0)
    const activeFilter = ref('all')
    const sortBy = ref('relevance')
    
    const hotSearchTags = ref([
      'Vue.js', 'React', 'TypeScript', 'Node.js', 'Python',
      '前端面试', '算法', '微服务', '人工智能', 'Docker'
    ])
    
    const filterTabs = ref([
      { id: 'all', name: '全部', count: 128, icon: categoryIcon },
      { id: 'posts', name: '文章', count: 98, icon: bookIcon },
      { id: 'authors', name: '作者', count: 30, icon: userIcon }
    ])
    
    const searchResults = ref([
      {
        id: 1,
        title: 'Vue 3.0 Composition API 完全指南',
        excerpt: '深入探讨 Vue 3.0 的 Composition API，包括 setup 函数、响应式系统等核心概念...',
        coverImage: 'https://picsum.photos/200/150?random=20',
        author: { name: '技术小白', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=1' },
        createdAt: '2025-12-15',
        views: '2.3k'
      },
      {
        id: 2,
        title: 'Vue Router 4.0 新特性解析',
        excerpt: '详细介绍 Vue Router 4.0 的新特性，包括动态路由、导航守卫等...',
        coverImage: 'https://picsum.photos/200/150?random=21',
        author: { name: '路由专家', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=7' },
        createdAt: '2025-12-14',
        views: '1.8k'
      },
      {
        id: 3,
        title: 'Vuex 到 Pinia 迁移指南',
        excerpt: '从 Vuex 迁移到 Pinia 的完整指南，包括状态管理的最佳实践...',
        coverImage: '',
        author: { name: '状态管理者', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=8' },
        createdAt: '2025-12-13',
        views: '1.5k'
      }
    ])
    
    const recommendPosts = ref([
      {
        id: 1,
        title: 'Vue 3.0 Composition API 完全指南',
        excerpt: '深入探讨 Vue 3.0 的 Composition API。',
        coverImage: 'https://picsum.photos/800/400?random=30',
        category: '前端开发',
        tags: ['Vue.js', 'JavaScript'],
        author: { name: '技术小白', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=1' },
        createdAt: '2025-12-15',
        views: 2356,
        likes: 186,
        comments: 42,
        isLiked: false,
        isBookmarked: false
      },
      {
        id: 2,
        title: 'TypeScript 高级类型详解',
        excerpt: '深入讲解 TypeScript 的高级类型特性。',
        coverImage: 'https://picsum.photos/800/400?random=31',
        category: '前端开发',
        tags: ['TypeScript'],
        author: { name: '代码大师', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=2' },
        createdAt: '2025-12-14',
        views: 1824,
        likes: 142,
        comments: 35,
        isLiked: false,
        isBookmarked: false
      },
      {
        id: 3,
        title: 'Node.js 微服务架构实践',
        excerpt: '探索如何构建微服务架构。',
        coverImage: 'https://picsum.photos/800/400?random=32',
        category: '后端开发',
        tags: ['Node.js', '微服务'],
        author: { name: '架构师老王', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=3' },
        createdAt: '2025-12-13',
        views: 1567,
        likes: 98,
        comments: 28,
        isLiked: false,
        isBookmarked: false
      },
      {
        id: 4,
        title: 'React Hooks 最佳实践',
        excerpt: '学习如何设计高质量的自定义 Hook。',
        coverImage: 'https://picsum.photos/800/400?random=33',
        category: '前端开发',
        tags: ['React', 'Hooks'],
        author: { name: 'React爱好者', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=4' },
        createdAt: '2025-12-12',
        views: 1345,
        likes: 112,
        comments: 22,
        isLiked: false,
        isBookmarked: false
      }
    ])
    
    const performSearch = () => {
      if (searchQuery.value.trim()) {
        hasSearched.value = true
        totalResults.value = 128
        searchTime.value = 35
      }
    }
    
    const searchByTag = (tag: string) => {
      searchQuery.value = tag
      performSearch()
    }
    
    const highlightText = (text: string) => {
      if (!searchQuery.value) return text
      const regex = new RegExp(`(${searchQuery.value})`, 'gi')
      return text.replace(regex, '<mark>$1</mark>')
    }
    
    const loadMore = () => {
      console.log('加载更多')
    }
    
    onMounted(() => {
      const query = route.query.q as string
      const tag = route.query.tag as string
      
      if (query) {
        searchQuery.value = query
        performSearch()
      } else if (tag) {
        searchQuery.value = tag
        performSearch()
      }
    })
    
    return {
      searchQuery,
      hasSearched,
      totalResults,
      searchTime,
      activeFilter,
      sortBy,
      hotSearchTags,
      filterTabs,
      searchResults,
      recommendPosts,
      performSearch,
      searchByTag,
      highlightText,
      loadMore
    }
  }
})
</script>

<style scoped>
.blog-search {
  min-height: 100vh;
  background: var(--background-gradient);
}

.main-content {
  padding-top: 64px;
}

.content-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px;
}

/* Search Header */
.search-header {
  margin-bottom: 32px;
}

.search-box-large {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 12px 12px 24px;
  background: white;
  border-radius: 32px;
  box-shadow: var(--shadow-md);
  margin-bottom: 16px;
}

.search-icon {
  width: 24px;
  height: 24px;
  opacity: 0.4;
}

.search-box-large input {
  flex: 1;
  border: none;
  font-size: 16px;
  color: var(--text-dark);
}

.search-box-large input::placeholder {
  color: var(--text-muted);
}

.search-btn {
  padding: 14px 32px;
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
  color: white;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 500;
  transition: var(--transition-default);
}

.search-btn:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 16px rgba(58, 156, 255, 0.3);
}

.search-info {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-left: 24px;
}

.result-count {
  font-size: 15px;
  color: var(--text-light);
}

.result-count strong {
  color: var(--primary-color);
}

.search-time {
  font-size: 13px;
  color: var(--text-muted);
}

/* Hot Search */
.hot-search {
  margin-bottom: 40px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-dark);
  margin-bottom: 20px;
}

.title-icon {
  width: 22px;
  height: 22px;
  opacity: 0.7;
}

.hot-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.hot-tag {
  padding: 10px 20px;
  background: white;
  color: var(--text-light);
  border-radius: 24px;
  font-size: 14px;
  box-shadow: var(--shadow-sm);
  transition: var(--transition-default);
}

.hot-tag:hover {
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
  color: white;
  transform: translateY(-2px);
}

/* Main Grid */
.main-grid {
  display: flex;
  gap: 32px;
}

.results-section {
  flex: 1;
  min-width: 0;
}

/* Filter Bar */
.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding: 16px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: var(--shadow-sm);
}

.filter-tabs {
  display: flex;
  gap: 8px;
}

.filter-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: transparent;
  color: var(--text-light);
  border-radius: 8px;
  font-size: 14px;
  transition: var(--transition-default);
}

.filter-tab:hover {
  background: var(--border-light);
}

.filter-tab.active {
  background: linear-gradient(120deg, rgba(58, 156, 255, 0.1), rgba(168, 213, 255, 0.2));
  color: var(--primary-color);
}

.tab-icon {
  width: 16px;
  height: 16px;
  opacity: 0.6;
}

.filter-tab.active .tab-icon {
  opacity: 1;
}

.tab-count {
  padding: 2px 8px;
  background: var(--border-light);
  border-radius: 12px;
  font-size: 12px;
}

.filter-tab.active .tab-count {
  background: rgba(58, 156, 255, 0.2);
}

.sort-select {
  position: relative;
}

.sort-select select {
  padding: 10px 36px 10px 16px;
  background: var(--border-light);
  border: none;
  border-radius: 8px;
  font-size: 14px;
  color: var(--text-light);
  appearance: none;
  cursor: pointer;
}

.select-icon {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  height: 16px;
  opacity: 0.5;
  pointer-events: none;
}

/* Results List */
.results-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-item {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  transition: var(--transition-default);
}

.result-item:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.result-content {
  display: flex;
  gap: 20px;
  padding: 20px;
}

.result-main {
  flex: 1;
  min-width: 0;
}

.result-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-dark);
  margin-bottom: 10px;
  line-height: 1.5;
}

.result-title :deep(mark) {
  background: rgba(58, 156, 255, 0.2);
  color: var(--primary-color);
  padding: 0 2px;
  border-radius: 2px;
}

.result-excerpt {
  font-size: 14px;
  color: var(--text-light);
  line-height: 1.7;
  margin-bottom: 12px;
}

.result-excerpt :deep(mark) {
  background: rgba(58, 156, 255, 0.2);
  color: var(--primary-color);
  padding: 0 2px;
  border-radius: 2px;
}

.result-meta {
  display: flex;
  align-items: center;
  gap: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-muted);
}

.author-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
}

.meta-icon {
  width: 14px;
  height: 14px;
  opacity: 0.5;
}

.result-cover {
  width: 180px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.result-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* Load More */
.load-more {
  display: flex;
  justify-content: center;
  padding: 32px 0;
}

.load-more-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 32px;
  background: white;
  color: var(--text-light);
  border-radius: 24px;
  font-size: 14px;
  box-shadow: var(--shadow-sm);
  transition: var(--transition-default);
}

.load-more-btn:hover {
  color: var(--primary-color);
  box-shadow: var(--shadow-md);
}

.load-more-btn .btn-icon {
  width: 18px;
  height: 18px;
  opacity: 0.6;
}

/* No Results */
.no-results {
  text-align: center;
  padding: 60px 20px;
}

.no-results-icon {
  width: 80px;
  height: 80px;
  opacity: 0.3;
  margin-bottom: 20px;
}

.no-results h3 {
  font-size: 18px;
  color: var(--text-dark);
  margin-bottom: 8px;
}

.no-results p {
  font-size: 14px;
  color: var(--text-muted);
}

/* Recommend Section */
.recommend-section {
  margin-top: 40px;
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

@media (max-width: 1024px) {
  .main-grid {
    flex-direction: column;
  }
  
  .recommend-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .search-box-large {
    flex-direction: column;
    padding: 16px;
    border-radius: 16px;
  }
  
  .search-box-large input {
    width: 100%;
    text-align: center;
  }
  
  .search-btn {
    width: 100%;
  }
  
  .filter-bar {
    flex-direction: column;
    gap: 16px;
  }
  
  .result-content {
    flex-direction: column-reverse;
  }
  
  .result-cover {
    width: 100%;
    height: 160px;
  }
}
</style>
