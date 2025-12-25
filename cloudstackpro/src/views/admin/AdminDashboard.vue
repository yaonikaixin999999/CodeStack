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

      <section class="line-grid">
        <div
          v-for="chart in lineCharts"
          :key="chart.id"
          class="card line-card"
          :style="chart.accentStyle"
        >
          <div class="chart-header line-header">
            <div>
              <p class="chart-label">{{ chart.label }}</p>
              <h3 class="chart-title">{{ chart.title }}</h3>
            </div>
            <div class="line-header-actions">
              <span class="chart-chip">{{ chart.rangeLabel }}</span>
            </div>
          </div>
          <div class="line-chart">
            <svg
              class="line-canvas"
              :viewBox="`0 0 ${chart.width} ${chart.height}`"
              @pointermove="handleLinePointer(chart.id, $event)"
              @pointerleave="clearLineActive(chart.id)"
              @pointerdown="handleLinePointer(chart.id, $event)"
            >
              <defs>
                <linearGradient :id="chart.strokeId" x1="0%" y1="0%" x2="100%" y2="0%">
                  <stop offset="0%" :stop-color="chart.accentStart" stop-opacity="0.95" />
                  <stop offset="100%" :stop-color="chart.accentEnd" stop-opacity="0.95" />
                </linearGradient>
                <linearGradient :id="chart.areaId" x1="0%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" :stop-color="chart.accentStart" stop-opacity="0.28" />
                  <stop offset="100%" :stop-color="chart.accentEnd" stop-opacity="0" />
                </linearGradient>
              </defs>
              <g class="line-grid-lines">
                <line
                  v-for="(grid, index) in chart.gridLines"
                  :key="`${chart.id}-grid-${index}`"
                  class="line-grid-line"
                  :x1="chart.gridLeft"
                  :x2="chart.gridRight"
                  :y1="grid.y"
                  :y2="grid.y"
                />
              </g>
              <g class="line-axis">
                <text
                  v-for="(grid, index) in chart.gridLines"
                  :key="`${chart.id}-y-${index}`"
                  class="line-axis-label line-axis-label--y"
                  :x="chart.gridLeft - 8"
                  :y="grid.y + 3"
                >
                  {{ grid.value }}
                </text>
                <text
                  v-for="(tick, index) in chart.axisLabels"
                  :key="`${chart.id}-x-${index}`"
                  class="line-axis-label line-axis-label--x"
                  :x="tick.x"
                  :y="chart.axisY"
                  :style="{ textAnchor: tick.anchor || 'middle' }"
                >
                  {{ tick.label }}
                </text>
              </g>
              <path
                v-if="!chart.empty"
                class="line-area"
                :d="chart.areaPath"
                :fill="`url(#${chart.areaId})`"
              />
              <path
                v-if="!chart.empty"
                class="line-path"
                :d="chart.path"
                :stroke="`url(#${chart.strokeId})`"
                pathLength="100"
              />
              <g v-if="chart.activePoint" class="line-active">
                <line
                  class="line-cross"
                  :x1="chart.activePoint.x"
                  :x2="chart.activePoint.x"
                  :y1="chart.gridTop"
                  :y2="chart.gridBottom"
                />
                <circle
                  class="line-active-dot"
                  :cx="chart.activePoint.x"
                  :cy="chart.activePoint.y"
                  r="5"
                />
              </g>
            </svg>
            <div v-if="chart.activePoint" class="line-tooltip" :style="chart.tooltipStyle">
              <div class="line-tooltip-label">{{ chart.activePoint.label }}</div>
              <div class="line-tooltip-value">{{ chart.activePoint.value }}{{ chart.unit }}</div>
            </div>
            <div v-if="chart.empty" class="line-empty">
              <div>暂无数据</div>
              <span>等待统计数据</span>
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
import { adminService, type AdminPost, type DashboardPayload, type ModerationItem, type ChartPoint } from '@/services/adminService'

const router = useRouter()
const currentUser = userService.getCurrentUser()
const isAdmin = currentUser?.isAdmin || false

const heroKeyword = ref('')
const errorMessage = ref('')
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

const categorySlices = ref<PieSlice[]>([])
const postSeries = ref<ChartPoint[]>([])
const commentSeries = ref<ChartPoint[]>([])
const loginSeries = ref<ChartPoint[]>([])

const pieSegmentRadius = 160
const pieRingRadius = 130

const setError = (msg: string) => {
  errorMessage.value = msg
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

type LinePoint = ChartPoint & { x: number; y: number; index: number }
type LineAxisLabel = { x: number; label: string; anchor?: 'start' | 'middle' | 'end' }
type LineGrid = { y: number; value: number }
type LineChartConfig = {
  id: 'login' | 'comment' | 'post'
  label: string
  title: string
  rangeLabel: string
  unit: string
  accentStart: string
  accentEnd: string
  series: ChartPoint[]
}

const lineChartBox = {
  width: 640,
  height: 220,
  padding: {
    top: 18,
    right: 20,
    bottom: 38,
    left: 40
  }
}

const lineActive = reactive<Record<LineChartConfig['id'], number | null>>({
  login: null,
  comment: null,
  post: null
})

const clamp = (value: number, min: number, max: number) => Math.min(max, Math.max(min, value))

const buildSmoothPath = (points: Array<{ x: number; y: number }>) => {
  if (points.length === 0) return ''
  if (points.length === 1) return `M ${points[0].x} ${points[0].y}`
  let d = `M ${points[0].x} ${points[0].y}`
  for (let i = 1; i < points.length; i += 1) {
    const prev = points[i - 1]
    const current = points[i]
    const midX = (prev.x + current.x) / 2
    const midY = (prev.y + current.y) / 2
    d += ` Q ${prev.x} ${prev.y} ${midX} ${midY}`
  }
  const last = points[points.length - 1]
  d += ` T ${last.x} ${last.y}`
  return d
}

const buildLineChartModel = (config: LineChartConfig) => {
  const { width, height, padding } = lineChartBox
  const plotWidth = width - padding.left - padding.right
  const plotHeight = height - padding.top - padding.bottom
  const series = config.series ?? []
  const values = series.map(point => point.value)
  const maxValue = values.length ? Math.max(...values) : 0
  const minValue = 0
  const range = maxValue - minValue || 1
  const count = series.length
  const step = count > 1 ? plotWidth / (count - 1) : 0
  const points: LinePoint[] = series.map((point, index) => {
    const x = padding.left + (count > 1 ? step * index : plotWidth / 2)
    const y = padding.top + (1 - (point.value - minValue) / range) * plotHeight
    return { ...point, x, y, index }
  })
  const path = buildSmoothPath(points)
  const baseY = padding.top + plotHeight
  const areaPath = points.length
    ? `${path} L ${points[points.length - 1].x} ${baseY} L ${points[0].x} ${baseY} Z`
    : ''
  const axisLabels: LineAxisLabel[] = points
    .map(point => ({ x: point.x, label: point.label, index: point.index }))
    .filter(item => {
      if (points.length <= 4) return true
      const lastIndex = points.length - 1
      const midIndex = Math.floor(lastIndex / 2)
      return item.index === 0 || item.index === midIndex || item.index === lastIndex
    })
    .map(item => {
      const lastIndex = points.length - 1
      const anchor = item.index === 0 ? 'start' : item.index === lastIndex ? 'end' : 'middle'
      return { x: item.x, label: item.label, anchor }
    })
  const gridLines: LineGrid[] = [0, 0.5, 1].map(ratio => ({
    y: padding.top + plotHeight * ratio,
    value: Math.round(maxValue - (maxValue - minValue) * ratio)
  }))
  const sum = values.reduce((total, value) => total + value, 0)
  const avg = values.length ? Math.round(sum / values.length) : 0
  const currentValue = values.length ? values[values.length - 1] : 0
  const trend = values.length >= 2 ? ((values[values.length - 1] - values[0]) / (values[0] || 1)) * 100 : 0
  const trendAbs = Math.abs(trend)
  const empty = values.length === 0
  const activeIndex = lineActive[config.id]
  const safeActiveIndex = activeIndex !== null && activeIndex !== undefined && points.length
    ? clamp(activeIndex, 0, points.length - 1)
    : null
  const activePoint = safeActiveIndex !== null ? points[safeActiveIndex] : null
  const tooltipStyle = activePoint
    ? {
      '--tooltip-x': `${(activePoint.x / width) * 100}%`,
      '--tooltip-y': `${(activePoint.y / height) * 100}%`
    }
    : {}
  const accentStyle = {
    '--accent-start': config.accentStart,
    '--accent-end': config.accentEnd,
    '--accent-soft': toRgba(config.accentStart, 0.35)
  }
  const currentLabel = empty ? '--' : `${currentValue}${config.unit}`
  const maxLabel = empty ? '--' : `${maxValue}${config.unit}`
  const avgLabel = empty ? '--' : `${avg}${config.unit}`
  const trendDisplay = values.length >= 2 ? `${trendAbs.toFixed(1)}%` : '--'
  return {
    ...config,
    width,
    height,
    gridLeft: padding.left,
    gridRight: padding.left + plotWidth,
    gridTop: padding.top,
    gridBottom: baseY,
    axisY: baseY + 16,
    axisLabels,
    gridLines,
    points,
    path,
    areaPath,
    sum,
    avg,
    currentValue,
    maxValue,
    currentLabel,
    maxLabel,
    avgLabel,
    trend,
    trendAbs,
    trendUp: trend >= 0,
    trendDisplay,
    activeIndex: safeActiveIndex,
    activePoint,
    tooltipStyle,
    accentStyle,
    strokeId: `line-stroke-${config.id}`,
    areaId: `line-area-${config.id}`,
    empty
  }
}

const lineChartConfigs = computed<LineChartConfig[]>(() => [
  {
    id: 'login',
    label: '用户活跃 · last_login_at',
    title: '近7天活跃人数',
    rangeLabel: '近7天',
    unit: '人',
    accentStart: '#3b82f6',
    accentEnd: '#60a5fa',
    series: loginSeries.value
  },
  {
    id: 'comment',
    label: '评论发布',
    title: '近4天评论数量',
    rangeLabel: '近4天',
    unit: '条',
    accentStart: '#0ea5e9',
    accentEnd: '#7dd3fc',
    series: commentSeries.value
  },
  {
    id: 'post',
    label: '文章发布',
    title: '近4天文章数量',
    rangeLabel: '近4天',
    unit: '篇',
    accentStart: '#6366f1',
    accentEnd: '#a5b4fc',
    series: postSeries.value
  }
])

const lineCharts = computed(() => lineChartConfigs.value.map(buildLineChartModel))

const handleLinePointer = (chartId: LineChartConfig['id'], event: PointerEvent) => {
  const chart = lineCharts.value.find(item => item.id === chartId)
  if (!chart || chart.points.length === 0) return
  const target = event.currentTarget as SVGSVGElement
  const rect = target.getBoundingClientRect()
  const offsetX = event.clientX - rect.left
  const ratio = offsetX / rect.width
  const index = clamp(Math.round(ratio * (chart.points.length - 1)), 0, chart.points.length - 1)
  lineActive[chartId] = index
}

const clearLineActive = (chartId: LineChartConfig['id']) => {
  lineActive[chartId] = null
}

const loadChartData = async () => {
  try {
    const chartRes = await adminService.getCharts({
      postDays: 4,
      commentDays: 4,
      loginDays: 7
    })
    if (chartRes.success && chartRes.data) {
      categorySlices.value = chartRes.data.categorySlices || []
      postSeries.value = chartRes.data.postSeries || []
      commentSeries.value = chartRes.data.commentSeries || []
      loginSeries.value = chartRes.data.loginSeries || []
    } else {
      categorySlices.value = []
      postSeries.value = []
      commentSeries.value = []
      loginSeries.value = []
    }
  } catch (e) {
    console.error('加载图表数据失败', e)
    categorySlices.value = []
    postSeries.value = []
    commentSeries.value = []
    loginSeries.value = []
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

.chart-chip {
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.04em;
  color: #64748b;
  background: rgba(248, 250, 252, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.3);
}

.line-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin: 20px 0 12px;
}

.line-card {
  position: relative;
  padding: 20px;
  border-radius: 20px;
  background: #ffffff;
  border: 1px solid rgba(226, 232, 240, 0.9);
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
  overflow: visible;
  z-index: 1;
}

.line-card::before {
  content: '';
  display: none;
}

.line-card::after {
  content: '';
  display: none;
}

.line-header {
  align-items: flex-start;
  gap: 10px;
  flex-wrap: wrap;
}

.line-header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}

.line-chart {
  position: relative;
  margin-top: 12px;
  height: 220px;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid rgba(226, 232, 240, 0.8);
  overflow: visible;
}

.line-canvas {
  width: 100%;
  height: 100%;
  display: block;
  cursor: pointer;
  pointer-events: auto;
  touch-action: none;
}

.line-grid-line {
  stroke: rgba(148, 163, 184, 0.2);
  stroke-dasharray: 4 6;
}

.line-axis-label {
  fill: #64748b;
  font-size: 10px;
  font-weight: 500;
}

.line-axis-label--y {
  text-anchor: end;
}

.line-axis-label--x {
  dominant-baseline: middle;
}

.line-area {
  opacity: 0.35;
}

.line-path {
  fill: none;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
  stroke-dasharray: 100;
  stroke-dashoffset: 100;
  animation: line-draw 1.4s ease forwards;
}


.line-cross {
  stroke: rgba(148, 163, 184, 0.45);
  stroke-dasharray: 4 6;
}

.line-active-dot {
  fill: #ffffff;
  stroke: var(--accent-end, #0ea5e9);
  stroke-width: 3;
  filter: drop-shadow(0 4px 10px rgba(15, 23, 42, 0.18));
}

.line-tooltip {
  position: absolute;
  left: var(--tooltip-x);
  top: var(--tooltip-y);
  transform: translate(-50%, -120%);
  padding: 8px 10px;
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.8);
  color: #ffffff;
  font-size: 12px;
  border: 1px solid rgba(255, 255, 255, 0.35);
  backdrop-filter: blur(8px);
  box-shadow: 0 18px 30px rgba(15, 23, 42, 0.25);
  pointer-events: none;
  min-width: 92px;
  text-align: center;
  z-index: 50;
}

.line-tooltip-value {
  font-size: 14px;
  font-weight: 700;
  margin-top: 2px;
}

.line-empty {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  text-align: center;
  color: var(--text-muted);
  font-size: 12px;
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(6px);
}

.line-empty span {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 4px;
}

.line-grid-lines,
.line-axis,
.line-area,
.line-path,
.line-active {
  pointer-events: none;
}


.line-header-actions .chart-chip {
  background: #ffffff;
}

@keyframes line-draw {
  from {
    stroke-dashoffset: 100;
  }
  to {
    stroke-dashoffset: 0;
  }
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

  .chart-row {
    margin: 12px 16px;
  }

  .line-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    margin: 16px 16px 12px;
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

  .chart-title {
    font-size: 18px;
  }

  .line-grid {
    grid-template-columns: 1fr;
    margin: 12px;
  }

  .line-chart {
    height: 200px;
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
