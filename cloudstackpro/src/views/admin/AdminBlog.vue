<template>
  <div class="admin-blog-page">
    <AdminHeader />
    <main class="main-content">
      <div class="content-container">
        <AdminHero v-model="keyword" @search="goSearch" />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import AdminHeader from '@/components/admin/AdminHeader.vue'
import AdminHero from '@/components/admin/AdminHero.vue'

const router = useRouter()
const keyword = ref('')

const goSearch = () => {
  const kw = keyword.value.trim()
  if (!kw) return
  // 管理员保持在后台路由，携带查询词
  router.push({ name: 'AdminBlogForward', query: { keyword: kw } })
}
</script>

<style scoped>
.admin-blog-page {
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
