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
              <img src="@/assets/blog/icons/search.svg" alt="搜索" class="search-icon" />
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
                  <img src="@/assets/blog/icons/book.svg" alt="文章" class="title-icon" />
                  最新文章
                </h2>
              </div>
              <div class="header-tabs">
                <button 
                  v-for="tab in tabs" 
                  :key="tab.id" 
                  class="tab-btn"
                  :class="{ active: activeTab === tab.id }"
                  @click="switchTab(tab.id)"
                >
                  {{ tab.name }}
                </button>
              </div>
            </div>
            
            <!-- 加载状态 -->
            <div v-if="loading" class="loading-state">
              <div class="loading-spinner"></div>
              <p>加载中...</p>
            </div>
            
            <div v-else class="posts-grid">
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
                <img src="@/assets/blog/icons/chevrons-down.svg" alt="加载更多" class="btn-icon" />
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
import { defineComponent, ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import BlogHeader from '@/components/blog/BlogHeader.vue'
import BlogFooter from '@/components/blog/BlogFooter.vue'
import BlogSidebar from '@/components/blog/BlogSidebar.vue'
import PostCard from '@/components/blog/PostCard.vue'
import { blogService, type PostListItem, type Category } from '@/services/blogService'

// 导入图标
import homeIcon from '@/assets/blog/icons/home.svg'
import codeIcon from '@/assets/blog/icons/code.svg'
import bookIcon from '@/assets/blog/icons/book.svg'
import zapIcon from '@/assets/blog/icons/zap.svg'
import categoryIcon from '@/assets/blog/icons/category.svg'

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
    const currentPage = ref(0)
    const totalPages = ref(0)
    const loading = ref(false)
    
    // 默认分类（用于没有从后端加载时）
    const defaultCategories = [
      { id: 'all', name: '全部', icon: homeIcon },
      { id: 'frontend', name: '前端开发', icon: codeIcon },
      { id: 'backend', name: '后端开发', icon: bookIcon },
      { id: 'ai', name: '人工智能', icon: zapIcon },
      { id: 'other', name: '其他', icon: categoryIcon }
    ]
    
    const categories = ref(defaultCategories)
    const serverCategories = ref<Category[]>([])
    
    const tabs = ref([
      { id: 'latest', name: '最新' },
      { id: 'hot', name: '热门' },
      { id: 'recommend', name: '推荐' }
    ])
    
    // 文章列表
    const posts = ref<any[]>([])
    
    // 加载分类
    const loadCategories = async () => {
      try {
        const response = await blogService.categories.getAll()
        if (response.success && response.data) {
          serverCategories.value = response.data
          // 映射分类到前端显示格式
          const iconMap: Record<string, string> = {
            '前端开发': codeIcon,
            '后端开发': bookIcon,
            '人工智能': zapIcon
          }
          const mappedCategories = response.data.map(cat => ({
            id: cat.id.toString(),
            name: cat.name,
            icon: iconMap[cat.name] || categoryIcon,
            dbId: cat.id
          }))
          categories.value = [
            { id: 'all', name: '全部', icon: homeIcon },
            ...mappedCategories
          ]
        }
      } catch (error) {
        console.error('加载分类失败:', error)
      }
    }
    
    // 加载文章列表
    const loadPosts = async () => {
      loading.value = true
      try {
        let response
        
        if (activeTab.value === 'hot') {
          // 热门文章
          response = await blogService.posts.getHot(20)
          if (response.success) {
            posts.value = transformPosts(response.data || [])
          }
        } else if (activeTab.value === 'recommend') {
          // 推荐文章
          response = await blogService.posts.getFeatured(20)
          if (response.success) {
            posts.value = transformPosts(response.data || [])
          }
        } else {
          // 最新文章
          const params: any = {
            page: currentPage.value,
            size: 10
          }
          
          // 如果选择了分类
          if (activeCategory.value !== 'all') {
            const cat = categories.value.find(c => c.id === activeCategory.value)
            if (cat && (cat as any).dbId) {
              params.categoryId = (cat as any).dbId
            }
          }
          
          response = await blogService.posts.getList(params)
          if (response.success && response.data) {
            const pageData = response.data
            posts.value = transformPosts(pageData.content || [])
            totalPages.value = pageData.totalPages
          }
        }
      } catch (error) {
        console.error('加载文章失败:', error)
        // 加载失败时使用示例数据
        posts.value = getDefaultPosts()
      } finally {
        loading.value = false
      }
    }
    
    // 转换后端数据格式到前端格式
    const transformPosts = (data: PostListItem[]): any[] => {
      return data.map(post => ({
        id: post.id,
        title: post.title,
        excerpt: post.excerpt || '',
        coverImage: post.coverImage || `https://picsum.photos/800/400?random=${post.id}`,
        category: post.category?.name || '未分类',
        tags: post.tags?.map(t => t.name) || [],
        author: {
          name: post.author?.nickname || post.author?.username || '匿名用户',
          avatar: post.author?.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${post.author?.id || 1}`
        },
        createdAt: post.publishedAt ? new Date(post.publishedAt).toLocaleDateString() : '未知',
        views: post.viewCount || 0,
        likes: post.likeCount || 0,
        comments: post.commentCount || 0,
        isLiked: false,
        isBookmarked: false,
        isTop: post.isTop,
        isFeatured: post.isFeatured
      }))
    }
    
    // 默认示例数据
    const getDefaultPosts = () => [
      {
        id: 1,
        title: 'Vue 3.0 Composition API 完全指南：从入门到精通',
        excerpt: '深入探讨 Vue 3.0 的 Composition API，包括 setup 函数、响应式系统、生命周期钩子等核心概念。',
        coverImage: 'https://picsum.photos/800/400?random=1',
        category: '前端开发',
        tags: ['Vue.js', 'JavaScript', '前端框架'],
        author: { name: '技术小白', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=1' },
        createdAt: '2025-12-15',
        views: 2356,
        likes: 186,
        comments: 42,
        isLiked: false,
        isBookmarked: false
      }
    ]
    
    const displayedPosts = computed(() => {
      return posts.value
    })
    
    const handleSearch = () => {
      if (searchQuery.value.trim()) {
        router.push({ path: '/blog/search', query: { q: searchQuery.value } })
      }
    }
    
    const setActiveCategory = (categoryId: string) => {
      activeCategory.value = categoryId
      currentPage.value = 0
      loadPosts()
    }
    
    const handleLike = async (postId: number) => {
      const post = posts.value.find(p => p.id === postId)
      if (post) {
        try {
          if (post.isLiked) {
            await blogService.posts.unlike(postId)
            post.isLiked = false
            post.likes--
          } else {
            await blogService.posts.like(postId)
            post.isLiked = true
            post.likes++
          }
        } catch (error) {
          console.error('点赞操作失败:', error)
          // 本地切换状态（用于演示）
          post.isLiked = !post.isLiked
          post.likes += post.isLiked ? 1 : -1
        }
      }
    }
    
    const handleBookmark = async (postId: number) => {
      const post = posts.value.find(p => p.id === postId)
      if (post) {
        try {
          if (post.isBookmarked) {
            await blogService.posts.unbookmark(postId)
            post.isBookmarked = false
          } else {
            await blogService.posts.bookmark(postId)
            post.isBookmarked = true
          }
        } catch (error) {
          console.error('收藏操作失败:', error)
          // 本地切换状态（用于演示）
          post.isBookmarked = !post.isBookmarked
        }
      }
    }
    
    const handleShare = (post: any) => {
      // 复制链接到剪贴板
      const url = `${window.location.origin}/blog/post/${post.id}`
      navigator.clipboard.writeText(url).then(() => {
        alert('链接已复制到剪贴板')
      })
    }
    
    const loadMorePosts = async () => {
      if (currentPage.value < totalPages.value - 1) {
        currentPage.value++
        loading.value = true
        try {
          const response = await blogService.posts.getList({
            page: currentPage.value,
            size: 10
          })
          if (response.success && response.data) {
            const newPosts = transformPosts(response.data.content || [])
            posts.value = [...posts.value, ...newPosts]
          }
        } catch (error) {
          console.error('加载更多失败:', error)
        } finally {
          loading.value = false
        }
      }
    }
    
    // 切换标签时重新加载
    const switchTab = (tabId: string) => {
      activeTab.value = tabId
      currentPage.value = 0
      loadPosts()
    }
    
    // 组件挂载时加载数据
    onMounted(() => {
      loadCategories()
      loadPosts()
    })
    
    return {
      searchQuery,
      activeCategory,
      activeTab,
      categories,
      tabs,
      displayedPosts,
      loading,
      handleSearch,
      setActiveCategory,
      handleLike,
      handleBookmark,
      handleShare,
      loadMorePosts,
      switchTab
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
