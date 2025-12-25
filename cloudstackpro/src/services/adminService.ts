import axios from 'axios'

// 动态获取 API 地址
const getApiBaseUrl = () => {
  const hostname = window.location.hostname
  if (hostname === 'localhost' || hostname === '127.0.0.1') {
    return 'http://localhost:8082/api/admin'
  }
  return `http://${hostname}:8082/api/admin`
}

const api = axios.create({
  baseURL: getApiBaseUrl(),
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

api.interceptors.request.use(config => {
  const adminToken = localStorage.getItem('token')
  const blogToken = localStorage.getItem('blog_token')
  const token = adminToken || blogToken
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export interface AdminStats {
  totalPosts?: number
  totalUsers?: number
  totalComments?: number
  pendingPosts?: number
  pendingComments?: number
}

export interface AdminPost {
  id: number
  title: string
  status?: number
  statusLabel?: string
  userId?: number
  username?: string
  nickname?: string
  email?: string
  updatedAt?: string
  createdAt?: string
}

export interface AdminComment {
  id: number
  userId?: number
  content: string
  createdAt?: string
}

export interface ModerationItem {
  id: number
  title: string
  type: string
  createdAt?: string
}

export interface DashboardPayload {
  stats?: AdminStats
  recentPosts?: AdminPost[]
  moderationQueue?: ModerationItem[]
  metrics?: { label: string; value: string; progress: number; trend: number }[]
  announcements?: { id: number; title: string; time: string; tag: string; color: string }[]
}

export interface ChartPoint {
  label: string
  value: number
}

export interface ChartSlice {
  label: string
  value: number
  color?: string
  id?: number
  name?: string
}

export interface ChartPayload {
  postSeries?: ChartPoint[]
  commentSeries?: ChartPoint[]
  loginSeries?: ChartPoint[]
  categorySlices?: ChartSlice[]
}

export interface AdminUser {
  id: number
  username: string
  email?: string
  role?: string
  status?: number
  lastLoginAt?: string
  avatar?: string
}

export const adminService = {
  async getDashboard() {
    const res = await api.get<{ success: boolean; message?: string; data?: DashboardPayload }>('/dashboard')
    return res.data
  },

  async getCharts(params: { postDays: number; commentDays: number; loginDays: number }) {
    const res = await api.get<{ success: boolean; message?: string; data?: ChartPayload }>('/charts', { params })
    return res.data
  },

  async listPosts(params: { status?: number; limit?: number; q?: string } = {}) {
    const res = await api.get<{ success: boolean; data?: AdminPost[]; message?: string }>('/posts', { params })
    return res.data
  },

  async listComments(params: { status?: number; limit?: number } = {}) {
    const res = await api.get<{ success: boolean; data?: AdminComment[]; message?: string }>('/comments', { params })
    return res.data
  },

  async approvePost(id: number) {
    const res = await api.post<{ success: boolean; message?: string }>(`/posts/${id}/approve`)
    return res.data
  },

  async rejectPost(id: number) {
    const res = await api.post<{ success: boolean; message?: string }>(`/posts/${id}/reject`)
    return res.data
  },

  async deletePost(id: number) {
    const res = await api.delete<{ success: boolean; message?: string }>(`/posts/${id}`)
    return res.data
  },

  async approveComment(id: number) {
    const res = await api.post<{ success: boolean; message?: string }>(`/comments/${id}/approve`)
    return res.data
  },

  async deleteComment(id: number) {
    const res = await api.delete<{ success: boolean; message?: string }>(`/comments/${id}`)
    return res.data
  },

  async createAnnouncement(payload: { title: string; content: string; type?: string }) {
    const res = await api.post<{ success: boolean; message?: string }>(`/announcements`, payload)
    return res.data
  },

  async listUsers(params: Record<string, any> = {}) {
    const res = await api.get<{ success: boolean; data?: AdminUser[]; message?: string }>('/users', { params })
    return res.data
  },

  async muteUser(id: number) {
    const res = await api.post<{ success: boolean; message?: string }>(`/users/${id}/mute`)
    return res.data
  },

  async banUser(id: number) {
    const res = await api.post<{ success: boolean; message?: string }>(`/users/${id}/ban`)
    return res.data
  },

  async unbanUser(id: number) {
    const res = await api.post<{ success: boolean; message?: string }>(`/users/${id}/unban`)
    return res.data
  },

  async uploadAvatar(file: File) {
    const form = new FormData()
    form.append('file', file)
    const res = await api.post<{ success: boolean; data?: string; message?: string }>(`/avatar`, form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    return res.data
  }
}
