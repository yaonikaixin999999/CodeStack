<template>
  <div class="admin-page">
    <AdminHeader :avatar="currentUser?.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin'" />

    <div class="content-container">
      <AdminHero v-model="heroKeyword" @search="goSearchHero" />
    </div>

    <div v-if="errorMessage" class="error-banner">
      {{ errorMessage }}
    </div>

    <main class="content-container">
      <!-- 先放全站指标 + 公告 -->
      <section class="insight-grid">
        <div class="card insight-card">
          <div class="card-header">
            <div>
              <p class="card-label">流量与互动</p>
              <h3 class="card-title">全站指标</h3>
            </div>
            <button class="pill-btn ghost" @click="exportData">
              <i class="fas fa-download"></i>
              导出数据
            </button>
          </div>
          <div class="metrics">
            <div v-if="engagementMetrics.length === 0" class="empty-row">暂无数据，等待接入统计服务</div>
            <div v-for="metric in engagementMetrics" :key="metric.label" class="metric">
              <div class="metric-top">
                <span class="metric-label">{{ metric.label }}</span>
                <span class="metric-value">{{ metric.value }}</span>
              </div>
              <div class="metric-bar">
                <div class="metric-bar-fill" :style="{ width: metric.progress + '%' }"></div>
              </div>
              <div class="metric-foot">
                <span>{{ metric.progress }}% 目标完成</span>
                <span :class="metric.trend >= 0 ? 'up' : 'down'">
                  <i :class="metric.trend >= 0 ? 'fas fa-arrow-up' : 'fas fa-arrow-down'"></i>
                  {{ Math.abs(metric.trend) }}%
                </span>
              </div>
            </div>
          </div>
        </div>

        <div class="card announcement-card">
          <div class="card-header">
            <div>
              <p class="card-label">公告</p>
              <h3 class="card-title">系统提示</h3>
            </div>
            <div class="announcement-actions">
              <button class="pill-btn ghost" @click="loadDashboard">
                <i class="fas fa-sync-alt"></i>
                刷新
              </button>
              <button class="pill-btn" @click="openAnnModal = true">
                <i class="fas fa-plus"></i>
                发布公告
              </button>
            </div>
          </div>
          <div class="announcement-list">
            <div v-if="announcements.length === 0" class="empty-row">暂无公告</div>
            <div v-for="item in announcements" :key="item.id" class="announcement">
              <div class="announcement-dot" :style="{ background: item.color }"></div>
              <div>
                <div class="announcement-title">{{ item.title }}</div>
                <div class="announcement-time">{{ item.time }}</div>
              </div>
              <span class="announcement-tag">{{ item.tag }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 再放统计卡片 -->
      <section class="stats-grid">
        <div
          v-for="item in stats"
          :key="item.label"
          class="stat-card card clickable"
          @click="handleStatClick(item.label)"
        >
          <div class="stat-icon" :style="{ background: item.bg }">
            <i :class="item.icon"></i>
          </div>
          <div class="stat-meta">
            <div class="stat-label">{{ item.label }}</div>
            <div class="stat-value">{{ item.value }}</div>
          </div>
        </div>
      </section>

      <section class="chart-grid">
        <div class="card chart-card">
          <div class="chart-decor-blob chart-decor-blob--1"></div>
          <div class="chart-decor-blob chart-decor-blob--2"></div>
          
          <div class="chart-header">
            <div>
              <p class="chart-label">发布趋势</p>
              <h3 class="chart-title">文章发布量</h3>
            </div>
            <div class="chart-meta-box">
              <span class="chart-meta-label">峰值</span>
              <span class="chart-meta-value">{{ postChart.max }}</span>
            </div>
          </div>
          <div
            class="chart-canvas"
            @mousemove="handleChartMove($event, 'post')"
            @mouseleave="clearChartHover('post')"
            @click="toggleChartPin($event, 'post')"
          >
            <div class="chart-yaxis">
              <span v-for="tick in postYAxis" :key="`post-${tick}`">{{ tick }}</span>
            </div>
            <div class="chart-grid-lines">
              <div v-for="i in 5" :key="i" class="chart-grid-line"></div>
            </div>
            <svg viewBox="0 0 320 170" class="line-chart" aria-hidden="true" preserveAspectRatio="none">
              <defs>
                <linearGradient id="postFill" x1="0" y1="0" x2="0" y2="170" gradientUnits="userSpaceOnUse">
                  <stop offset="0" stop-color="#3B82F6" stop-opacity="0.3" />
                  <stop offset="1" stop-color="#3B82F6" stop-opacity="0" />
                </linearGradient>
                <filter id="postShadow" x="-50%" y="-50%" width="200%" height="200%">
                  <feGaussianBlur in="SourceAlpha" stdDeviation="4" result="blur" />
                  <feOffset in="blur" dx="0" dy="4" result="offsetBlur" />
                  <feFlood flood-color="#3B82F6" flood-opacity="0.3" result="offsetColor" />
                  <feComposite in="offsetColor" in2="offsetBlur" operator="in" result="offsetBlur" />
                  <feGaussianBlur in="SourceAlpha" stdDeviation="8" result="glowBlur" />
                  <feFlood flood-color="#3B82F6" flood-opacity="0.4" result="glowColor" />
                  <feComposite in="glowColor" in2="glowBlur" operator="in" result="glow" />
                  <feMerge>
                    <feMergeNode in="offsetBlur" />
                    <feMergeNode in="glow" />
                    <feMergeNode in="SourceGraphic" />
                  </feMerge>
                </filter>
              </defs>
              <path class="chart-area" :d="postChart.area" fill="url(#postFill)" />
              <path class="chart-line" :d="postChart.line" stroke="#3B82F6" stroke-width="3" stroke-linecap="round" filter="url(#postShadow)" fill="none" />
            </svg>
            <div v-if="postHover" class="chart-cursor-line" :style="{ left: chartLeft('post', postHover.dotX) }"></div>
            <div
              v-if="postHover"
              class="chart-active-dot"
              :style="{ left: chartLeft('post', postHover.dotX), top: chartTop('post', postHover.dotY) }"
            ></div>
            <div
              v-if="postHover"
              class="chart-tooltip"
              :class="{ 'chart-tooltip--below': isTooltipBelow('post', postHover.dotY) }"
              :style="{ left: chartLeft('post', postHover.dotX), top: chartTop('post', postHover.dotY) }"
            >
              <div class="chart-tooltip-header">{{ postHover.label }} 数据分析</div>
              <div class="chart-tooltip-body">
                <div class="chart-tooltip-row">
                  <span>核心指标:</span>
                  <span class="chart-tooltip-value">{{ postHover.value }}</span>
                </div>
              </div>
            </div>
            <div class="chart-axis">
              <span
                v-for="tick in postAxisTicks"
                :key="tick.label"
                class="chart-axis-tick"
                :style="{ left: tick.left }"
              >
                {{ tick.label }}
              </span>
            </div>
          </div>
        </div>

        <div class="card chart-card">
          <div class="chart-decor-blob chart-decor-blob--3"></div>
          <div class="chart-decor-blob chart-decor-blob--4"></div>

          <div class="chart-header">
            <div>
              <p class="chart-label">互动趋势</p>
              <h3 class="chart-title">评论发布量</h3>
            </div>
            <div class="chart-meta-box">
              <span class="chart-meta-label">峰值</span>
              <span class="chart-meta-value">{{ commentChart.max }}</span>
            </div>
          </div>
          <div
            class="chart-canvas"
            @mousemove="handleChartMove($event, 'comment')"
            @mouseleave="clearChartHover('comment')"
            @click="toggleChartPin($event, 'comment')"
          >
            <div class="chart-yaxis">
              <span v-for="tick in commentYAxis" :key="`comment-${tick}`">{{ tick }}</span>
            </div>
            <div class="chart-grid-lines">
              <div v-for="i in 5" :key="i" class="chart-grid-line"></div>
            </div>
            <svg viewBox="0 0 320 170" class="line-chart" aria-hidden="true" preserveAspectRatio="none">
              <defs>
                <linearGradient id="commentFill" x1="0" y1="0" x2="0" y2="170" gradientUnits="userSpaceOnUse">
                  <stop offset="0" stop-color="#6366F1" stop-opacity="0.3" />
                  <stop offset="1" stop-color="#6366F1" stop-opacity="0" />
                </linearGradient>
                <filter id="commentShadow" x="-50%" y="-50%" width="200%" height="200%">
                  <feGaussianBlur in="SourceAlpha" stdDeviation="4" result="blur" />
                  <feOffset in="blur" dx="0" dy="4" result="offsetBlur" />
                  <feFlood flood-color="#6366F1" flood-opacity="0.3" result="offsetColor" />
                  <feComposite in="offsetColor" in2="offsetBlur" operator="in" result="offsetBlur" />
                  <feGaussianBlur in="SourceAlpha" stdDeviation="8" result="glowBlur" />
                  <feFlood flood-color="#6366F1" flood-opacity="0.4" result="glowColor" />
                  <feComposite in="glowColor" in2="glowBlur" operator="in" result="glow" />
                  <feMerge>
                    <feMergeNode in="offsetBlur" />
                    <feMergeNode in="glow" />
                    <feMergeNode in="SourceGraphic" />
                  </feMerge>
                </filter>
              </defs>
              <path class="chart-area" :d="commentChart.area" fill="url(#commentFill)" />
              <path class="chart-line" :d="commentChart.line" stroke="#6366F1" stroke-width="3" stroke-linecap="round" filter="url(#commentShadow)" fill="none" />
            </svg>
            <div v-if="commentHover" class="chart-cursor-line chart-cursor-line--violet" :style="{ left: chartLeft('comment', commentHover.dotX) }"></div>
            <div
              v-if="commentHover"
              class="chart-active-dot chart-active-dot--violet"
              :style="{ left: chartLeft('comment', commentHover.dotX), top: chartTop('comment', commentHover.dotY) }"
            ></div>
            <div
              v-if="commentHover"
              class="chart-tooltip"
              :class="{ 'chart-tooltip--below': isTooltipBelow('comment', commentHover.dotY) }"
              :style="{ left: chartLeft('comment', commentHover.dotX), top: chartTop('comment', commentHover.dotY) }"
            >
              <div class="chart-tooltip-header chart-tooltip-header--violet">{{ commentHover.label }} 数据分析</div>
              <div class="chart-tooltip-body">
                <div class="chart-tooltip-row">
                  <span>核心指标:</span>
                  <span class="chart-tooltip-value chart-tooltip-value--violet">{{ commentHover.value }}</span>
                </div>
              </div>
            </div>
            <div class="chart-axis">
              <span
                v-for="tick in commentAxisTicks"
                :key="tick.label"
                class="chart-axis-tick"
                :style="{ left: tick.left }"
              >
                {{ tick.label }}
              </span>
            </div>
          </div>
        </div>
      </section>

      <section class="chart-row">
        <div class="card chart-card chart-wide">
          <div class="chart-decor-blob chart-decor-blob--1"></div>
          <div class="chart-decor-blob chart-decor-blob--2"></div>

          <div class="chart-header">
            <div>
              <p class="chart-label">活跃趋势</p>
              <h3 class="chart-title">用户登录频次</h3>
            </div>
            <div class="chart-meta-box">
              <span class="chart-meta-label">峰值</span>
              <span class="chart-meta-value">{{ loginChart.max }}</span>
            </div>
          </div>
          <div
            class="chart-canvas chart-canvas--wide"
            @mousemove="handleChartMove($event, 'login')"
            @mouseleave="clearChartHover('login')"
            @click="toggleChartPin($event, 'login')"
          >
            <div class="chart-yaxis">
              <span v-for="tick in loginYAxis" :key="`login-${tick}`">{{ tick }}</span>
            </div>
            <div class="chart-grid-lines">
              <div v-for="i in 5" :key="i" class="chart-grid-line"></div>
            </div>
            <svg viewBox="0 0 520 170" class="line-chart" aria-hidden="true" preserveAspectRatio="none">
              <defs>
                <linearGradient id="loginFill" x1="0" y1="0" x2="0" y2="170" gradientUnits="userSpaceOnUse">
                  <stop offset="0" stop-color="#10B981" stop-opacity="0.3" />
                  <stop offset="1" stop-color="#10B981" stop-opacity="0" />
                </linearGradient>
                <filter id="loginShadow" x="-50%" y="-50%" width="200%" height="200%">
                  <feGaussianBlur in="SourceAlpha" stdDeviation="4" result="blur" />
                  <feOffset in="blur" dx="0" dy="4" result="offsetBlur" />
                  <feFlood flood-color="#10B981" flood-opacity="0.3" result="offsetColor" />
                  <feComposite in="offsetColor" in2="offsetBlur" operator="in" result="offsetBlur" />
                  <feGaussianBlur in="SourceAlpha" stdDeviation="8" result="glowBlur" />
                  <feFlood flood-color="#10B981" flood-opacity="0.4" result="glowColor" />
                  <feComposite in="glowColor" in2="glowBlur" operator="in" result="glow" />
                  <feMerge>
                    <feMergeNode in="offsetBlur" />
                    <feMergeNode in="glow" />
                    <feMergeNode in="SourceGraphic" />
                  </feMerge>
                </filter>
              </defs>
              <path class="chart-area" :d="loginChart.area" fill="url(#loginFill)" />
              <path class="chart-line" :d="loginChart.line" stroke="#10B981" stroke-width="3" stroke-linecap="round" filter="url(#loginShadow)" fill="none" />
            </svg>
            <div v-if="loginHover" class="chart-cursor-line chart-cursor-line--mint" :style="{ left: chartLeft('login', loginHover.dotX) }"></div>
            <div
              v-if="loginHover"
              class="chart-active-dot chart-active-dot--mint"
              :style="{ left: chartLeft('login', loginHover.dotX), top: chartTop('login', loginHover.dotY) }"
            ></div>
            <div
              v-if="loginHover"
              class="chart-tooltip"
              :class="{ 'chart-tooltip--below': isTooltipBelow('login', loginHover.dotY) }"
              :style="{ left: chartLeft('login', loginHover.dotX), top: chartTop('login', loginHover.dotY) }"
            >
              <div class="chart-tooltip-header chart-tooltip-header--mint">{{ loginHover.label }} 数据分析</div>
              <div class="chart-tooltip-body">
                <div class="chart-tooltip-row">
                  <span>核心指标:</span>
                  <span class="chart-tooltip-value chart-tooltip-value--mint">{{ loginHover.value }}</span>
                </div>
              </div>
            </div>
            <div class="chart-axis chart-axis--wide">
              <span
                v-for="tick in loginAxisTicks"
                :key="tick.label"
                class="chart-axis-tick"
                :style="{ left: tick.left }"
              >
                {{ tick.label }}
              </span>
            </div>
          </div>
        </div>
      </section>

      <section class="chart-row">
        <div class="card chart-card chart-card--sun chart-pie">
          <div class="chart-header">
            <div>
              <p class="chart-label">内容分布</p>
              <h3 class="chart-title">文章类型占比</h3>
            </div>
            <span class="chart-chip">分类统计</span>
          </div>
          <div class="pie-wrap">
            <div class="pie-stage" :style="pieAccentStyle">
              <div class="pie-glow" :style="pieGlowStyle"></div>
              <svg viewBox="0 0 400 400" class="pie-chart" aria-hidden="true">
                <defs>
                  <filter id="pie-crystal" x="-50%" y="-50%" width="200%" height="200%">
                    <feGaussianBlur stdDeviation="4" result="blur" />
                    <feSpecularLighting result="spec" in="blur" specularConstant="1.2" specularExponent="20" lighting-color="#ffffff">
                      <fePointLight x="0" y="-100" z="200" />
                    </feSpecularLighting>
                    <feComposite in="spec" in2="SourceAlpha" operator="in" result="specOut" />
                    <feComposite in="SourceGraphic" in2="blur" operator="over" />
                    <feMerge>
                      <feMergeNode in="SourceGraphic" />
                      <feMergeNode in="specOut" />
                    </feMerge>
                  </filter>
                  <linearGradient id="pie-active-stroke" x1="0%" y1="0%" x2="100%" y2="0%">
                    <stop offset="0%" stop-color="#ffffff" stop-opacity="0.6" />
                    <stop offset="50%" stop-color="#ffffff" stop-opacity="1" />
                    <stop offset="100%" stop-color="#ffffff" stop-opacity="0.6" />
                  </linearGradient>
                  <linearGradient
                    v-for="slice in pieChart.slices"
                    :key="slice.gradId"
                    :id="slice.gradId"
                    x1="0%"
                    y1="0%"
                    x2="100%"
                    y2="100%"
                  >
                    <stop offset="0%" :stop-color="slice.startColor" stop-opacity="0.8" />
                    <stop offset="100%" :stop-color="slice.endColor" stop-opacity="0.4" />
                  </linearGradient>
                </defs>
                <g class="pie-top" transform="rotate(-90 200 200)">
                  <circle
                    v-for="(slice, index) in pieChart.slices"
                    :key="slice.label"
                    cx="200"
                    cy="200"
                    :r="pieSegmentRadius"
                    fill="none"
                    stroke-linecap="round"
                    stroke-width="40"
                    :stroke="`url(#${slice.gradId})`"
                    class="pie-slice"
                    :class="{ 'pie-slice--active': pieActiveIndex === index }"
                    :style="pieSliceStyle(slice, index)"
                    @mouseenter="setActivePie(index)"
                    @click="setActivePie(index)"
                  />
                </g>
                <g v-if="activePie && !pieChart.empty" class="pie-highlight" transform="rotate(-90 200 200)">
                  <circle
                    cx="200"
                    cy="200"
                    :r="pieSegmentRadius + 4"
                    fill="none"
                    stroke-linecap="round"
                    stroke-width="2"
                    stroke="url(#pie-active-stroke)"
                    class="pie-highlight-ring"
                    :style="{ ...pieSliceStyle(activePie, pieActiveIndex ?? 0), '--delay': '0ms' }"
                  />
                </g>
                <circle class="pie-ring" cx="200" cy="200" :r="pieRingRadius" />
                <circle
                  class="pie-ring pie-ring--active"
                  cx="200"
                  cy="200"
                  :r="pieRingRadius"
                  :stroke="pieAccentStart"
                  :style="pieRingStyle"
                />
              </svg>
              <div class="pie-center">
                <transition name="pie-fade" mode="out-in">
                  <div :key="pieCenterLabel" class="pie-center-label">{{ pieCenterLabel }}</div>
                </transition>
                <transition name="pie-fade" mode="out-in">
                  <div v-if="pieCenterMeta" :key="pieCenterMeta" class="pie-center-meta">{{ pieCenterMeta }}</div>
                </transition>
                <transition name="pie-pop" mode="out-in">
                  <div :key="pieCenterAmount" class="pie-center-value" :style="{ backgroundImage: `linear-gradient(135deg, ${pieAccentStart}, ${pieAccentEnd})`, WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }">{{ pieCenterAmount }}</div>
                </transition>
                <transition name="pie-fade" mode="out-in">
                  <div :key="pieCenterPercent" class="pie-center-percent">{{ pieCenterPercent }}</div>
                </transition>
              </div>
            </div>
            <div class="pie-legend">
              <div
                v-for="(slice, index) in pieChart.slices"
                :key="slice.label"
                class="pie-item"
                :class="{ 'is-active': pieActiveIndex === index }"
                :style="{ '--legend-accent': slice.startColor }"
                @mouseenter="setActivePie(index)"
                @click="setActivePie(index)"
              >
                <span
                  class="pie-swatch"
                  :style="{ background: `linear-gradient(90deg, ${slice.startColor}, ${slice.endColor})` }"
                ></span>
                <span class="pie-label">
                  <span class="pie-label-id">ID {{ slice.label }}</span>
                  <span v-if="slice.name" class="pie-label-name">{{ slice.name }}</span>
                </span>
                <span class="pie-value">{{ slice.percent }}%</span>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>

    <div v-if="openAnnModal" class="modal-mask">
      <div class="modal">
        <div class="modal-header">
          <h3>发布公告</h3>
          <button class="pill-btn ghost" @click="closeAnnModal">关闭</button>
        </div>
        <div class="modal-body">
          <label class="modal-field">
            标题
            <input v-model="annForm.title" type="text" placeholder="请输入公告标题" />
          </label>
          <label class="modal-field">
            内容
            <textarea v-model="annForm.content" rows="4" placeholder="请输入公告内容"></textarea>
          </label>
          <label class="modal-field">
            类型
            <input v-model="annForm.type" type="text" placeholder="如：系统/更新/安全" />
          </label>
        </div>
        <div class="modal-footer">
          <button class="pill-btn ghost" @click="closeAnnModal">取消</button>
          <button class="pill-btn" @click="submitAnn">发布</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AdminHeader from '@/components/admin/AdminHeader.vue'
import AdminHero from '@/components/admin/AdminHero.vue'
import { userService } from '@/services/userService'
import { adminService, type AdminPost, type DashboardPayload, type ModerationItem } from '@/services/adminService'

const router = useRouter()
const currentUser = userService.getCurrentUser()
const isAdmin = currentUser?.isAdmin || false

const heroKeyword = ref('')
const errorMessage = ref('')
const postCommentDays = 5
const loginDays = 10
const stats = ref([
  { label: '总文章', value: '0', icon: 'fas fa-feather-alt', bg: 'linear-gradient(135deg, #e0f2ff, #cde6ff)' },
  { label: '用户数', value: '0', icon: 'fas fa-users', bg: 'linear-gradient(135deg, #e6f7ff, #e8f5ff)' },
  { label: '评论', value: '0', icon: 'fas fa-comments', bg: 'linear-gradient(135deg, #fff4e6, #ffe8d9)' },
  { label: '待审核', value: '0', icon: 'fas fa-bell', bg: 'linear-gradient(135deg, #f2e8ff, #e7ddff)' }
])

const recentPosts = ref<AdminPost[]>([])
const moderationQueue = ref<ModerationItem[]>([])

const dashboardCache = ref<DashboardPayload | null>(null)
const engagementMetrics = reactive<{ label: string; value: string; progress: number; trend: number }[]>([])
const announcements = reactive<{ id: number; title: string; time: string; tag: string; color: string }[]>([])
const openAnnModal = ref(false)
const annForm = reactive({ title: '', content: '', type: '系统' })

type SeriesPoint = { label: string; value: number }
type PieSlice = { label: string; value: number; color?: string; id?: number; name?: string }
type PieChartSlice = {
  label: string
  value: number
  id?: number
  name?: string
  color: string
  startColor: string
  endColor: string
  shadow: string
  dash: string
  offset: number
  percent: number
  gradId: string
}

const postSeries = ref<SeriesPoint[]>([])
const commentSeries = ref<SeriesPoint[]>([])
const loginSeries = ref<SeriesPoint[]>([])
const categorySlices = ref<PieSlice[]>([])

const pieSegmentRadius = 160
const pieRingRadius = 130

const postAxis = computed(() => (postSeries.value.length ? postSeries.value : buildDateSeries([], postCommentDays)))
const commentAxis = computed(() => (commentSeries.value.length ? commentSeries.value : buildDateSeries([], postCommentDays)))
const loginAxis = computed(() => (loginSeries.value.length ? loginSeries.value : buildDateSeries([], loginDays)))

type ChartKind = 'post' | 'comment' | 'login'
type ChartHover = { x: number; y: number; dotX: number; dotY: number; label: string; value: number; pinned: boolean }

const postHover = ref<ChartHover | null>(null)
const commentHover = ref<ChartHover | null>(null)
const loginHover = ref<ChartHover | null>(null)
const postPinned = ref(false)
const commentPinned = ref(false)
const loginPinned = ref(false)

const setError = (msg: string) => {
  errorMessage.value = msg
}

const formatKey = (date: Date) => {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

const formatLabel = (date: Date) => {
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${m}-${d}`
}

const buildDateSeries = (dates: Array<string | undefined>, days: number) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const keys: string[] = []
  const labels: string[] = []
  for (let i = days - 1; i >= 0; i -= 1) {
    const d = new Date(today)
    d.setDate(today.getDate() - i)
    keys.push(formatKey(d))
    labels.push(formatLabel(d))
  }

  const counts = new Map(keys.map(key => [key, 0]))
  dates.forEach(dateStr => {
    if (!dateStr) return
    const date = new Date(dateStr)
    if (Number.isNaN(date.getTime())) return
    date.setHours(0, 0, 0, 0)
    const key = formatKey(date)
    if (counts.has(key)) {
      counts.set(key, (counts.get(key) || 0) + 1)
    }
  })

  return labels.map((label, index) => ({
    label,
    value: counts.get(keys[index]) || 0
  }))
}

const buildLinePath = (points: Array<{ x: number; y: number }>) => {
  if (!points.length) return ''
  
  // Catmull-Rom to Cubic Bezier conversion
  const k = 0.3 // tension
  
  let path = `M ${points[0].x.toFixed(1)},${points[0].y.toFixed(1)}`
  
  for (let i = 0; i < points.length - 1; i++) {
    const p0 = points[Math.max(0, i - 1)]
    const p1 = points[i]
    const p2 = points[i + 1]
    const p3 = points[Math.min(points.length - 1, i + 2)]
    
    const cp1x = p1.x + (p2.x - p0.x) * k
    const cp1y = p1.y + (p2.y - p0.y) * k
    const cp2x = p2.x - (p3.x - p1.x) * k
    const cp2y = p2.y - (p3.y - p1.y) * k
    
    path += ` C ${cp1x.toFixed(1)},${cp1y.toFixed(1)} ${cp2x.toFixed(1)},${cp2y.toFixed(1)} ${p2.x.toFixed(1)},${p2.y.toFixed(1)}`
  }
  
  return path
}

const buildLineChart = (series: SeriesPoint[], width: number, height: number, fallbackLength: number) => {
  const safeSeries = series.length ? series : Array.from({ length: fallbackLength }, () => ({ label: '', value: 0 }))
  const values = safeSeries.map(point => point.value)
  const max = Math.max(...values, 1)
  const min = 0
  const range = max - min || 1
  const padding = 10
  const usableWidth = width - padding * 2
  const usableHeight = height - padding * 2
  const step = safeSeries.length > 1 ? usableWidth / (safeSeries.length - 1) : 0
  const points = safeSeries.map((point, index) => {
    const x = padding + index * step
    const y = height - padding - ((point.value - min) / range) * usableHeight
    return { x, y }
  })
  const line = buildLinePath(points)
  const first = points[0]
  const last = points[points.length - 1]
  const baseY = height - padding
  const area = `${line} L ${last.x.toFixed(1)},${baseY.toFixed(1)} L ${first.x.toFixed(1)},${baseY.toFixed(1)} Z`
  return { line, area, last, max, points, width, height }
}

const darkenHex = (hex: string, amount = 0.18) => {
  if (!hex || !hex.startsWith('#')) return hex
  const raw = hex.replace('#', '')
  const normalized = raw.length === 3 ? raw.split('').map(c => c + c).join('') : raw
  if (normalized.length !== 6) return hex
  const clamp = (value: number) => Math.min(255, Math.max(0, value))
  const num = parseInt(normalized, 16)
  const r = clamp(((num >> 16) & 0xff) - Math.round(255 * amount))
  const g = clamp(((num >> 8) & 0xff) - Math.round(255 * amount))
  const b = clamp((num & 0xff) - Math.round(255 * amount))
  return `#${[r, g, b].map(v => v.toString(16).padStart(2, '0')).join('')}`
}

const toRgba = (hex: string, alpha: number) => {
  if (!hex || !hex.startsWith('#')) return `rgba(79, 156, 255, ${alpha})`
  const raw = hex.replace('#', '')
  const normalized = raw.length === 3 ? raw.split('').map(c => c + c).join('') : raw
  if (normalized.length !== 6) return `rgba(79, 156, 255, ${alpha})`
  const num = parseInt(normalized, 16)
  const r = (num >> 16) & 0xff
  const g = (num >> 8) & 0xff
  const b = num & 0xff
  return `rgba(${r}, ${g}, ${b}, ${alpha})`
}

const buildPieChart = (slices: PieSlice[]) => {
  // Analogous colors: Cyan -> Blue -> Purple -> Pink (Tech/Neon feel)
  const palette = ['#06b6d4', '#3b82f6', '#8b5cf6', '#d946ef', '#f43f5e']
  const total = slices.reduce((sum, slice) => sum + slice.value, 0)
  const safeTotal = total || 1
  const radius = pieSegmentRadius
  const circumference = 2 * Math.PI * radius
  let offset = 0
  const source = total === 0
    ? [{ label: '暂无数据', value: 1, color: '#e2e8f0' }]
    : (slices.length ? slices : [{ label: '暂无数据', value: 1, color: '#e2e8f0' }])
  const mapped: PieChartSlice[] = source.map((slice, index) => {
    const percent = Math.round((slice.value / safeTotal) * 100)
    const dashValue = (slice.value / safeTotal) * circumference
    const color = slice.color || palette[index % palette.length]
    const startColor = darkenHex(color, -0.12)
    const endColor = darkenHex(color, 0.18)
    const result: PieChartSlice = {
      label: slice.label,
      value: slice.value,
      id: slice.id,
      name: slice.name,
      color,
      startColor,
      endColor,
      shadow: darkenHex(color, 0.28),
      dash: `${dashValue} ${circumference - dashValue}`,
      offset: -offset,
      percent,
      gradId: `pie-grad-${index}`
    }
    offset += dashValue
    return result
  })
  return {
    slices: mapped,
    empty: total === 0,
    total,
    circumference
  }
}

const postChart = computed(() => buildLineChart(postSeries.value, 320, 170, postCommentDays))
const commentChart = computed(() => buildLineChart(commentSeries.value, 320, 170, postCommentDays))
const loginChart = computed(() => buildLineChart(loginSeries.value, 520, 170, loginDays))
const pieChart = computed(() => buildPieChart(categorySlices.value))

const activePieIndex = ref(0)
const pieActiveIndex = computed(() => {
  const count = pieChart.value.slices.length
  if (!count) return null
  return Math.max(0, Math.min(activePieIndex.value, count - 1))
})
const activePie = computed(() => {
  if (pieActiveIndex.value === null) return null
  return pieChart.value.slices[pieActiveIndex.value] || null
})
const pieTotal = computed(() => pieChart.value.total || 0)
const pieAccentStart = computed(() => activePie.value?.startColor || '#7dd3fc')
const pieAccentEnd = computed(() => activePie.value?.endColor || '#60a5fa')
const pieAccentStyle = computed(() => ({
  '--accent-start': pieAccentStart.value,
  '--accent-end': pieAccentEnd.value
}))
const pieGlowStyle = computed(() => ({
  background: `radial-gradient(circle at 30% 30%, ${toRgba(pieAccentStart.value, 0.35)} 0%, transparent 70%)`
}))
const pieActivePercent = computed(() => {
  if (pieChart.value.empty || !activePie.value || !pieTotal.value) return 0
  return (activePie.value.value / pieTotal.value) * 100
})
const pieRingCircumference = 2 * Math.PI * pieRingRadius
const pieRingStyle = computed(() => ({
  strokeDasharray: `${pieRingCircumference}`,
  strokeDashoffset: `${pieRingCircumference - (pieActivePercent.value / 100) * pieRingCircumference}`
}))
const pieCenterLabel = computed(() => {
  if (activePie.value) return activePie.value.name || activePie.value.label
  if (pieChart.value.empty) return '暂无数据'
  return '全部'
})
const pieCenterMeta = computed(() => {
  if (!pieChart.value.empty && activePie.value && activePie.value.label) return `ID ${activePie.value.label}`
  return ''
})
const pieCenterAmount = computed(() => {
  if (pieChart.value.empty || !activePie.value) return 0
  return activePie.value.value
})
const pieCenterPercent = computed(() => `${pieActivePercent.value.toFixed(1)}%`)
const setActivePie = (index: number) => {
  activePieIndex.value = index
}

const pieSliceStyle = (slice: { dash: string; offset: number }, index: number) => ({
  '--dash': slice.dash,
  '--offset': String(slice.offset),
  '--circumference': String(pieChart.value.circumference),
  '--delay': `${index * 80}ms`
})

const buildYAxisTicks = (max: number, steps = 4) => {
  const safeMax = Math.max(1, max)
  const step = Math.ceil(safeMax / steps)
  const top = step * steps
  return Array.from({ length: steps + 1 }, (_, idx) => top - idx * step)
}

const postYAxis = computed(() => buildYAxisTicks(postChart.value.max, 4))
const commentYAxis = computed(() => buildYAxisTicks(commentChart.value.max, 4))
const loginYAxis = computed(() => buildYAxisTicks(loginChart.value.max, 4))

const buildAxisTicks = (axis: SeriesPoint[], chart: { points: Array<{ x: number }>; width: number }) => {
  if (!axis.length || !chart.points.length || !chart.width) return []
  const length = Math.min(axis.length, chart.points.length)
  return axis.slice(0, length).map((point, index) => {
    const x = chart.points[index]?.x ?? 0
    const left = `${(x / chart.width) * 100}%`
    return { label: point.label, left }
  })
}

const postAxisTicks = computed(() => buildAxisTicks(postAxis.value, postChart.value))
const commentAxisTicks = computed(() => buildAxisTicks(commentAxis.value, commentChart.value))
const loginAxisTicks = computed(() => buildAxisTicks(loginAxis.value, loginChart.value))

const buildGridStyle = (chart: { points: Array<{ x: number }>; width: number }) => {
  if (!chart.points.length || chart.width <= 0) return {}
  const offset = chart.points[0]?.x ?? 0
  const step = chart.points.length > 1 ? chart.points[1].x - chart.points[0].x : chart.width
  return {
    '--grid-step': `${(step / chart.width) * 100}%`,
    '--grid-offset': `${(offset / chart.width) * 100}%`
  }
}

const postGridStyle = computed(() => buildGridStyle(postChart.value))
const commentGridStyle = computed(() => buildGridStyle(commentChart.value))
const loginGridStyle = computed(() => buildGridStyle(loginChart.value))

const getChartConfig = (kind: ChartKind) => {
  if (kind === 'post') {
    return { axis: postAxis.value, chart: postChart.value, hover: postHover, pinned: postPinned }
  }
  if (kind === 'comment') {
    return { axis: commentAxis.value, chart: commentChart.value, hover: commentHover, pinned: commentPinned }
  }
  return { axis: loginAxis.value, chart: loginChart.value, hover: loginHover, pinned: loginPinned }
}

const chartDims: Record<ChartKind, { width: number; height: number }> = {
  post: { width: 320, height: 170 },
  comment: { width: 320, height: 170 },
  login: { width: 520, height: 170 }
}

const chartLeft = (kind: ChartKind, dotX: number) => {
  const width = chartDims[kind].width
  return `calc(40px + ((100% - 60px) * ${dotX} / ${width}))`
}

const chartTop = (kind: ChartKind, dotY: number) => {
  const height = chartDims[kind].height
  return `calc(20px + ((100% - 50px) * ${dotY} / ${height}))`
}

const isTooltipBelow = (kind: ChartKind, dotY: number) => {
  const height = chartDims[kind].height
  return dotY / height <= 0.28
}

const resolveChartHover = (event: MouseEvent, kind: ChartKind) => {
  const { axis, chart } = getChartConfig(kind)
  const points = chart.points || []
  const maxIndex = Math.min(axis.length, points.length) - 1
  if (maxIndex < 0) return null
  const canvas = event.currentTarget as HTMLElement | null
  if (!canvas) return null
  
  const rect = canvas.getBoundingClientRect()
  const paddingLeft = 40
  const paddingRight = 20
  const chartWidthPixels = rect.width - paddingLeft - paddingRight
  
  // Calculate mouse position relative to chart area
  const mouseX = event.clientX - rect.left - paddingLeft
  const clampedX = Math.max(0, Math.min(chartWidthPixels, mouseX))
  
  // Map to internal chart coordinates (0..320)
  const chartX = (clampedX / chartWidthPixels) * chart.width
  
  const firstX = points[0].x
  const step = points.length > 1 ? points[1].x - points[0].x : 0
  let index = 0
  if (step) {
    index = Math.round((chartX - firstX) / step)
  }
  index = Math.max(0, Math.min(maxIndex, index))
  
  const point = points[index]
  const axisPoint = axis[index]
  
  // Return chart coordinates (dotX, dotY) directly
  // We will handle the screen mapping in CSS for perfect alignment
  return {
    x: 0, // Not used for dot positioning anymore
    y: 0, // Not used for dot positioning anymore
    dotX: point.x,
    dotY: point.y,
    label: axisPoint?.label ?? '',
    value: axisPoint?.value ?? 0,
    pinned: false
  }
}

const handleChartMove = (event: MouseEvent, kind: ChartKind) => {
  const config = getChartConfig(kind)
  if (config.pinned.value) return
  const hover = resolveChartHover(event, kind)
  config.hover.value = hover
}

const clearChartHover = (kind: ChartKind) => {
  const config = getChartConfig(kind)
  if (config.pinned.value) return
  config.hover.value = null
}

const toggleChartPin = (event: MouseEvent, kind: ChartKind) => {
  const config = getChartConfig(kind)
  if (config.pinned.value) {
    config.pinned.value = false
    config.hover.value = resolveChartHover(event, kind)
    return
  }
  const hover = resolveChartHover(event, kind)
  if (!hover) return
  config.pinned.value = true
  config.hover.value = { ...hover, pinned: true }
}

const loadChartData = async () => {
  try {
    const chartRes = await adminService.getCharts({
      postDays: postCommentDays,
      commentDays: postCommentDays,
      loginDays
    })
    if (chartRes.success && chartRes.data) {
      postSeries.value = chartRes.data.postSeries || buildDateSeries([], postCommentDays)
      commentSeries.value = chartRes.data.commentSeries || buildDateSeries([], postCommentDays)
      loginSeries.value = chartRes.data.loginSeries || buildDateSeries([], loginDays)
      categorySlices.value = chartRes.data.categorySlices || []
    } else {
      postSeries.value = buildDateSeries([], postCommentDays)
      commentSeries.value = buildDateSeries([], postCommentDays)
      loginSeries.value = buildDateSeries([], loginDays)
      categorySlices.value = []
    }
  } catch (e) {
    console.error('加载图表数据失败', e)
    postSeries.value = buildDateSeries([], postCommentDays)
    commentSeries.value = buildDateSeries([], postCommentDays)
    loginSeries.value = buildDateSeries([], loginDays)
    categorySlices.value = []
  }
}

const loadDashboard = async () => {
  try {
    const res = await adminService.getDashboard()
    if (res.success && res.data) {
      applyDashboard(res.data)
    } else {
      setError(res.message || '加载管理看板失败')
    }
  } catch (e) {
    console.error('加载管理看板失败', e)
    setError('加载管理看板失败，请检查登录状态或网络')
  } finally {
    await loadChartData()
  }
}

const applyDashboard = (data: DashboardPayload) => {
  dashboardCache.value = data
  stats.value = [
    { label: '总文章', value: String(data.stats?.totalPosts ?? 0), icon: 'fas fa-feather-alt', bg: 'linear-gradient(135deg, #e0f2ff, #cde6ff)' },
    { label: '用户数', value: String(data.stats?.totalUsers ?? 0), icon: 'fas fa-users', bg: 'linear-gradient(135deg, #e6f7ff, #e8f5ff)' },
    { label: '评论', value: String(data.stats?.totalComments ?? 0), icon: 'fas fa-comments', bg: 'linear-gradient(135deg, #fff4e6, #ffe8d9)' },
    { label: '待审核文章', value: String(data.stats?.pendingPosts ?? 0), icon: 'fas fa-clipboard-check', bg: 'linear-gradient(135deg, #f2e8ff, #e7ddff)' },
    { label: '待审核评论', value: String(data.stats?.pendingComments ?? 0), icon: 'fas fa-comment-dots', bg: 'linear-gradient(135deg, #fff4e6, #ffe8d9)' }
  ]
  recentPosts.value = data.recentPosts || []
  moderationQueue.value = data.moderationQueue || []
  engagementMetrics.splice(0, engagementMetrics.length, ...(data.metrics || []))
  announcements.splice(0, announcements.length, ...(data.announcements || []))
}

const goToBlog = () => {
  router.push(isAdmin ? '/admin' : '/blog')
}

const goToChat = () => {
  router.push('/chat')
}

const goToWrite = () => {
  router.push('/admin/blog')
}

const goToProfile = () => {
  router.push('/admin/profile')
}

const goSearch = (kw: string) => {
  if (!kw) return
  router.push({ name: 'AdminBlogForward', query: { keyword: kw } })
}

const goSearchHero = () => goSearch(heroKeyword.value.trim())

const handleStatClick = (label: string) => {
  if (label === '总文章') {
    router.push('/admin/blog')
  }
  if (label === '用户数') {
    router.push('/admin/users')
  }
  if (label === '待审核文章' || label === '待审核评论') {
    router.push('/admin/moderation')
  }
}

const statusClass = (status?: number) => {
  if (status === 1) return 'published'
  if (status === 2) return 'review'
  if (status === 3) return 'rejected'
  return 'draft'
}

const approvePost = async (id: number) => {
  try {
    await adminService.approvePost(id)
    recentPosts.value = recentPosts.value.map(p => p.id === id ? { ...p, status: 1, statusLabel: '已发布' } : p)
  } catch (e) {
    console.error('通过文章失败', e)
    setError('通过文章失败，请重试')
  }
}

const rejectPost = async (id: number) => {
  try {
    await adminService.rejectPost(id)
    recentPosts.value = recentPosts.value.map(p => p.id === id ? { ...p, status: 3, statusLabel: '已下架' } : p)
  } catch (e) {
    console.error('拒绝文章失败', e)
    setError('拒绝文章失败，请重试')
  }
}

const deletePost = async (id: number) => {
  try {
    await adminService.deletePost(id)
    recentPosts.value = recentPosts.value.filter(p => p.id !== id)
  } catch (e) {
    console.error('删除文章失败', e)
    setError('删除文章失败，请重试')
  }
}

const handleModeration = async (item: ModerationItem, approve: boolean) => {
  try {
    if (item.type === '评论') {
      if (approve) {
        await adminService.approveComment(item.id)
      } else {
        await adminService.deleteComment(item.id)
      }
    } else {
      if (approve) {
        await adminService.approvePost(item.id)
      } else {
        await adminService.rejectPost(item.id)
      }
    }
  } catch (e) {
    console.error('处理审核任务失败', e)
    setError('处理审核任务失败，请重试')
  } finally {
    moderationQueue.value = moderationQueue.value.filter(q => !(q.id === item.id && q.type === item.type))
  }
}

let sse: EventSource | null = null

const setupSse = () => {
  const token = localStorage.getItem('token')
  if (!token) return
  const url = `http://localhost:8082/api/admin/stream?token=${encodeURIComponent(token)}`
  sse = new EventSource(url)
  sse.addEventListener('refresh', () => loadDashboard())
  sse.onerror = () => {
    setError('实时连接中断，尝试刷新或重登录')
    sse?.close()
    sse = null
  }
}

const exportData = () => {
  if (!dashboardCache.value) {
    setError('暂无可导出的数据，请先加载看板')
    return
  }
  const blob = new Blob([JSON.stringify(dashboardCache.value, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'dashboard.json'
  a.click()
  URL.revokeObjectURL(url)
}

const closeAnnModal = () => {
  openAnnModal.value = false
}

const submitAnn = async () => {
  if (!annForm.title.trim() || !annForm.content.trim()) {
    setError('公告标题和内容不能为空')
    return
  }
  try {
    await adminService.createAnnouncement({
      title: annForm.title,
      content: annForm.content,
      type: annForm.type
    })
    closeAnnModal()
    annForm.title = ''
    annForm.content = ''
    annForm.type = '系统'
    await loadDashboard()
  } catch (e) {
    console.error('发布公告失败', e)
    setError('发布公告失败，请重试')
  }
}

onMounted(() => {
  if (isAdmin) {
    loadDashboard()
    setupSse()
  }
})

onUnmounted(() => {
  if (sse) {
    sse.close()
    sse = null
  }
})
</script>

<style scoped>
.admin-page {
  min-height: 100vh;
  background: linear-gradient(180deg, rgba(230, 242, 255, 0.65), #ffffff);
  padding: 64px 0 48px;
}

.content-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px 40px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  padding: 24px 0 0;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
}

.stat-card.clickable {
  cursor: pointer;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  color: var(--primary-dark);
}

.stat-icon i {
  font-size: 18px;
}

.stat-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-label {
  font-size: 13px;
  color: var(--text-muted);
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-dark);
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin: 20px 0 10px;
}

.chart-row {
  margin: 16px 0;
}

.chart-card {
  position: relative;
  padding: 24px;
  border-radius: 2.5rem;
  border: 1px solid rgba(255, 255, 255, 0.8);
  background: #ffffff;
  box-shadow: 0 20px 50px rgba(8, 112, 184, 0.07);
  overflow: hidden;
}

.chart-decor-blob {
  position: absolute;
  border-radius: 9999px;
  filter: blur(64px);
  opacity: 0.6;
  z-index: 0;
  pointer-events: none;
}

.chart-decor-blob--1 {
  top: -80px;
  right: -80px;
  width: 250px;
  height: 250px;
  background: #eff6ff;
}

.chart-decor-blob--2 {
  bottom: -60px;
  left: -60px;
  width: 200px;
  height: 200px;
  background: #eef2ff;
}

.chart-decor-blob--3 {
  top: -80px;
  right: -80px;
  width: 250px;
  height: 250px;
  background: #f5f3ff;
}

.chart-decor-blob--4 {
  bottom: -60px;
  left: -60px;
  width: 200px;
  height: 200px;
  background: #fdf4ff;
}

.chart-meta-box {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  background: rgba(248, 250, 252, 0.5);
  border: 1px solid rgba(241, 245, 249, 0.8);
  padding: 6px 16px;
  border-radius: 12px;
}

.chart-meta-label {
  font-size: 10px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #94a3b8;
  font-weight: 700;
}

.chart-meta-value {
  font-size: 18px;
  font-weight: 700;
  color: #3b82f6;
  line-height: 1.2;
}

.chart-yaxis {
  position: absolute;
  left: 0;
  top: 20px;
  bottom: 30px;
  width: 40px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: flex-end;
  padding-right: 8px;
  font-size: 11px;
  color: #94a3b8;
  pointer-events: none;
}

.chart-canvas {
  position: relative;
  z-index: 1;
  padding: 20px 20px 30px 40px;
  border-radius: 0;
  background: transparent;
  border: none;
  box-shadow: none;
  backdrop-filter: none;
  cursor: crosshair;
  user-select: none;
  height: 220px;
}

.chart-canvas::before {
  content: none;
}

.chart-canvas::after {
  content: none;
}

.chart-grid-lines {
  position: absolute;
  inset: 20px 0 30px 40px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  pointer-events: none;
}

.chart-grid-line {
  width: 100%;
  height: 1px;
  border-top: 1px dashed rgba(226, 232, 240, 0.4);
}

.line-chart {
  width: 100%;
  height: 100%;
  display: block;
  overflow: visible;
}

.chart-cursor-line {
  position: absolute;
  top: 20px;
  bottom: 30px;
  width: 0;
  border-left: 1px dashed #3B82F6;
  opacity: 0.5;
  pointer-events: none;
  z-index: 5;
  transform: none;
}

.chart-cursor-line--violet {
  border-color: #6366F1;
}

.chart-cursor-line--mint {
  border-color: #10B981;
}

.chart-active-dot {
  position: absolute;
  width: 14px;
  height: 14px;
  box-sizing: content-box;
  border-radius: 50%;
  background-color: #3B82F6;
  border: 5px solid #ffffff;
  box-shadow: 0 4px 8px rgba(59, 130, 246, 0.4);
  transition: all 0.2s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  transform: translate(-50%, -50%);
  z-index: 6;
  pointer-events: none;
  animation: dot-pop 0.3s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}

.chart-active-dot--violet {
  background-color: #6366F1;
  box-shadow: 0 4px 6px rgba(99, 102, 241, 0.4);
}

.chart-active-dot--mint {
  background-color: #10B981;
  box-shadow: 0 4px 6px rgba(16, 185, 129, 0.4);
}

.chart-tooltip {
  position: absolute;
  min-width: 160px;
  padding: 16px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(59, 130, 246, 0.1);
  box-shadow: 0 10px 25px -5px rgba(59, 130, 246, 0.15), 0 8px 10px -6px rgba(59, 130, 246, 0.1);
  backdrop-filter: blur(12px);
  transform: translate(-50%, -100%);
  margin-top: -16px;
  z-index: 10;
  pointer-events: none;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.chart-tooltip--below {
  transform: translate(-50%, 0);
  margin-top: 16px;
}

.chart-tooltip-header {
  font-size: 13px;
  font-weight: 700;
  color: #1e3a8a;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(59, 130, 246, 0.1);
}

.chart-tooltip-header--violet {
  color: #312e81;
  border-color: rgba(99, 102, 241, 0.1);
}

.chart-tooltip-header--mint {
  color: #064e3b;
  border-color: rgba(16, 185, 129, 0.1);
}

.chart-tooltip-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chart-tooltip-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: #64748b;
}

.chart-tooltip-value {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 16px;
  font-weight: 700;
  color: #3b82f6;
}

.chart-tooltip-value--violet {
  color: #6366F1;
}

.chart-tooltip-value--mint {
  color: #10B981;
}

.chart-axis {
  position: absolute;
  bottom: 0;
  left: 40px;
  right: 20px;
  height: 24px;
  font-size: 11px;
  font-weight: 500;
  font-variant-numeric: tabular-nums;
  color: var(--text-muted);
  overflow: visible;
}

.chart-axis-tick {
  position: absolute;
  top: 0;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}

.chart-axis-tick::before {
  content: '';
  width: 1px;
  height: 4px;
  background: rgba(148, 163, 184, 0.2);
}

.chart-footer {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  font-size: 12px;
  color: var(--text-muted);
}

.chart-wide .line-chart {
  /* height: 160px; Removed to allow SVG to fill the container naturally */
}

.chart-pie .chart-header {
  margin-bottom: 18px;
}

.pie-wrap {
  position: relative;
  z-index: 1;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
  gap: 60px;
}

.pie-legend {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.pie-stage {
  position: relative;
  width: 460px;
  height: 460px;
  margin: 0 auto;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 24px 54px rgba(15, 23, 42, 0.05), inset 0 0 40px rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(8px);
}

.pie-stage::before {
  content: '';
  position: absolute;
  inset: 10px;
  border-radius: 30px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.7), rgba(255, 255, 255, 0));
  opacity: 0.6;
  pointer-events: none;
}

.pie-glow {
  position: absolute;
  inset: -40px;
  border-radius: 50%;
  filter: blur(50px);
  opacity: 0.9;
  transition: background 0.5s ease;
  pointer-events: none;
  z-index: 0;
}

.pie-chart {
  width: 100%;
  height: 100%;
  display: block;
  position: relative;
  z-index: 2;
}

.pie-slice {
  stroke-dasharray: var(--dash);
  stroke-dashoffset: var(--offset);
  animation: pie-reveal 1.2s ease forwards;
  animation-delay: var(--delay, 0ms);
  transition: transform 0.3s ease, stroke-width 0.3s ease, filter 0.3s ease;
  transform-origin: 200px 200px;
}

.pie-top .pie-slice {
  cursor: pointer;
  filter: drop-shadow(0 12px 18px rgba(15, 23, 42, 0.12));
}

.pie-slice--active {
  filter: drop-shadow(0 16px 24px rgba(15, 23, 42, 0.22));
  transform: scale(1.05);
}

.pie-highlight-ring {
  filter: drop-shadow(0 0 8px rgba(255, 255, 255, 0.6));
  stroke-dasharray: var(--dash);
  stroke-dashoffset: var(--offset);
  transform-origin: center;
  transition: all 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
  animation: pie-pop 0.8s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}

.pie-ring {
  fill: none;
  stroke: rgba(148, 163, 184, 0.35);
  stroke-width: 2;
  opacity: 0.7;
}

.pie-ring--active {
  stroke-width: 4;
  stroke-linecap: round;
  transition: stroke-dashoffset 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.pie-center {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 6px;
  z-index: 3;
  pointer-events: none;
}

.pie-center-label {
  font-size: 11px;
  letter-spacing: 0.3em;
  text-transform: uppercase;
  color: #94a3b8;
  max-width: 220px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pie-center-meta {
  font-size: 10px;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: #a3b1c6;
  max-width: 220px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pie-center-value {
  font-size: 48px;
  font-weight: 600;
  line-height: 1;
  letter-spacing: -0.02em;
  font-variant-numeric: tabular-nums;
  background-image: linear-gradient(135deg, var(--accent-start), var(--accent-end));
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.pie-center-percent {
  font-size: 12px;
  font-weight: 600;
  color: #94a3b8;
}

.pie-fade-enter-active,
.pie-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.pie-fade-enter-from,
.pie-fade-leave-to {
  opacity: 0;
  transform: translateY(6px);
}

.pie-pop-enter-active,
.pie-pop-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease, filter 0.3s ease;
}

.pie-pop-enter-from,
.pie-pop-leave-to {
  opacity: 0;
  transform: translateY(6px) scale(0.96);
  filter: blur(4px);
}

.pie-legend {
  display: grid;
  gap: 12px;
  align-content: center;
}

.pie-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 12px;
  border: 1px solid rgba(226, 232, 240, 0.8);
  background: rgba(255, 255, 255, 0.65);
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.04);
  transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease, opacity 0.25s ease;
  cursor: pointer;
  font-size: 13px;
  color: var(--text-dark);
}

.pie-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 30px rgba(15, 23, 42, 0.08);
}

.pie-item.is-active {
  border-color: var(--legend-accent, rgba(79, 156, 255, 0.4));
  box-shadow: 0 16px 30px rgba(15, 23, 42, 0.08);
  transform: translateY(-2px);
}

.pie-swatch {
  width: 34px;
  height: 6px;
  border-radius: 999px;
  box-shadow: 0 6px 12px rgba(15, 23, 42, 0.12);
}

.pie-label {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  color: #0f172a;
}

.pie-label-id {
  font-weight: 600;
}

.pie-label-name {
  font-size: 11px;
  color: #94a3b8;
}

.pie-value {
  font-weight: 600;
  color: var(--text-muted);
}

@keyframes line-draw {
  to {
    stroke-dashoffset: 0;
  }
}

@keyframes area-rise {
  to {
    opacity: 0.35;
    transform: translateY(0);
  }
}

@keyframes dot-pop {
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes pie-reveal {
  from {
    stroke-dashoffset: var(--circumference);
  }
  to {
    stroke-dashoffset: var(--offset);
  }
}

.management-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
  margin: 20px 0;
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

.content-card {
  padding: 18px;
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

.status-pill {
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.status-pill.published {
  background: rgba(34, 197, 94, 0.15);
  color: #15803d;
}

.status-pill.review {
  background: rgba(251, 191, 36, 0.2);
  color: #a16207;
}

.status-pill.draft {
  background: rgba(148, 163, 184, 0.2);
  color: #475569;
}

.status-pill.rejected {
  background: rgba(239, 68, 68, 0.2);
  color: #b91c1c;
}

.side-card {
  padding: 18px;
}

.badge {
  background: linear-gradient(120deg, var(--primary-color), var(--primary-light));
  color: #fff;
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 700;
}

.queue-list {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.queue-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border: 1px solid var(--border-light);
  border-radius: var(--border-radius-sm);
  transition: var(--transition-default);
}

.queue-item:hover {
  border-color: var(--primary-light);
  box-shadow: var(--shadow-sm);
}

.queue-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.queue-title {
  font-weight: 600;
  color: var(--text-dark);
}

.queue-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text-muted);
  font-size: 12px;
}

.divider {
  color: var(--border-color);
}

.queue-actions {
  display: flex;
  gap: 8px;
}

.insight-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
  padding: 0;
}

.insight-card,
.announcement-card {
  padding: 18px;
}

.announcement-actions {
  display: flex;
  gap: 8px;
}

.metrics {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}

.metric {
  padding: 12px;
  border: 1px solid var(--border-light);
  border-radius: var(--border-radius-md);
  background: #fff;
}

.empty-row {
  padding: 12px;
  color: var(--text-muted);
  font-size: 13px;
}

.metric-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.metric-label {
  color: var(--text-muted);
  font-size: 13px;
}

.metric-value {
  font-weight: 700;
  color: var(--text-dark);
}

.metric-bar {
  height: 8px;
  border-radius: 999px;
  background: var(--border-light);
  overflow: hidden;
  margin-bottom: 6px;
}

.metric-bar-fill {
  height: 100%;
  background: linear-gradient(120deg, var(--primary-color), var(--primary-light));
}

.metric-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-muted);
}

.metric-foot .up {
  color: #22c55e;
}

.metric-foot .down {
  color: #ef4444;
}

.announcement-list {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.announcement {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 10px;
  align-items: center;
  padding: 10px;
  border-radius: var(--border-radius-sm);
  border: 1px solid var(--border-light);
  transition: var(--transition-default);
}

.announcement:hover {
  border-color: var(--primary-light);
  box-shadow: var(--shadow-sm);
}

.announcement-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.announcement-title {
  font-weight: 600;
  color: var(--text-dark);
}

.announcement-time {
  font-size: 12px;
  color: var(--text-muted);
}

.announcement-tag {
  font-size: 12px;
  padding: 6px 10px;
  border-radius: 999px;
  background: var(--border-light);
  color: var(--text-dark);
}

.row-actions {
  display: flex;
  gap: 8px;
}

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  z-index: 3000;
}

.modal {
  width: 480px;
  max-width: 90vw;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  padding: 16px;
}

.modal-header,
.modal-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.modal-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin: 12px 0;
}

.modal-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 14px;
  color: var(--text-dark);
}

.modal-field input,
.modal-field textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
}

.modal-footer .pill-btn {
  min-width: 96px;
}

.error-banner {
  margin-bottom: 12px;
  padding: 10px 12px;
  border-radius: var(--border-radius-sm);
  background: rgba(239, 68, 68, 0.08);
  color: #b91c1c;
  border: 1px solid rgba(239, 68, 68, 0.2);
}

@media (max-width: 1024px) {
  .management-grid,
  .insight-grid {
    grid-template-columns: 1fr;
    margin: 20px 16px;
  }

  .chart-grid {
    grid-template-columns: 1fr;
    margin: 20px 16px 10px;
  }

  .chart-row {
    margin: 12px 16px;
  }

  .pie-wrap {
    grid-template-columns: 1fr;
    justify-items: center;
    text-align: center;
  }

  .stats-grid {
    padding: 24px 16px 0;
  }
}

@media (max-width: 640px) {
  .chart-card {
    padding: 16px;
  }

  .chart-canvas {
    padding: 10px 12px 10px 32px;
  }

  .chart-title {
    font-size: 18px;
  }

  .stats-grid {
    padding: 20px 12px 0;
  }

  .pie-stage {
    width: 240px;
    height: 240px;
  }

  .pie-center-value {
    font-size: 36px;
  }
}
</style>
