<template>
  <div class="admin-page">
    <AdminHeader :avatar="currentUser?.avatar" />

    <div class="content-container">
      <AdminHero
        title="用户管理"
        subtitle="掌握用户状态，保持社区秩序"
        placeholder="搜索用户或邮箱..."
        v-model="heroKeyword"
        @search="goSearchHero"
      />
    </div>

    <main class="content-container">
      <div v-if="!isAdmin" class="notice-card card">
        <p class="notice-title">当前账号不是管理员</p>
        <p class="notice-desc">请使用管理员账号登录后再访问本页面。</p>
      </div>

      <div v-else-if="errorMessage" class="error-banner">
        {{ errorMessage }}
      </div>

      <section v-else class="grid">
        <div class="card">
          <div class="card-header">
            <div>
              <p class="card-label">用户列表</p>
              <h3 class="card-title">用户状态</h3>
            </div>
          </div>
          <div class="table-wrapper">
            <table class="content-table">
              <thead>
                <tr>
                  <th>用户</th>
                  <th>邮箱</th>
                  <th>角色</th>
                  <th>状态</th>
                  <th>最近登录</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="user in users" :key="user.id">
                  <td class="title-cell">
                    <span class="dot"></span>
                    <span class="clickable" @click="goProfile(user)">{{ user.username }}</span>
                  </td>
                  <td>{{ user.email }}</td>
                  <td>{{ user.role }}</td>
                  <td>
                    <span class="status-pill" :class="userStatusClass(user.status)">{{ statusText(user.status) }}</span>
                  </td>
                  <td>{{ user.lastLoginAt || '—' }}</td>
                  <td class="row-actions">
                    <button class="pill-btn ghost" @click="muteUser(user.id)" v-if="user.status !== 0">禁言</button>
                    <button class="pill-btn danger" @click="banUser(user.id)" v-if="user.status !== -1">封禁</button>
                    <button class="pill-btn ghost" @click="unbanUser(user.id)" v-if="user.status === -1 || user.status === 0">解封</button>
                  </td>
                </tr>
                <tr v-if="users.length === 0">
                  <td colspan="6" class="empty-cell">暂无用户数据</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { userService } from '@/services/userService'
import { adminService, type AdminUser } from '@/services/adminService'
import AdminHeader from '@/components/admin/AdminHeader.vue'
import AdminHero from '@/components/admin/AdminHero.vue'

const router = useRouter()
const currentUser = userService.getCurrentUser()
const isAdmin = currentUser?.isAdmin || false

const heroKeyword = ref('')
const users = ref<AdminUser[]>([])
const errorMessage = ref('')

const goProfile = (user?: AdminUser) => {
  if (!user?.id) return
  router.push(`/admin/blog/profile/${user.id}`)
}

const goSearchHero = () => {
  if (!heroKeyword.value.trim()) return
  router.push({ path: '/admin/users', query: { q: heroKeyword.value.trim() } })
}

const loadUsers = async () => {
  try {
    const res = await adminService.listUsers({})
    if (res.success) {
      users.value = res.data || []
    }
  } catch (e) {
    console.error('加载用户失败', e)
    errorMessage.value = '加载用户失败，请检查登录状态或网络'
  }
}

const userStatusClass = (status?: number) => {
  if (status === 1) return 'active'
  if (status === 0) return 'muted'
  if (status === -1) return 'banned'
  return 'muted'
}

const statusText = (status?: number) => {
  if (status === 1) return '正常'
  if (status === 0) return '禁言'
  if (status === -1) return '封禁'
  return '未知'
}

const muteUser = async (id: number) => {
  await adminService.muteUser(id)
  users.value = users.value.map(u => (u.id === id ? { ...u, status: 0 } : u))
}

const banUser = async (id: number) => {
  await adminService.banUser(id)
  users.value = users.value.map(u => (u.id === id ? { ...u, status: -1 } : u))
}

const unbanUser = async (id: number) => {
  await adminService.unbanUser(id)
  users.value = users.value.map(u => (u.id === id ? { ...u, status: 1 } : u))
}

onMounted(() => {
  if (isAdmin) {
    loadUsers()
  }
})
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

.grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
}

.card {
  background: #fff;
  border-radius: 14px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px 12px;
  border-bottom: 1px solid var(--border-light);
}

.card-label {
  font-size: 12px;
  color: var(--text-muted);
  letter-spacing: 0.2px;
}

.card-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-dark);
  margin-top: 4px;
}

.table-wrapper {
  margin-top: 0;
  padding: 0 20px 16px;
  overflow-x: auto;
}

.content-table {
  width: 100%;
  border-collapse: collapse;
}

/* 让表格与卡片边缘更贴近，避免左右大间距 */
.card .table-wrapper {
  margin: 0;
}

.card .content-table {
  width: calc(100% + 2px);
  margin: 0 -1px;
  table-layout: fixed;
}

/* 对齐博客风格的表格内边距与字体 */
.content-table th,
.content-table td {
  padding: 14px 12px;
  text-align: left;
  font-size: 14px;
  color: var(--text-dark);
  border-bottom: 1px solid var(--border-light);
}

.content-table th {
  color: var(--text-muted);
  font-weight: 600;
}

.title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}

.row-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.clickable {
  cursor: pointer;
  color: var(--primary-color);
}

.pill-btn {
  border-radius: 999px;
  padding: 8px 14px;
  border: 1px solid var(--border-color);
  background: #fff;
  color: var(--text-dark);
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  transition: var(--transition-default);
}

.pill-btn.ghost {
  background: transparent;
}

.pill-btn.danger {
  color: #ef4444;
  border-color: rgba(239, 68, 68, 0.3);
}

.pill-btn:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.status-pill {
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.status-pill.active {
  background: rgba(34, 197, 94, 0.15);
  color: #15803d;
}

.status-pill.muted {
  background: rgba(251, 191, 36, 0.2);
  color: #a16207;
}

.status-pill.banned {
  background: rgba(239, 68, 68, 0.2);
  color: #b91c1c;
}

.truncate {
  max-width: 320px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty-cell {
  text-align: center;
  color: var(--text-muted);
  padding: 20px 0;
}

.notice-card {
  padding: 18px;
  margin-bottom: 16px;
}

.notice-title {
  font-weight: 700;
  color: var(--text-dark);
}

.notice-desc {
  color: var(--text-muted);
  margin-top: 6px;
}

.error-banner {
  margin-bottom: 12px;
  padding: 10px 12px;
  border-radius: var(--border-radius-sm);
  background: rgba(239, 68, 68, 0.08);
  color: #b91c1c;
  border: 1px solid rgba(239, 68, 68, 0.2);
}

@media (max-width: 768px) {
  .admin-page {
    padding: 48px 0 32px;
  }

  .content-container {
    padding: 0 16px 32px;
  }
}

@media (max-width: 640px) {
  .admin-page {
    padding: 40px 0 28px;
  }
}
</style>
