<template>
  <div class="admin-profile-page">
    <AdminHeader />

    <section class="hero">
      <div class="hero-inner">
        <p class="hero-kicker">管理员中心</p>
        <h1 class="hero-title">更新头像与资料，让形象保持一致</h1>
        <p class="hero-subtitle">更好地对外展示，保持与团队统一的专业形象</p>
      </div>
    </section>

    <main class="content">
      <div class="content-container">
        <div class="profile-card">
          <div class="profile-main">
            <div class="avatar-block">
              <img :src="userInfo.avatar" :alt="userInfo.name" class="avatar" />
              <input ref="avatarInput" type="file" accept="image/*" class="hidden-input" @change="onAvatarChange" />
              <button v-if="isOwner" class="avatar-btn" @click="triggerAvatar">更换头像</button>
            </div>
            <div class="profile-info">
              <div class="name-row">
                <h2 class="name">{{ userInfo.name }}</h2>
                <span class="badge">管理员</span>
                <span class="level">Lv.{{ userInfo.level }}</span>
              </div>
              <p class="bio">{{ userInfo.bio }}</p>
              <div class="chip-row">
                <span class="chip" v-for="badge in tags" :key="badge">{{ badge }}</span>
              </div>
              <div class="meta-row">
                <span class="status-pill muted" v-if="userInfo.status === 0">已禁言</span>
                <span class="status-pill banned" v-else-if="userInfo.status === -1">已封禁</span>
                <span class="meta-item">
                  <img src="@/assets/blog/icons/category.svg" alt="职业" />
                  {{ userInfo.profession }}
                </span>
                <span class="meta-item">
                  <img src="@/assets/blog/icons/home.svg" alt="位置" />
                  {{ userInfo.location }}
                </span>
                <span class="meta-item">
                  <img src="@/assets/blog/icons/clock.svg" alt="时间" />
                  {{ userInfo.joinDate }} 加入
                </span>
              </div>
              <div class="action-row">
                <template v-if="showSocialActions">
                  <button class="btn follow-btn" :class="{ following: isFollowed }" @click="toggleFollowProfile">
                    <img :src="isFollowed ? checkIcon : plusIcon" alt="关注" />
                    {{ isFollowed ? '已关注' : '关注' }}
                  </button>
                  <button class="btn ghost">
                    <img src="@/assets/blog/icons/comment.svg" alt="私信" />
                    私信
                  </button>
                </template>
              </div>
            </div>
          </div>

          <div class="stats-row">
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

        <div class="info-grid">
          <section class="info-card about-card">
            <div class="card-header">
              <h3>关于我</h3>
              <span class="card-tip">保持社区内容健康、有序</span>
            </div>
            <p class="about-text">{{ userInfo.bio }}</p>
          </section>

          <section class="info-card content-card">
            <div class="content-tabs">
              <button
                v-for="tab in contentTabs"
                :key="tab.id"
                class="tab-btn"
                :class="{ active: activeTab === tab.id }"
                @click="activeTab = tab.id"
              >
                <img :src="tab.icon" :alt="tab.name" />
                {{ tab.name }}
                <span class="tab-count">{{ tab.count }}</span>
              </button>
            </div>

            <div class="list" v-if="activeTab === 'posts'">
              <article v-for="post in userPosts" :key="post.id" class="list-item">
                <router-link :to="`/admin/blog/post/${post.id}`" class="item-link">
                  <div class="item-cover" v-if="post.coverImage">
                    <img :src="post.coverImage" :alt="post.title" />
                  </div>
                  <div class="item-body">
                    <h4>{{ post.title }}</h4>
                    <p>{{ post.excerpt }}</p>
                    <div class="item-meta">
                      <span>
                        <img src="@/assets/blog/icons/clock.svg" alt="时间" />
                        {{ post.createdAt }}
                      </span>
                      <span>
                        <img src="@/assets/blog/icons/eye.svg" alt="阅读" />
                        {{ post.views }}
                      </span>
                      <span>
                        <img src="@/assets/blog/icons/heart.svg" alt="点赞" />
                        {{ post.likes }}
                      </span>
                    </div>
                  </div>
                </router-link>
              </article>
              <div v-if="!userPosts.length" class="empty-state">
                <img src="@/assets/blog/icons/comment.svg" alt="空空如也" />
                <p class="empty-title">还没有发布文章</p>
                <p class="empty-desc">去创作一篇吧</p>
              </div>
            </div>

            <div class="list" v-if="activeTab === 'bookmarks'">
              <article v-for="post in bookmarks" :key="post.id" class="list-item">
                <div class="item-link">
                  <div class="item-cover" v-if="post.coverImage">
                    <img :src="post.coverImage" :alt="post.title" />
                  </div>
                  <div class="item-body">
                    <h4>{{ post.title }}</h4>
                    <p>{{ post.excerpt }}</p>
                    <div class="item-meta">
                      <span class="author">
                        <img :src="post.author.avatar" :alt="post.author.name" class="author-avatar" />
                        {{ post.author.name }}
                      </span>
                      <span>
                        <img src="@/assets/blog/icons/clock.svg" alt="收藏时间" />
                        收藏于 {{ post.bookmarkedAt }}
                      </span>
                    </div>
                  </div>
                </div>
                <button class="icon-btn" @click="removeBookmark(post.id)">
                  <img src="@/assets/blog/icons/close.svg" alt="删除收藏" />
                </button>
              </article>
              <div v-if="!bookmarks.length" class="empty-state">
                <img src="@/assets/blog/icons/bookmark.svg" alt="空空如也" />
                <p class="empty-title">还没有收藏</p>
                <p class="empty-desc">去多看看内容，像 B 站一样点个收藏吧</p>
              </div>
            </div>

            <div class="list grid" v-if="activeTab === 'following'">
              <div v-for="user in following" :key="user.id" class="user-card">
                <img :src="user.avatar" :alt="user.name" class="user-card-avatar" />
                <div class="user-card-info">
                  <h4>{{ user.name }}</h4>
                  <p>{{ user.bio }}</p>
                  <span class="user-card-followers">{{ user.followers }} 粉丝</span>
                </div>
                <button class="btn follow-btn" :class="{ following: user.isFollowed }" @click="toggleFollowUser(user)">
                  <img :src="user.isFollowed ? checkIcon : plusIcon" alt="关注" />
                  {{ user.isFollowed ? '已关注' : '关注' }}
                </button>
              </div>
              <div v-if="!following.length" class="empty-state">
                <img src="@/assets/blog/icons/user.svg" alt="空空如也" />
                <p class="empty-title">还没有关注</p>
                <p class="empty-desc">去发现感兴趣的作者，关注他们的更新吧</p>
              </div>
            </div>

            <div class="list grid" v-if="activeTab === 'followers'">
              <div v-for="user in followers" :key="user.id" class="user-card">
                <img :src="user.avatar" :alt="user.name" class="user-card-avatar" />
                <div class="user-card-info">
                  <h4>{{ user.name }}</h4>
                  <p>{{ user.bio }}</p>
                  <span class="user-card-followers">{{ user.followers }} 粉丝</span>
                </div>
                <button class="btn follow-btn" :class="{ following: user.isFollowed }" @click="toggleFollowUser(user)">
                  <img :src="user.isFollowed ? checkIcon : plusIcon" alt="关注" />
                  {{ user.isFollowed ? '已关注' : '关注' }}
                </button>
              </div>
              <div v-if="!followers.length" class="empty-state">
                <img src="@/assets/blog/icons/heart.svg" alt="空空如也" />
                <p class="empty-title">还没有粉丝</p>
                <p class="empty-desc">多发布高质量内容，吸引更多粉丝吧</p>
              </div>
            </div>
          </section>

          <aside class="info-card side-card">
            <div class="side-section">
              <h3>成就徽章</h3>
              <div class="achievement-grid">
                <div v-for="badge in achievements" :key="badge.title" class="achievement">
                  <img :src="badge.icon" :alt="badge.title" />
                  <div>
                    <p class="achievement-title">{{ badge.title }}</p>
                    <p class="achievement-desc">{{ badge.desc }}</p>
                  </div>
                </div>
              </div>
            </div>
            <div class="side-section">
              <h3>技能概览</h3>
              <div class="skills-list">
                <div v-for="skill in skills" :key="skill.name" class="skill-item">
                  <div class="skill-head">
                    <span>{{ skill.name }}</span>
                    <span class="skill-level">{{ skill.level }}</span>
                  </div>
                  <div class="skill-bar">
                    <div class="skill-progress" :style="{ width: skill.progress + '%' }"></div>
                  </div>
                </div>
              </div>
            </div>
            <div class="side-section">
              <h3>标签</h3>
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
import { defineComponent, onMounted, ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import AdminHeader from '@/components/admin/AdminHeader.vue'
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
  components: { AdminHeader },
  setup() {
    const route = useRoute()
    const loading = ref(false)
    const isOwner = ref(true)
    const profileUserId = ref<number | null>(null)
    const currentUserId = ref<number | null>(null)
    const isFollowed = ref(false)
    const activeTab = ref('posts')
    const avatarInput = ref<HTMLInputElement | null>(null)
    const userInfo = ref({
      name: 'Admin',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin',
      level: 8,
      profession: '内容审核 · 站点运营',
      location: 'Shanghai, CN',
      website: '',
      joinDate: '2024-03',
      bio: 'CloudStack Blog 站点管理员，负责内容审核与社区运营。',
      status: 1,
      stats: {
        posts: 128,
        followers: '2.4k',
        following: 86,
        likes: '8.9k',
        views: '32k'
      }
    })

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
    const followLoadingIds = ref<Set<number>>(new Set())
    const followStates = ref<Record<number, boolean>>({})
    const FOLLOW_CACHE_KEY = 'follow_states'

    const broadcastAvatar = (avatar?: string) => {
      const local = localStorage.getItem('blog_user')
      const parsed = local ? JSON.parse(local) : {}
      const avatarToUse = avatar || parsed.avatar || localStorage.getItem('avatar')
      if (avatarToUse) {
        window.dispatchEvent(new CustomEvent('avatar-updated', { detail: avatarToUse }))
      }
    }

    const loadFollowCache = () => {
      try {
        const raw = localStorage.getItem(FOLLOW_CACHE_KEY)
        if (raw) {
          const parsed = JSON.parse(raw)
          if (parsed && typeof parsed === 'object') {
            followStates.value = parsed
          }
        }
      } catch (e) {
        // ignore
      }
    }

    const saveFollowCache = () => {
      try {
        localStorage.setItem(FOLLOW_CACHE_KEY, JSON.stringify(followStates.value))
      } catch (e) {
        // ignore
      }
    }

    const refreshLocalStats = () => {
      const postCount = contentTabs.value[0]?.count ?? userPosts.value.length
      const followingsCount = contentTabs.value[2]?.count ?? following.value.length
      const followersCount = contentTabs.value[3]?.count ?? followers.value.length
      if (!Number.isNaN(Number(postCount))) userInfo.value.stats.posts = Number(postCount)
      if (!Number.isNaN(Number(followingsCount))) userInfo.value.stats.following = Number(followingsCount)
      if (!Number.isNaN(Number(followersCount))) userInfo.value.stats.followers = String(followersCount)
    }

    const achievements = ref([
      { title: '优秀管理员', desc: '季度最佳贡献', icon: iconExcellent },
      { title: '作者认证', desc: '优质内容创作者', icon: iconAuthor },
      { title: '榜样先锋', desc: '社区活跃榜样', icon: iconTrophy },
      { title: '高能热度', desc: '高影响力作者', icon: iconFire },
      { title: '钻石品质', desc: '超高质量内容', icon: iconDiamond },
      { title: '最佳作者', desc: '年度优秀作者', icon: iconWriter }
    ])

    const skills = ref([
      { name: '内容审核', level: '专家', progress: 90 },
      { name: '社区运营', level: '高级', progress: 82 },
      { name: '安全合规', level: '高级', progress: 78 },
      { name: '数据分析', level: '中级', progress: 65 }
    ])

    const tags = ref(['内容审核', '社区运营', '安全风控', 'AI 护航'])

    const isProfileCenter = computed(() => route.name === 'AdminProfile')

    const viewingUserId = computed(() => {
      if (isProfileCenter.value) return NaN
      const pid = route.params.id
      if (typeof pid === 'string' && pid.trim() !== '') return Number(pid)
      return NaN
    })

    const formatDate = (dateStr?: string) => {
      if (!dateStr) return ''
      const d = new Date(dateStr)
      if (Number.isNaN(d.getTime())) return dateStr
      return `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')}`
    }

    const formatCount = (n?: number | string) => {
      const num = typeof n === 'string' ? Number(n) : n
      if (num === undefined || Number.isNaN(num)) return n ?? '0'
      if (num >= 10000) return `${(num / 10000).toFixed(1)}w`
      if (num >= 1000) return `${(num / 1000).toFixed(1)}k`
      return String(num)
    }

    const loadUserContent = async (uid: number) => {
      // 文章
      try {
        const postRes = await blogService.posts.getList({ authorId: uid, page: 0, size: 50 })
        if (postRes.success && postRes.data) {
          const items = (postRes.data.content || []).map(p => ({
            id: p.id,
            title: p.title,
            excerpt: p.excerpt || '',
            coverImage: p.coverImage || '',
            createdAt: formatDate(p.publishedAt || p.createdAt),
            views: formatCount(p.viewCount),
            likes: formatCount(p.likeCount)
          }))
          userPosts.value = items
          contentTabs.value[0].count = postRes.data.totalElements ?? items.length
        }
      } catch (e) {
        console.error('加载用户文章失败', e)
        userPosts.value = []
      }

      // 关注/粉丝
      try {
        const f1 = await blogService.users.getFollowing(uid)
        if (f1.success && f1.data) {
          following.value = (f1.data.content || f1.data || []).map(u => {
            const uid = Number((u as any).id)
            const serverFollowed = true // this list is the users I already follow
            followStates.value[uid] = serverFollowed
            return {
              id: uid,
              name: (u as any).nickname || (u as any).username,
              bio: (u as any).bio || '',
              followers: (u as any).followerCount || 0,
              isFollowed: serverFollowed,
              avatar: (u as any).avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${uid || 1}`
            }
          })
          saveFollowCache()
          contentTabs.value[2].count = following.value.length
        }
      } catch (e) {
        console.error('加载关注列表失败', e)
        following.value = []
      }

      try {
        const f2 = await blogService.users.getFollowers(uid)
        if (f2.success && f2.data) {
          followers.value = (f2.data.content || f2.data || []).map(u => {
            const uid2 = Number((u as any).id)
            const serverFollowed = !!(u as any).followed
            const cached = followStates.value[uid2]
            const isFollowedFlag = cached !== undefined ? cached : serverFollowed
            followStates.value[uid2] = isFollowedFlag
            return {
              id: uid2,
              name: (u as any).nickname || (u as any).username,
              bio: (u as any).bio || '',
              followers: (u as any).followerCount || 0,
              isFollowed: isFollowedFlag,
              avatar: (u as any).avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${uid2 || 1}`
            }
          })
          saveFollowCache()
          contentTabs.value[3].count = followers.value.length
        }
      } catch (e) {
        console.error('加载粉丝列表失败', e)
        followers.value = []
      }

      // 收藏（仅自己）
      if (isOwner.value) {
        try {
          const bm = await blogService.bookmarks.getList({ page: 0, size: 5 })
          if (bm.success && bm.data) {
            const rawList = bm.data.content || bm.data || []
            const items = rawList
              .map((item: any) => {
                const post = item?.post || item
                if (!post) return null
                return {
                  id: post.id,
                  title: post.title,
                  excerpt: post.excerpt || '',
                  coverImage: post.coverImage || '',
                  author: {
                    name: post.author?.nickname || post.author?.username || '匿名',
                    avatar: post.author?.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${post.author?.id || 1}`
                  },
                  bookmarkedAt: formatDate(item?.createdAt || post.publishedAt || post.createdAt)
                }
              })
              .filter(Boolean)
            bookmarks.value = items as any[]
            contentTabs.value[1].count = bm.data.totalElements ?? items.length
          }
        } catch (e) {
          console.error('加载收藏失败', e)
          bookmarks.value = []
        }
      } else {
        bookmarks.value = []
        contentTabs.value[1].count = 0
      }

      refreshLocalStats()
    }

    const checkFollowStatus = async (targetId: number) => {
      if (followStates.value[targetId] !== undefined) {
        isFollowed.value = followStates.value[targetId]
        return
      }
      if (!currentUserId.value) return
      try {
        const res = await blogService.users.getFollowing(currentUserId.value, { page: 0, size: 500 })
        if (res.success && res.data) {
          const list = res.data.content || res.data || []
          const matched = list.some((u: any) => Number(u.id) === Number(targetId))
          isFollowed.value = matched
          followStates.value[targetId] = matched
          saveFollowCache()
        }
      } catch (e) {
        console.error('检查关注状态失败', e)
      }
    }

    const triggerAvatar = () => {
      if (!isOwner.value) return
      avatarInput.value?.click()
    }

    const toDataUrl = (file: File) =>
      new Promise<string>((resolve, reject) => {
        const reader = new FileReader()
        reader.onload = () => resolve(reader.result as string)
        reader.onerror = reject
        reader.readAsDataURL(file)
      })

    const compressImage = (file: File, maxLength = 180000) =>
      new Promise<string>((resolve, reject) => {
        const img = new Image()
        img.onload = () => {
          const canvas = document.createElement('canvas')
          let width = 160
          let height = (img.height / img.width) * width
          let quality = 0.7
          let dataUrl = ''
          for (let i = 0; i < 6; i++) {
            canvas.width = width
            canvas.height = height
            const ctx = canvas.getContext('2d')
            ctx?.drawImage(img, 0, 0, width, height)
            dataUrl = canvas.toDataURL('image/jpeg', quality)
            if (dataUrl.length <= maxLength) break
            width *= 0.85
            height *= 0.85
            quality *= 0.85
          }
          resolve(dataUrl)
        }
        img.onerror = reject
        img.src = URL.createObjectURL(file)
      })

    const onAvatarChange = async (e: Event) => {
      if (!isOwner.value) return
      const files = (e.target as HTMLInputElement)?.files
      if (!files || files.length === 0) return
      const file = files[0]
      try {
        loading.value = true
        const dataUrl = await compressImage(file)
        if (!dataUrl) return
        userInfo.value.avatar = dataUrl
        const updated = await blogService.auth.updateProfile({ avatar: dataUrl })
        // 尽量使用后端返回的最新用户信息
        const latestUser =
          (updated && (updated as any).data) ||
          (await blogService.auth.getCurrentUser().catch(() => null))?.data ||
          null
        const avatarToUse = latestUser?.avatar || dataUrl
        const local = localStorage.getItem('blog_user')
        if (local || latestUser) {
          const parsed = local ? JSON.parse(local) : {}
          const next = { ...parsed, ...(latestUser || {}), avatar: avatarToUse }
          localStorage.setItem('blog_user', JSON.stringify(next))
        }
        localStorage.setItem('avatar', avatarToUse)
        window.dispatchEvent(new CustomEvent('avatar-updated', { detail: avatarToUse }))
      } catch (err: any) {
        console.error('上传头像失败', err?.response || err)
      } finally {
        loading.value = false
        if (avatarInput.value) {
          avatarInput.value.value = ''
        }
      }
    }

    const loadProfile = async () => {
      try {
        loading.value = true
        if (!currentUserId.value) {
          try {
            const self = await blogService.auth.getCurrentUser()
            if (self?.data?.id) {
              currentUserId.value = self.data.id
            }
          } catch (e) {
            // ignore
          }
        }
        const otherId = viewingUserId.value
          if (!Number.isNaN(otherId)) {
            const res = await blogService.users.getById(otherId)
            if (res?.data) {
              isOwner.value = false
              profileUserId.value = res.data.id
              isFollowed.value = followStates.value[res.data.id] ?? !!(res.data as any).followed
              followStates.value[res.data.id] = isFollowed.value
              saveFollowCache()
              userInfo.value = {
              name: res.data.nickname || res.data.username || '用户',
              avatar: res.data.avatar || userInfo.value.avatar,
              level: res.data.level || userInfo.value.level,
              profession: res.data.profession || userInfo.value.profession,
              location: res.data.location || userInfo.value.location,
              website: (res.data as any).website || userInfo.value.website,
              joinDate: res.data.createdAt ? res.data.createdAt.slice(0, 10) : userInfo.value.joinDate,
              bio: res.data.bio || userInfo.value.bio,
              status: (res.data as any).status ?? userInfo.value.status ?? 1,
              stats: {
                posts: res.data.postCount ?? userInfo.value.stats.posts,
                followers: String(res.data.followerCount ?? userInfo.value.stats.followers),
                following: res.data.followingCount ?? userInfo.value.stats.following,
                likes: String(res.data.likeCount ?? userInfo.value.stats.likes),
                  views: String(res.data.viewCount ?? userInfo.value.stats.views)
                }
              }
              if ((res.data as any).tags?.length) {
                tags.value = (res.data as any).tags.map((t: any) => t.name || t).filter(Boolean)
              }
              await loadUserContent(otherId)
              await checkFollowStatus(otherId)
              return
            }
          }

          const selfRes = await blogService.auth.getCurrentUser()
          if (selfRes?.data) {
            isOwner.value = true
            profileUserId.value = selfRes.data.id
            currentUserId.value = selfRes.data.id
            userInfo.value = {
              name: selfRes.data.nickname || selfRes.data.username || 'Admin',
              avatar: selfRes.data.avatar || userInfo.value.avatar,
              level: selfRes.data.level || userInfo.value.level,
              profession: selfRes.data.profession || userInfo.value.profession,
              location: selfRes.data.location || userInfo.value.location,
              website: (selfRes.data as any).website || userInfo.value.website,
              joinDate: selfRes.data.createdAt ? selfRes.data.createdAt.slice(0, 10) : userInfo.value.joinDate,
              bio: selfRes.data.bio || userInfo.value.bio,
              status: (selfRes.data as any).status ?? userInfo.value.status ?? 1,
              stats: {
                posts: selfRes.data.postCount ?? userInfo.value.stats.posts,
                followers: String(selfRes.data.followerCount ?? userInfo.value.stats.followers),
                following: selfRes.data.followingCount ?? userInfo.value.stats.following,
                likes: String(selfRes.data.likeCount ?? userInfo.value.stats.likes),
                views: String(selfRes.data.viewCount ?? userInfo.value.stats.views)
              }
            }
            if ((selfRes.data as any).tags?.length) {
              tags.value = (selfRes.data as any).tags.map((t: any) => t.name || t).filter(Boolean)
            }
            if (selfRes?.data?.id) {
              await loadUserContent(selfRes.data.id)
            }
            // 覆盖本地用户缓存，避免头像回退
            localStorage.setItem('blog_user', JSON.stringify(selfRes.data))
            if (selfRes.data.avatar) {
              localStorage.setItem('avatar', selfRes.data.avatar)
              broadcastAvatar(selfRes.data.avatar)
            }
            refreshLocalStats()
          } else if (isProfileCenter.value) {
            // 个人中心兜底，避免显示关注/私信
            isOwner.value = true
          }
      } catch (error) {
        console.error('加载管理员资料失败', error)
      } finally {
        loading.value = false
      }
    }

   const toggleFollowProfile = async () => {
     if (!profileUserId.value || isOwner.value) return
     const uid = profileUserId.value
     if (followLoadingIds.value.has(uid)) return
     followLoadingIds.value.add(uid)
      try {
        if (isFollowed.value) {
          await blogService.users.unfollow(uid)
          isFollowed.value = false
          const count = Number(userInfo.value.stats.followers) || 0
         userInfo.value.stats.followers = String(Math.max(0, count - 1))
       } else {
         await blogService.users.follow(uid)
         isFollowed.value = true
         const count = Number(userInfo.value.stats.followers) || 0
         userInfo.value.stats.followers = String(count + 1)
       }
        followStates.value[uid] = isFollowed.value
        saveFollowCache()
        refreshLocalStats()
        broadcastAvatar()
        // 重新拉取以与服务端对齐
        await loadUserContent(uid)
      } catch (err) {
        console.error('切换关注失败', err)
      } finally {
        followLoadingIds.value.delete(uid)
      }
    }

    const toggleFollowUser = async (user: any) => {
      if (!user?.id) return
      const uid = user.id
      if (followLoadingIds.value.has(uid)) return
      followLoadingIds.value.add(uid)
      try {
        if (user.isFollowed) {
          await blogService.users.unfollow(uid)
          user.isFollowed = false
        } else {
          await blogService.users.follow(uid)
          user.isFollowed = true
        }
        followStates.value[uid] = user.isFollowed
        saveFollowCache()
        refreshLocalStats()
        // 重新拉取列表，确保刷新关注状态
        if (profileUserId.value) {
          await loadUserContent(profileUserId.value)
        }
        broadcastAvatar()
      } catch (err) {
        console.error('切换关注失败', err)
      } finally {
        followLoadingIds.value.delete(uid)
      }
    }

    const removeBookmark = async (id: number) => {
      if (!id) return
      try {
        await blogService.posts.unbookmark(id)
      } catch (err) {
        console.error('取消收藏失败', err)
      } finally {
        const before = bookmarks.value.length
        bookmarks.value = bookmarks.value.filter(b => b.id !== id)
        const delta = bookmarks.value.length - before
        if (delta !== 0) {
          contentTabs.value[1].count = Math.max(0, Number(contentTabs.value[1].count || 0) + delta)
        }
      }
    }

    onMounted(() => {
      loadFollowCache()
      loadProfile()
    })
    watch(
      () => [route.fullPath, route.params.id],
      () => {
        loadProfile()
      }
    )

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
      toggleFollowProfile,
      toggleFollowUser,
      removeBookmark,
      triggerAvatar,
      onAvatarChange,
      avatarInput,
      isProfileCenter,
      showOwnerActions: computed(() => isOwner.value || isProfileCenter.value),
      showSocialActions: computed(() => !isOwner.value && !isProfileCenter.value),
      plusIcon,
      checkIcon
    }
  }
})
</script>

<style scoped>
.admin-profile-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f9ff 0%, #ffffff 100%);
  padding-top: 72px;
}

.hero {
  background: linear-gradient(135deg, #e8f2ff 0%, #f6fbff 100%);
  padding: 56px 0 32px;
  position: relative;
  overflow: hidden;
}

.hero::after,
.hero::before {
  content: '';
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(66, 151, 255, 0.12) 0%, rgba(66, 151, 255, 0) 60%);
}

.hero::after {
  width: 260px;
  height: 260px;
  top: -40px;
  right: 10%;
}

.hero::before {
  width: 200px;
  height: 200px;
  bottom: -60px;
  left: 12%;
}

.hero-inner {
  max-width: 960px;
  margin: 0 auto;
  text-align: center;
  position: relative;
  z-index: 1;
}

.hero-kicker {
  display: inline-block;
  padding: 6px 14px;
  background: rgba(58, 156, 255, 0.1);
  color: #3a9cff;
  border-radius: 999px;
  font-size: 13px;
  margin-bottom: 12px;
}

.hero-title {
  font-size: 32px;
  color: #1f2d3d;
  margin-bottom: 8px;
}

.hero-subtitle {
  color: #6b7280;
  font-size: 15px;
  margin-bottom: 20px;
}

.content {
  padding: 24px 0 48px;
}

.content-container {
  max-width: 1120px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(19, 40, 68, 0.08);
  padding: 26px 28px;
}

.profile-main {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  flex-wrap: wrap;
  border-bottom: 1px solid #eef2f7;
  padding-bottom: 18px;
}

.avatar-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.avatar {
  width: 96px;
  height: 96px;
  border-radius: 20px;
  border: 3px solid #eef2f7;
  object-fit: cover;
}

.avatar-btn {
  padding: 8px 14px;
  border-radius: 999px;
  background: #eef6ff;
  color: #3a9cff;
  font-size: 13px;
}

.hidden-input {
  display: none;
}

.profile-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 260px;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.name {
  font-size: 22px;
  color: #1f2d3d;
  margin: 0;
}

.badge {
  padding: 6px 10px;
  background: rgba(58, 156, 255, 0.12);
  color: #2f80ed;
  border-radius: 12px;
  font-size: 12px;
}

.level {
  font-size: 13px;
  color: #6b7280;
}

.bio {
  color: #4b5563;
  font-size: 14px;
  margin: 0;
  line-height: 1.6;
}

.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  padding: 6px 12px;
  background: #f4f6fb;
  border-radius: 999px;
  color: #2f80ed;
  font-size: 12px;
}

.meta-row {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  color: #6b7280;
  font-size: 13px;
}

.status-pill {
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.status-pill.muted {
  background: rgba(255, 193, 7, 0.12);
  color: #b7791f;
}

.status-pill.banned {
  background: rgba(255, 77, 79, 0.12);
  color: #d4380d;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.meta-item img {
  width: 16px;
  height: 16px;
  opacity: 0.6;
}

.action-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: 10px;
  font-size: 14px;
  transition: all 0.2s ease;
  border: none;
  background: transparent;
  cursor: pointer;
}

.btn img {
  width: 16px;
  height: 16px;
}

.btn.primary {
  background: linear-gradient(120deg, #3a9cff, #6fc8ff);
  color: #fff;
}

.btn.primary.outline {
  background: #fff;
  color: #3a9cff;
  border: 1px solid #a8d5ff;
}

.btn.ghost {
  background: #f4f6fb;
  color: #1f2d3d;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
  padding-top: 18px;
  margin-top: 6px;
}

.stat-card {
  background: #f8fbff;
  border-radius: 12px;
  padding: 16px 12px;
  text-align: center;
  min-height: 84px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.stat-value {
  display: block;
  font-size: 20px;
  color: #1f2d3d;
  font-weight: 700;
}

.stat-label {
  color: #6b7280;
  font-size: 12px;
}

.info-grid {
  display: grid;
  grid-template-columns: 2fr 1.1fr;
  gap: 16px;
}

.info-card {
  background: #fff;
  border-radius: 16px;
  padding: 18px;
  box-shadow: 0 10px 40px rgba(19, 40, 68, 0.06);
}

.about-card .card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  color: #1f2d3d;
}

.card-tip {
  font-size: 12px;
  color: #6b7280;
}

.about-text {
  color: #4b5563;
  font-size: 14px;
  line-height: 1.6;
}

.content-card {
  grid-column: 1 / 2;
}

.content-tabs {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.tab-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  background: #f7f9fc;
  border-radius: 999px;
  color: #4b5563;
  font-size: 13px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 6px 12px rgba(31, 45, 61, 0.06);
  transition: all 0.2s ease;
}

.tab-btn img {
  width: 18px;
  height: 18px;
}

.tab-btn.active {
  background: linear-gradient(120deg, #3a9cff, #6fc8ff);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 8px 18px rgba(58, 156, 255, 0.2);
}

.tab-count {
  background: rgba(255, 255, 255, 0.85);
  color: #2f80ed;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.4);
}

.list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.list.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 32px 20px;
  background: #f8fbff;
  border-radius: 12px;
  color: #6b7280;
  text-align: center;
  box-shadow: inset 0 0 0 1px #eef2f7;
}

.empty-state img {
  width: 48px;
  height: 48px;
  opacity: 0.6;
}

.empty-title {
  font-size: 16px;
  color: #1f2d3d;
  margin: 0;
}

.empty-desc {
  font-size: 13px;
  margin: 0;
  color: #6b7280;
}

.list-item {
  background: #f8fbff;
  border-radius: 12px;
  padding: 12px;
  display: flex;
  gap: 12px;
  align-items: center;
}

.item-link {
  display: flex;
  gap: 12px;
  text-decoration: none;
  color: inherit;
  flex: 1;
}

.item-cover {
  width: 120px;
  height: 80px;
  border-radius: 10px;
  overflow: hidden;
  flex-shrink: 0;
}

.item-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-body h4 {
  margin: 0 0 6px;
  color: #1f2d3d;
  font-size: 15px;
}

.item-body p {
  margin: 0 0 8px;
  color: #6b7280;
  font-size: 13px;
}

.item-meta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  color: #6b7280;
  font-size: 12px;
}

.item-meta img {
  width: 14px;
  height: 14px;
  opacity: 0.6;
}

.icon-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
}

.author-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
}

.user-card {
  background: #f8fbff;
  border-radius: 12px;
  padding: 14px;
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 10px 30px rgba(31, 45, 61, 0.08);
}

.user-card-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  flex-shrink: 0;
}

.user-card-info h4 {
  margin: 0 0 4px;
  color: #1f2d3d;
  font-size: 14px;
}

.user-card-info p {
  margin: 0 0 6px;
  color: #6b7280;
  font-size: 12px;
}

.user-card-followers {
  font-size: 12px;
  color: #9ca3af;
}

.user-card .user-card-info {
  flex: 1;
  min-width: 0;
}

.follow-btn {
  border: 1px solid #dbeafe;
  border-radius: 12px;
  background: #f7fbff;
  color: #1f4b99;
  padding: 9px 16px;
  font-weight: 700;
  min-width: 92px;
  justify-content: center;
  transition: all 0.2s ease;
  box-shadow: 0 8px 16px rgba(58, 156, 255, 0.12);
}

.follow-btn img {
  width: 16px;
  height: 16px;
}

.follow-btn.following {
  background: linear-gradient(120deg, #3a9cff, #6fc8ff);
  color: #fff;
  border: none;
  box-shadow: 0 12px 24px rgba(58, 156, 255, 0.2);
}

.follow-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 22px rgba(31, 45, 61, 0.12);
}

.side-card {
  grid-column: 2 / 3;
}

.side-section + .side-section {
  margin-top: 16px;
}

.achievement-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 10px;
}

.achievement {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 10px;
  background: #f8fbff;
  border-radius: 10px;
}

.achievement img {
  width: 36px;
  height: 36px;
}

.achievement-title {
  margin: 0;
  color: #1f2d3d;
  font-size: 14px;
}

.achievement-desc {
  margin: 2px 0 0;
  color: #6b7280;
  font-size: 12px;
}

.skills-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.skill-item {
  padding: 10px;
  background: #f8fbff;
  border-radius: 10px;
}

.skill-head {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #1f2d3d;
}

.skill-level {
  color: #6b7280;
}

.skill-bar {
  margin-top: 8px;
  height: 6px;
  background: #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
}

.skill-progress {
  height: 100%;
  background: linear-gradient(90deg, #3a9cff, #6fc8ff);
}

.tags-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  padding: 6px 12px;
  background: #f4f6fb;
  color: #2f80ed;
  border-radius: 10px;
  font-size: 12px;
}


@media (max-width: 1024px) {
  .info-grid {
    grid-template-columns: 1fr;
  }

  .side-card {
    grid-column: 1 / 2;
  }

  .stats-row {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .profile-main {
    flex-direction: column;
    align-items: flex-start;
  }

  .stats-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .list.grid {
    grid-template-columns: 1fr;
  }
}
</style>
