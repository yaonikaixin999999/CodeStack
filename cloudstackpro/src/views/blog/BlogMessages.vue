<template>
  <div class="blog-messages">
    <BlogHeader />
    
    <main class="messages-main">
      <div class="messages-container">
        <!-- 会话列表侧边栏 -->
        <aside class="conversations-sidebar" :class="{ 'mobile-hidden': activeOtherUserId }">
          <div class="sidebar-header">
            <h2 class="sidebar-title">
              <img src="@/assets/blog/icons/comment.svg" alt="私信" class="title-icon" />
              <span>私信消息</span>
            </h2>
            <button class="new-message-btn" @click="showNewMessageModal = true">
              <img src="@/assets/blog/icons/edit.svg" alt="新消息" />
            </button>
          </div>
          
          <!-- 搜索框 -->
          <div class="search-wrapper">
            <img src="@/assets/blog/icons/search.svg" alt="搜索" class="search-icon" />
            <input 
              type="text" 
              placeholder="搜索会话..." 
              v-model="searchQuery"
              class="search-input"
            />
          </div>
          
          <!-- 会话列表 -->
          <div class="conversations-list" v-if="!loading">
            <div 
              v-for="conversation in filteredConversations" 
              :key="conversation.otherUserId"
              class="conversation-item"
              :class="{ active: activeOtherUserId === conversation.otherUserId }"
              @click="selectConversation(conversation)"
            >
              <div class="conversation-avatar">
                <img :src="getAvatar(conversation.otherUser)" :alt="getDisplayName(conversation.otherUser)" />
              </div>
              <div class="conversation-info">
                <div class="conversation-header">
                  <span class="conversation-name">{{ getDisplayName(conversation.otherUser) }}</span>
                  <span class="conversation-time">{{ formatTime(conversation.lastMessageAt) }}</span>
                </div>
                <p class="conversation-preview">
                  {{ conversation.lastMessageContent || '暂无消息' }}
                </p>
              </div>
            </div>
            
            <div v-if="filteredConversations.length === 0" class="empty-conversations">
              <img src="@/assets/blog/icons/comment.svg" alt="无会话" class="empty-icon" />
              <p>暂无会话</p>
              <button class="start-chat-btn" @click="showNewMessageModal = true">开始聊天</button>
            </div>
          </div>
          
          <div v-else class="loading-state">
            <div class="loading-spinner"></div>
            <span>加载中...</span>
          </div>
        </aside>
        
        <!-- 聊天区域 -->
        <section class="chat-area" v-if="activeOtherUserId && activeOtherUser">
          <!-- 聊天头部 -->
          <div class="chat-header">
            <button class="back-btn mobile-only" @click="clearActiveChat">
              <img src="@/assets/blog/icons/chevron-down.svg" alt="返回" class="back-icon" />
            </button>
            <div class="chat-user-info">
              <img :src="getAvatar(activeOtherUser)" :alt="getDisplayName(activeOtherUser)" class="chat-avatar" />
              <div class="chat-user-detail">
                <span class="chat-user-name">{{ getDisplayName(activeOtherUser) }}</span>
                <span class="chat-user-status">{{ activeOtherUser?.username ? '@' + activeOtherUser.username : '' }}</span>
              </div>
            </div>
            <div class="chat-actions">
              <!-- <button class="action-btn" @click="viewProfile(activeOtherUser?.id)">
                <img src="@/assets/blog/icons/user.svg" alt="查看主页" />
              </button> -->
              <button class="action-btn danger" @click="confirmDeleteConversation">
                <img src="@/assets/blog/icons/bookmark.svg" alt="删除会话" />
              </button>
            </div>
          </div>
          
          <!-- 消息列表 -->
          <div class="messages-list" ref="messagesContainer">
            <div v-if="messagesLoading" class="messages-loading">
              <div class="loading-spinner"></div>
            </div>
            
            <template v-else>
              <div 
                v-for="message in messages" 
                :key="message.id"
                class="message-item"
                :class="{ mine: message.isMine }"
              >
                <img 
                  v-if="!message.isMine" 
                  :src="getAvatar(message.sender)" 
                  :alt="getDisplayName(message.sender)" 
                  class="message-avatar"
                />
                <div class="message-content">
                  <div class="message-bubble">
                    <p class="message-text">{{ message.content }}</p>
                  </div>
                  <span class="message-time">
                    {{ formatMessageTime(message.createdAt) }}
                  </span>
                </div>
                <img 
                  v-if="message.isMine" 
                  :src="currentUserAvatar" 
                  alt="我" 
                  class="message-avatar"
                />
              </div>
              
              <div v-if="messages.length === 0" class="empty-messages">
                <img src="@/assets/blog/icons/comment.svg" alt="无消息" class="empty-icon" />
                <p>开始你们的对话吧</p>
              </div>
            </template>
          </div>
          
          <!-- 输入区域 -->
          <div class="message-input-area">
            <div class="input-wrapper">
              <textarea 
                v-model="newMessage"
                placeholder="输入消息..."
                @keydown.enter.exact.prevent="sendMessage"
                @keydown.enter.shift.exact="newMessage += '\n'"
                rows="1"
                ref="messageInput"
              ></textarea>
              <div class="input-actions">
                <button 
                  class="send-btn" 
                  :disabled="!newMessage.trim()" 
                  @click="sendMessage"
                >
                  <img src="@/assets/blog/icons/trending.svg" alt="发送" class="send-icon" />
                  <span>发送</span>
                </button>
              </div>
            </div>
          </div>
        </section>
        
        <!-- 空状态 -->
        <section class="chat-area empty-state" v-else>
          <div class="empty-chat">
            <div class="empty-illustration">
              <img src="@/assets/blog/icons/comment.svg" alt="选择会话" class="illustration-icon" />
            </div>
            <h3>选择一个会话开始聊天</h3>
            <p>或者发起一个新的对话</p>
            <button class="primary-btn" @click="showNewMessageModal = true">
              <img src="@/assets/blog/icons/edit.svg" alt="新消息" />
              <span>发起新对话</span>
            </button>
          </div>
        </section>
      </div>
    </main>
    
    <!-- 新消息弹窗 -->
    <div v-if="showNewMessageModal" class="modal-overlay" @click.self="showNewMessageModal = false">
      <div class="new-message-modal">
        <div class="modal-header">
          <h3>发起新对话</h3>
          <button class="close-btn" @click="showNewMessageModal = false">
            <img src="@/assets/blog/icons/close.svg" alt="关闭" />
          </button>
        </div>
        <div class="modal-body">
          <div class="search-user-wrapper">
            <img src="@/assets/blog/icons/search.svg" alt="搜索" class="search-icon" />
            <input 
              type="text" 
              placeholder="搜索用户名或昵称..." 
              v-model="userSearchQuery"
              @input="searchUsers"
            />
          </div>
          
          <div class="user-search-results" v-if="searchedUsers.length > 0">
            <div 
              v-for="user in searchedUsers" 
              :key="user.id"
              class="user-result-item"
              @click="startConversationWithUser(user)"
            >
              <img :src="getAvatar(user)" :alt="getDisplayName(user)" class="user-avatar" />
              <div class="user-info">
                <span class="user-name">{{ getDisplayName(user) }}</span>
                <span class="user-username">@{{ user.username }}</span>
              </div>
            </div>
          </div>
          
          <div v-else-if="userSearchQuery && !searchingUsers" class="no-results">
            <p>未找到匹配的用户</p>
          </div>
          
          <div v-if="searchingUsers" class="searching">
            <div class="loading-spinner small"></div>
            <span>搜索中...</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 删除确认弹窗 -->
    <div v-if="showDeleteConfirm" class="modal-overlay" @click.self="showDeleteConfirm = false">
      <div class="confirm-modal">
        <div class="confirm-content">
          <img src="@/assets/blog/icons/bookmark.svg" alt="删除" class="confirm-icon" />
          <h3>删除会话</h3>
          <p>确定要删除与 {{ getDisplayName(activeOtherUser) }} 的会话吗？删除后将无法恢复。</p>
        </div>
        <div class="confirm-actions">
          <button class="cancel-btn" @click="showDeleteConfirm = false">取消</button>
          <button class="delete-btn" @click="deleteConversation">删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import BlogHeader from '@/components/blog/BlogHeader.vue'
import { blogService, type Conversation, type Message, type User, type SearchUser } from '@/services/blogService'

export default defineComponent({
  name: 'BlogMessages',
  components: {
    BlogHeader
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    
    // 状态
    const loading = ref(true)
    const messagesLoading = ref(false)
    const conversations = ref<Conversation[]>([])
    const activeOtherUserId = ref<number | null>(null)
    const activeOtherUser = ref<SearchUser | null>(null)
    const messages = ref<Message[]>([])
    const newMessage = ref('')
    const searchQuery = ref('')
    const showNewMessageModal = ref(false)
    const showDeleteConfirm = ref(false)
    const userSearchQuery = ref('')
    const searchedUsers = ref<SearchUser[]>([])
    const searchingUsers = ref(false)
    const messagesContainer = ref<HTMLElement | null>(null)
    const messageInput = ref<HTMLTextAreaElement | null>(null)
    
    // 当前用户
    const currentUser = ref<User | null>(null)
    
    // 计算属性
    const currentUserAvatar = computed(() => {
      if (currentUser.value?.avatar) {
        return currentUser.value.avatar
      }
      return `https://api.dicebear.com/7.x/avataaars/svg?seed=${currentUser.value?.username || 'default'}`
    })
    
    const filteredConversations = computed(() => {
      if (!searchQuery.value) return conversations.value
      const query = searchQuery.value.toLowerCase()
      return conversations.value.filter(c => {
        const name = getDisplayName(c.otherUser).toLowerCase()
        const username = c.otherUser?.username?.toLowerCase() || ''
        return name.includes(query) || username.includes(query)
      })
    })
    
    // 方法
    const getAvatar = (user: any) => {
      if (user?.avatar) return user.avatar
      return `https://api.dicebear.com/7.x/avataaars/svg?seed=${user?.username || 'default'}`
    }
    
    const getDisplayName = (user: any) => {
      return user?.nickname || user?.username || '用户'
    }
    
    const formatTime = (dateStr: string) => {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      const now = new Date()
      const diff = now.getTime() - date.getTime()
      const days = Math.floor(diff / (1000 * 60 * 60 * 24))
      
      if (days === 0) {
        return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      } else if (days === 1) {
        return '昨天'
      } else if (days < 7) {
        const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
        return weekdays[date.getDay()]
      } else {
        return date.toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' })
      }
    }
    
    const formatMessageTime = (dateStr: string) => {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      const now = new Date()
      const diff = now.getTime() - date.getTime()
      const days = Math.floor(diff / (1000 * 60 * 60 * 24))
      
      if (days === 0) {
        return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      } else if (days === 1) {
        return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      } else {
        return date.toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' }) + 
               ' ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      }
    }
    
    // 加载会话列表
    const loadConversations = async () => {
      loading.value = true
      try {
        const response = await blogService.messages.getConversations()
        if (response.success) {
          conversations.value = response.data || []
        }
      } catch (error) {
        console.error('加载会话列表失败:', error)
      } finally {
        loading.value = false
      }
    }
    
    // 选择会话
    const selectConversation = async (conversation: Conversation) => {
      activeOtherUserId.value = conversation.otherUserId
      activeOtherUser.value = conversation.otherUser
      await loadMessages(conversation.otherUserId)
      
      // 聚焦输入框
      nextTick(() => {
        messageInput.value?.focus()
      })
    }
    
    // 加载消息
    const loadMessages = async (otherUserId: number) => {
      messagesLoading.value = true
      try {
        const response = await blogService.messages.getMessages(otherUserId)
        if (response.success) {
          messages.value = response.data || []
          scrollToBottom()
        }
      } catch (error) {
        console.error('加载消息失败:', error)
      } finally {
        messagesLoading.value = false
      }
    }
    
    // 发送消息
    const sendMessage = async () => {
      if (!newMessage.value.trim() || !activeOtherUserId.value) return
      
      const content = newMessage.value.trim()
      newMessage.value = ''
      
      try {
        const response = await blogService.messages.send({
          receiverId: activeOtherUserId.value,
          content
        })
        
        if (response.success) {
          messages.value.push(response.data)
          scrollToBottom()
          
          // 更新会话列表中的最后消息
          const conv = conversations.value.find(c => c.otherUserId === activeOtherUserId.value)
          if (conv) {
            conv.lastMessageContent = content
            conv.lastMessageAt = new Date().toISOString()
          } else {
            // 如果是新会话，重新加载会话列表
            await loadConversations()
          }
        }
      } catch (error) {
        console.error('发送消息失败:', error)
        newMessage.value = content // 恢复消息内容
      }
    }
    
    // 滚动到底部
    const scrollToBottom = () => {
      nextTick(() => {
        if (messagesContainer.value) {
          messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
        }
      })
    }
    
    // 搜索用户
    let searchTimeout: number | null = null
    const searchUsers = () => {
      if (searchTimeout) clearTimeout(searchTimeout)
      
      if (!userSearchQuery.value.trim()) {
        searchedUsers.value = []
        return
      }
      
      searchingUsers.value = true
      searchTimeout = window.setTimeout(async () => {
        try {
          const response = await blogService.messages.searchUsers(userSearchQuery.value)
          if (response.success) {
            searchedUsers.value = response.data || []
          }
        } catch (error) {
          console.error('搜索用户失败:', error)
          searchedUsers.value = []
        } finally {
          searchingUsers.value = false
        }
      }, 300)
    }
    
    // 与用户开始对话
    const startConversationWithUser = async (user: SearchUser) => {
      showNewMessageModal.value = false
      userSearchQuery.value = ''
      searchedUsers.value = []
      
      activeOtherUserId.value = user.id
      activeOtherUser.value = user
      
      // 检查是否已在会话列表中
      const existingConv = conversations.value.find(c => c.otherUserId === user.id)
      if (!existingConv) {
        // 添加到会话列表
        conversations.value.unshift({
          otherUserId: user.id,
          lastMessageContent: '',
          lastMessageAt: new Date().toISOString(),
          otherUser: user
        })
      }
      
      await loadMessages(user.id)
      
      nextTick(() => {
        messageInput.value?.focus()
      })
    }
    
    // 确认删除会话
    const confirmDeleteConversation = () => {
      showDeleteConfirm.value = true
    }
    
    // 清空当前聊天
    const clearActiveChat = () => {
      activeOtherUserId.value = null
      activeOtherUser.value = null
      messages.value = []
    }
    
    // 删除会话
    const deleteConversation = async () => {
      if (!activeOtherUserId.value) return
      
      try {
        await blogService.messages.deleteConversation(activeOtherUserId.value)
        conversations.value = conversations.value.filter(c => c.otherUserId !== activeOtherUserId.value)
        activeOtherUserId.value = null
        activeOtherUser.value = null
        messages.value = []
        showDeleteConfirm.value = false
      } catch (error) {
        console.error('删除会话失败:', error)
      }
    }
    
    // 查看用户主页
    const viewProfile = (userId: number | undefined) => {
      if (userId) {
        router.push(`/blog/profile/${userId}`)
      }
    }
    
    // 初始化
    onMounted(async () => {
      currentUser.value = blogService.auth.getLocalUser()
      await loadConversations()
      
      // 如果URL中有userId参数，自动打开与该用户的会话
      const userId = route.query.userId as string
      if (userId) {
        const targetUserId = parseInt(userId)
        
        // 检查是否已在会话列表中
        const existingConv = conversations.value.find(c => c.otherUserId === targetUserId)
        if (existingConv) {
          await selectConversation(existingConv)
        } else {
          // 需要获取用户信息
          try {
            const userResponse = await blogService.users.getById(targetUserId)
            if (userResponse.success) {
              const user = userResponse.data
              await startConversationWithUser({
                id: user.id,
                username: user.username,
                nickname: user.nickname,
                avatar: user.avatar
              })
            }
          } catch (error) {
            console.error('获取用户信息失败:', error)
          }
        }
      }
    })
    
    // 监听消息变化，滚动到底部
    watch(() => messages.value.length, () => {
      scrollToBottom()
    })
    
    return {
      loading,
      messagesLoading,
      conversations,
      activeOtherUserId,
      activeOtherUser,
      messages,
      newMessage,
      searchQuery,
      showNewMessageModal,
      showDeleteConfirm,
      userSearchQuery,
      searchedUsers,
      searchingUsers,
      messagesContainer,
      messageInput,
      currentUserAvatar,
      filteredConversations,
      getAvatar,
      getDisplayName,
      formatTime,
      formatMessageTime,
      loadConversations,
      selectConversation,
      sendMessage,
      searchUsers,
      startConversationWithUser,
      confirmDeleteConversation,
      clearActiveChat,
      deleteConversation,
      viewProfile
    }
  }
})
</script>

<style scoped>
.blog-messages {
  min-height: 100vh;
  background: linear-gradient(135deg, #e6f2ff 0%, #ffffff 100%)
}

.messages-main {
  padding-top: 80px;
  min-height: calc(100vh - 80px);
}

.messages-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  display: flex;
  gap: 20px;
  height: calc(100vh - 120px);
}

/* 会话列表侧边栏 */
.conversations-sidebar {
  width: 360px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.25rem;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
}

.title-icon {
  width: 24px;
  height: 24px;
  opacity: 0.8;
}

.new-message-btn {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #3a9cff 0%, #667eea 100%);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.new-message-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(58, 156, 255, 0.4);
}

.new-message-btn img {
  width: 20px;
  height: 20px;
  filter: brightness(0) invert(1);
}

.search-wrapper {
  padding: 12px 20px;
  position: relative;
}

.search-icon {
  position: absolute;
  left: 32px;
  top: 50%;
  transform: translateY(-50%);
  width: 18px;
  height: 18px;
  opacity: 0.5;
}

.search-input {
  width: 100%;
  padding: 12px 12px 12px 40px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  font-size: 0.95rem;
  outline: none;
  transition: all 0.3s ease;
  background: rgba(0, 0, 0, 0.02);
}

.search-input:focus {
  border-color: #3a9cff;
  background: white;
  box-shadow: 0 0 0 3px rgba(58, 156, 255, 0.1);
}

.conversations-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.conversation-item:hover {
  background: rgba(58, 156, 255, 0.08);
}

.conversation-item.active {
  background: linear-gradient(135deg, rgba(58, 156, 255, 0.15) 0%, rgba(102, 126, 234, 0.15) 100%);
}

.conversation-avatar {
  position: relative;
  flex-shrink: 0;
}

.conversation-avatar img {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
}

.conversation-info {
  flex: 1;
  min-width: 0;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.conversation-name {
  font-weight: 600;
  color: #1a1a2e;
  font-size: 0.95rem;
}

.conversation-time {
  font-size: 0.75rem;
  color: #666;
}

.conversation-preview {
  color: #666;
  font-size: 0.85rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin: 0;
}

.empty-conversations {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  text-align: center;
}

.empty-icon {
  width: 64px;
  height: 64px;
  opacity: 0.3;
  margin-bottom: 16px;
}

.empty-conversations p {
  color: #666;
  margin-bottom: 16px;
}

.start-chat-btn {
  padding: 10px 24px;
  background: linear-gradient(135deg, #3a9cff 0%, #667eea 100%);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.start-chat-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(58, 156, 255, 0.4);
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #666;
  gap: 12px;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(58, 156, 255, 0.2);
  border-top-color: #3a9cff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-spinner.small {
  width: 20px;
  height: 20px;
  border-width: 2px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 聊天区域 */
.chat-area {
  flex: 1;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.chat-area.empty-state {
  justify-content: center;
  align-items: center;
}

.empty-chat {
  text-align: center;
  padding: 40px;
}

.empty-illustration {
  width: 120px;
  height: 120px;
  background: linear-gradient(135deg, rgba(58, 156, 255, 0.1) 0%, rgba(102, 126, 234, 0.1) 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
}

.illustration-icon {
  width: 60px;
  height: 60px;
  opacity: 0.5;
}

.empty-chat h3 {
  font-size: 1.25rem;
  color: #1a1a2e;
  margin: 0 0 8px;
}

.empty-chat p {
  color: #666;
  margin: 0 0 24px;
}

.primary-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 28px;
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
  color: white;
  border: none;
  border-radius: 25px;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.primary-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(58, 156, 255, 0.4);
}

.primary-btn img {
  width: 18px;
  height: 18px;
  filter: brightness(0) invert(1);
}

/* 聊天头部 */
.chat-header {
  padding: 16px 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  gap: 16px;
  background: white;
}

.back-btn {
  display: none;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: rgba(0, 0, 0, 0.05);
  border: none;
  cursor: pointer;
  align-items: center;
  justify-content: center;
}

.back-icon {
  width: 20px;
  height: 20px;
  transform: rotate(90deg);
}

.chat-user-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
}

.chat-user-detail {
  display: flex;
  flex-direction: column;
}

.chat-user-name {
  font-weight: 600;
  color: #1a1a2e;
  font-size: 1rem;
}

.chat-user-status {
  font-size: 0.8rem;
  color: #666;
}

.chat-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: rgba(0, 0, 0, 0.05);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.action-btn:hover {
  background: rgba(58, 156, 255, 0.1);
}

.action-btn.danger:hover {
  background: rgba(239, 68, 68, 0.1);
}

.action-btn img {
  width: 20px;
  height: 20px;
  opacity: 0.7;
}

/* 消息列表 */
.messages-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.messages-loading {
  display: flex;
  justify-content: center;
  padding: 40px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  max-width: 75%;
}

.message-item.mine {
  margin-left: auto;
}

.message-avatar {
  margin-top: 4px;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.action-btn danger{
    background-color: #fc0000;
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message-item.mine .message-content {
  align-items: flex-end;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 18px;
  background: #f0f2f5;
  max-width: 100%;
}

.message-item.mine .message-bubble {
  background: linear-gradient(135deg, #3a9cff 0%, #667eea 100%);
  color: white;
}

.message-text {
  margin: 0;
  font-size: 0.95rem;
  line-height: 1.5;
  word-break: break-word;
  white-space: pre-wrap;
}

.message-time {
  font-size: 0.75rem;
  color: #999;
  padding: 0 8px;
}

.empty-messages {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
  color: #666;
}

.empty-messages .empty-icon {
  width: 48px;
  height: 48px;
  opacity: 0.3;
  margin-bottom: 12px;
}

/* 输入区域 */
.message-input-area {
  padding: 16px 20px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  background: white;
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  background: #f0f2f5;
  border-radius: 24px;
  padding: 8px 8px 8px 20px;
}

.input-wrapper textarea {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 0.95rem;
  resize: none;
  outline: none;
  max-height: 120px;
  line-height: 1.5;
  padding: 8px 0;
}

.input-actions {
  display: flex;
  align-items: center;
}

.send-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  background: linear-gradient(135deg, #3a9cff 0%, #667eea 100%);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.send-btn:not(:disabled):hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(58, 156, 255, 0.4);
}

.send-icon {
  width: 18px;
  height: 18px;
  filter: brightness(0) invert(1);
  transform: rotate(-45deg);
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.new-message-modal {
  width: 90%;
  max-width: 420px;
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.1rem;
  color: #1a1a2e;
}

.close-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: rgba(0, 0, 0, 0.05);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn img {
  width: 16px;
  height: 16px;
  opacity: 0.6;
}

.modal-body {
  padding: 20px;
  max-height: 400px;
  overflow-y: auto;
}

.search-user-wrapper {
  position: relative;
  margin-bottom: 16px;
}

.search-user-wrapper .search-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  width: 18px;
  height: 18px;
  opacity: 0.5;
}

.search-user-wrapper input {
  width: 100%;
  padding: 12px 12px 12px 44px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  font-size: 0.95rem;
  outline: none;
  transition: all 0.3s ease;
}

.search-user-wrapper input:focus {
  border-color: #3a9cff;
  box-shadow: 0 0 0 3px rgba(58, 156, 255, 0.1);
}

.user-search-results {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.user-result-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.user-result-item:hover {
  background: rgba(58, 156, 255, 0.08);
}

.user-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-weight: 600;
  color: #1a1a2e;
}

.user-username {
  font-size: 0.85rem;
  color: #666;
}

.no-results, .searching {
  text-align: center;
  padding: 24px;
  color: #666;
}

.searching {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

/* 确认弹窗 */
.confirm-modal {
  width: 90%;
  max-width: 360px;
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.confirm-content {
  padding: 32px 24px;
  text-align: center;
}

.confirm-icon {
  width: 48px;
  height: 48px;
  opacity: 0.6;
  margin-bottom: 16px;
}

.confirm-content h3 {
  margin: 0 0 8px;
  color: #1a1a2e;
  font-size: 1.1rem;
}

.confirm-content p {
  margin: 0;
  color: #666;
  font-size: 0.9rem;
  line-height: 1.5;
}

.confirm-actions {
  display: flex;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.confirm-actions button {
  flex: 1;
  padding: 16px;
  border: none;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.cancel-btn {
  background: white;
  color: #666;
}

.cancel-btn:hover {
  background: #f5f5f5;
}

.delete-btn {
  background: #ef4444;
  color: white;
}

.delete-btn:hover {
  background: #dc2626;
}

/* 响应式 */
@media (max-width: 768px) {
  .messages-container {
    flex-direction: column;
    padding: 10px;
  }
  
  .conversations-sidebar {
    width: 100%;
    height: auto;
    max-height: 100%;
  }
  
  .conversations-sidebar.mobile-hidden {
    display: none;
  }
  
  .chat-area {
    position: fixed;
    inset: 0;
    border-radius: 0;
    z-index: 100;
  }
  
  .back-btn {
    display: flex;
  }
  
  .mobile-only {
    display: flex;
  }
}
</style>
