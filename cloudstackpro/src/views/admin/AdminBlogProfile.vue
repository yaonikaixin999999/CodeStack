<template>
  <div class="admin-blog-profile">
    <main class="main-content">
      <div class="profile-topbar">
        <button class="back-btn" @click="goBack">
          <img src="@/assets/blog/icons/chevron-left.svg" alt="返回" class="back-icon" />
          返回
        </button>
      </div>
      <header class="profile-header">
        <div class="header-bg">
          <div class="bg-pattern"></div>
        </div>
        <div class="header-content">
          <div class="user-avatar-wrapper">
            <img :src="userInfo.avatar" :alt="userInfo.name" class="user-avatar" />
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
          <div class="user-actions" v-if="isViewingUserById">
            <button class="action-btn primary" :class="{ followed: isFollowed }" @click="toggleFollow">
              <img :src="isFollowed ? checkIcon : plusIcon" alt="" class="btn-icon" />
              {{ isFollowed ? '已关注' : '关注' }}
            </button>
            <button class="action-btn" :disabled="isOwner">
              <img src="@/assets/blog/icons/comment.svg" alt="私信" class="btn-icon" />
              私信
            </button>
          </div>
        </div>
      </header>
      
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
      
      <div class="content-container">
        <div class="main-grid">
          <section class="posts-section">
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
            
            <div class="posts-list" v-if="activeTab === 'posts'">
              <article v-for="post in userPosts" :key="post.id" class="post-item">
                <router-link :to="`/admin/blog/post/${post.id}`" class="post-content">
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
            
            <div class="bookmarks-list" v-if="activeTab === 'bookmarks'">
              <article v-for="post in bookmarks" :key="post.id" class="post-item">
                <router-link :to="`/admin/blog/post/${post.id}`" class="post-content">
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
          
          <aside class="profile-sidebar">
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
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { blogService } from '@/services/blogService'

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
  name: 'AdminBlogProfile',
  setup() {
    const route = useRoute()
    const router = useRouter()
    
    const loading = ref(true)
    const isOwner = ref(false)
    const isFollowed = ref(false)
    const activeTab = ref('posts')
    const currentUser = ref(blogService.auth.getLocalUser())
    const isViewingUserById = ref(!!route.params.id)
    
    const makeDefaultUserInfo = (seed: string, id?: number) => ({
      id: id || 0,
      avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=${seed}`,
      name: id ? `用户 #${id}` : '用户',
      level: 1,
      badges: ['新手作者'],
      bio: '这个人很懒，什么都没有留下。',
      profession: '开发者',
      location: '未知',
      joinDate: '未知',
      stats: {
        posts: 0,
        followers: '0',
        following: 0,
        likes: '0',
        views: '0'
      }
    })

    const getProfileSeed = () => {
      const routeId = route.params.id ? String(route.params.id) : ''
      return routeId || String(currentUser.value?.id || 'profile')
    }

    const userInfo = ref(makeDefaultUserInfo(getProfileSeed()))
    
    const contentTabs = ref([
      { id: 'posts', name: '文章', count: 0, icon: bookIcon },
      { id: 'bookmarks', name: '收藏', count: 0, icon: bookmarkIcon },
      { id: 'following', name: '关注', count: 0, icon: userIcon },
      { id: 'followers', name: '粉丝', count: 0, icon: heartIcon }
    ])
    
    const userPosts = ref<any[]>([])
    const bookmarks = ref<any[]>([])
    const following = ref<any[]>([])
    const followers = ref<any[]>([])
    
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
    
    const loadUserProfile = async () => {
      const userId = route.params.id as string
      loading.value = true
      
      try {
        if (userId) {
          isOwner.value = currentUser.value?.id === Number(userId)
          
          const response = await blogService.users.getById(Number(userId))
          if (response.success && response.data) {
            const user = response.data
            updateUserInfo(user)
          }
        } else if (currentUser.value) {
          isOwner.value = true
          
          const response = await blogService.auth.getCurrentUser()
          if (response.success && response.data) {
            updateUserInfo(response.data)
          } else {
            updateUserInfo(currentUser.value)
          }
        }
      } catch (error) {
        console.error('加载用户信息失败:', error)
        if (userId) {
          userInfo.value = makeDefaultUserInfo(String(userId), Number(userId))
        } else if (currentUser.value && !route.params.id) {
          updateUserInfo(currentUser.value)
        } else {
          userInfo.value = makeDefaultUserInfo(getProfileSeed())
        }
      } finally {
        loading.value = false
      }
    }
    
    const updateUserInfo = (user: any) => {
      const likeCount = user.likeCount ?? user.likeReceivedCount ?? 0
      userInfo.value = {
        id: user.id,
        avatar: user.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${user.id}`,
        name: user.nickname || user.username || '用户',
        level: Math.min(Math.floor((user.postCount || 0) / 10) + 1, 10),
        badges: getBadges(user),
        bio: user.bio || '这个人很懒，什么都没有留下。',
        profession: user.profession || '开发者',
        location: user.location || '未知',
        joinDate: formatJoinDate(user.createdAt),
        stats: {
          posts: user.postCount || 0,
          followers: formatCount(user.followerCount || 0),
          following: user.followingCount || 0,
          likes: formatCount(likeCount),
          views: formatCount(user.viewCount || 0)
        }
      }
      
      contentTabs.value[0].count = user.postCount || 0
      contentTabs.value[1].count = user.bookmarkCount || 0
      contentTabs.value[2].count = user.followingCount || 0
      contentTabs.value[3].count = user.followerCount || 0
    }
    
    const getBadges = (user: any) => {
      const badges: string[] = []
      if ((user.postCount || 0) >= 10) badges.push('优秀作者')
      if ((user.followerCount || 0) >= 100) badges.push('人气作者')
      const likeCount = user.likeCount ?? user.likeReceivedCount ?? 0
      if (likeCount >= 500) badges.push('技术达人')
      return badges.length > 0 ? badges : ['新手作者']
    }
    
    const formatJoinDate = (dateStr: string) => {
      if (!dateStr) return '未知'
      const date = new Date(dateStr)
      return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}`
    }
    
    const formatCount = (count: number): string => {
      if (count >= 10000) {
        return (count / 10000).toFixed(1) + 'w'
      }
      if (count >= 1000) {
        return (count / 1000).toFixed(1) + 'k'
      }
      return count.toString()
    }
    
    const loadUserPosts = async () => {
      try {
        const userId = userInfo.value.id || currentUser.value?.id
        if (!userId) return
        
        const response = await blogService.posts.getList({
          authorId: userId,
          page: 0,
          size: 100
        })
        
        if (response.success && response.data) {
          userPosts.value = response.data.content.map(post => ({
            id: post.id,
            title: post.title,
            excerpt: post.excerpt || '',
            coverImage: post.coverImage || '',
            createdAt: formatDate(post.publishedAt || post.createdAt || ''),
            views: formatCount(post.viewCount || 0),
            likes: post.likeCount || 0,
            comments: post.commentCount || 0
          }))
        }
      } catch (error) {
        console.error('加载用户文章失败:', error)
        userPosts.value = getDefaultPosts()
      }
    }
    
    const loadBookmarks = async () => {
      try {
        const response = await blogService.bookmarks.getList()
        if (response.success && response.data) {
          bookmarks.value = response.data.content.map((item: any) => ({
            id: item.post?.id,
            title: item.post?.title || '',
            excerpt: item.post?.excerpt || '',
            coverImage: item.post?.coverImage || '',
            author: {
              name: item.post?.author?.nickname || item.post?.author?.username || '匿名',
              avatar: item.post?.author?.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${item.post?.author?.id || 1}`
            },
            bookmarkedAt: formatDate(item.createdAt || '')
          }))
        }
      } catch (error) {
        console.error('加载收藏列表失败:', error)
      }
    }
    
    const loadFollowing = async () => {
      try {
        const userId = userInfo.value.id || currentUser.value?.id
        if (!userId) return
        
        const response = await blogService.users.getFollowing(userId)
        if (response.success && response.data) {
          following.value = response.data.content.map(user => ({
            id: user.id,
            name: user.nickname || user.username || '用户',
            avatar: user.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${user.id}`,
            bio: user.bio || '这个人很懒',
            followers: formatCount(user.followerCount || 0),
            isFollowed: true
          }))
        }
      } catch (error) {
        console.error('加载关注列表失败:', error)
      }
    }
    
    const loadFollowers = async () => {
      try {
        const userId = userInfo.value.id || currentUser.value?.id
        if (!userId) return
        
        const response = await blogService.users.getFollowers(userId)
        if (response.success && response.data) {
          followers.value = response.data.content.map(user => ({
            id: user.id,
            name: user.nickname || user.username || '用户',
            avatar: user.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${user.id}`,
            bio: user.bio || '这个人很懒',
            followers: formatCount(user.followerCount || 0),
            isFollowed: user.followed || false
          }))
        }
      } catch (error) {
        console.error('加载粉丝列表失败:', error)
      }
    }
    
    const formatDate = (dateStr: string) => {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
    }
    
    const getDefaultPosts = () => [
      {
        id: 1,
        title: 'Vue 3.0 Composition API 完全指南',
        excerpt: '深入探讨 Vue 3.0 的 Composition API。',
        coverImage: 'https://picsum.photos/300/200?random=40',
        createdAt: '2025-12-15',
        views: '2.3k',
        likes: 186,
        comments: 42
      }
    ]
    
    const toggleFollow = async () => {
      try {
        if (isFollowed.value) {
          await blogService.users.unfollow(userInfo.value.id)
          isFollowed.value = false
        } else {
          await blogService.users.follow(userInfo.value.id)
          isFollowed.value = true
        }
      } catch (error) {
        console.error('关注操作失败:', error)
        isFollowed.value = !isFollowed.value
      }
    }
    
    watch(activeTab, async (newTab) => {
      switch (newTab) {
        case 'posts':
          if (userPosts.value.length === 0) await loadUserPosts()
          break
        case 'bookmarks':
          if (bookmarks.value.length === 0) await loadBookmarks()
          break
        case 'following':
          if (following.value.length === 0) await loadFollowing()
          break
        case 'followers':
          if (followers.value.length === 0) await loadFollowers()
          break
      }
    })

    watch(
      () => route.params.id,
      async () => {
        isViewingUserById.value = !!route.params.id
        userInfo.value = makeDefaultUserInfo(getProfileSeed())
        activeTab.value = 'posts'
        userPosts.value = []
        bookmarks.value = []
        following.value = []
        followers.value = []
        await loadUserProfile()
        await loadUserPosts()
      }
    )
    
    onMounted(async () => {
      await loadUserProfile()
      await loadUserPosts()
    })

    const goBack = () => {
      if (window.history.length > 1) {
        router.back()
        return
      }
      if (route.query.from === 'moderation') {
        router.push('/admin/moderation')
        return
      }
      router.push('/admin/users')
    }
    
    return {
      loading,
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
      checkIcon,
      goBack,
      isViewingUserById
    }
  }
})
</script>

<style scoped>
.admin-blog-profile {
  min-height: 100vh;
  background: var(--background-gradient);
}

.main-content {
  padding-top: 0;
}

.content-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.profile-topbar {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.9);
  border-bottom: 1px solid rgba(226, 232, 240, 0.7);
  backdrop-filter: blur(10px);
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: 12px;
  background: #ffffff;
  border: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: var(--shadow-sm);
  color: var(--text-dark);
  cursor: pointer;
}

.back-icon {
  width: 16px;
  height: 16px;
  opacity: 0.6;
}

.user-actions .action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.profile-header {
  position: relative;
  margin-bottom: 24px;
}

.header-bg {
  height: 100px;
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

.main-grid {
  display: flex;
  gap: 32px;
  padding-bottom: 48px;
}

.posts-section {
  flex: 1;
  min-width: 0;
}

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
