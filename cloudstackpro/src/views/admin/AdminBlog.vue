<template>
  <div class="admin-blog">
    <AdminHeader />

    <main class="main-content">
      <div class="content-container">
        <section class="hero-banner">
          <div class="hero-content">
            <h1 class="hero-title">CloudStack Blog</h1>
            <p class="hero-subtitle">内容管理 · 审核协作 · 高效运营</p>
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

        <div class="main-grid">
          <section class="posts-section">
            <div class="section-header">
              <div class="header-left">
                <h2 class="section-title">
                  <img src="@/assets/blog/icons/book.svg" alt="文章" class="title-icon" />
                  文章列表
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
                :postRouteBase="postRouteBase"
                @like="handleLike"
                @bookmark="handleBookmark"
                @share="handleShare"
              />
            </div>

            <div class="load-more" v-if="canLoadMore">
              <button class="load-more-btn" @click="loadMorePosts">
                <img src="@/assets/blog/icons/chevrons-down.svg" alt="加载更多" class="btn-icon" />
                <span>加载更多</span>
              </button>
            </div>
          </section>

          <BlogSidebar
            :showAuthorCard="false"
            :showArchive="false"
            :postRouteBase="postRouteBase"
            tagRoutePath="/admin/blog"
            tagQueryKey="tag"
          />
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminHeader from '@/components/admin/AdminHeader.vue'
import BlogSidebar from '@/components/blog/BlogSidebar.vue'
import PostCard from '@/components/blog/PostCard.vue'
import { blogService, type Category, type PostListItem } from '@/services/blogService'

import homeIcon from '@/assets/blog/icons/home.svg'
import codeIcon from '@/assets/blog/icons/code.svg'
import bookIcon from '@/assets/blog/icons/book.svg'
import zapIcon from '@/assets/blog/icons/zap.svg'
import categoryIcon from '@/assets/blog/icons/category.svg'

const router = useRouter()
const route = useRoute()

const postRouteBase = '/admin/blog/post'

const searchQuery = ref(typeof route.query.keyword === 'string' ? route.query.keyword : '')
const activeCategory = ref('all')
const activeTab = ref(typeof route.query.tab === 'string' ? route.query.tab : 'latest')
const currentPage = ref(0)
const totalPages = ref(0)
const loading = ref(false)

const tabs = ref([
  { id: 'latest', name: '最新' },
  { id: 'hot', name: '热门' },
  { id: 'recommend', name: '推荐' }
])

const defaultCategories = [
  { id: 'all', name: '全部', icon: homeIcon },
  { id: 'frontend', name: '前端开发', icon: codeIcon },
  { id: 'backend', name: '后端开发', icon: bookIcon },
  { id: 'ai', name: '人工智能', icon: zapIcon },
  { id: 'other', name: '其他', icon: categoryIcon }
]

const categories = ref<any[]>(defaultCategories)
const serverCategories = ref<Category[]>([])

const posts = ref<any[]>([])
const displayedPosts = computed(() => posts.value)

const canLoadMore = computed(() => {
  if (loading.value) return false
  if (activeTab.value !== 'latest') return false
  if (!totalPages.value) return false
  return currentPage.value < totalPages.value - 1
})

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
    isTop: (post as any).isTop,
    isFeatured: (post as any).isFeatured
  }))
}

const loadCategories = async () => {
  try {
    const response = await blogService.categories.getAll()
    if (response.success && response.data) {
      serverCategories.value = response.data
      const iconMap: Record<string, string> = {
        前端开发: codeIcon,
        后端开发: bookIcon,
        人工智能: zapIcon
      }
      const mappedCategories = response.data.map(cat => ({
        id: cat.id.toString(),
        name: cat.name,
        icon: iconMap[cat.name] || categoryIcon,
        dbId: cat.id
      }))
      categories.value = [{ id: 'all', name: '全部', icon: homeIcon }, ...mappedCategories]
    }
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const loadPosts = async () => {
  loading.value = true
  try {
    const keyword = typeof route.query.keyword === 'string' ? route.query.keyword : ''
    const tag = typeof route.query.tag === 'string' ? route.query.tag : ''

    if (tag) {
      const response = await blogService.tags.getPosts(tag, currentPage.value, 10)
      if (response.success && response.data) {
        const pageData = response.data
        const newPosts = transformPosts(pageData.content || [])
        posts.value = currentPage.value === 0 ? newPosts : [...posts.value, ...newPosts]
        totalPages.value = pageData.totalPages
      }
      return
    }

    if (keyword) {
      const response = await blogService.posts.search(keyword, { page: currentPage.value, size: 10 })
      if (response.success && response.data) {
        const pageData = response.data
        const newPosts = transformPosts(pageData.content || [])
        posts.value = currentPage.value === 0 ? newPosts : [...posts.value, ...newPosts]
        totalPages.value = pageData.totalPages
      }
      return
    }

    if (activeTab.value === 'hot') {
      const response = await blogService.posts.getHot(20)
      if (response.success) {
        posts.value = transformPosts(response.data || [])
        totalPages.value = 0
      }
      return
    }

    if (activeTab.value === 'recommend') {
      const response = await blogService.posts.getFeatured(20)
      if (response.success) {
        posts.value = transformPosts(response.data || [])
        totalPages.value = 0
      }
      return
    }

    const params: any = {
      page: currentPage.value,
      size: 10
    }

    if (activeCategory.value !== 'all') {
      const cat = categories.value.find(c => c.id === activeCategory.value)
      if (cat && cat.dbId) params.categoryId = cat.dbId
    }

    const response = await blogService.posts.getList(params)
    if (response.success && response.data) {
      const pageData = response.data
      const newPosts = transformPosts(pageData.content || [])
      posts.value = currentPage.value === 0 ? newPosts : [...posts.value, ...newPosts]
      totalPages.value = pageData.totalPages
    }
  } catch (error) {
    console.error('加载文章失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  const kw = searchQuery.value.trim()
  const nextQuery: any = { ...route.query }
  if (kw) nextQuery.keyword = kw
  else delete nextQuery.keyword
  delete nextQuery.tag
  currentPage.value = 0
  router.push({ name: 'AdminBlogForward', query: nextQuery })
}

const setActiveCategory = (categoryId: string) => {
  activeCategory.value = categoryId
  currentPage.value = 0
  loadPosts()
}

const switchTab = (tabId: string) => {
  activeTab.value = tabId
  currentPage.value = 0
  const nextQuery: any = { ...route.query }
  if (tabId === 'latest') delete nextQuery.tab
  else nextQuery.tab = tabId
  router.push({ name: 'AdminBlogForward', query: nextQuery })
}

const loadMorePosts = async () => {
  if (!canLoadMore.value) return
  currentPage.value++
  await loadPosts()
}

const handleLike = async (postId: number) => {
  const post = posts.value.find(p => p.id === postId)
  if (!post) return
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
    post.isLiked = !post.isLiked
    post.likes += post.isLiked ? 1 : -1
  }
}

const handleBookmark = async (postId: number) => {
  const post = posts.value.find(p => p.id === postId)
  if (!post) return
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
    post.isBookmarked = !post.isBookmarked
  }
}

const handleShare = (post: any) => {
  const url = `${window.location.origin}${postRouteBase}/${post.id}`
  navigator.clipboard.writeText(url).then(() => {
    alert('链接已复制到剪贴板')
  })
}

watch(
  () => [route.query.keyword, route.query.tag, route.query.tab],
  async () => {
    searchQuery.value = typeof route.query.keyword === 'string' ? route.query.keyword : ''
    activeTab.value = typeof route.query.tab === 'string' ? route.query.tab : 'latest'
    currentPage.value = 0
    await loadPosts()
  }
)

onMounted(async () => {
  await loadCategories()
  await loadPosts()
})
</script>

<style scoped>
.admin-blog {
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
  background: #fff;
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
  outline: none;
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
  border: none;
  cursor: pointer;
  transition: var(--transition-default, all 0.2s ease);
  box-shadow: 0 4px 12px rgba(58, 156, 255, 0.3);
}

.search-btn:hover {
  transform: scale(1.02);
  box-shadow: 0 6px 16px rgba(58, 156, 255, 0.35);
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

/* Category Nav */
.category-nav {
  margin-bottom: 32px;
}

.category-list {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding: 8px 0;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 18px;
  background: var(--card-bg);
  border-radius: 16px;
  border: 1px solid transparent;
  cursor: pointer;
  transition: var(--transition-default);
  white-space: nowrap;
}

.category-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

.category-item.active {
  background: linear-gradient(120deg, rgba(58, 156, 255, 0.15), rgba(168, 213, 255, 0.15));
  border-color: rgba(58, 156, 255, 0.25);
  color: var(--primary-color);
}

.category-icon {
  width: 20px;
  height: 20px;
  opacity: 0.7;
}

/* Main Grid */
.main-grid {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.posts-section {
  flex: 1;
  min-width: 0;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-dark);
}

.title-icon {
  width: 22px;
  height: 22px;
  opacity: 0.8;
}

.header-tabs {
  display: flex;
  gap: 10px;
}

.tab-btn {
  padding: 8px 14px;
  border-radius: 999px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  background: rgba(255, 255, 255, 0.75);
  cursor: pointer;
  color: var(--text-light);
}

.tab-btn.active {
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
  color: #fff;
  border-color: transparent;
}

.loading-state {
  padding: 36px 0;
  text-align: center;
  color: var(--text-muted);
}

.posts-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 24px;
}

.load-more {
  display: flex;
  justify-content: center;
  padding: 28px 0 40px;
}

.load-more-btn {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 12px 18px;
  border-radius: 999px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  background: rgba(255, 255, 255, 0.85);
  cursor: pointer;
}

.btn-icon {
  width: 18px;
  height: 18px;
  opacity: 0.7;
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
  .hero-banner {
    padding: 60px 16px 80px;
  }
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
}
</style>
