<template>
  <div class="blog-category">
    <BlogHeader />
    
    <main class="main-content">
      <div class="content-container">
        <!-- 分类头部 -->
        <header class="category-header">
          <div class="category-info">
            <div class="category-icon-wrapper">
              <img :src="currentCategory.icon" :alt="currentCategory.name" class="category-icon" />
            </div>
            <div class="category-details">
              <h1 class="category-name">{{ currentCategory.name }}</h1>
              <p class="category-desc">{{ currentCategory.description }}</p>
              <div class="category-stats">
                <span class="stat">
                  <img src="@/assets/icons/book.svg" alt="文章" class="stat-icon" />
                  {{ currentCategory.postCount }} 篇文章
                </span>
                <span class="stat">
                  <img src="@/assets/icons/user.svg" alt="作者" class="stat-icon" />
                  {{ currentCategory.authorCount }} 位作者
                </span>
                <span class="stat">
                  <img src="@/assets/icons/eye.svg" alt="阅读" class="stat-icon" />
                  {{ currentCategory.viewCount }} 次阅读
                </span>
              </div>
            </div>
          </div>
        </header>
        
        <!-- 分类导航 -->
        <nav class="category-nav">
          <router-link 
            v-for="category in allCategories" 
            :key="category.id"
            :to="`/category/${category.id}`"
            class="nav-item"
            :class="{ active: category.id === currentCategoryId }"
          >
            <img :src="category.icon" :alt="category.name" class="nav-icon" />
            <span>{{ category.name }}</span>
          </router-link>
        </nav>
        
        <!-- 内容区 -->
        <div class="main-grid">
          <section class="posts-section">
            <!-- 排序选项 -->
            <div class="sort-bar">
              <div class="sort-options">
                <button 
                  v-for="option in sortOptions" 
                  :key="option.id"
                  class="sort-btn"
                  :class="{ active: currentSort === option.id }"
                  @click="currentSort = option.id"
                >
                  {{ option.name }}
                </button>
              </div>
              <div class="view-options">
                <button 
                  class="view-btn" 
                  :class="{ active: viewMode === 'grid' }"
                  @click="viewMode = 'grid'"
                >
                  <img src="@/assets/icons/category.svg" alt="网格" />
                </button>
                <button 
                  class="view-btn"
                  :class="{ active: viewMode === 'list' }"
                  @click="viewMode = 'list'"
                >
                  <img src="@/assets/icons/menu.svg" alt="列表" />
                </button>
              </div>
            </div>
            
            <!-- 文章列表 -->
            <div class="posts-grid" :class="{ 'list-view': viewMode === 'list' }">
              <PostCard 
                v-for="post in categoryPosts" 
                :key="post.id" 
                :post="post"
                @like="handleLike"
                @bookmark="handleBookmark"
              />
            </div>
            
            <!-- 分页 -->
            <div class="pagination">
              <button class="page-btn prev" :disabled="currentPage === 1" @click="currentPage--">
                <img src="@/assets/icons/chevron-left.svg" alt="上一页" />
                <span>上一页</span>
              </button>
              <div class="page-numbers">
                <button 
                  v-for="page in totalPages" 
                  :key="page"
                  class="page-num"
                  :class="{ active: currentPage === page }"
                  @click="currentPage = page"
                >
                  {{ page }}
                </button>
              </div>
              <button class="page-btn next" :disabled="currentPage === totalPages" @click="currentPage++">
                <span>下一页</span>
                <img src="@/assets/icons/chevron-right.svg" alt="下一页" />
              </button>
            </div>
          </section>
          
          <BlogSidebar />
        </div>
      </div>
    </main>
    
    <BlogFooter />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import BlogHeader from '@/components/BlogHeader.vue'
import BlogFooter from '@/components/BlogFooter.vue'
import BlogSidebar from '@/components/BlogSidebar.vue'
import PostCard from '@/components/PostCard.vue'

import codeIcon from '@/assets/icons/code.svg'
import bookIcon from '@/assets/icons/book.svg'
import zapIcon from '@/assets/icons/zap.svg'
import settingsIcon from '@/assets/icons/settings.svg'
import categoryIcon from '@/assets/icons/category.svg'

export default defineComponent({
  name: 'BlogCategory',
  components: {
    BlogHeader,
    BlogFooter,
    BlogSidebar,
    PostCard
  },
  setup() {
    const route = useRoute()
    
    const currentCategoryId = computed(() => route.params.name as string || 'tech')
    const currentPage = ref(1)
    const currentSort = ref('latest')
    const viewMode = ref('grid')
    const totalPages = ref(5)
    
    const allCategories = ref([
      { id: 'tech', name: '技术分享', icon: codeIcon, description: '技术文章、教程和经验分享', postCount: 1234, authorCount: 156, viewCount: '12.5w' },
      { id: 'frontend', name: '前端开发', icon: bookIcon, description: '前端技术、框架和最佳实践', postCount: 856, authorCount: 98, viewCount: '8.6w' },
      { id: 'backend', name: '后端开发', icon: settingsIcon, description: '后端架构、数据库和API设计', postCount: 678, authorCount: 76, viewCount: '6.8w' },
      { id: 'ai', name: '人工智能', icon: zapIcon, description: '机器学习、深度学习和AI应用', postCount: 432, authorCount: 54, viewCount: '4.5w' },
      { id: 'other', name: '其他', icon: categoryIcon, description: '职场、生活和其他话题', postCount: 321, authorCount: 45, viewCount: '3.2w' }
    ])
    
    const currentCategory = computed(() => {
      return allCategories.value.find(c => c.id === currentCategoryId.value) || allCategories.value[0]
    })
    
    const sortOptions = ref([
      { id: 'latest', name: '最新' },
      { id: 'hot', name: '最热' },
      { id: 'recommend', name: '推荐' }
    ])
    
    const categoryPosts = ref([
      {
        id: 1,
        title: 'Vue 3.0 Composition API 完全指南',
        excerpt: '深入探讨 Vue 3.0 的 Composition API，包括 setup 函数、响应式系统等核心概念。',
        coverImage: 'https://picsum.photos/800/400?random=10',
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
        excerpt: '深入讲解 TypeScript 的高级类型特性，包括泛型约束、条件类型等。',
        coverImage: 'https://picsum.photos/800/400?random=11',
        category: '前端开发',
        tags: ['TypeScript', '类型系统'],
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
        excerpt: '探索如何将传统的单体应用拆分为微服务架构。',
        coverImage: 'https://picsum.photos/800/400?random=12',
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
        excerpt: '学习如何设计和实现高质量的自定义 Hook。',
        coverImage: 'https://picsum.photos/800/400?random=13',
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
    
    const handleLike = (postId: number) => {
      const post = categoryPosts.value.find(p => p.id === postId)
      if (post) {
        post.isLiked = !post.isLiked
        post.likes += post.isLiked ? 1 : -1
      }
    }
    
    const handleBookmark = (postId: number) => {
      const post = categoryPosts.value.find(p => p.id === postId)
      if (post) {
        post.isBookmarked = !post.isBookmarked
      }
    }
    
    return {
      currentCategoryId,
      currentCategory,
      allCategories,
      sortOptions,
      currentSort,
      viewMode,
      categoryPosts,
      currentPage,
      totalPages,
      handleLike,
      handleBookmark
    }
  }
})
</script>

<style scoped>
.blog-category {
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

/* Category Header */
.category-header {
  margin-bottom: 32px;
}

.category-info {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 32px;
  background: white;
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-sm);
}

.category-icon-wrapper {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(120deg, rgba(58, 156, 255, 0.1), rgba(168, 213, 255, 0.2));
  border-radius: 20px;
}

.category-icon {
  width: 40px;
  height: 40px;
}

.category-details {
  flex: 1;
}

.category-name {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-dark);
  margin-bottom: 8px;
}

.category-desc {
  font-size: 15px;
  color: var(--text-light);
  margin-bottom: 16px;
}

.category-stats {
  display: flex;
  gap: 24px;
}

.stat {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--text-muted);
}

.stat-icon {
  width: 16px;
  height: 16px;
  opacity: 0.5;
}

/* Category Navigation */
.category-nav {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  overflow-x: auto;
  padding-bottom: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: white;
  border-radius: 24px;
  font-size: 14px;
  color: var(--text-light);
  white-space: nowrap;
  box-shadow: var(--shadow-sm);
  transition: var(--transition-default);
}

.nav-item:hover {
  color: var(--primary-color);
}

.nav-item.active {
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
  color: white;
}

.nav-icon {
  width: 18px;
  height: 18px;
  opacity: 0.7;
}

.nav-item.active .nav-icon {
  filter: brightness(0) invert(1);
  opacity: 1;
}

/* Main Grid */
.main-grid {
  display: flex;
  gap: 32px;
}

.posts-section {
  flex: 1;
  min-width: 0;
}

/* Sort Bar */
.sort-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding: 12px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: var(--shadow-sm);
}

.sort-options {
  display: flex;
  gap: 8px;
}

.sort-btn {
  padding: 8px 16px;
  background: transparent;
  color: var(--text-light);
  font-size: 14px;
  border-radius: 20px;
  transition: var(--transition-default);
}

.sort-btn:hover {
  background: var(--border-light);
}

.sort-btn.active {
  background: linear-gradient(120deg, rgba(58, 156, 255, 0.1), rgba(168, 213, 255, 0.2));
  color: var(--primary-color);
  font-weight: 500;
}

.view-options {
  display: flex;
  gap: 4px;
}

.view-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border-radius: 8px;
  transition: var(--transition-default);
}

.view-btn:hover {
  background: var(--border-light);
}

.view-btn.active {
  background: linear-gradient(120deg, rgba(58, 156, 255, 0.1), rgba(168, 213, 255, 0.2));
}

.view-btn img {
  width: 18px;
  height: 18px;
  opacity: 0.5;
}

.view-btn.active img {
  opacity: 1;
  filter: invert(48%) sepia(91%) saturate(1417%) hue-rotate(196deg) brightness(101%) contrast(102%);
}

/* Posts Grid */
.posts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.posts-grid.list-view {
  grid-template-columns: 1fr;
}

/* Pagination */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 40px;
  padding-top: 24px;
}

.page-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  background: white;
  color: var(--text-light);
  border-radius: 8px;
  font-size: 14px;
  box-shadow: var(--shadow-sm);
  transition: var(--transition-default);
}

.page-btn:hover:not(:disabled) {
  color: var(--primary-color);
  box-shadow: var(--shadow-md);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-btn img {
  width: 16px;
  height: 16px;
  opacity: 0.6;
}

.page-numbers {
  display: flex;
  gap: 8px;
}

.page-num {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  color: var(--text-light);
  border-radius: 8px;
  font-size: 14px;
  box-shadow: var(--shadow-sm);
  transition: var(--transition-default);
}

.page-num:hover {
  color: var(--primary-color);
}

.page-num.active {
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
  color: white;
}

@media (max-width: 1024px) {
  .main-grid {
    flex-direction: column;
  }
  
  .posts-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .category-info {
    flex-direction: column;
    text-align: center;
  }
  
  .category-stats {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .sort-bar {
    flex-direction: column;
    gap: 12px;
  }
  
  .pagination {
    flex-wrap: wrap;
  }
  
  .page-numbers {
    order: 3;
    width: 100%;
    justify-content: center;
    margin-top: 8px;
  }
}
</style>
