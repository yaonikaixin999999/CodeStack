<template>
  <div class="chat-page">
    <header class="chat-header">
      <div class="brand">
        <div class="brand-mark">CS</div>
        <div>
          <div class="brand-title">CloudStack 聊天</div>
          <div class="brand-sub">{{ isAdmin ? '管理员聊天' : '和管理员或其他用户交流' }}</div>
        </div>
      </div>
      <div class="header-actions">
        <button class="btn btn-secondary" @click="goBack">
          {{ isAdmin ? '返回管理' : '返回博客' }}
        </button>
      </div>
    </header>

    <div class="chat-container card">
      <aside class="contacts">
        <div class="contacts-header">
          <h4>联系人</h4>
        </div>
        <div
          class="contact"
          v-for="c in contacts"
          :key="c.id"
          :class="{ active: c.id === activeContact?.id }"
          @click="setActive(c)"
        >
          <img :src="c.avatar" class="avatar-sm" />
          <div class="meta">
            <div class="name-row">
              <span class="name">{{ c.name }}</span>
              <span v-if="c.isAdmin" class="badge">管理员</span>
            </div>
            <span class="snippet">{{ c.lastMessage }}</span>
          </div>
          <span class="time">{{ c.time }}</span>
        </div>
      </aside>

      <section class="chat-panel" v-if="activeContact">
        <div class="chat-panel-header">
          <div class="user">
            <img :src="activeContact.avatar" class="avatar-sm" />
            <div>
              <div class="name-row">
                <span class="name">{{ activeContact.name }}</span>
                <span v-if="activeContact.isAdmin" class="badge">管理员</span>
              </div>
              <span class="status">{{ activeContact.status }}</span>
            </div>
          </div>
        </div>

        <div class="chat-body" ref="chatBody">
          <div v-for="m in messages" :key="m.id" class="message" :class="{ mine: m.senderId === currentUserId }">
            <div class="bubble">
              <p>{{ m.content }}</p>
              <span class="time">{{ m.time }}</span>
            </div>
          </div>
        </div>

        <div class="chat-input">
          <textarea v-model="draft" placeholder="输入消息..." rows="2"></textarea>
          <div class="chat-actions">
            <button class="pill-btn primary" :disabled="!draft.trim() || !currentUserId" @click="send">发送</button>
          </div>
        </div>
      </section>

      <section v-else class="chat-empty">
        选择一个联系人开始聊天
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { userService } from '@/services/userService'
import axios from 'axios'

const router = useRouter()
const route = useRoute()
const currentUser = userService.getCurrentUser()
const currentUserId = currentUser?.userId || 0
const isAdmin = currentUser?.isAdmin || false

const ADMIN_USER_ID = 1
const MAX_RECENT = 12
const RECENT_KEY = currentUserId ? `chat_recent_${currentUserId}` : 'chat_recent_guest'
const CHAT_API_BASE_URL = 'http://localhost:8082/api/chat'

interface ChatContact {
  id: number
  name: string
  avatar: string
  lastMessage: string
  time: string
  lastMessageAt?: number | string
  lastMessageId?: number
  isAdmin: boolean
  status: string
}

interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
}

interface ChatMessage {
  id: number
  senderId: number
  receiverId: number
  content: string
  createdAt?: string
}

const api = axios.create({
  baseURL: CHAT_API_BASE_URL,
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

const contacts = ref<ChatContact[]>([])
const messages = ref<{ id: number; senderId: number; content: string; time: string }[]>([])
const activeContact = ref<ChatContact | null>(null)
const draft = ref('')
const chatBody = ref<HTMLDivElement>()
let pollTimer: number | undefined
let polling = false

const goBack = () => {
  router.push(isAdmin ? '/admin' : '/blog')
}

const formatTime = (dateStr?: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  if (Number.isNaN(date.getTime())) return ''
  return date.toLocaleTimeString()
}

const sortContacts = () => {
  contacts.value.sort((a, b) => {
    if (!isAdmin) {
      if (a.isAdmin) return -1
      if (b.isAdmin) return 1
    }
    return b.lastMessageAt - a.lastMessageAt
  })
}

const persistRecent = () => {
  if (!currentUserId) return
  const list = contacts.value.filter(c => !(isAdmin && c.isAdmin))
  localStorage.setItem(RECENT_KEY, JSON.stringify(list.slice(0, MAX_RECENT)))
}

const ensureAdminContact = () => {
  if (isAdmin) return
  if (!contacts.value.find(c => c.id === ADMIN_USER_ID)) {
    contacts.value.unshift({
      id: ADMIN_USER_ID,
      name: '管理员',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin',
      lastMessage: '有问题随时找我',
      time: '刚刚',
      lastMessageAt: 0,
      lastMessageId: 0,
      isAdmin: true,
      status: '在线'
    })
  }
}

const ensureContact = (targetId?: number, targetName?: string, targetAvatar?: string, isAdminTarget = false) => {
  if (targetId === undefined || targetId === null) return
  let existing = contacts.value.find(c => c.id === targetId)
  if (!existing) {
    existing = {
      id: targetId,
      name: targetName || `用户${targetId}`,
      avatar: targetAvatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${targetId}`,
      lastMessage: '',
      time: '刚刚',
      lastMessageAt: Date.now(),
      lastMessageId: 0,
      isAdmin: isAdminTarget,
      status: '在线'
    }
    contacts.value.unshift(existing)
  }
  ensureAdminContact()
  sortContacts()
  persistRecent()
  activeContact.value = existing
}

const loadAdminContacts = async () => {
  const res = await api.get<ApiResponse<ChatContact[]>>('/contacts')
  const list = res.data.data || []
  contacts.value = list.map(c => ({
    ...c,
    time: formatTime(c.lastMessageAt ? c.lastMessageAt.toString() : undefined) || '',
    lastMessageAt: c.lastMessageAt ? new Date(c.lastMessageAt.toString()).getTime() : 0,
    lastMessage: c.lastMessage || ''
  }))
  sortContacts()
  persistRecent()
  if (contacts.value.length > 0 && !activeContact.value) {
    activeContact.value = contacts.value[0]
    await loadConversation()
  }
}

const setActive = async (c: ChatContact) => {
  activeContact.value = c
  await loadConversation()
}

const send = async () => {
  if (!draft.value.trim() || !activeContact.value || !currentUserId) return
  const payload = {
    senderId: currentUserId,
    receiverId: activeContact.value.id,
    content: draft.value
  }
  await api.post<ApiResponse<ChatMessage>>('/send', payload)
  draft.value = ''
  await loadConversation()
}

const syncContactFromConversation = (contact: ChatContact, list: ChatMessage[]) => {
  if (list.length === 0) return
  const latest = list[0]
  contact.lastMessage = latest.content
  contact.time = formatTime(latest.createdAt) || '刚刚'
  contact.lastMessageAt = latest.createdAt ? new Date(latest.createdAt).getTime() : Date.now()
  contact.lastMessageId = latest.id
}

const applyMessages = async (list: ChatMessage[]) => {
  messages.value = list.map(item => ({
    id: item.id,
    senderId: item.senderId,
    content: item.content,
    time: formatTime(item.createdAt) || new Date().toLocaleTimeString()
  }))
  await nextTick()
  if (chatBody.value) {
    chatBody.value.scrollTop = chatBody.value.scrollHeight
  }
}

const loadConversation = async () => {
  if (!activeContact.value || !currentUserId) return
  const res = await api.get<ApiResponse<ChatMessage[]>>('/conversation', {
    params: { userA: currentUserId, userB: activeContact.value.id }
  })
  const list = res.data.data || []
  syncContactFromConversation(activeContact.value, list)
  await applyMessages(list)
  sortContacts()
  persistRecent()
}

const refreshContact = async (contact: ChatContact) => {
  if (!currentUserId) return
  const res = await api.get<ApiResponse<ChatMessage[]>>('/conversation', {
    params: { userA: currentUserId, userB: contact.id }
  })
  const list = res.data.data || []
  syncContactFromConversation(contact, list)
  if (activeContact.value && activeContact.value.id === contact.id) {
    await applyMessages(list)
  }
}

const refreshAll = async () => {
  if (polling || !currentUserId || contacts.value.length === 0) return
  polling = true
  try {
    for (const contact of contacts.value) {
      try {
        await refreshContact(contact)
      } catch {
        // ignore per-contact failures
      }
    }
    sortContacts()
    persistRecent()
  } finally {
    polling = false
  }
}

const startPolling = () => {
  pollTimer = window.setInterval(async () => {
    await refreshAll()
  }, 3000)
}

const stopPolling = () => {
  if (pollTimer) {
    window.clearInterval(pollTimer)
    pollTimer = undefined
  }
}

const initRecentContacts = () => {
  if (isAdmin) return
  const saved = localStorage.getItem(RECENT_KEY)
  if (saved) {
    try {
      const list = JSON.parse(saved) as ChatContact[]
      list
        .filter(c => !(isAdmin && c.isAdmin))
        .slice(0, MAX_RECENT)
        .forEach(c => contacts.value.push(c))
    } catch {
      // ignore
    }
  }
  ensureAdminContact()
  sortContacts()
}

onMounted(async () => {
  if (isAdmin) {
    await loadAdminContacts()
  } else {
    initRecentContacts()
  }
  const q = route.query
  const targetId = q.targetId ? Number(q.targetId) : undefined
  const targetName = typeof q.targetName === 'string' ? q.targetName : undefined
  const targetAvatar = typeof q.targetAvatar === 'string' ? q.targetAvatar : undefined
  if (targetId !== undefined) {
    ensureContact(targetId, targetName, targetAvatar, targetId === ADMIN_USER_ID)
  } else if (contacts.value.length > 0) {
    activeContact.value = contacts.value[0]
  }
  if (activeContact.value) {
    await loadConversation()
  }
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.chat-page {
  min-height: 100vh;
  background: var(--background-gradient);
  padding: 24px 32px 32px;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-mark {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: linear-gradient(135deg, #3a9cff, #7cc5ff);
  color: #fff;
  font-weight: 700;
  display: grid;
  place-items: center;
  box-shadow: var(--shadow-md);
}

.brand-title {
  font-weight: 800;
  color: var(--text-dark);
  font-size: 18px;
}

.brand-sub {
  color: var(--text-muted);
  font-size: 13px;
}

.header-actions .btn {
  padding: 8px 12px;
  border-radius: 10px;
  border: 1px solid var(--border-color);
  background: #fff;
  color: var(--text-dark);
}

.card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-sm);
}

.chat-container {
  display: grid;
  grid-template-columns: 280px 1fr;
  min-height: 520px;
}

.contacts {
  border-right: 1px solid var(--border-light);
  padding: 16px;
  background: #f9fbff;
}

.contacts-header {
  font-weight: 700;
  margin-bottom: 12px;
  color: var(--text-dark);
}

.contact {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 10px;
  align-items: center;
  padding: 10px;
  border-radius: 10px;
  cursor: pointer;
  transition: var(--transition-default);
}

.contact:hover {
  background: rgba(58, 156, 255, 0.08);
}

.contact.active {
  background: rgba(58, 156, 255, 0.12);
}

.meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name {
  font-weight: 700;
  color: var(--text-dark);
}

.snippet {
  font-size: 12px;
  color: var(--text-muted);
}

.time {
  font-size: 12px;
  color: var(--text-muted);
}

.badge {
  padding: 2px 8px;
  border-radius: 10px;
  background: rgba(58, 156, 255, 0.15);
  color: #3a9cff;
  font-size: 11px;
}

.avatar-sm {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid var(--border-color);
}

.chat-panel {
  display: flex;
  flex-direction: column;
  padding: 0;
}

.chat-panel-header {
  padding: 16px;
  border-bottom: 1px solid var(--border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status {
  font-size: 12px;
  color: var(--text-muted);
}

.chat-body {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  background: #f9fbff;
}

.message {
  margin-bottom: 12px;
  display: flex;
}

.message.mine {
  justify-content: flex-end;
}

.bubble {
  max-width: 70%;
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: 12px;
  padding: 10px 12px;
  box-shadow: var(--shadow-sm);
}

.message.mine .bubble {
  background: rgba(58, 156, 255, 0.1);
  border-color: rgba(58, 156, 255, 0.2);
}

.bubble p {
  margin: 0 0 6px 0;
  color: var(--text-dark);
}

.bubble .time {
  font-size: 11px;
}

.chat-input {
  padding: 12px 16px;
  border-top: 1px solid var(--border-light);
  background: #fff;
}

.chat-input textarea {
  width: 100%;
  border: 1px solid var(--border-color);
  border-radius: 10px;
  padding: 10px;
  font-size: 14px;
  resize: none;
  background: #fff;
}

.chat-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.pill-btn {
  border-radius: 999px;
  padding: 8px 16px;
  border: 1px solid var(--border-color);
  background: #fff;
  color: var(--text-dark);
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  transition: var(--transition-default);
}

.pill-btn.primary {
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
  color: #fff;
  border: none;
}

.pill-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.chat-empty {
  display: grid;
  place-items: center;
  padding: 40px;
  color: var(--text-muted);
}

@media (max-width: 900px) {
  .chat-container {
    grid-template-columns: 1fr;
  }
  .contacts {
    border-right: none;
    border-bottom: 1px solid var(--border-light);
  }
}
</style>
