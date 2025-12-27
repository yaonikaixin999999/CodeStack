<template>
  <div class="admin-page">
    <AdminHeader :avatar="currentUser?.avatar" />

    <div class="content-container">
      <AdminHero
        title="内容审核中心"
        subtitle="保障社区内容健康、真实与安全"
        placeholder="搜索待审核内容或作者..."
        v-model="heroKeyword"
        @search="goSearchHero"
      />
    </div>

    <main class="content-container">
      <div v-if="errorMessage" class="error-banner">
        {{ errorMessage }}
      </div>

      <div v-if="!isAdmin" class="notice-card card">
        <p class="notice-title">当前账号不是管理员</p>
        <p class="notice-desc">请使用管理员账号登录后再访问本页面。</p>
      </div>

      <section v-else class="moderation-grid">
        <div class="card moderation-card">
          <div class="card-header">
            <div>
              <p class="card-label">文章审核</p>
              <h3 class="card-title">待发布文章</h3>
            </div>
          </div>
          <div class="table-wrapper">
            <table class="content-table">
              <thead>
                <tr>
                  <th>标题</th>
                  <th>作者</th>
                  <th>分类</th>
                  <th>提交时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <template v-for="post in pendingPosts" :key="post.id">
                  <tr>
                    <td class="title-cell">
                      <span class="dot" />
                      <router-link :to="`/admin/blog/post/${post.id}`" class="link-title">
                        {{ post.title }}
                      </router-link>
                    </td>
                    <td>
                      <router-link
                        v-if="post.userId"
                        :to="{ path: `/admin/blog/profile/${post.userId}`, query: { from: 'moderation' } }"
                        class="link-user"
                      >
                        {{ post.nickname || post.username || `用户 ${post.userId}` }}
                      </router-link>
                      <span v-else>—</span>
                    </td>
                    <td>—</td>
                    <td>{{ post.createdAt || '—' }}</td>
                    <td class="row-actions">
                      <button class="pill-btn ghost" :disabled="aiPostLoading[post.id]" @click="reviewPostWithAi(post.id)">
                        {{ aiPostLoading[post.id] ? 'AI审核中...' : 'AI审核' }}
                      </button>
                      <button class="pill-btn ghost" @click="approvePost(post.id)">通过</button>
                      <button class="pill-btn danger" @click="rejectPost(post.id)">驳回</button>
                    </td>
                  </tr>
                  <tr v-if="aiPostReviews[post.id]" class="ai-row">
                    <td colspan="5">
                      <div class="ai-result">
                        <span class="ai-badge" :class="getDecisionClass(aiPostReviews[post.id].decision)">
                          AI建议：{{ getDecisionLabel(aiPostReviews[post.id].decision) }}
                        </span>
                        <span v-if="aiPostReviews[post.id].matchedKeywords?.length">
                          命中关键词：{{ aiPostReviews[post.id].matchedKeywords?.join('、') }}
                        </span>
                        <span v-if="aiPostReviews[post.id].reasons?.length">
                          原因：{{ formatReasons(aiPostReviews[post.id].reasons) }}
                        </span>
                      </div>
                    </td>
                  </tr>
                </template>
                <tr v-if="pendingPosts.length === 0">
                  <td colspan="5" class="empty-cell">暂无待审核文章</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="card moderation-card">
          <div class="card-header">
            <div>
              <p class="card-label">评论审核</p>
              <h3 class="card-title">待审核评论</h3>
            </div>
          </div>
          <div class="table-wrapper">
            <table class="content-table">
              <thead>
                <tr>
                  <th>用户名</th>
                  <th>内容</th>
                  <th>时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <template v-for="comment in pendingComments" :key="comment.id">
                  <tr>
                    <td>
                      <router-link
                        v-if="comment.userId"
                        :to="{ path: `/admin/blog/profile/${comment.userId}`, query: { from: 'moderation' } }"
                        class="link-user"
                      >
                        {{ comment.username }}
                      </router-link>
                      <span v-else>—</span>
                    </td>
                    <td class="truncate">{{ comment.content }}</td>
                    <td>{{ comment.createdAt || '—' }}</td>
                    <td class="row-actions">
                      <button class="pill-btn ghost" :disabled="aiCommentLoading[comment.id]" @click="reviewCommentWithAi(comment.id)">
                        {{ aiCommentLoading[comment.id] ? 'AI审核中...' : 'AI审核' }}
                      </button>
                      <button class="pill-btn ghost" @click="approveComment(comment.id)">通过</button>
                      <button class="pill-btn danger" @click="deleteComment(comment.id)">删除</button>
                    </td>
                  </tr>
                  <tr v-if="aiCommentReviews[comment.id]" class="ai-row">
                    <td colspan="4">
                      <div class="ai-result">
                        <span class="ai-badge" :class="getDecisionClass(aiCommentReviews[comment.id].decision)">
                          AI建议：{{ getDecisionLabel(aiCommentReviews[comment.id].decision) }}
                        </span>
                        <span v-if="aiCommentReviews[comment.id].matchedKeywords?.length">
                          命中关键词：{{ aiCommentReviews[comment.id].matchedKeywords?.join('、') }}
                        </span>
                        <span v-if="aiCommentReviews[comment.id].reasons?.length">
                          原因：{{ formatReasons(aiCommentReviews[comment.id].reasons) }}
                        </span>
                      </div>
                    </td>
                  </tr>
                </template>
                <tr v-if="pendingComments.length === 0">
                  <td colspan="4" class="empty-cell">暂无待审核评论</td>
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
import { adminService, type AdminComment, type AdminPost, type AiModerationResult, type ModerationDecision } from '@/services/adminService'
import AdminHeader from '@/components/admin/AdminHeader.vue'
import AdminHero from '@/components/admin/AdminHero.vue'

const router = useRouter()
const currentUser = userService.getCurrentUser()
const isAdmin = currentUser?.isAdmin || false

const heroKeyword = ref('')
const errorMessage = ref('')
const pendingPosts = ref<AdminPost[]>([])
const pendingComments = ref<AdminComment[]>([])
const aiPostReviews = ref<Record<number, AiModerationResult>>({})
const aiCommentReviews = ref<Record<number, AiModerationResult>>({})
const aiPostLoading = ref<Record<number, boolean>>({})
const aiCommentLoading = ref<Record<number, boolean>>({})

const setError = (msg: string) => {
  errorMessage.value = msg
}

const goSearchHero = () => {
  if (!heroKeyword.value.trim()) return
  loadData(heroKeyword.value.trim())
}

const loadData = async (keyword?: string) => {
  try {
    const [postRes, commentRes] = await Promise.all([
      adminService.listPosts({ status: 2, limit: 50, q: keyword }),
      adminService.listComments({ status: 0, limit: 50 })
    ])
    if (postRes.success) pendingPosts.value = postRes.data || []
    if (commentRes.success) pendingComments.value = commentRes.data || []
    aiPostReviews.value = {}
    aiCommentReviews.value = {}
  } catch (e) {
    console.error('加载待审核数据失败', e)
    setError('加载待审核数据失败，请检查登录状态或网络')
  }
}

const approvePost = async (id: number) => {
  try {
    await adminService.approvePost(id)
    pendingPosts.value = pendingPosts.value.filter(p => p.id !== id)
    delete aiPostReviews.value[id]
  } catch (e) {
    console.error('通过文章失败', e)
    setError('通过文章失败，请重试')
  }
}

const rejectPost = async (id: number) => {
  try {
    await adminService.rejectPost(id)
    pendingPosts.value = pendingPosts.value.filter(p => p.id !== id)
    delete aiPostReviews.value[id]
  } catch (e) {
    console.error('拒绝文章失败', e)
    setError('拒绝文章失败，请重试')
  }
}

const approveComment = async (id: number) => {
  try {
    await adminService.approveComment(id)
    pendingComments.value = pendingComments.value.filter(c => c.id !== id)
    delete aiCommentReviews.value[id]
  } catch (e) {
    console.error('通过评论失败', e)
    setError('通过评论失败，请重试')
  }
}

const deleteComment = async (id: number) => {
  try {
    await adminService.deleteComment(id)
    pendingComments.value = pendingComments.value.filter(c => c.id !== id)
    delete aiCommentReviews.value[id]
  } catch (e) {
    console.error('删除评论失败', e)
    setError('删除评论失败，请重试')
  }
}

const getDecisionLabel = (decision?: ModerationDecision) => {
  if (decision === 'APPROVE') return '通过'
  if (decision === 'REJECT') return '驳回'
  if (decision === 'REVIEW') return '需人工复核'
  return '未知'
}

const getDecisionClass = (decision?: ModerationDecision) => {
  if (decision === 'APPROVE') return 'approve'
  if (decision === 'REJECT') return 'reject'
  if (decision === 'REVIEW') return 'review'
  return ''
}

const formatReasons = (reasons?: string[]) => {
  if (!reasons || reasons.length === 0) return '未提供原因'
  const map: Record<string, string> = {
    blocklist_hit: '命中禁用关键词',
    reviewlist_hit: '疑似广告/引流',
    clean: '未发现明显风险',
    manual_review_required: '需人工复核',
    status_not_pending: '当前状态不是待审核'
  }
  return reasons.map(r => map[r] || r).join('、')
}

const reviewPostWithAi = async (id: number) => {
  try {
    aiPostLoading.value[id] = true
    const res = await adminService.reviewPost(id)
    if (res.success && res.data) {
      aiPostReviews.value[id] = res.data
    } else {
      setError(res.message || 'AI审核失败，请重试')
    }
  } catch (e) {
    console.error('AI审核文章失败', e)
    setError('AI审核失败，请重试')
  } finally {
    aiPostLoading.value[id] = false
  }
}

const reviewCommentWithAi = async (id: number) => {
  try {
    aiCommentLoading.value[id] = true
    const res = await adminService.reviewComment(id)
    if (res.success && res.data) {
      aiCommentReviews.value[id] = res.data
    } else {
      setError(res.message || 'AI审核失败，请重试')
    }
  } catch (e) {
    console.error('AI审核评论失败', e)
    setError('AI审核失败，请重试')
  } finally {
    aiCommentLoading.value[id] = false
  }
}

onMounted(() => {
  if (isAdmin) {
    loadData()
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

.moderation-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
}

.moderation-card {
  padding: 18px;
}

.card {
  background: #fff;
  border-radius: var(--border-radius-md);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-light);
}

.card-label {
  font-size: 12px;
  color: var(--text-muted);
}

.card-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-dark);
}

.table-wrapper {
  margin-top: 12px;
  overflow-x: auto;
}

.content-table {
  width: 100%;
  border-collapse: collapse;
}

.content-table th,
.content-table td {
  padding: 12px 10px;
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
}

.ai-row td {
  background: rgba(248, 250, 252, 0.9);
}

.ai-result {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  font-size: 12px;
  color: var(--text-muted);
}

.ai-badge {
  padding: 4px 10px;
  border-radius: 999px;
  font-weight: 600;
  border: 1px solid transparent;
  color: var(--text-dark);
  background: #f1f5f9;
}

.ai-badge.approve {
  background: #ecfdf3;
  color: #047857;
  border-color: rgba(4, 120, 87, 0.2);
}

.ai-badge.reject {
  background: #fef2f2;
  color: #b91c1c;
  border-color: rgba(185, 28, 28, 0.2);
}

.ai-badge.review {
  background: #fff7ed;
  color: #c2410c;
  border-color: rgba(194, 65, 12, 0.2);
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
