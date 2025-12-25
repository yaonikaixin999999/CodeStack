<template>
  <header class="blog-header">
    <div class="header-container">
      <div class="header-left">
        <router-link to="/blog" class="logo">
          <span class="logo-text">CloudStack</span>
          <span class="logo-badge">Blog</span>
        </router-link>
        <nav class="header-nav">
          <router-link to="/blog" class="nav-link" :class="{ active: isActive('/') }">
            <img src="@/assets/blog/icons/home.svg" alt="首页" class="nav-icon" />
            <span>首页</span>
          </router-link>
          <router-link to="/blog/category/tech" class="nav-link" :class="{ active: isActive('/category') }">
            <img src="@/assets/blog/icons/category.svg" alt="分类" class="nav-icon" />
            <span>分类</span>
          </router-link>
          <router-link to="/blog/search" class="nav-link" :class="{ active: isActive('/search') }">
            <img src="@/assets/blog/icons/trending.svg" alt="热门" class="nav-icon" />
            <span>热门</span>
          </router-link>
        </nav>
      </div>
      
      <div class="header-center">
        <div class="search-box">
          <img src="@/assets/blog/icons/search.svg" alt="搜索" class="search-icon" />
          <input 
            type="text" 
            placeholder="搜索文章、标签或作者..." 
            v-model="searchQuery"
            @keyup.enter="handleSearch"
          />
        </div>
      </div>
      
      <div class="header-right">
        <router-link to="/blog/write" class="write-btn">
          <img src="@/assets/blog/icons/edit.svg" alt="写文章" class="btn-icon" />
          <span>写文章</span>
        </router-link>
        
        <div class="user-actions">
          <button class="action-btn notification-btn" @click="$router.push('/blog/messages')">
            <img src="@/assets/blog/icons/comment.svg" alt="消息" class="action-icon" />
          </button>
          
          <div class="user-menu" @click="toggleUserMenu">
            <div class="user-avatar">
              <img :src="userAvatar" :alt="displayName" />
            </div>
            <img src="@/assets/blog/icons/chevron-down.svg" alt="展开" class="chevron-icon" />
            
            <div v-if="showUserMenu" class="user-dropdown">
              <div class="dropdown-user-info">
                <span class="user-name">{{ displayName }}</span>
              </div>
              <div class="dropdown-divider"></div>
              <router-link to="/blog/profile" class="dropdown-item">
                <img src="@/assets/blog/icons/user.svg" alt="个人中心" class="dropdown-icon" />
                <span>个人中心</span>
              </router-link>
              <router-link to="/blog/profile" class="dropdown-item">
                <img src="@/assets/blog/icons/bookmark.svg" alt="我的收藏" class="dropdown-icon" />
                <span>我的收藏</span>
              </router-link>
              <router-link to="/blog/profile" class="dropdown-item">
                <img src="@/assets/blog/icons/settings.svg" alt="设置" class="dropdown-icon" />
                <span>设置</span>
              </router-link>
              <div class="dropdown-divider"></div>
              <button class="dropdown-item logout-btn" @click="goBackToEditor">
                <img src="@/assets/blog/icons/home.svg" alt="返回" class="dropdown-icon" />
                <span>返回首页</span>
              </button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 移动端菜单按钮 -->
      <button class="mobile-menu-btn" @click="toggleMobileMenu">
        <img src="@/assets/blog/icons/menu.svg" alt="菜单" />
      </button>
    </div>
    
    <!-- 移动端菜单 -->
    <div v-if="showMobileMenu" class="mobile-menu">
      <router-link to="/blog" class="mobile-nav-link">首页</router-link>
      <router-link to="/blog/category/tech" class="mobile-nav-link">分类</router-link>
      <router-link to="/blog/search" class="mobile-nav-link">热门</router-link>
      <router-link to="/blog/write" class="mobile-nav-link">写文章</router-link>
      <router-link to="/blog/profile" class="mobile-nav-link">个人中心</router-link>
    </div>
  </header>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { blogService, type User } from '@/services/blogService'

export default defineComponent({
  name: 'BlogHeader',
  setup() {
    const router = useRouter()
    const route = useRoute()
    
    const searchQuery = ref('')
    const showUserMenu = ref(false)
    const showMobileMenu = ref(false)
    const currentUser = ref<User | null>(null)
    
    // 获取用户信息
    const loadUserInfo = () => {
      currentUser.value = blogService.auth.getLocalUser()
    }
    
    // 用户头像
    const userAvatar = computed(() => {
      if (currentUser.value?.avatar) {
        return currentUser.value.avatar
      }
      return `https://api.dicebear.com/7.x/avataaars/svg?seed=${currentUser.value?.username || 'default'}`
    })
    
    // 用户名
    const displayName = computed(() => {
      return currentUser.value?.nickname || currentUser.value?.username || '用户'
    })
    
    const isActive = (path: string) => {
      return route.path === path || route.path.startsWith(path + '/')
    }
    
    const handleSearch = () => {
      if (searchQuery.value.trim()) {
        router.push({ path: '/blog/search', query: { q: searchQuery.value } })
      }
    }
    
    const toggleUserMenu = () => {
      showUserMenu.value = !showUserMenu.value
    }
    
    const toggleMobileMenu = () => {
      showMobileMenu.value = !showMobileMenu.value
    }
    
    // 返回编辑器首页
    const goBackToEditor = () => {
      router.push('/editor')
    }
    
    onMounted(() => {
      loadUserInfo()
    })
    
    return {
      searchQuery,
      showUserMenu,
      showMobileMenu,
      currentUser,
      userAvatar,
      displayName,
      isActive,
      handleSearch,
      toggleUserMenu,
      toggleMobileMenu,
      goBackToEditor
    }
  }
})
</script>

<style scoped>
.blog-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--border-color);
  z-index: 1000;
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 32px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.logo-badge {
  font-size: 12px;
  font-weight: 500;
  padding: 2px 8px;
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
  color: white;
  border-radius: 12px;
}

.header-nav {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  color: var(--text-light);
  font-size: 14px;
  font-weight: 500;
  transition: var(--transition-default);
}

.nav-link:hover {
  background: rgba(58, 156, 255, 0.08);
  color: var(--primary-color);
}

.nav-link.active {
  background: rgba(58, 156, 255, 0.12);
  color: var(--primary-color);
}

.nav-icon {
  width: 18px;
  height: 18px;
  opacity: 0.7;
}

.nav-link:hover .nav-icon,
.nav-link.active .nav-icon {
  opacity: 1;
}

.header-center {
  flex: 1;
  max-width: 480px;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  background: var(--border-light);
  border-radius: 24px;
  transition: var(--transition-default);
}

.search-box:focus-within {
  background: white;
  box-shadow: 0 0 0 2px rgba(58, 156, 255, 0.2);
}

.search-icon {
  width: 18px;
  height: 18px;
  opacity: 0.5;
}

.search-box input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 14px;
  color: var(--text-dark);
}

.search-box input::placeholder {
  color: var(--text-muted);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.write-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
  color: white;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 500;
  transition: var(--transition-default);
}

.write-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(58, 156, 255, 0.3);
  color: white;
}

.btn-icon {
  width: 16px;
  height: 16px;
  filter: brightness(0) invert(1);
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.action-btn {
  position: relative;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border-radius: 50%;
  transition: var(--transition-default);
}

.action-btn:hover {
  background: var(--border-light);
}

.action-icon {
  width: 20px;
  height: 20px;
  opacity: 0.7;
}

.notification-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  background: #ff4757;
  color: white;
  font-size: 10px;
  font-weight: 600;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-menu {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px;
  border-radius: 24px;
  cursor: pointer;
  transition: var(--transition-default);
}

.user-menu:hover {
  background: var(--border-light);
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid var(--border-color);
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.chevron-icon {
  width: 16px;
  height: 16px;
  opacity: 0.5;
}

.user-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 180px;
  background: white;
  border-radius: 12px;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-color);
  padding: 8px;
  z-index: 100;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  color: var(--text-dark);
  font-size: 14px;
  transition: var(--transition-default);
  width: 100%;
  background: transparent;
  text-align: left;
}

.dropdown-item:hover {
  background: var(--border-light);
  color: var(--primary-color);
}

.dropdown-icon {
  width: 18px;
  height: 18px;
  opacity: 0.6;
}

.dropdown-divider {
  height: 1px;
  background: var(--border-color);
  margin: 8px 0;
}

.logout-btn {
  color: #ff4757;
}

.logout-btn:hover {
  background: rgba(255, 71, 87, 0.1);
  color: #ff4757;
}

.mobile-menu-btn {
  display: none;
  width: 40px;
  height: 40px;
  align-items: center;
  justify-content: center;
  background: transparent;
  border-radius: 8px;
}

.mobile-menu-btn img {
  width: 24px;
  height: 24px;
}

.mobile-menu {
  display: none;
  flex-direction: column;
  padding: 16px;
  border-top: 1px solid var(--border-color);
}

.mobile-nav-link {
  padding: 12px 16px;
  color: var(--text-dark);
  font-size: 15px;
  border-radius: 8px;
}

.mobile-nav-link:hover {
  background: var(--border-light);
}

@media (max-width: 1024px) {
  .header-nav {
    display: none;
  }
  
  .header-center {
    max-width: 320px;
  }
}

@media (max-width: 768px) {
  .header-container {
    padding: 0 16px;
  }
  
  .header-center,
  .write-btn,
  .user-actions {
    display: none;
  }
  
  .mobile-menu-btn {
    display: flex;
  }
  
  .mobile-menu {
    display: flex;
  }
}
</style>
