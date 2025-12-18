<template>
  <div
    :class="['ai-chat-panel', { minimized: isMinimized }]"
    ref="aiPanelRef"
    :style="{ width: isMinimized ? '50px' : `${width}px` }"
  >
    <!-- AI面板拖动控件 -->
    <div
      v-if="!isMinimized"
      class="ai-panel-resizer"
      ref="aiPanelResizerRef"
      @mousedown="handleAIPanelMouseDown"
      title="拖动调整AI面板宽度"
    ></div>

    <div class="ai-chat-header">
      <div class="ai-chat-title">
        <div class="ai-chat-icon">
          <img :src="aiLogo" alt="AI 助手" class="ai-logo-img" />
        </div>
        <span v-if="!isMinimized">AI 助手</span>
      </div>
      <div class="ai-chat-actions">
        <template v-if="!isMinimized">
          <button
            class="ai-action-btn"
            @click="isConfigOpen = !isConfigOpen"
            title="设置"
          >
            <img :src="settingsIcon" alt="设置" width="20" height="20" />
          </button>
          <button
            class="ai-action-btn"
            @click="handleClearConversation"
            title="清除对话"
          >
            <img :src="clearIcon" alt="清除对话" width="20" height="20" />
          </button>
        </template>

        <button
          class="ai-action-btn"
          @click="toggleMinimize"
          :title="isMinimized ? '展开' : '最小化'"
        >
          <img
            :src="isMinimized ? expandIcon : minimizeIcon"
            :alt="isMinimized ? '展开' : '最小化'"
            width="20"
            height="20"
          />
        </button>
      </div>
    </div>

    <template v-if="!isMinimized">
      <!-- 配置面板 -->
      <div v-if="isConfigOpen" class="ai-key-config">
        <div class="ai-key-header">
          <h3>AI 模型设置</h3>
        </div>

        <!-- 模型来源切换 -->
        <div class="ai-config-item">
          <label for="model-source" class="ai-config-label">模型来源</label>
          <div class="ai-config-source-group">
            <div class="ai-source-option">
              <input
                type="radio"
                id="qwen-source"
                name="model-source"
                :checked="useAliyunModel"
                @change="handleToggleQwenModel"
              />
              <label for="qwen-source" class="source-label">
                <img :src="qwenIcon" alt="Qwen" class="source-icon" />
                Qwen
              </label>
            </div>
            <div class="ai-source-option">
              <input
                type="radio"
                id="openai-source"
                name="model-source"
                :checked="!useAliyunModel"
                @change="handleToggleOpenAIModel"
              />
              <label for="openai-source" class="source-label">
                <img :src="openAiIcon" alt="OpenAI" class="source-icon" />
                OpenAI
              </label>
            </div>
          </div>
        </div>

        <!-- 模型名称 -->
        <div class="ai-config-item">
          <label class="ai-config-label">选择模型</label>
          <select
            class="ai-key-input"
            v-model="modelName"
            @change="handleModelNameChange"
          >
            <option v-for="model in availableModels" :key="model" :value="model">
              {{ model }}
            </option>
          </select>
          <p v-if="loadingModels" class="ai-key-note loading">
            正在加载可用模型...
          </p>
        </div>

        <!-- API Key配置 -->
        <div class="ai-key-header" style="margin-top: 20px">
          <h3>API Key 设置</h3>
          <p class="ai-key-info">
            {{ useAliyunModel ? '请输入你的阿里云百炼 API Key' : '请输入你的 OpenAI API Key' }}
          </p>
        </div>
        <div class="ai-key-input-container">
          <input
            type="password"
            class="ai-key-input"
            :placeholder="useAliyunModel ? '输入阿里云 API Key (sk-xxx)' : '输入 OpenAI API Key'"
            v-model="apiKey"
          />
          <button class="ai-key-submit" @click="handleSaveApiKey">
            保存
          </button>
        </div>
        <p class="ai-key-note">
          API Key 将安全地存储在你的浏览器中，不会上传到任何服务器。
          {{ useAliyunModel ? '阿里云API Key格式: sk-xxxxxxxxxx' : '' }}
        </p>

        <div v-if="errorMessage" class="ai-error-message">
          <div class="error-icon">⚠️</div>
          <span>{{ errorMessage }}</span>
        </div>

        <div class="ai-config-actions">
          <button class="ai-config-save" @click="isConfigOpen = false">
            完成
          </button>
        </div>
      </div>

      <div v-if="errorMessage" class="ai-error-message">{{ errorMessage }}</div>

      <div class="ai-chat-content">
        <div class="ai-messages-container">
          <!-- 空状态 -->
          <div v-if="messages.length === 0" class="ai-empty-state">
            <div class="ai-welcome">
              <div class="ai-welcome-icon">
                <img :src="aiLogo" alt="AI 助手" class="ai-welcome-logo" />
              </div>
              <h2>询问AI Copilot</h2>
              <p>AI Copilot由AI提供支持，为您的编程问题提供解答和代码建议</p>
            </div>

            <div class="ai-examples">
              <h3>你可以尝试问我：</h3>
              <div class="ai-examples-list">
                <div
                  v-for="(example, index) in conversationExamples"
                  :key="index"
                  class="ai-example-item"
                  @click="handleExampleClick(example)"
                >
                  <i class="icon-chat-bubble">💬</i>
                  <span>{{ example }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 消息列表 - 移除头像 -->
          <div
            v-else
            v-for="(msg, index) in messages"
            :key="index"
            :class="['ai-message', msg.role === 'user' ? 'user-message' : 'assistant-message']"
          >
            <div class="message-content">
              <div class="message-bubble">
                <div v-html="formatMessageContent(msg.content)"></div>
              </div>
              <div class="message-time">{{ formatTime(msg.timestamp) }}</div>
            </div>
          </div>

          <!-- 打字指示器 - 移除头像 -->
          <div v-if="isTyping" class="ai-message assistant-message">
            <div class="message-content">
              <div class="message-bubble">
                <div class="ai-typing-indicator">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            </div>
          </div>

          <div ref="messagesEndRef"></div>
        </div>

        <div class="ai-chat-input-container">
          <div class="ai-input-wrapper">
            <textarea
              ref="textareaRef"
              class="ai-input-field"
              placeholder="发送消息给AI助手..."
              v-model="input"
              @keydown="handleKeyPress"
            />

            <button
              :class="['ai-send-button', { disabled: !input.trim() || isTyping }]"
              @click="handleSendMessage"
              :disabled="!input.trim() || isTyping"
              title="发送消息"
            >
              <img :src="sendIcon" alt="发送" width="20" height="20" />
            </button>
          </div>
          <div class="ai-input-info">
            <div v-if="input.length > 0" :class="['ai-helper-text', { warning: input.length > 2000 }]">
              {{ input.length }}/2000
            </div>
            <div class="ai-helper-text">
              <kbd>Enter</kbd> 发送 | <kbd>Shift + Enter</kbd> 换行
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick, watch, h } from 'vue'
import { Message, aiService } from '../../services/aiService'
import { apiKeyService } from '../../services/apiKeyService'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

// 导入图标
import aiLogo from '@/images/icons8-windows副驾驶-240.png'
import settingsIcon from '@/images/icons8-设置-40.png'
import clearIcon from '@/images/icons8-创建新的-40.png'
import minimizeIcon from '@/images/icons8-最小化-40.png'
import expandIcon from '@/images/icons8-最大化-40.png'
import userAvatar from '@/images/user.jpg'
import sendIcon from '@/images/icons8-发送-40.png'
import openAiIcon from '@/images/icons8-聊天室-50.png'
import qwenIcon from '@/images/icons8-github-240.png' // 使用指定的图标作为Qwen图标

// Props
interface Props {
  width: number
  onResize: (width: number) => void
}

const props = defineProps<Props>()

// 响应式数据
const messages = ref<Message[]>([])
const input = ref('')
const isTyping = ref(false)
const apiKey = ref('')
const errorMessage = ref<string | null>(null)
const isConfigOpen = ref(false)
const isMinimized = ref(false)

// Refs
const messagesEndRef = ref<HTMLDivElement>()
const aiPanelRef = ref<HTMLDivElement>()
const aiPanelResizerRef = ref<HTMLDivElement>()
const textareaRef = ref<HTMLTextAreaElement>()

// 面板拖动相关
const isDraggingAIPanel = ref(false)

// 模型配置
const useAliyunModel = ref(true)
const modelName = ref('qwen-max-latest')
const availableModels = ref<string[]>([
  'qwen-max-latest',
  'qwen-plus-latest',
  'qwen-turbo-latest',
  'qwen-long-latest',
  'qwen-omni-turbo-latest',
  'qwq-plus-latest'
])
const loadingModels = ref(false)

// 示例对话提示
const conversationExamples = [
  "能帮我解释一下React的工作原理吗？",
  "如何优化这段代码的性能？",
  "编写一个简单的Node.js Express服务器",
  "请帮我修复这段代码中的错误"
]

// 获取可用模型列表
const fetchAvailableModels = async () => {
  try {
    loadingModels.value = true
    errorMessage.value = null

    const models = await aiService.getAvailableModels()
    availableModels.value = models

    // 自动选择第一个模型（如果当前模型不在列表中）
    if (models.length > 0 && !models.includes(modelName.value)) {
      modelName.value = models[0]
      aiService.setModel(models[0])
      localStorage.setItem('model_name', models[0])
    }
  } catch (error) {
    console.error('获取模型列表失败:', error)
    errorMessage.value = '获取模型列表失败，使用默认模型'
    if (useAliyunModel.value) {
      availableModels.value = ['qwen-max-latest', 'qwen-plus-latest', 'qwen-turbo-latest', 'qwen-long-latest', 'qwen-omni-turbo-latest','qwq-plus-latest']
    } else {
      availableModels.value = ['gpt-4.5', 'gpt-4.1', 'o4-mini', 'o3', 'o3-pro']
    }
  } finally {
    loadingModels.value = false
  }
}

// 处理模型配置变更
const handleToggleQwenModel = () => {
  useAliyunModel.value = true
  aiService.toggleModelSource(true)
  localStorage.setItem('use_aliyun_model', 'true')
  
  // 设置默认的阿里云API Key
  const defaultApiKey = 'sk-c2a1475efdb2400f821de7681c4e7c2d'
  apiKey.value = defaultApiKey
  aiService.setApiKey(defaultApiKey)
  localStorage.setItem('api_key', defaultApiKey)
  
  fetchAvailableModels()
}

const handleToggleOpenAIModel = () => {
  useAliyunModel.value = false
  aiService.toggleModelSource(false)
  localStorage.setItem('use_aliyun_model', 'false')
  
  // 清空API Key，让用户重新输入OpenAI的
  apiKey.value = ''
  aiService.setApiKey('')
  localStorage.setItem('api_key', '')
  
  fetchAvailableModels()
}

const handleModelNameChange = (event: Event) => {
  const target = event.target as HTMLSelectElement
  const name = target.value
  
  modelName.value = name
  aiService.setModel(name)
  localStorage.setItem('model_name', name)
}

// 处理面板拖动
const handleMouseMove = (e: MouseEvent) => {
  if (isDraggingAIPanel.value) {
    const container = document.querySelector('.ide-container')
    if (container && aiPanelRef.value) {
      const containerRect = container.getBoundingClientRect()
      const newWidth = Math.max(
        250,  // 最小宽度
        Math.min(
          500,  // 最大宽度
          containerRect.right - e.clientX
        )
      )
      props.onResize(newWidth)
    }
  }
}

const handleMouseUp = () => {
  isDraggingAIPanel.value = false
}

const handleAIPanelMouseDown = (e: MouseEvent) => {
  e.preventDefault()
  isDraggingAIPanel.value = true
}

// 发送消息前验证（使用流式输出）
const handleSendMessage = async () => {
  if (!input.value.trim() || isTyping.value) return

  try {
    errorMessage.value = null

    // 创建用户消息
    const userMessage: Message = {
      role: 'user',
      content: input.value,
      timestamp: Date.now()
    }

    // 更新消息列表
    messages.value.push(userMessage)
    const userInput = input.value
    input.value = ''
    isTyping.value = true

    if (textareaRef.value) {
      textareaRef.value.style.height = 'auto'
    }

    // 准备传递给AI的消息（不包含待创建的助手消息）
    const messagesToSend = [...messages.value]
    let assistantMessage: Message | null = null
    
    await aiService.sendMessageStream(messagesToSend, (chunk: string) => {
      // 第一次接收到内容时，关闭打字指示器并创建助手消息
      if (!assistantMessage) {
        isTyping.value = false // 关闭打字指示器
        assistantMessage = {
          role: 'assistant',
          content: chunk,
          timestamp: Date.now()
        }
        messages.value.push(assistantMessage)
      } else {
        // 后续更新消息内容
        assistantMessage.content += chunk
      }
      
      // 强制触发 Vue 的响应式更新
      messages.value = [...messages.value]
      
      // 确保滚动到底部
      nextTick(() => {
        messagesEndRef.value?.scrollIntoView({ behavior: 'smooth' })
      })
    })
  } catch (error) {
    console.error('发送消息错误:', error)
    errorMessage.value = error instanceof Error ? error.message : '发送消息时出现错误'
    
    // 如果出错且创建了助手消息但内容为空，则移除
    const lastMessage = messages.value[messages.value.length - 1]
    if (lastMessage && lastMessage.role === 'assistant' && !lastMessage.content.trim()) {
      messages.value.pop()
    }
  } finally {
    isTyping.value = false
  }
}

const handleKeyPress = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSendMessage()
  }
}

const handleSaveApiKey = () => {
  try {
    if (!apiKey.value.trim()) {
      errorMessage.value = 'API Key 不能为空'
      return
    }

    apiKeyService.saveApiKey(apiKey.value)
    aiService.setApiKey(apiKey.value)
    localStorage.setItem('api_key', apiKey.value)
    isConfigOpen.value = false
    errorMessage.value = null
  } catch (error) {
    errorMessage.value = 'API Key 无效'
  }
}

const handleClearConversation = () => {
  messages.value = []
}

const handleExampleClick = (example: string) => {
  input.value = example
  if (textareaRef.value) {
    textareaRef.value.focus()
  }
}

const toggleMinimize = () => {
  isMinimized.value = !isMinimized.value
}

// 格式化时间
const formatTime = (timestamp: number | undefined) => {
  if (!timestamp) return ''
  return new Date(timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

// 格式化消息内容，处理 Markdown
const formatMessageContent = (content: string): string => {
  if (!content) return ''
  
  try {
    // 配置 marked 选项
    marked.setOptions({
      breaks: true,
      gfm: true
    })
    
    // 转换 Markdown 为 HTML - 确保同步处理
    const htmlResult = marked.parse(content)
    
    // 如果是 Promise，等待解析
    if (htmlResult instanceof Promise) {
      // 对于流式输出，我们暂时返回原始内容，避免阻塞
      return DOMPurify.sanitize(content.replace(/\n/g, '<br>'))
    }
    
    // 使用 DOMPurify 清理 HTML
    return DOMPurify.sanitize(htmlResult as string)
  } catch (error) {
    console.error('Markdown 解析错误:', error)
    // 出错时返回安全的原始内容
    return DOMPurify.sanitize(content.replace(/\n/g, '<br>'))
  }
}

// 监听消息变化，自动滚动
watch(messages, async () => {
  await nextTick()
  messagesEndRef.value?.scrollIntoView({ behavior: 'smooth' })
}, { deep: true, flush: 'post' })

// 监听输入变化，自动调整高度
watch(input, async () => {
  await nextTick()
  if (textareaRef.value) {
    textareaRef.value.style.height = 'auto'
    textareaRef.value.style.height = `${Math.min(100, textareaRef.value.scrollHeight)}px`
  }
})

// 监听拖动状态
watch(isDraggingAIPanel, (newValue) => {
  if (newValue) {
    document.addEventListener('mousemove', handleMouseMove)
    document.addEventListener('mouseup', handleMouseUp)
  } else {
    document.removeEventListener('mousemove', handleMouseMove)
    document.removeEventListener('mouseup', handleMouseUp)
  }
})

// 初始化
onMounted(() => {
  // 从localStorage读取设置
  const savedUseAliyun = localStorage.getItem('use_aliyun_model') !== 'false' // 默认true
  const savedApiKey = localStorage.getItem('api_key') || 'sk-c2a1475efdb2400f821de7681c4e7c2d'
  const savedModelName = localStorage.getItem('model_name') || 'qwen-max-latest'

  useAliyunModel.value = savedUseAliyun
  apiKey.value = savedApiKey
  modelName.value = savedModelName

  // 应用设置到服务
  aiService.toggleModelSource(savedUseAliyun)
  aiService.setApiKey(savedApiKey)
  aiService.setModel(savedModelName)

  // 加载模型列表
  fetchAvailableModels()
})

// 清理事件监听器
onUnmounted(() => {
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleMouseUp)
})
</script>

<style>
:root {
    /* 面板基础颜色变量 - 与主题保持一致的淡蓝白色调 */
    --ai-primary: #2b6cb0;
    --ai-primary-light: #ebf4ff;
    --ai-primary-dark: #2c5282;
    --ai-accent: #3182ce;
    --ai-surface-lightest: #ffffff;
    --ai-surface-light: #f7fafc;
    --ai-surface-mid: #edf2f7;
    --ai-surface-dark: #e2e8f0;
    --ai-text-dark: #2d3748;
    --ai-text-mid: #4a5568;
    --ai-text-light: #718096;
    --ai-border-light: #e2e8f0;
    --ai-shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.05);
    --ai-shadow-md: 0 4px 6px rgba(0, 0, 0, 0.05);
    --ai-shadow-lg: 0 10px 15px rgba(0, 0, 0, 0.1);
    --ai-transition-default: all 0.2s ease;
    --ai-border-radius: 8px;
    --ai-spacing-xs: 4px;
    --ai-spacing-sm: 8px;
    --ai-spacing-md: 16px;
    --ai-spacing-lg: 24px;
}

/* 主面板容器 */
.ai-chat-panel {
    width: 320px;
    height: 100%;
    display: flex;
    flex-direction: column;
    background-color: var(--ai-surface-light);
    border-left: 1px solid var(--ai-border-light);
    position: relative;
    min-width: 250px;
    max-width: 500px;
    transition: width 0.2s ease, background-color 0.3s ease;
    overflow: hidden;
}

/* 最小化状态 */
.ai-chat-panel.minimized {
    min-width: unset;
    width: 48px !important;
    background-color: var(--ai-surface-lightest);
    border-left: 1px solid var(--ai-border-light);
    box-shadow: -2px 0 8px rgba(0, 0, 0, 0.05);
    transition: width 0.3s ease, background-color 0.3s ease, box-shadow 0.3s ease;
}

.ai-chat-panel.minimized .ai-chat-header {
    height: auto;
    padding: 12px 0;
    justify-content: center;
    flex-direction: column;
    gap: 12px;
    border-bottom: none;
    background-color: transparent;
    box-shadow: none;
}

.ai-chat-panel.minimized .ai-chat-title {
    justify-content: center;
    width: 100%;
}

.ai-chat-panel.minimized .ai-chat-icon {
    margin: 0 auto;
}

.ai-chat-panel.minimized .ai-chat-icon .ai-logo-img {
    width: 28px;
    height: 28px;
    opacity: 0.85;
    transition: transform 0.2s ease, opacity 0.2s ease;
}

.ai-chat-panel.minimized .ai-chat-icon:hover .ai-logo-img {
    transform: scale(1.1);
    opacity: 1;
}

.ai-chat-panel.minimized .ai-chat-actions {
    justify-content: center;
    width: 100%;
}

.ai-chat-panel.minimized .ai-action-btn {
    width: 32px;
    height: 32px;
    background-color: var(--ai-surface-light);
    border-radius: 50%;
}

.ai-chat-panel.minimized .ai-action-btn:hover {
    background-color: var(--ai-primary-light);
    transform: scale(1.1);
}

.ai-chat-panel.minimized:hover {
    background-color: var(--ai-surface-light);
    border-left-color: var(--ai-primary);
}

/* 面板拖动控件 */
.ai-panel-resizer {
    width: 8px;
    height: 100%;
    position: absolute;
    left: -4px;
    top: 0;
    cursor: col-resize;
    background-color: transparent;
    transition: var(--ai-transition-default);
    z-index: 10;
}

.ai-panel-resizer:hover,
.ai-panel-resizer:active {
    background-color: rgba(49, 130, 206, 0.6);
}

/* 面板头部 */
.ai-chat-header {
    height: 48px;
    padding: 0 var(--ai-spacing-md);
    display: flex;
    align-items: center;
    justify-content: space-between;
    border-bottom: 1px solid var(--ai-border-light);
    background-color: var(--ai-surface-mid);
    box-shadow: var(--ai-shadow-sm);
    z-index: 2;
}

.ai-chat-title {
    display: flex;
    align-items: center;
    gap: var(--ai-spacing-sm);
}

.ai-chat-title span {
    font-weight: 600;
    font-size: 15px;
    color: var(--ai-primary-dark);
    white-space: nowrap;
}

.ai-chat-icon {
    display: flex;
    align-items: center;
    justify-content: center;
}

/* 头部操作按钮 */
.ai-chat-actions {
    display: flex;
    align-items: center;
    gap: 4px;
}

.ai-action-btn {
    background: none;
    border: none;
    width: 28px;
    height: 28px;
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: var(--ai-text-mid);
    transition: var(--ai-transition-default);
    padding: 0;
}

.ai-action-btn:hover {
    background-color: rgba(0, 0, 0, 0.05);
    color: var(--ai-primary);
}

/* 内容区域 */
.ai-chat-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    height: calc(100% - 48px);
}

/* API Key 配置区域 */
.ai-key-config {
    padding: var(--ai-spacing-md);
    background-color: var(--ai-surface-mid);
    border-bottom: 1px solid var(--ai-border-light);
    animation: slideDown 0.3s ease;
}

@keyframes slideDown {
    from {
        opacity: 0;
        transform: translateY(-10px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.ai-key-header {
    margin-bottom: var(--ai-spacing-md);
}

.ai-key-header h3 {
    font-size: 16px;
    font-weight: 600;
    margin: 0 0 var(--ai-spacing-xs);
    color: var(--ai-primary-dark);
}

.ai-key-info {
    font-size: 13px;
    color: var(--ai-text-mid);
    margin: 0;
}

.ai-key-input-container {
    display: flex;
    gap: var(--ai-spacing-sm);
    margin-bottom: var(--ai-spacing-sm);
}

.ai-key-input {
    flex: 1;
    /* color: #ffffff; */
    padding: 8px 12px;
    border: 1px solid var(--ai-border-light);
    border-radius: var(--ai-border-radius);
    font-size: 14px;
    background-color: var(--ai-surface-lightest);
    transition: var(--ai-transition-default);
    width: 100%;
}

.ai-key-input:focus {
    outline: none;
    border-color: var(--ai-accent);
    box-shadow: 0 0 0 1px var(--ai-accent);
}

.ai-key-submit,
.ai-config-save {
    padding: 8px 16px;
    background-color: var(--ai-primary);
    color: white;
    border: none;
    border-radius: var(--ai-border-radius);
    font-weight: 500;
    cursor: pointer;
    transition: var(--ai-transition-default);
}

.ai-key-submit:hover,
.ai-config-save:hover {
    background-color: var(--ai-primary-dark);
}

.ai-key-note {
    font-size: 12px;
    color: var(--ai-text-light);
    margin: 0;
    line-height: 1.5;
}

/* 错误消息 */
.ai-error-message {
    margin: var(--ai-spacing-xs) var(--ai-spacing-md);
    padding: var(--ai-spacing-sm);
    border-radius: var(--ai-border-radius);
    background-color: #fee2e2;
    border: 1px solid #f87171;
    color: #b91c1c;
    font-size: 13px;
    animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
    from { opacity: 0; }
    to { opacity: 1; }
}

/* 消息容器 */
.ai-messages-container {
    flex: 1;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: var(--ai-spacing-md);
    padding: var(--ai-spacing-md);
    scroll-behavior: smooth;
}

/* 自定义滚动条 */
.ai-messages-container::-webkit-scrollbar {
    width: 6px;
}

.ai-messages-container::-webkit-scrollbar-track {
    background: transparent;
}

.ai-messages-container::-webkit-scrollbar-thumb {
    background-color: var(--ai-border-light);
    border-radius: 20px;
}

.ai-messages-container::-webkit-scrollbar-thumb:hover {
    background-color: var(--ai-text-light);
}

/* 空状态 */
.ai-empty-state {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    gap: var(--ai-spacing-lg);
    padding: var(--ai-spacing-md) 0;
}

.ai-welcome {
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    margin-bottom: var(--ai-spacing-md);
}

.ai-welcome-icon {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 80px;
    height: 80px;
    background-color: var(--ai-primary-light);
    border-radius: 50%;
    box-shadow: var(--ai-shadow-sm);
    padding: 4px;
}

.ai-welcome h2 {
    margin: 0 0 var(--ai-spacing-xs);
    font-size: 18px;
    font-weight: 600;
    color: var(--ai-primary-dark);
}

.ai-welcome p {
    margin: 0;
    font-size: 14px;
    color: var(--ai-text-mid);
    max-width: 280px;
}

/* 示例提问 */
.ai-examples {
    width: 100%;
}

.ai-examples h3 {
    font-size: 14px;
    font-weight: 600;
    margin: 0 0 var(--ai-spacing-sm);
    color: var(--ai-text-mid);
    text-align: center;
}

.ai-examples-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.ai-example-item {
    padding: var(--ai-spacing-sm) var(--ai-spacing-md);
    background-color: var(--ai-surface-lightest);
    border: 1px solid var(--ai-border-light);
    border-radius: var(--ai-border-radius);
    font-size: 13px;
    cursor: pointer;
    transition: var(--ai-transition-default);
    display: flex;
    align-items: center;
    gap: var(--ai-spacing-sm);
    color: var(--ai-text-mid);
}

.ai-example-item:hover {
    background-color: var(--ai-primary-light);
    border-color: var(--ai-accent);
}

.icon-chat-bubble {
    font-size: 16px;
    color: var(--ai-primary);
}

/* 消息样式 */
.ai-message {
    display: flex;
    flex-direction: column;
    animation: messageAppear 0.3s ease;
    max-width: 100%;
    margin-bottom: 12px;
}

@keyframes messageAppear {
    from {
        opacity: 0;
        transform: translateY(10px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.message-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;
    max-width: 100%;
}

.message-bubble {
    padding: var(--ai-spacing-md);
    border-radius: var(--ai-border-radius);
    background-color: var(--ai-surface-mid);
    line-height: 1.5;
    font-size: 14px;
    color: var(--ai-text-dark);
    overflow-wrap: break-word;
    word-break: break-word;
    position: relative;
}

.user-message .message-bubble {
    background-color: var(--ai-primary-light);
    border: 1px solid rgba(43, 108, 176, 0.1);
}

.user-message .message-bubble::before {
    content: "你";
    position: absolute;
    top: -8px;
    right: 8px;
    background-color: var(--ai-primary);
    color: white;
    font-size: 10px;
    padding: 2px 6px;
    border-radius: 10px;
    font-weight: 500;
}

.assistant-message .message-bubble {
    background-color: var(--ai-surface-lightest);
    border: 1px solid var(--ai-border-light);
}

.assistant-message .message-bubble::before {
    content: "AI";
    position: absolute;
    top: -8px;
    left: 8px;
    background-color: var(--ai-primary);
    color: white;
    font-size: 10px;
    padding: 2px 6px;
    border-radius: 10px;
    font-weight: 500;
}

.message-time {
    font-size: 11px;
    color: var(--ai-text-light);
    margin-top: 4px;
}

.user-message .message-time {
    text-align: right;
    margin-right: 8px;
}

.assistant-message .message-time {
    text-align: left;
    margin-left: 8px;
}

/* 打字指示器 */
.ai-typing-indicator {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: var(--ai-spacing-xs);
}

.ai-typing-indicator span {
    width: 8px;
    height: 8px;
    background-color: var(--ai-primary);
    border-radius: 50%;
    display: inline-block;
    opacity: 0.6;
    animation: typing 1.5s infinite ease-in-out;
}

.ai-typing-indicator span:nth-child(2) {
    animation-delay: 0.2s;
}

.ai-typing-indicator span:nth-child(3) {
    animation-delay: 0.4s;
}

@keyframes typing {
    0%, 60%, 100% {
        transform: translateY(0);
    }
    30% {
        transform: translateY(-6px);
    }
}

/* 输入区域 */
.ai-chat-input-container {
    padding: var(--ai-spacing-md);
    background-color: var(--ai-surface-light);
    border-top: 1px solid var(--ai-border-light);
    display: flex;
    flex-direction: column;
    gap: var(--ai-spacing-xs);
}

.ai-input-wrapper {
    position: relative;
    display: flex;
    border-radius: var(--ai-border-radius);
    background-color: var(--ai-surface-lightest);
    border: 1px solid var(--ai-border-light);
    transition: var(--ai-transition-default);
    box-shadow: var(--ai-shadow-sm);
}

.ai-input-wrapper:focus-within {
    border-color: var(--ai-primary);
    box-shadow: 0 0 0 2px rgba(49, 130, 206, 0.15);
}

.ai-input-field {
    flex: 1;
    padding: 10px 16px;
    padding-right: 48px;
    background-color: transparent;
    border: none;
    font-family: inherit;
    font-size: 14px;
    color: var(--ai-text-dark);
    resize: none;
    min-height: 20px;
    max-height: 120px;
    line-height: 1.5;
    overflow-y: auto;
}

.ai-input-field:focus {
    outline: none;
}

.ai-input-field::placeholder {
    color: var(--ai-text-light);
}

.ai-send-button {
    position: absolute;
    right: 8px;
    top: 50%;
    transform: translateY(-50%);
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background-color: var(--ai-primary);
    border: none;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.2s ease;
    padding: 0;
    box-shadow: 0 2px 5px rgba(43, 108, 176, 0.2);
}

.ai-send-button img {
    width: 20px;
    height: 20px;
    opacity: 0.95;
    transition: opacity 0.2s ease;
}

.ai-send-button:hover:not(.disabled) {
    background-color: var(--ai-primary-dark);
    transform: translateY(-50%) scale(1.05);
}

.ai-send-button:hover:not(.disabled) img {
    opacity: 1;
}

.ai-send-button.disabled {
    background-color: var(--ai-surface-dark);
    cursor: not-allowed;
    opacity: 0.6;
}

.ai-send-button.disabled img {
    opacity: 0.6;
}

.ai-input-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 2px var(--ai-spacing-sm) 0;
}

.ai-helper-text {
    font-size: 11px;
    color: var(--ai-text-light);
}

.ai-helper-text.warning {
    color: #f59e0b;
}

kbd {
    background-color: var(--ai-surface-mid);
    border: 1px solid var(--ai-border-light);
    border-radius: 3px;
    box-shadow: 0 1px 0 rgba(0, 0, 0, 0.1);
    padding: 1px 4px;
    font-family: inherit;
    font-size: 10px;
    color: var(--ai-text-mid);
}

.ai-logo-img {
    width: 24px;
    height: 24px;
    border-radius: 4px;
    object-fit: cover;
}

.ai-welcome-logo {
    width: 48px;
    height: 48px;
    border-radius: 8px;
    object-fit: cover;
}

/* 配置项样式 */
.ai-config-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 15px;
}

.ai-config-label {
    /* color: #ffffff; */
    margin-bottom: 8px;
    text-align: center;
    width: 100%;
}

/* 单选按钮组样式 */
.ai-config-source-group {
    display: flex;
    flex-direction: column;
    gap: 12px;
    width: 100%;
}

.ai-source-option {
    display: flex;
    align-items: center;
    padding: 8px 12px;
    border: 1px solid var(--ai-border-light);
    border-radius: var(--ai-border-radius);
    transition: var(--ai-transition-default);
    cursor: pointer;
}

.ai-source-option:hover {
    background-color: var(--ai-surface-light);
    border-color: var(--ai-primary);
}

.ai-source-option input[type="radio"] {
    margin-right: 8px;
}

.source-label {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    font-size: 14px;
    color: var(--ai-text-dark);
}

.source-icon {
    width: 20px;
    height: 20px;
    object-fit: contain;
}

.ai-source-option input[type="radio"]:checked + .source-label {
    color: var(--ai-primary);
    font-weight: 600;
}

.ai-source-option:has(input[type="radio"]:checked) {
    background-color: var(--ai-primary-light);
    border-color: var(--ai-primary);
}

.ai-config-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
}

select.ai-key-input {
    cursor: pointer;
    appearance: none;
    background-repeat: no-repeat;
    background-position: right 12px center;
    padding-right: 36px;
}

/* Markdown 样式 */
.message-bubble h1,
.message-bubble h2,
.message-bubble h3,
.message-bubble h4,
.message-bubble h5,
.message-bubble h6 {
    margin: 12px 0 8px 0;
    font-weight: 600;
    color: var(--ai-text-dark);
}

.message-bubble h1 { font-size: 1.4em; }
.message-bubble h2 { font-size: 1.3em; }
.message-bubble h3 { font-size: 1.2em; }
.message-bubble h4 { font-size: 1.1em; }
.message-bubble h5 { font-size: 1.05em; }
.message-bubble h6 { font-size: 1em; }

.message-bubble p {
    margin: 8px 0;
    line-height: 1.6;
}

.message-bubble ul,
.message-bubble ol {
    margin: 8px 0;
    padding-left: 20px;
}

.message-bubble li {
    margin: 4px 0;
}

.message-bubble blockquote {
    margin: 12px 0;
    padding: 8px 12px;
    border-left: 3px solid var(--ai-primary);
    background-color: var(--ai-surface-light);
    font-style: italic;
}

.message-bubble code {
    background-color: var(--ai-surface-mid);
    padding: 2px 4px;
    border-radius: 3px;
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
    font-size: 0.9em;
}

.message-bubble pre {
    margin: 12px 0;
    padding: 12px;
    background-color: var(--ai-surface-mid);
    border-radius: var(--ai-border-radius);
    overflow-x: auto;
    border: 1px solid var(--ai-border-light);
}

.message-bubble pre code {
    background: none;
    padding: 0;
    border-radius: 0;
    font-size: 0.85em;
    line-height: 1.4;
}

.message-bubble table {
    border-collapse: collapse;
    width: 100%;
    margin: 12px 0;
}

.message-bubble th,
.message-bubble td {
    border: 1px solid var(--ai-border-light);
    padding: 8px 12px;
    text-align: left;
}

.message-bubble th {
    background-color: var(--ai-surface-mid);
    font-weight: 600;
}

.message-bubble a {
    color: var(--ai-primary);
    text-decoration: none;
}

.message-bubble a:hover {
    text-decoration: underline;
}

.message-bubble strong {
    font-weight: 600;
}

.message-bubble em {
    font-style: italic;
}

.message-bubble hr {
    border: none;
    border-top: 1px solid var(--ai-border-light);
    margin: 16px 0;
}
</style>