<template>
  <div class="admin-page">
    <AdminHeader :avatar="profile.avatar || currentUser?.avatar" />

    <section class="hero-banner">
      <div class="hero-content">
        <h1 class="hero-title">管理员中心</h1>
        <p class="hero-subtitle">更新头像与资料，让形象保持一致</p>
        <div class="hero-search">
          <i class="fas fa-search"></i>
          <input v-model="heroKeyword" type="text" placeholder="查找设置或帮助..." @keyup.enter="goSearchHero" />
          <button class="search-btn" @click="goSearchHero">搜索</button>
        </div>
      </div>
      <div class="hero-decoration">
        <div class="decoration-circle circle-1"></div>
        <div class="decoration-circle circle-2"></div>
        <div class="decoration-circle circle-3"></div>
      </div>
    </section>

    <main class="content-container">
      <section class="profile card">
        <div class="profile-header">
          <div class="avatar-upload">
            <img :src="profile.avatar" class="avatar-lg" />
            <label class="upload-btn">
              <input type="file" accept="image/*" @change="onFileChange" />
              更换头像
            </label>
            <p v-if="uploadState.error" class="error-text">{{ uploadState.error }}</p>
            <p v-if="uploadState.success" class="success-text">头像已更新</p>
          </div>
          <div class="profile-info">
            <div class="name-row">
              <h2 class="name">{{ profile.name }}</h2>
              <span class="badge">管理员</span>
            </div>
            <p class="bio">{{ profile.bio }}</p>
            <div class="tags">
              <span class="tag" v-for="tag in profile.tags" :key="tag">{{ tag }}</span>
            </div>
          </div>
        </div>
        <div class="profile-stats">
          <div class="stat">
            <div class="stat-value">{{ profile.posts }}</div>
            <div class="stat-label">文章</div>
          </div>
          <div class="stat">
            <div class="stat-value">{{ profile.followers }}</div>
            <div class="stat-label">关注者</div>
          </div>
          <div class="stat">
            <div class="stat-value">{{ profile.likes }}</div>
            <div class="stat-label">获赞</div>
          </div>
        </div>
      </section>

      <section class="card about">
        <h3>关于我</h3>
        <p>{{ profile.about }}</p>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AdminHeader from '@/components/admin/AdminHeader.vue'
import { adminService } from '@/services/adminService'
import { userService } from '@/services/userService'

const router = useRouter()
const currentUser = userService.getCurrentUser()

const heroKeyword = ref('')

const profile = reactive({
  name: 'Admin',
  avatar: localStorage.getItem('avatar') || 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin',
  bio: 'CloudStack Blog 站点管理员，负责内容审核与社区运营。',
  tags: ['内容审核', '社区运营', '安全风控', 'AI 审核'],
  posts: 128,
  followers: '2.4k',
  likes: '8.9k',
  about: '保持社区内容健康、有序，让创作者专注于输出价值。'
})

const uploadState = reactive({
  uploading: false,
  error: '',
  success: false
})

const goSearchHero = () => {
  if (!heroKeyword.value.trim()) return
  router.push({ path: '/admin', query: { q: heroKeyword.value.trim() } })
}

const onFileChange = async (e: Event) => {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  uploadState.error = ''
  uploadState.success = false
  if (!file) return
  if (file.size > 2 * 1024 * 1024) {
    uploadState.error = '文件大小超出 2MB'
    return
  }
  try {
    uploadState.uploading = true
    const res = await adminService.uploadAvatar(file)
    if (res.success && res.data) {
      profile.avatar = res.data
      localStorage.setItem('avatar', res.data)
      uploadState.success = true
    } else {
      uploadState.error = res.message || '上传失败'
    }
  } catch (err: any) {
    uploadState.error = err?.message || '上传失败'
  } finally {
    uploadState.uploading = false
    if (target) target.value = ''
  }
}
</script>

<style scoped>
.admin-page {
  min-height: 100vh;
  padding: 64px 0 48px;
  background: linear-gradient(180deg, rgba(230, 242, 255, 0.65), #ffffff);
}

.content-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px 40px;
}

.card {
  background: #fff;
  border-radius: var(--border-radius-md);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
  padding: 20px;
}

.profile {
  margin-bottom: 16px;
}

.profile-header {
  display: flex;
  gap: 16px;
  align-items: center;
}

.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.avatar-lg {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid var(--border-light);
}

.upload-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 6px 12px;
  background: linear-gradient(120deg, #3a9cff, #7cc5ff);
  color: #fff;
  border-radius: 999px;
  font-size: 13px;
  cursor: pointer;
}

.upload-btn input {
  display: none;
}

.error-text {
  color: #ef4444;
  font-size: 13px;
}

.success-text {
  color: #16a34a;
  font-size: 13px;
}

.profile-info {
  flex: 1;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name {
  margin: 0;
  font-size: 22px;
  color: var(--text-dark);
}

.badge {
  padding: 4px 10px;
  border-radius: 12px;
  background: rgba(58, 156, 255, 0.15);
  color: #3a9cff;
  font-size: 12px;
}

.bio {
  margin: 8px 0;
  color: var(--text-light);
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(58, 156, 255, 0.12);
  color: #3a9cff;
  font-size: 12px;
}

.profile-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.stat {
  text-align: center;
  padding: 12px;
  border-radius: 12px;
  background: var(--border-light);
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-dark);
}

.stat-label {
  color: var(--text-muted);
  font-size: 12px;
}

.about h3 {
  margin: 0 0 8px 0;
}

.about p {
  color: var(--text-light);
  margin: 0;
}

.hero-banner {
  position: relative;
  padding: 120px 24px 80px;
  text-align: center;
  overflow: hidden;
  background: linear-gradient(180deg, rgba(230, 242, 255, 0.85), rgba(255, 255, 255, 0.95));
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin: 0 auto;
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

.hero-search i {
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

.hero-search .search-btn {
  padding: 12px 28px;
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
  color: white;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 500;
  transition: var(--transition-default);
}

.hero-search .search-btn:hover {
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

@media (max-width: 768px) {
  .admin-page {
    padding: 48px 0 32px;
  }

  .content-container {
    padding: 0 16px 32px;
  }

  .profile-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-banner {
    padding: 100px 16px 72px;
  }
}

@media (max-width: 640px) {
  .hero-banner {
    padding: 80px 12px 60px;
  }

  .hero-title {
    font-size: 36px;
  }

  .hero-subtitle {
    font-size: 16px;
  }

  .hero-search {
    padding: 8px 10px;
  }
}
</style>
