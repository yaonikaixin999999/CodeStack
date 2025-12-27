<template>
  <header class="blog-header">
    <div class="header-container">
      <div class="header-left">
        <router-link to="/admin" class="logo">
          <span class="logo-text">CloudStack</span>
          <span class="logo-badge">Admin</span>
        </router-link>
        <nav class="header-nav">
          <router-link to="/admin" class="nav-link" :class="{ active: isActive('/admin') }">
            <img src="@/assets/blog/icons/home.svg" alt="首页" class="nav-icon" />
            <span>首页</span>
          </router-link>
          <router-link to="/admin/moderation" class="nav-link" :class="{ active: isActive('/admin/moderation') }">
            <img src="@/assets/blog/icons/category.svg" alt="审核" class="nav-icon" />
            <span>审核</span>
          </router-link>
          <router-link to="/admin/users" class="nav-link" :class="{ active: isActive('/admin/users') }">
            <img src="@/assets/blog/icons/user.svg" alt="用户" class="nav-icon" />
            <span>用户</span>
          </router-link>
          <router-link to="/admin/blog" class="nav-link" :class="{ active: isActive('/admin/blog') }">
            <img src="@/assets/blog/icons/trending.svg" alt="博客" class="nav-icon" />
            <span>博客</span>
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
          <button class="action-btn notification-btn" @click="goChat">
            <img src="@/assets/blog/icons/comment.svg" alt="消息" class="action-icon" />
            <span v-if="unreadCount > 0" class="notification-dot"></span>
          </button>

          <div class="user-menu" @click="toggleUserMenu">
            <div class="user-avatar">
              <img :src="avatar" alt="用户头像" />
            </div>
            <img src="@/assets/blog/icons/chevron-down.svg" alt="展开" class="chevron-icon" />

            <div v-if="showUserMenu" class="user-dropdown">
              <router-link to="/admin/profile" class="dropdown-item">
                <img src="@/assets/blog/icons/user.svg" alt="个人中心" class="dropdown-icon" />
                <span>个人中心</span>
              </router-link>
              <!-- <router-link to="/admin/blog/profile" class="dropdown-item">
                <img src="@/assets/blog/icons/bookmark.svg" alt="我的收藏" class="dropdown-icon" />
                <span>我的收藏</span>
              </router-link> -->
              <!-- <router-link to="/admin/blog/profile" class="dropdown-item">
                <img src="@/assets/blog/icons/settings.svg" alt="设置" class="dropdown-icon" />
                <span>设置</span>
              </router-link> -->
              <div class="dropdown-divider"></div>
              <button class="dropdown-item logout-btn" @click="handleLogout">
                <img src="@/assets/blog/icons/close.svg" alt="退出" class="dropdown-icon" />
                <span>退出登录</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <button class="mobile-menu-btn" @click="toggleMobileMenu">
        <img src="@/assets/blog/icons/menu.svg" alt="菜单" />
      </button>
    </div>

    <div v-if="showMobileMenu" class="mobile-menu">
      <router-link to="/admin" class="mobile-nav-link">首页</router-link>
      <router-link to="/admin/moderation" class="mobile-nav-link">审核</router-link>
      <router-link to="/admin/users" class="mobile-nav-link">用户</router-link>
      <router-link to="/admin/blog" class="mobile-nav-link">博客</router-link>
      <router-link to="/blog/write" class="mobile-nav-link">写文章</router-link>
      <router-link to="/admin/profile" class="mobile-nav-link">个人中心</router-link>
    </div>
  </header>
</template>

<script lang="ts">
import { defineComponent, ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { userService } from '@/services/userService'
import { blogService } from '@/services/blogService'

export default defineComponent({
  name: 'AdminHeader',
  props: {
    avatar: {
      type: String,
      default: 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin'
    }
  },
  setup(props) {
    const router = useRouter()
    const route = useRoute()

    const searchQuery = ref('')
    const showUserMenu = ref(false)
    const showMobileMenu = ref(false)
    const unreadCount = ref(0)
    const avatarSrc = ref(
      props.avatar ||
        (localStorage.getItem('blog_user') ? JSON.parse(localStorage.getItem('blog_user') || '{}').avatar : '') ||
        localStorage.getItem('avatar') ||
        'https://api.dicebear.com/7.x/avataaars/svg?seed=admin'
    )

    const refreshAvatar = (custom?: string) => {
      avatarSrc.value =
        custom ||
        props.avatar ||
        (localStorage.getItem('blog_user') ? JSON.parse(localStorage.getItem('blog_user') || '{}').avatar : '') ||
        localStorage.getItem('avatar') ||
        'https://api.dicebear.com/7.x/avataaars/svg?seed=admin'
    }

    const handleAvatarEvent = (e: Event) => {
      const detail = (e as CustomEvent).detail as string | undefined
      refreshAvatar(detail)
    }

    const syncFromServer = async () => {
      try {
        const res = await blogService.auth.getCurrentUser()
        if (res?.data) {
          const user = res.data as any
          const local = localStorage.getItem('blog_user')
          const parsed = local ? JSON.parse(local) : {}
          const next = { ...parsed, ...user }
          localStorage.setItem('blog_user', JSON.stringify(next))
          if (user.avatar) {
            localStorage.setItem('avatar', user.avatar)
            avatarSrc.value = user.avatar
          } else {
            refreshAvatar()
          }
        }
      } catch (e) {
        // ignore if not logged in
      }
    }

    onMounted(() => {
      refreshAvatar()
      syncFromServer()
      loadUnreadCount()
      window.addEventListener('avatar-updated', handleAvatarEvent)
      window.addEventListener('storage', handleAvatarEvent)
    })

    onBeforeUnmount(() => {
      window.removeEventListener('avatar-updated', handleAvatarEvent)
      window.removeEventListener('storage', handleAvatarEvent)
    })

    watch(
      () => props.avatar,
      val => {
        if (val) {
          avatarSrc.value = val
        }
      }
    )

    const isActive = (path: string) => {
      if (path === '/admin') {
        return route.path === '/admin'
      }
      return route.path === path || route.path.startsWith(path + '/')
    }

    const handleSearch = () => {
      if (searchQuery.value.trim()) {
        router.push({ path: '/admin/blog/search', query: { q: searchQuery.value } })
      }
    }

    const toggleUserMenu = () => {
      showUserMenu.value = !showUserMenu.value
    }

    const handleLogout = () => {
      userService.logout()
      blogService.auth.logout()
      showUserMenu.value = false
      showMobileMenu.value = false
      router.push('/login')
    }

    const toggleMobileMenu = () => {
      showMobileMenu.value = !showMobileMenu.value
    }

    const goChat = () => {
      router.push('/admin/messages')
    }

    const loadUnreadCount = async () => {
      try {
        const res = await blogService.notifications.getUnreadCount()
        unreadCount.value = (res as any)?.data ?? 0
      } catch {
        unreadCount.value = 0
      }
    }

    return {
      searchQuery,
      showUserMenu,
      showMobileMenu,
      isActive,
      handleSearch,
      toggleUserMenu,
      toggleMobileMenu,
      handleLogout,
      goChat,
      avatar: avatarSrc,
      unreadCount
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
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 24px;
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
  color: #fff;
  font-weight: 500;
  text-decoration: none;
  box-shadow: var(--shadow-sm);
  font-size: 14px;
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

.notification-dot {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 10px;
  height: 10px;
  background: #ff4757;
  border-radius: 50%;
  box-shadow: 0 0 0 2px white;
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
