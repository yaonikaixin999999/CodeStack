<template>
  <article class="post-card" :class="{ 'featured': featured }">
    <router-link :to="`/blog/post/${post.id}`" class="card-image-wrapper" v-if="post.coverImage">
      <div class="card-image">
        <img :src="post.coverImage" :alt="post.title" />
        <div class="image-overlay">
          <span class="category-badge">{{ post.category }}</span>
        </div>
      </div>
    </router-link>
    
    <div class="card-content">
      <div class="card-meta">
        <div class="author-info">
          <img :src="post.author.avatar" :alt="post.author.name" class="author-avatar" />
          <span class="author-name">{{ post.author.name }}</span>
        </div>
        <span class="post-date">
          <img src="@/assets/blog/icons/clock.svg" alt="时间" class="meta-icon" />
          {{ formatDate(post.createdAt) }}
        </span>
      </div>
      
      <router-link :to="`${postRouteBase}/${post.id}`" class="card-title">
        {{ post.title }}
      </router-link>
      
      <p class="card-excerpt">{{ post.excerpt }}</p>
      
      <div class="card-tags">
        <span v-for="tag in post.tags.slice(0, 3)" :key="tag" class="tag">
          {{ tag }}
        </span>
      </div>
      
      <div class="card-footer">
        <div class="card-stats">
          <span class="stat-item">
            <img src="@/assets/blog/icons/eye.svg" alt="阅读" class="stat-icon" />
            {{ formatNumber(post.views) }}
          </span>
          <span class="stat-item">
            <img src="@/assets/blog/icons/heart.svg" alt="点赞" class="stat-icon" />
            {{ formatNumber(post.likes) }}
          </span>
          <span class="stat-item">
            <img src="@/assets/blog/icons/comment.svg" alt="评论" class="stat-icon" />
            {{ formatNumber(post.comments) }}
          </span>
        </div>
        
        <div class="card-actions">
          <button v-if="canDelete" class="action-btn delete" @click="deletePost">
            <img src="@/assets/blog/icons/close.svg" alt="删除" />
          </button>
          <button class="action-btn" :class="{ 'liked': post.isLiked }" @click="toggleLike">
            <img src="@/assets/blog/icons/heart.svg" alt="点赞" />
          </button>
          <button class="action-btn" :class="{ 'bookmarked': post.isBookmarked }" @click="toggleBookmark">
            <img src="@/assets/blog/icons/bookmark.svg" alt="收藏" />
          </button>
          <button class="action-btn" @click="sharePost">
            <img src="@/assets/blog/icons/share.svg" alt="分享" />
          </button>
        </div>
      </div>
    </div>
  </article>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'

interface Author {
  name: string
  avatar: string
}

interface Post {
  id: number
  title: string
  excerpt: string
  coverImage?: string
  category: string
  tags: string[]
  author: Author
  createdAt: string
  views: number
  likes: number
  comments: number
  isLiked: boolean
  isBookmarked: boolean
}

export default defineComponent({
  name: 'PostCard',
  props: {
    post: {
      type: Object as PropType<Post>,
      required: true
    },
    featured: {
      type: Boolean,
      default: false
    },
    postRouteBase: {
      type: String,
      default: '/blog/post'
    },
    canDelete: {
      type: Boolean,
      default: false
    }
  },
  emits: ['like', 'bookmark', 'share', 'delete'],
  setup(props, { emit }) {
    const formatDate = (dateStr: string) => {
      const date = new Date(dateStr)
      const now = new Date()
      const diff = now.getTime() - date.getTime()
      const days = Math.floor(diff / (1000 * 60 * 60 * 24))
      
      if (days === 0) return '今天'
      if (days === 1) return '昨天'
      if (days < 7) return `${days}天前`
      if (days < 30) return `${Math.floor(days / 7)}周前`
      if (days < 365) return `${Math.floor(days / 30)}个月前`
      return `${Math.floor(days / 365)}年前`
    }
    
    const formatNumber = (num: number) => {
      if (num >= 10000) return `${(num / 10000).toFixed(1)}w`
      if (num >= 1000) return `${(num / 1000).toFixed(1)}k`
      return num.toString()
    }
    
    const toggleLike = () => {
      emit('like', props.post.id)
    }
    
    const toggleBookmark = () => {
      emit('bookmark', props.post.id)
    }
    
    const sharePost = () => {
      emit('share', props.post)
    }

    const deletePost = () => {
      emit('delete', props.post.id)
    }
    
    return {
      formatDate,
      formatNumber,
      toggleLike,
      toggleBookmark,
      sharePost,
      deletePost
    }
  }
})
</script>

<style scoped>
.post-card {
  background: var(--card-bg);
  border-radius: var(--border-radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  transition: var(--transition-default);
}

.post-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-4px);
}

.post-card.featured {
  grid-column: span 2;
}

.card-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.post-card.featured .card-image {
  height: 280px;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.post-card:hover .card-image img {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, rgba(0,0,0,0.1) 0%, rgba(0,0,0,0.5) 100%);
  display: flex;
  align-items: flex-start;
  padding: 16px;
}

.category-badge {
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.95);
  color: var(--primary-color);
  font-size: 12px;
  font-weight: 500;
  border-radius: 20px;
}

.card-content {
  padding: 20px;
}

.card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--border-light);
}

.author-name {
  font-size: 13px;
  color: var(--text-light);
  font-weight: 500;
}

.post-date {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-muted);
}

.meta-icon {
  width: 14px;
  height: 14px;
  opacity: 0.5;
}

.card-title {
  display: block;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-dark);
  margin-bottom: 12px;
  line-height: 1.5;
  transition: var(--transition-default);
}

.post-card.featured .card-title {
  font-size: 22px;
}

.card-title:hover {
  color: var(--primary-color);
}

.card-excerpt {
  font-size: 14px;
  color: var(--text-light);
  line-height: 1.7;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-card.featured .card-excerpt {
  -webkit-line-clamp: 3;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.tag {
  padding: 4px 12px;
  background: linear-gradient(120deg, rgba(58, 156, 255, 0.08), rgba(168, 213, 255, 0.15));
  color: var(--primary-color);
  font-size: 12px;
  border-radius: 16px;
  transition: var(--transition-default);
}

.tag:hover {
  background: linear-gradient(120deg, rgba(58, 156, 255, 0.15), rgba(168, 213, 255, 0.25));
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 16px;
  border-top: 1px solid var(--border-light);
}

.card-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-muted);
}

.stat-icon {
  width: 14px;
  height: 14px;
  opacity: 0.5;
}

.card-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border-radius: 50%;
  transition: var(--transition-default);
}

.action-btn img {
  width: 16px;
  height: 16px;
  opacity: 0.5;
}

.action-btn:hover {
  background: var(--border-light);
}

.action-btn:hover img {
  opacity: 0.8;
}

.action-btn.liked,
.action-btn.bookmarked {
  background: rgba(58, 156, 255, 0.1);
}

.action-btn.liked img,
.action-btn.bookmarked img {
  opacity: 1;
  filter: invert(48%) sepia(91%) saturate(1417%) hue-rotate(196deg) brightness(101%) contrast(102%);
}

.action-btn.delete {
  background: rgba(255, 77, 79, 0.1);
}

.action-btn.delete img {
  opacity: 1;
  filter: invert(39%) sepia(78%) saturate(2456%) hue-rotate(333deg) brightness(103%) contrast(103%);
}

@media (max-width: 768px) {
  .post-card.featured {
    grid-column: span 1;
  }
  
  .card-image,
  .post-card.featured .card-image {
    height: 180px;
  }
  
  .card-title,
  .post-card.featured .card-title {
    font-size: 16px;
  }
}
</style>
