<template>
  <div class="blog-home">
    <BlogHeader />
    
    <main class="main-content">
      <div class="content-container">
        <!-- 头部横幅 -->
        <section class="hero-banner">
          <div class="hero-content">
            <h1 class="hero-title">CloudStack Blog</h1>
            <p class="hero-subtitle">分享技术 · 记录成长 · 连接世界</p>
            <div class="hero-search">
              <img src="@/assets/icons/search.svg" alt="搜索" class="search-icon" />
              <input type="text" placeholder="搜索感兴趣的内容..." v-model="searchQuery" @keyup.enter="handleSearch" />
              <button class="search-btn" @click="handleSearch">搜索</button>
            </div>
          </div>
          <div class="hero-decoration">
            <div class="decoration-circle circle-1"></div>
            <div class="decoration-circle circle-2"></div>
            <div class="decoration-circle circle-3"></div>
          </div>
        </section>
        
        <!-- 分类导航 -->
        <section class="category-nav">
          <div class="category-list">
            <button 
              v-for="category in categories" 
              :key="category.id" 
              class="category-item"
              :class="{ active: activeCategory === category.id }"
              @click="setActiveCategory(category.id)"
            >
              <img :src="category.icon" :alt="category.name" class="category-icon" />
              <span>{{ category.name }}</span>
            </button>
          </div>
        </section>
        
        <!-- 主内容区 -->
        <div class="main-grid">
          <!-- 文章列表 -->
          <section class="posts-section">
            <div class="section-header">
              <div class="header-left">
                <h2 class="section-title">
                  <img src="@/assets/icons/book.svg" alt="文章" class="title-icon" />
                  最新文章
                </h2>
              </div>
              <div class="header-tabs">
                <button 
                  v-for="tab in tabs" 
                  :key="tab.id" 
                  class="tab-btn"
                  :class="{ active: activeTab === tab.id }"
                  @click="activeTab = tab.id"
                >
                  {{ tab.name }}
                </button>
              </div>
            </div>
            
            <div class="posts-grid">
              <PostCard 
                v-for="(post, index) in displayedPosts" 
                :key="post.id" 
                :post="post"
                :featured="index === 0"
                @like="handleLike"
                @bookmark="handleBookmark"
                @share="handleShare"
              />
            </div>
            
            <!-- 加载更多 -->
            <div class="load-more">
              <button class="load-more-btn" @click="loadMorePosts">
                <img src="@/assets/icons/chevrons-down.svg" alt="加载更多" class="btn-icon" />
                <span>加载更多</span>
              </button>
            </div>
          </section>
          
          <!-- 侧边栏 -->
          <BlogSidebar />
        </div>
      </div>
    </main>
    
    <BlogFooter />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import BlogHeader from '@/components/BlogHeader.vue'
import BlogFooter from '@/components/BlogFooter.vue'
import BlogSidebar from '@/components/BlogSidebar.vue'
import PostCard from '@/components/PostCard.vue'

// 导入图标
import homeIcon from '@/assets/icons/home.svg'
import codeIcon from '@/assets/icons/code.svg'
import bookIcon from '@/assets/icons/book.svg'
import zapIcon from '@/assets/icons/zap.svg'
import categoryIcon from '@/assets/icons/category.svg'

export default defineComponent({
  name: 'BlogHome',
  components: {
    BlogHeader,
    BlogFooter,
    BlogSidebar,
    PostCard
  },
  setup() {
    const router = useRouter()
    
    const searchQuery = ref('')
    const activeCategory = ref('all')
    const activeTab = ref('latest')
    const currentPage = ref(1)
    
    const categories = ref([
      { id: 'all', name: '全部', icon: homeIcon },
      { id: 'frontend', name: '前端开发', icon: codeIcon },
      { id: 'backend', name: '后端开发', icon: bookIcon },
      { id: 'ai', name: '人工智能', icon: zapIcon },
      { id: 'other', name: '其他', icon: categoryIcon }
    ])
    
    const tabs = ref([
      { id: 'latest', name: '最新' },
      { id: 'hot', name: '热门' },
      { id: 'recommend', name: '推荐' }
    ])
    
    const posts = ref([
      {
        id: 1,
        title: 'Vue 3.0 Composition API 完全指南：从入门到精通',
        excerpt: '深入探讨 Vue 3.0 的 Composition API，包括 setup 函数、响应式系统、生命周期钩子等核心概念，帮助你快速掌握新版本的开发方式。',
        coverImage: 'https://picsum.photos/800/400?random=1',
        category: '前端开发',
        tags: ['Vue.js', 'JavaScript', '前端框架'],
        author: {
          name: '技术小白',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=1'
        },
        createdAt: '2025-12-15',
        views: 2356,
        likes: 186,
        comments: 42,
        isLiked: false,
        isBookmarked: false
      },
      {
        id: 2,
        title: 'TypeScript 高级类型系统深入理解：泛型与类型推断',
        excerpt: '本文将深入讲解 TypeScript 的高级类型特性，包括泛型约束、条件类型、映射类型等，让你的代码更加类型安全。',
        coverImage: 'https://picsum.photos/800/400?random=2',
        category: '前端开发',
        tags: ['TypeScript', '类型系统', '编程语言'],
        author: {
          name: '代码大师',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=2'
        },
        createdAt: '2025-12-14',
        views: 1824,
        likes: 142,
        comments: 35,
        isLiked: true,
        isBookmarked: false
      },
      {
        id: 3,
        title: 'Node.js 微服务架构实践：从单体到分布式',
        excerpt: '探索如何将传统的单体应用拆分为微服务架构，涵盖服务发现、负载均衡、API网关等核心概念。',
        coverImage: 'https://picsum.photos/800/400?random=3',
        category: '后端开发',
        tags: ['Node.js', '微服务', '架构设计'],
        author: {
          name: '架构师老王',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=3'
        },
        createdAt: '2025-12-13',
        views: 1567,
        likes: 98,
        comments: 28,
        isLiked: false,
        isBookmarked: true
      },
      {
        id: 4,
        title: 'React Hooks 最佳实践：自定义 Hook 设计模式',
        excerpt: '学习如何设计和实现高质量的自定义 Hook，提高代码复用性和可维护性，让你的 React 项目更加优雅。',
        coverImage: 'https://picsum.photos/800/400?random=4',
        category: '前端开发',
        tags: ['React', 'Hooks', '设计模式'],
        author: {
          name: 'React爱好者',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=4'
        },
        createdAt: '2025-12-12',
        views: 1345,
        likes: 112,
        comments: 22,
        isLiked: false,
        isBookmarked: false
      },
      {
        id: 5,
        title: 'Python 机器学习入门：使用 Scikit-learn 构建第一个模型',
        excerpt: '本教程将带你从零开始学习机器学习，使用 Python 和 Scikit-learn 库构建你的第一个分类模型。',
        coverImage: 'https://picsum.photos/800/400?random=5',
        category: '人工智能',
        tags: ['Python', '机器学习', 'Scikit-learn'],
        author: {
          name: 'AI探索者',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=5'
        },
        createdAt: '2025-12-11',
        views: 2108,
        likes: 167,
        comments: 38,
        isLiked: false,
        isBookmarked: false
      },
      {
        id: 6,
        title: '前端性能优化实战：让你的网站快如闪电',
        excerpt: '从代码分割、懒加载、缓存策略到性能监控，全方位提升你的网站性能，提供极致的用户体验。',
        coverImage: 'https://picsum.photos/800/400?random=6',
        category: '前端开发',
        tags: ['性能优化', 'Web开发', '用户体验'],
        author: {
          name: '性能调优师',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=6'
        },
        createdAt: '2025-12-10',
        views: 1876,
        likes: 145,
        comments: 31,
        isLiked: true,
        isBookmarked: true
      }
    ])
    
    const displayedPosts = computed(() => {
      let filtered = posts.value
      
      if (activeCategory.value !== 'all') {
        const categoryMap: Record<string, string> = {
          'frontend': '前端开发',
          'backend': '后端开发',
          'ai': '人工智能',
          'other': '其他'
        }
        filtered = filtered.filter(p => p.category === categoryMap[activeCategory.value])
      }
      
      if (activeTab.value === 'hot') {
        filtered = [...filtered].sort((a, b) => b.views - a.views)
      } else if (activeTab.value === 'recommend') {
        filtered = [...filtered].sort((a, b) => b.likes - a.likes)
      }
      
      return filtered
    })
    
    const handleSearch = () => {
      if (searchQuery.value.trim()) {
        router.push({ path: '/search', query: { q: searchQuery.value } })
      }
    }
    
    const setActiveCategory = (categoryId: string) => {
      activeCategory.value = categoryId
    }
    
    const handleLike = (postId: number) => {
      const post = posts.value.find(p => p.id === postId)
      if (post) {
        post.isLiked = !post.isLiked
        post.likes += post.isLiked ? 1 : -1
      }
    }
    
    const handleBookmark = (postId: number) => {
      const post = posts.value.find(p => p.id === postId)
      if (post) {
        post.isBookmarked = !post.isBookmarked
      }
    }
    
    const handleShare = (post: any) => {
      console.log('Share post:', post.title)
    }
    
    const loadMorePosts = () => {
      currentPage.value++
      // 这里可以添加加载更多文章的逻辑
    }
    
    return {
      searchQuery,
      activeCategory,
      activeTab,
      categories,
      tabs,
      displayedPosts,
      handleSearch,
      setActiveCategory,
      handleLike,
      handleBookmark,
      handleShare,
      loadMorePosts
    }
  }
})
</script>

<style scoped>
.blog-home {
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

/* Hero Banner */
.hero-banner {
  position: relative;
  padding: 60px 0;
  text-align: center;
  overflow: hidden;
}

.hero-content {
  position: relative;
  z-index: 1;
}

.hero-title {
  font-size: 48px;
  font-weight: 700;
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  margin-bottom: 16px;
}

.hero-subtitle {
  font-size: 18px;
  color: var(--text-light);
  margin-bottom: 32px;
}

.hero-search {
  display: flex;
  align-items: center;
  max-width: 560px;
  margin: 0 auto;
  padding: 8px 8px 8px 20px;
  background: white;
  border-radius: 32px;
  box-shadow: var(--shadow-md);
}

.hero-search .search-icon {
  width: 20px;
  height: 20px;
  opacity: 0.4;
  margin-right: 12px;
}

.hero-search input {
  flex: 1;
  border: none;
  font-size: 15px;
  color: var(--text-dark);
}

.hero-search input::placeholder {
  color: var(--text-muted);
}

.search-btn {
  padding: 12px 28px;
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
  color: white;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 500;
  transition: var(--transition-default);
}

.search-btn:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 12px rgba(58, 156, 255, 0.3);
}

.hero-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(120deg, rgba(58, 156, 255, 0.1), rgba(168, 213, 255, 0.1));
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  right: -50px;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: -50px;
  left: 10%;
}

.circle-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  left: -30px;
}

/* Category Navigation */
.category-nav {
  margin-bottom: 32px;
}

.category-list {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding: 8px 0;
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.category-list::-webkit-scrollbar {
  display: none;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: white;
  border-radius: 24px;
  font-size: 14px;
  color: var(--text-light);
  white-space: nowrap;
  transition: var(--transition-default);
  box-shadow: var(--shadow-sm);
}

.category-item:hover {
  color: var(--primary-color);
  box-shadow: var(--shadow-md);
}

.category-item.active {
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
  color: white;
}

.category-icon {
  width: 18px;
  height: 18px;
  opacity: 0.7;
}

.category-item:hover .category-icon,
.category-item.active .category-icon {
  opacity: 1;
}

.category-item.active .category-icon {
  filter: brightness(0) invert(1);
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

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-dark);
}

.title-icon {
  width: 24px;
  height: 24px;
  opacity: 0.7;
}

.header-tabs {
  display: flex;
  gap: 8px;
}

.tab-btn {
  padding: 8px 16px;
  background: transparent;
  color: var(--text-light);
  font-size: 14px;
  border-radius: 20px;
  transition: var(--transition-default);
}

.tab-btn:hover {
  background: var(--border-light);
}

.tab-btn.active {
  background: linear-gradient(120deg, rgba(58, 156, 255, 0.1), rgba(168, 213, 255, 0.2));
  color: var(--primary-color);
  font-weight: 500;
}

/* Posts Grid */
.posts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

/* Load More */
.load-more {
  display: flex;
  justify-content: center;
  padding: 40px 0;
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

@media (max-width: 1024px) {
  .main-grid {
    flex-direction: column;
  }
  
  .posts-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 32px;
  }
  
  .hero-subtitle {
    font-size: 15px;
  }
  
  .hero-search {
    flex-direction: column;
    padding: 16px;
    border-radius: 16px;
  }
  
  .hero-search input {
    width: 100%;
    margin-bottom: 12px;
    text-align: center;
  }
  
  .hero-search .search-icon {
    display: none;
  }
  
  .search-btn {
    width: 100%;
  }
  
  .section-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
}
</style>
