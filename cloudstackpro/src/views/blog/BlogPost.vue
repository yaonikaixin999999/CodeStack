<template>
  <div class="blog-post">
    <BlogHeader />
    
    <main class="main-content">
      <div class="content-container">
        <!-- 文章头部 -->
        <header class="post-header">
          <div class="breadcrumb">
            <router-link to="/" class="breadcrumb-link">首页</router-link>
            <img src="@/assets/blog/icons/chevron-right.svg" alt=">" class="breadcrumb-separator" />
            <router-link :to="`/category/${post.category}`" class="breadcrumb-link">{{ post.categoryName }}</router-link>
            <img src="@/assets/blog/icons/chevron-right.svg" alt=">" class="breadcrumb-separator" />
            <span class="breadcrumb-current">正文</span>
          </div>
          
          <h1 class="post-title">{{ post.title }}</h1>
          
          <div class="post-meta">
            <div class="author-info">
              <img :src="post.author.avatar" :alt="post.author.name" class="author-avatar" />
              <div class="author-details">
                <span class="author-name">{{ post.author.name }}</span>
                <span class="publish-info">
                  发布于 {{ formatDate(post.createdAt) }} · 阅读 {{ post.readTime }} 分钟
                </span>
              </div>
            </div>
            
            <div class="post-actions">
              <button class="action-btn" :class="{ active: isFollowing }" @click="toggleFollow">
                <img :src="isFollowing ? checkIcon : plusIcon" alt="关注" />
                <span>{{ isFollowing ? '已关注' : '关注' }}</span>
              </button>
            </div>
          </div>
          
          <div class="post-tags">
            <router-link 
              v-for="tag in post.tags" 
              :key="tag" 
              :to="`/blog/search?tag=${tag}`" 
              class="tag"
            >
              {{ tag }}
            </router-link>
          </div>
        </header>
        
        <!-- 封面图 -->
        <div class="post-cover" v-if="post.coverImage">
          <img :src="post.coverImage" :alt="post.title" />
        </div>
        
        <!-- 文章主体 -->
        <div class="post-layout">
          <!-- 左侧操作栏 -->
          <aside class="post-sidebar-left">
            <div class="action-panel">
              <button class="panel-btn" :class="{ active: post.isLiked }" @click="toggleLike">
                <img src="@/assets/blog/icons/heart.svg" alt="点赞" />
                <span>{{ post.likes }}</span>
              </button>
              <button class="panel-btn" :class="{ active: post.isBookmarked }" @click="toggleBookmark">
                <img src="@/assets/blog/icons/bookmark.svg" alt="收藏" />
                <span>{{ post.bookmarks }}</span>
              </button>
              <button class="panel-btn" @click="scrollToComments">
                <img src="@/assets/blog/icons/comment.svg" alt="评论" />
                <span>{{ post.comments }}</span>
              </button>
              <button class="panel-btn" @click="sharePost">
                <img src="@/assets/blog/icons/share.svg" alt="分享" />
              </button>
            </div>
          </aside>
          
          <!-- 文章内容 -->
          <article class="post-content">
            <div class="content-body" v-html="post.content"></div>
            
            <!-- 文章底部信息 -->
            <div class="post-footer">
              <div class="footer-stats">
                <span class="stat-item">
                  <img src="@/assets/blog/icons/eye.svg" alt="阅读" class="stat-icon" />
                  {{ post.views }} 阅读
                </span>
                <span class="stat-item">
                  <img src="@/assets/blog/icons/heart.svg" alt="点赞" class="stat-icon" />
                  {{ post.likes }} 点赞
                </span>
                <span class="stat-item">
                  <img src="@/assets/blog/icons/comment.svg" alt="评论" class="stat-icon" />
                  {{ post.comments }} 评论
                </span>
              </div>
              
              <div class="footer-actions">
                <button class="action-button" :class="{ active: post.isLiked }" @click="toggleLike">
                  <img src="@/assets/blog/icons/heart.svg" alt="点赞" />
                  <span>{{ post.isLiked ? '已点赞' : '点赞' }}</span>
                </button>
                <button class="action-button" :class="{ active: post.isBookmarked }" @click="toggleBookmark">
                  <img src="@/assets/blog/icons/bookmark.svg" alt="收藏" />
                  <span>{{ post.isBookmarked ? '已收藏' : '收藏' }}</span>
                </button>
                <button class="action-button" @click="sharePost">
                  <img src="@/assets/blog/icons/share.svg" alt="分享" />
                  <span>分享</span>
                </button>
              </div>
            </div>
            
            <!-- 作者卡片 -->
            <div class="author-card">
              <img :src="post.author.avatar" :alt="post.author.name" class="author-avatar-lg" />
              <div class="author-info-detail">
                <h3 class="author-name">{{ post.author.name }}</h3>
                <p class="author-bio">{{ post.author.bio }}</p>
                <div class="author-stats">
                  <span>{{ post.author.posts }} 篇文章</span>
                  <span>{{ post.author.followers }} 关注者</span>
                </div>
              </div>
              <button class="follow-btn" :class="{ following: isFollowing }" @click="toggleFollow">
                <img :src="isFollowing ? checkIcon : plusIcon" alt="关注" class="btn-icon" />
                <span>{{ isFollowing ? '已关注' : '关注' }}</span>
              </button>
            </div>
            
            <!-- 评论区 -->
            <section class="comments-section" id="comments">
              <h2 class="section-title">
                <img src="@/assets/blog/icons/comment.svg" alt="评论" class="title-icon" />
                评论 ({{ comments.length }})
              </h2>
              
              <!-- 评论输入框 -->
              <div class="comment-input">
                <img src="@/assets/blog/images/default-avatar.svg" alt="我的头像" class="my-avatar" />
                <div class="input-wrapper">
                  <textarea 
                    v-model="commentText" 
                    placeholder="写下你的评论..." 
                    rows="3"
                  ></textarea>
                  <div class="input-actions">
                    <button class="submit-btn" :disabled="!commentText.trim()" @click="submitComment">
                      发表评论
                    </button>
                  </div>
                </div>
              </div>
              
              <!-- 评论列表 -->
              <div class="comments-list">
                <div v-for="comment in comments" :key="comment.id" class="comment-item">
                  <img :src="comment.author.avatar" :alt="comment.author.name" class="comment-avatar" />
                  <div class="comment-content">
                    <div class="comment-header">
                      <span class="comment-author">{{ comment.author.name }}</span>
                      <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
                    </div>
                    <p class="comment-text">{{ comment.content }}</p>
                    <div class="comment-actions">
                      <button class="comment-action" @click="likeComment(comment.id)">
                        <img src="@/assets/blog/icons/heart.svg" alt="点赞" />
                        <span>{{ comment.likes }}</span>
                      </button>
                      <button class="comment-action" @click="replyComment(comment.id)">
                        <img src="@/assets/blog/icons/comment.svg" alt="回复" />
                        <span>回复</span>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </section>
          </article>
          
          <!-- 右侧目录 -->
          <aside class="post-sidebar-right">
            <div class="toc-panel">
              <h3 class="toc-title">
                <img src="@/assets/blog/icons/book.svg" alt="目录" class="toc-icon" />
                目录
              </h3>
              <ul class="toc-list">
                <li v-for="item in toc" :key="item.id" :class="['toc-item', `level-${item.level}`]">
                  <a :href="`#${item.id}`" class="toc-link">{{ item.text }}</a>
                </li>
              </ul>
            </div>
          </aside>
        </div>
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
import plusIcon from '@/assets/blog/icons/plus.svg'
import checkIcon from '@/assets/blog/icons/check.svg'

export default defineComponent({
  name: 'BlogPost',
  components: {
    BlogHeader,
    BlogFooter
  },
  setup() {
    const route = useRoute()
    
    const isFollowing = ref(false)
    const commentText = ref('')
    
    const post = ref({
      id: 1,
      title: 'Vue 3.0 Composition API 完全指南：从入门到精通',
      categoryName: '前端开发',
      category: 'frontend',
      coverImage: 'https://picsum.photos/1200/500?random=1',
      tags: ['Vue.js', 'JavaScript', '前端框架', 'Composition API'],
      author: {
        name: '技术小白',
        avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=1',
        bio: '专注前端技术分享，Vue.js 布道者，开源爱好者',
        posts: 128,
        followers: '2.5k'
      },
      createdAt: '2025-12-15',
      readTime: 15,
      views: 2356,
      likes: 186,
      bookmarks: 45,
      comments: 42,
      isLiked: false,
      isBookmarked: false,
      content: `
        <h2 id="intro">前言</h2>
        <p>Vue 3.0 带来了全新的 Composition API，这是一种基于函数的 API，它让我们能够更灵活地组织组件逻辑。在本文中，我们将深入探讨 Composition API 的核心概念和最佳实践。</p>
        
        <h2 id="why">为什么需要 Composition API？</h2>
        <p>在 Vue 2.x 中，我们使用选项式 API（Options API）来组织代码。虽然这种方式简单直观，但在大型组件中，相关的逻辑往往分散在不同的选项中，导致代码难以维护。</p>
        <p>Composition API 的出现就是为了解决这个问题，它允许我们将相关的逻辑集中在一起，形成可复用的组合函数。</p>
        
        <h2 id="setup">setup 函数</h2>
        <p>setup 函数是 Composition API 的入口点，它在组件创建之前执行，此时组件的 props 已经解析完毕，但组件实例尚未创建。</p>
        <pre><code class="language-javascript">
import { ref, onMounted } from 'vue'

export default {
  setup(props, context) {
    const count = ref(0)
    
    const increment = () => {
      count.value++
    }
    
    onMounted(() => {
      console.log('组件已挂载')
    })
    
    return {
      count,
      increment
    }
  }
}
        </code></pre>
        
        <h2 id="reactive">响应式系统</h2>
        <p>Vue 3 提供了两种创建响应式数据的方式：ref 和 reactive。</p>
        
        <h3 id="ref">ref</h3>
        <p>ref 用于创建一个响应式的引用值，它接收一个内部值并返回一个响应式且可变的 ref 对象。</p>
        
        <h3 id="reactive">reactive</h3>
        <p>reactive 用于创建一个响应式对象，它接收一个普通对象然后返回该普通对象的响应式代理。</p>
        
        <h2 id="hooks">生命周期钩子</h2>
        <p>Composition API 提供了与选项式 API 对应的生命周期钩子函数：</p>
        <ul>
          <li>onBeforeMount</li>
          <li>onMounted</li>
          <li>onBeforeUpdate</li>
          <li>onUpdated</li>
          <li>onBeforeUnmount</li>
          <li>onUnmounted</li>
        </ul>
        
        <h2 id="conclusion">总结</h2>
        <p>Composition API 为 Vue 开发者提供了更灵活、更强大的代码组织方式。虽然学习曲线可能稍陡，但掌握后将大大提高开发效率和代码质量。</p>
      `
    })
    
    const toc = ref([
      { id: 'intro', text: '前言', level: 1 },
      { id: 'why', text: '为什么需要 Composition API？', level: 1 },
      { id: 'setup', text: 'setup 函数', level: 1 },
      { id: 'reactive', text: '响应式系统', level: 1 },
      { id: 'ref', text: 'ref', level: 2 },
      { id: 'reactive', text: 'reactive', level: 2 },
      { id: 'hooks', text: '生命周期钩子', level: 1 },
      { id: 'conclusion', text: '总结', level: 1 }
    ])
    
    const comments = ref([
      {
        id: 1,
        author: {
          name: '前端小白',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=10'
        },
        content: '写得太好了！终于理解了 Composition API 的精髓，感谢分享！',
        createdAt: '2025-12-15',
        likes: 12
      },
      {
        id: 2,
        author: {
          name: 'Vue爱好者',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=11'
        },
        content: '请问 ref 和 reactive 在实际项目中该如何选择？有没有什么最佳实践？',
        createdAt: '2025-12-15',
        likes: 8
      },
      {
        id: 3,
        author: {
          name: '代码工匠',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=12'
        },
        content: '期待作者能继续分享更多关于 Vue 3 的高级用法，比如自定义指令和插件开发。',
        createdAt: '2025-12-14',
        likes: 5
      }
    ])
    
    const formatDate = (dateStr: string) => {
      const date = new Date(dateStr)
      const now = new Date()
      const diff = now.getTime() - date.getTime()
      const days = Math.floor(diff / (1000 * 60 * 60 * 24))
      
      if (days === 0) return '今天'
      if (days === 1) return '昨天'
      if (days < 7) return `${days}天前`
      
      return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
    }
    
    const toggleFollow = () => {
      isFollowing.value = !isFollowing.value
    }
    
    const toggleLike = () => {
      post.value.isLiked = !post.value.isLiked
      post.value.likes += post.value.isLiked ? 1 : -1
    }
    
    const toggleBookmark = () => {
      post.value.isBookmarked = !post.value.isBookmarked
      post.value.bookmarks += post.value.isBookmarked ? 1 : -1
    }
    
    const scrollToComments = () => {
      document.getElementById('comments')?.scrollIntoView({ behavior: 'smooth' })
    }
    
    const sharePost = () => {
      // 分享逻辑
      console.log('分享文章')
    }
    
    const submitComment = () => {
      if (commentText.value.trim()) {
        comments.value.unshift({
          id: Date.now(),
          author: {
            name: '我',
            avatar: ''
          },
          content: commentText.value,
          createdAt: new Date().toISOString(),
          likes: 0
        })
        commentText.value = ''
        post.value.comments++
      }
    }
    
    const likeComment = (commentId: number) => {
      const comment = comments.value.find(c => c.id === commentId)
      if (comment) {
        comment.likes++
      }
    }
    
    const replyComment = (commentId: number) => {
      console.log('回复评论:', commentId)
    }
    
    return {
      post,
      toc,
      comments,
      isFollowing,
      commentText,
      plusIcon,
      checkIcon,
      formatDate,
      toggleFollow,
      toggleLike,
      toggleBookmark,
      scrollToComments,
      sharePost,
      submitComment,
      likeComment,
      replyComment
    }
  }
})
</script>

<style scoped>
.blog-post {
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

/* Breadcrumb */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
}

.breadcrumb-link {
  font-size: 14px;
  color: var(--text-muted);
}

.breadcrumb-link:hover {
  color: var(--primary-color);
}

.breadcrumb-separator {
  width: 14px;
  height: 14px;
  opacity: 0.4;
}

.breadcrumb-current {
  font-size: 14px;
  color: var(--text-light);
}

/* Post Header */
.post-header {
  margin-bottom: 32px;
}

.post-title {
  font-size: 36px;
  font-weight: 700;
  color: var(--text-dark);
  line-height: 1.4;
  margin-bottom: 24px;
}

.post-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 14px;
}

.author-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: 2px solid var(--border-light);
}

.author-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-dark);
}

.publish-info {
  font-size: 13px;
  color: var(--text-muted);
}

.post-actions .action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: var(--border-light);
  border-radius: 20px;
  font-size: 13px;
  color: var(--text-light);
  transition: var(--transition-default);
}

.post-actions .action-btn:hover {
  background: linear-gradient(120deg, rgba(58, 156, 255, 0.1), rgba(168, 213, 255, 0.2));
  color: var(--primary-color);
}

.post-actions .action-btn.active {
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
  color: white;
}

.post-actions .action-btn img {
  width: 16px;
  height: 16px;
}

.post-actions .action-btn.active img {
  filter: brightness(0) invert(1);
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.post-tags .tag {
  padding: 6px 14px;
  background: linear-gradient(120deg, rgba(58, 156, 255, 0.08), rgba(168, 213, 255, 0.15));
  color: var(--primary-color);
  font-size: 13px;
  border-radius: 16px;
  transition: var(--transition-default);
}

.post-tags .tag:hover {
  background: linear-gradient(120deg, rgba(58, 156, 255, 0.15), rgba(168, 213, 255, 0.25));
}

/* Post Cover */
.post-cover {
  margin-bottom: 32px;
  border-radius: var(--border-radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-md);
}

.post-cover img {
  width: 100%;
  height: auto;
  display: block;
}

/* Post Layout */
.post-layout {
  display: flex;
  gap: 32px;
}

/* Left Sidebar */
.post-sidebar-left {
  width: 60px;
  flex-shrink: 0;
  position: sticky;
  top: 96px;
  height: fit-content;
}

.action-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 16px 8px;
  background: white;
  border-radius: 30px;
  box-shadow: var(--shadow-sm);
}

.panel-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px;
  background: transparent;
  border-radius: 12px;
  transition: var(--transition-default);
}

.panel-btn:hover {
  background: var(--border-light);
}

.panel-btn img {
  width: 22px;
  height: 22px;
  opacity: 0.6;
}

.panel-btn span {
  font-size: 11px;
  color: var(--text-muted);
}

.panel-btn.active {
  background: linear-gradient(120deg, rgba(58, 156, 255, 0.1), rgba(168, 213, 255, 0.2));
}

.panel-btn.active img {
  opacity: 1;
  filter: invert(48%) sepia(91%) saturate(1417%) hue-rotate(196deg) brightness(101%) contrast(102%);
}

.panel-btn.active span {
  color: var(--primary-color);
}

/* Post Content */
.post-content {
  flex: 1;
  min-width: 0;
  background: white;
  border-radius: var(--border-radius-md);
  padding: 40px;
  box-shadow: var(--shadow-sm);
}

.content-body {
  font-size: 16px;
  line-height: 1.8;
  color: var(--text-dark);
}

.content-body :deep(h2) {
  font-size: 24px;
  font-weight: 600;
  margin: 32px 0 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-light);
}

.content-body :deep(h3) {
  font-size: 20px;
  font-weight: 600;
  margin: 24px 0 12px;
}

.content-body :deep(p) {
  margin: 16px 0;
}

.content-body :deep(ul), .content-body :deep(ol) {
  margin: 16px 0;
  padding-left: 24px;
}

.content-body :deep(li) {
  margin: 8px 0;
}

.content-body :deep(pre) {
  margin: 20px 0;
  padding: 20px;
  background: #1e1e1e;
  border-radius: 8px;
  overflow-x: auto;
}

.content-body :deep(code) {
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 14px;
  color: #d4d4d4;
}

/* Post Footer */
.post-footer {
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid var(--border-light);
}

.footer-stats {
  display: flex;
  gap: 24px;
  margin-bottom: 20px;
}

.stat-item {
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

.footer-actions {
  display: flex;
  gap: 12px;
}

.action-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: var(--border-light);
  border-radius: 24px;
  font-size: 14px;
  color: var(--text-light);
  transition: var(--transition-default);
}

.action-button:hover {
  background: linear-gradient(120deg, rgba(58, 156, 255, 0.1), rgba(168, 213, 255, 0.2));
  color: var(--primary-color);
}

.action-button.active {
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
  color: white;
}

.action-button img {
  width: 18px;
  height: 18px;
  opacity: 0.6;
}

.action-button.active img {
  filter: brightness(0) invert(1);
  opacity: 1;
}

/* Author Card */
.author-card {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-top: 32px;
  padding: 24px;
  background: var(--border-light);
  border-radius: var(--border-radius-md);
}

.author-avatar-lg {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  border: 3px solid white;
}

.author-info-detail {
  flex: 1;
}

.author-info-detail .author-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-dark);
  margin-bottom: 6px;
}

.author-bio {
  font-size: 14px;
  color: var(--text-light);
  margin-bottom: 8px;
}

.author-stats {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: var(--text-muted);
}

.follow-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 24px;
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
  color: white;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 500;
  transition: var(--transition-default);
}

.follow-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(58, 156, 255, 0.3);
}

.follow-btn.following {
  background: var(--border-light);
  color: var(--text-light);
}

.follow-btn .btn-icon {
  width: 16px;
  height: 16px;
  filter: brightness(0) invert(1);
}

.follow-btn.following .btn-icon {
  filter: none;
  opacity: 0.6;
}

/* Comments Section */
.comments-section {
  margin-top: 40px;
  padding-top: 32px;
  border-top: 1px solid var(--border-light);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-dark);
  margin-bottom: 24px;
}

.title-icon {
  width: 22px;
  height: 22px;
  opacity: 0.7;
}

.comment-input {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
}

.my-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  flex-shrink: 0;
}

.input-wrapper {
  flex: 1;
}

.input-wrapper textarea {
  width: 100%;
  padding: 16px;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  font-size: 14px;
  resize: none;
  transition: var(--transition-default);
}

.input-wrapper textarea:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(58, 156, 255, 0.1);
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.submit-btn {
  padding: 10px 24px;
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
  color: white;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  transition: var(--transition-default);
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(58, 156, 255, 0.3);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.comment-item {
  display: flex;
  gap: 16px;
}

.comment-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  flex-shrink: 0;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.comment-author {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-dark);
}

.comment-date {
  font-size: 12px;
  color: var(--text-muted);
}

.comment-text {
  font-size: 14px;
  color: var(--text-light);
  line-height: 1.6;
  margin-bottom: 12px;
}

.comment-actions {
  display: flex;
  gap: 16px;
}

.comment-action {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background: transparent;
  border-radius: 4px;
  font-size: 12px;
  color: var(--text-muted);
  transition: var(--transition-default);
}

.comment-action:hover {
  background: var(--border-light);
  color: var(--primary-color);
}

.comment-action img {
  width: 14px;
  height: 14px;
  opacity: 0.5;
}

/* Right Sidebar */
.post-sidebar-right {
  width: 240px;
  flex-shrink: 0;
  position: sticky;
  top: 96px;
  height: fit-content;
}

.toc-panel {
  background: white;
  border-radius: var(--border-radius-md);
  padding: 20px;
  box-shadow: var(--shadow-sm);
}

.toc-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-dark);
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-light);
}

.toc-icon {
  width: 18px;
  height: 18px;
  opacity: 0.6;
}

.toc-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.toc-item {
  font-size: 13px;
}

.toc-item.level-2 {
  padding-left: 16px;
}

.toc-link {
  display: block;
  padding: 6px 10px;
  color: var(--text-light);
  border-radius: 6px;
  transition: var(--transition-default);
}

.toc-link:hover {
  background: var(--border-light);
  color: var(--primary-color);
}

@media (max-width: 1024px) {
  .post-sidebar-left,
  .post-sidebar-right {
    display: none;
  }
  
  .post-content {
    padding: 24px;
  }
}

@media (max-width: 768px) {
  .post-title {
    font-size: 24px;
  }
  
  .post-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .footer-actions {
    flex-wrap: wrap;
  }
  
  .author-card {
    flex-direction: column;
    text-align: center;
  }
  
  .comment-input {
    flex-direction: column;
  }
  
  .my-avatar {
    width: 40px;
    height: 40px;
  }
}
</style>
