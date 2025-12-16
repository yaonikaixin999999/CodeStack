<template>
    <div class="ide-container">
      <div class="ide-main-layout">
        <!-- 活动栏 -->
        <ActivityBar
      :activeTab="activeTab"
      :width="activityBarWidth"
      :onTabChange="handleTabChange"
      :onDragStart="handleDragStart"
    />
  
        <!-- 侧边栏 -->
        <div
          class="sidebar"
          ref="sidebarRef"
          :style="{ width: `${sidebarWidth}px` }"
        >
          <div class="sidebar-header">
            <span>CloudStack Pro</span>
            <div class="sidebar-actions">
              <!-- 用户信息和退出登录按钮 -->
              <div class="user-info" ref="userInfoRef">
                <div class="user-avatar" @click="toggleUserMenu">
                  <img src="@/images/user.jpg" alt="用户头像" class="user-avatar-img" />
                </div>
                
                <!-- 用户菜单下拉框 -->
                <div v-if="showUserMenu" class="user-menu">
                  <div class="user-menu-header">
                    <div class="user-details">
                      <span class="username">{{ currentUser?.username }}</span>
                      <span class="user-status">在线</span>
                    </div>
                  </div>
                  <div class="user-menu-divider"></div>
                  <div class="user-menu-items">
                    <button class="user-menu-item" @click="handleSettings">
                      <i class="fas fa-cog"></i>
                      <span>设置</span>
                    </button>
                    <button class="user-menu-item" @click="handleProfile">
                      <i class="fas fa-user-circle"></i>
                      <span>个人资料</span>
                    </button>
                    <button class="user-menu-item logout-item" @click="handleLogout">
                      <i class="fas fa-sign-out-alt"></i>
                      <span>退出登录</span>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="sidebar-content">
            <div v-if="activeTab === 'explorer'" class="file-explorer">
                <!-- 标记这里到时候要看 -->
                <FileExplorer
          :activeFile="activeFile"
          :setActiveFile="setActiveFile"
          :openFileTab="openFileTab"
          :updateEditorContent="updateEditorContent"
        />
            </div>
            <div v-if="activeTab === 'search'" class="search-panel">
              <Search />
            </div>
            <div v-if="activeTab === 'git'" class="p-4">
              <InviteCollaborator />
            </div>
            <div v-if="activeTab === 'debug'" class="debug-panel">
              <RunAndDebug />
            </div>
          </div>
          <!-- 侧边栏拖动控件 -->
          <div
            class="sidebar-resizer"
            ref="sidebarResizerRef"
            @mousedown="handleSidebarMouseDown"
          ></div>
        </div>
  
        <!-- 主内容区 -->
        <div class="editor-and-panel-container">
          <!-- 编辑器区域 -->
          <EditorArea
      ref="editorAreaRef"
      :activeFile="activeFile"
      :openFiles="openFiles"
      :fileContents="fileContents"
      :editorContent="editorContent"
      :editorLanguage="editorLanguage"
      :isCollaborating="isCollaborating"
      :isReceivingRemoteChange="isReceivingRemoteChange"
      :isUserTyping="isUserTyping"
      :collaborators="collaborators"
      :panelHeight="panelHeight"
      :isDraggingPanel="isDraggingPanel"
      @update:activeFile="activeFile = $event"
      @update:openFiles="openFiles = $event"
      @update:fileContents="fileContents = $event"
      @update:editorContent="editorContent = $event"
      @update:editorLanguage="editorLanguage = $event"
      @update:activePanelTab="activePanelTab = $event"
      @update:terminalOutput="terminalOutput = $event"
      @update:panelHeight="panelHeight = $event"
      @update:isDraggingPanel="isDraggingPanel = $event"
      @file-save="handleFileSave"
      @file-compile="handleFileCompile"
      @file-run="handleFileRun"
      @terminal-execute="handleTerminalExecute"
      @collaboration-content-change="handleCollaborationContentChange"
      @collaboration-cursor-change="handleCollaborationCursorChange"
      @onChange="handleEditorChange"
      @onDragStart="handleDragStart"
    />
        </div>
  
        <!-- AI 聊天面板 -->
        <AIChatPanel
          :width="aiPanelWidth"
          :onResize="handleAIPanelResize"
        />
      </div>
  
      <!-- 拖动遮罩层 -->
      <div
        v-if="isDraggingSidebar || isDraggingPanel || isDraggingActivityBar"
        :class="getOverlayClassName()"
      ></div>
      
      <!-- 退出登录确认弹窗 -->
      <div v-if="showLogoutConfirm" class="logout-overlay">
        <div class="logout-modal">
          <div class="logout-modal-header">
            <h3>确认退出</h3>
            <button class="close-btn" @click="showLogoutConfirm = false">
              <i class="fas fa-times"></i>
            </button>
          </div>
          <div class="logout-modal-content">
            <p>确定要退出登录吗？未保存的工作可能会丢失。</p>
          </div>
          <div class="logout-modal-actions">
            <button class="btn-secondary" @click="showLogoutConfirm = false">
              取消
            </button>
            <button class="btn-danger" @click="confirmLogout">
              确认退出
            </button>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref, reactive, onMounted, onUnmounted, nextTick, watch } from 'vue'
  import { useRouter } from 'vue-router'
  import axios from 'axios'
  
  // 导入组件
  import FileExplorer from '@/components/editorhome/FileExplorer.vue'
  import EditorArea from '@/components/editorhome/EditorArea.vue'
  import ActivityBar from '@/components/editorhome/ActivityBar.vue'
  import Search from '@/components/editorhome/Search.vue'
  import InviteCollaborator from '@/components/editorhome/InviteCollaborator.vue'
  import RunAndDebug from '@/components/editorhome/RunAndDebug.vue'
  import AIChatPanel from '@/components/editorhome/AIChatPanel.vue'
  
  // 导入用户服务
  import { userService } from '@/services/userService'
  
  // API基础URL
  const API_BASE_URL = 'http://192.168.22.16:3001/api'
  
  // 路由
  const router = useRouter()
  
  // 用户相关状态
  const currentUser = ref(userService.getCurrentUser())
  const showUserMenu = ref(false)
  const showLogoutConfirm = ref(false)
  
  // DOM 引用
  const userInfoRef = ref<HTMLDivElement>()
  const editorAreaRef = ref<any>()

// 接口定义
interface FileTab {
  path: string;
  content: string;
  modified: boolean;
}

// 响应式数据
const openTabs = ref<FileTab[]>([]);

// 文件资源管理器
const setActiveFile = (filePath: string) => {
  // 保存当前编辑器内容到当前标签
  if (activeFile.value) {
    const currentTab = openTabs.value.find(tab => tab.path === activeFile.value);
    if (currentTab) {
      currentTab.content = editorContent.value;
    }
  }

  // 切换到新文件
  activeFile.value = filePath;
  
  // 加载新文件内容到编辑器
  const newTab = openTabs.value.find(tab => tab.path === filePath);
  if (newTab) {
    editorContent.value = newTab.content;
  }
};

// 打开文件标签
const openFileTab = async (filePath: string, content: string = '') => {
  console.log('EditorHome: openFileTab 被调用', filePath, '内容长度:', content?.length || 0);
  
  // 检查是否已经打开
  const existingTab = openTabs.value.find(tab => tab.path === filePath);
  
  if (existingTab) {
    console.log('EditorHome: 文件已打开，直接切换');
    // 如果已经打开，更新内容并切换到该标签
    if (content) {
      existingTab.content = content;
    }
    setActiveFile(filePath);
    return;
  }

  // 如果没有提供内容，尝试从服务器获取
  if (!content) {
    console.log('EditorHome: 从服务器获取文件内容');
    try {
      const response = await axios.get(`${API_BASE_URL}/files/content`, {
        params: { path: filePath },
        timeout: 5000
      });
      content = response.data.content || '';
      console.log('EditorHome: 获取到文件内容，长度:', content.length);
    } catch (error: any) {
      console.error('EditorHome: 获取文件内容失败:', error);
      content = `// 无法加载文件内容: ${filePath}\n// 错误: ${error.message || '未知错误'}`;
    }
  }

  console.log('EditorHome: 创建新标签');
  // 创建新标签
  const newTab: FileTab = {
    path: filePath,
    content: content,
    modified: false
  };

  openTabs.value.push(newTab);
  
  // 更新状态
  openFiles.value = openTabs.value.map(tab => tab.path);
  fileContents.value[filePath] = content;
  
  console.log('EditorHome: 更新后的状态');
  console.log('- openFiles:', openFiles.value);
  console.log('- fileContents keys:', Object.keys(fileContents.value));
  
  setActiveFile(filePath);
  
  // 关键修复：调用 EditorArea 的 openFileTab 方法
  console.log('EditorHome: 转发 openFileTab 调用到 EditorArea', filePath);
  console.log('EditorHome: editorAreaRef.value 存在:', !!editorAreaRef.value);
  console.log('EditorHome: openFileTab 方法存在:', !!(editorAreaRef.value?.openFileTab));
  
  if (editorAreaRef.value && editorAreaRef.value.openFileTab) {
    try {
      await editorAreaRef.value.openFileTab(filePath, content);
      console.log('EditorHome: EditorArea.openFileTab 调用成功');
    } catch (error) {
      console.error('EditorHome: EditorArea.openFileTab 调用失败:', error);
    }
  } else {
    console.error('EditorHome: EditorArea 引用不存在');
    console.log('EditorHome: 可用方法:', editorAreaRef.value ? Object.keys(editorAreaRef.value) : 'editorAreaRef.value 为空');
  }
};

// 关闭文件标签
const closeTab = (filePath: string) => {
  const tabIndex = openTabs.value.findIndex(tab => tab.path === filePath);
  if (tabIndex === -1) return;

  // 移除标签
  openTabs.value.splice(tabIndex, 1);

  // 如果关闭的是当前活动文件
  if (activeFile.value === filePath) {
    if (openTabs.value.length > 0) {
      // 切换到相邻的标签
      const newActiveIndex = tabIndex > 0 ? tabIndex - 1 : 0;
      setActiveFile(openTabs.value[newActiveIndex].path);
    } else {
      // 没有其他标签了
      activeFile.value = '';
      editorContent.value = '';
    }
  }
};

// 更新编辑器内容（用于错误显示等）
const updateEditorContent = (content: string) => {
  editorContent.value = content;
};

// 获取文件名
const getFileName = (filePath: string) => {
  return filePath.split('/').pop() || '';
};

// 用户菜单相关方法
const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
}

const handleSettings = () => {
  console.log('打开设置')
  showUserMenu.value = false
  // 这里可以添加打开设置页面的逻辑
}

const handleProfile = () => {
  console.log('打开个人资料')
  showUserMenu.value = false
  // 这里可以添加打开个人资料页面的逻辑
}

const handleLogout = () => {
  showUserMenu.value = false
  showLogoutConfirm.value = true
}

const confirmLogout = () => {
  userService.logout()
  router.push('/login')
}

// 事件处理
const handleFileSave = (filePath: string, content: string) => {
  console.log('File saved:', filePath, content)
}

const handleFileCompile = (filePath: string) => {
  console.log('File compiled:', filePath)
}

const handleFileRun = (filePath: string) => {
  console.log('File run:', filePath)
}

const handleTerminalExecute = (command: string) => {
  console.log('Terminal command:', command)
}

const handleCollaborationContentChange = (content: string) => {
  console.log('Collaboration content change:', content)
}

const handleCollaborationCursorChange = (position: any, selection: any) => {
  console.log('Collaboration cursor change:', position, selection)
}

const handleTabChange = (tab: string) => {
  activeTab.value = tab
}

const handleEditorChange = (value: string | undefined) => {
  console.log('Editor change:', value)
}

const handleDragStart = () => {
  console.log('Drag start')
}

// 状态变量
const activeFile = ref('')
const openFiles = ref<string[]>([])
const fileContents = ref<Record<string, string>>({})
const editorContent = ref('')
const editorLanguage = ref('javascript')
const activePanelTab = ref('终端')
const terminalOutput = ref<string[]>([])
const panelHeight = ref(200)
const isDraggingPanel = ref(false)

// 协作状态
const isCollaborating = ref(false)
const isReceivingRemoteChange = ref(false)
const isUserTyping = ref(false)
const collaborators = ref([])
  
  // 拖动状态
  const isDraggingActivityBar = ref(false)
  const isDraggingSidebar = ref(false)
  const activityBarWidth = ref(50)
  const sidebarWidth = ref(320)
  const aiPanelWidth = ref(320)
  
  // DOM 引用
  const sidebarRef = ref<HTMLDivElement>()
  const terminalRef = ref<HTMLDivElement>()
  const sidebarResizerRef = ref<HTMLDivElement>()
  const settingsRef = ref<HTMLDivElement>()
  
  // 活动栏和设置菜单
  const activeTab = ref('explorer')
  const showSettingsMenu = ref(false)
  
  const handleSidebarMouseDown = (e: MouseEvent) => {
    e.preventDefault()
    isDraggingSidebar.value = true
  }
  
  const handleAIPanelResize = (newWidth: number) => {
    aiPanelWidth.value = newWidth
  }

  const getOverlayClassName = () => {
    if (isDraggingActivityBar.value) return 'resize-overlay dragging-activity-bar'
    if (isDraggingSidebar.value) return 'resize-overlay dragging-sidebar'
    if (isDraggingPanel.value) return 'resize-overlay dragging-panel'
    return 'resize-overlay'
  }
  
  // 鼠标事件处理
  const handleMouseMove = (e: MouseEvent) => {
    if (isDraggingActivityBar.value) {
      const newWidth = Math.max(50, Math.min(150, e.clientX))
      activityBarWidth.value = newWidth
    } else if (isDraggingSidebar.value) {
      const newWidth = Math.max(200, Math.min(500, e.clientX - activityBarWidth.value))
      sidebarWidth.value = newWidth
    } else if (isDraggingPanel.value) {
      const container = document.querySelector('.editor-and-panel-container')
      if (container) {
        const containerRect = container.getBoundingClientRect()
        const newHeight = Math.max(100, Math.min(400, containerRect.bottom - e.clientY))
        panelHeight.value = newHeight
      }
    }
  }
  
  const handleMouseUp = () => {
    isDraggingActivityBar.value = false
    isDraggingSidebar.value = false
    isDraggingPanel.value = false
  }
  
  // 点击外部关闭设置菜单
  const handleClickOutside = (event: MouseEvent) => {
    if (settingsRef.value && !settingsRef.value.contains(event.target as Node)) {
      showSettingsMenu.value = false
    }
    
    // 关闭用户菜单
    if (userInfoRef.value && !userInfoRef.value.contains(event.target as Node)) {
      showUserMenu.value = false
    }
  }
  
  // 监听拖动状态
  watch([isDraggingActivityBar, isDraggingSidebar, isDraggingPanel], ([activityBar, sidebar, panel]) => {
    if (activityBar || sidebar || panel) {
      document.addEventListener('mousemove', handleMouseMove)
      document.addEventListener('mouseup', handleMouseUp)
    } else {
      document.removeEventListener('mousemove', handleMouseMove)
      document.removeEventListener('mouseup', handleMouseUp)
    }
  })
  
  // 检查用户登录状态
  const checkAuth = () => {
    currentUser.value = userService.getCurrentUser()
    if (!currentUser.value) {
      router.push('/login')
    }
  }
  
  // 生命周期钩子
  onMounted(() => {
    checkAuth()
    document.addEventListener('mousedown', handleClickOutside)
  })
  
  onUnmounted(() => {
    document.removeEventListener('mousedown', handleClickOutside)
    document.removeEventListener('mousemove', handleMouseMove)
    document.removeEventListener('mouseup', handleMouseUp)
  })
  </script>

<style>
/* 基础样式重置 */
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

/* 变量定义 - 高质感淡蓝白色主题 */
:root {
    --primary-blue: #2b6cb0;
    --primary-light: #ebf4ff;
    --primary-dark: #2c5282;
    --accent-blue: #3182ce;
    --surface-lightest: #ffffff;
    --surface-light: #f7fafc;
    --surface-mid: #edf2f7;
    --surface-dark: #e2e8f0;
    --text-dark: #2d3748;
    --text-mid: #4a5568;
    --text-light: #718096;
    --border-light: #e2e8f0;
    --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);
    --shadow-md: 0 4px 6px rgba(0, 0, 0, 0.05);
    --shadow-lg: 0 10px 15px rgba(0, 0, 0, 0.1);
    --transition-default: all 0.2s ease;
    --danger-color: #e53e3e;
    --danger-hover: #c53030;
}

/* 主容器 */
.ide-container {
    display: flex;
    flex: 1;
    overflow: hidden;
    height: 100vh;
    width: 100vw;
    color: var(--text-dark);
    background-color: var(--surface-light);
    font-family: 'Inter', 'Segoe UI', Arial, sans-serif;
    font-size: 13px;
    position: fixed;
    top: 0;
    left: 0;
}

/* 侧边栏 */
.sidebar {
    width: 300px;
    background-color: var(--surface-light);
    border-right: 1px solid var(--border-light);
    display: flex;
    flex-direction: column;
    min-width: 150px;
    max-width: 500px;
    overflow: hidden;
    position: relative;
    flex-shrink: 0;
}

.sidebar-header {
    padding: 20px;
    font-weight: bold;
    display: flex;
    justify-content: space-between;
    align-items: center;
    background-color: var(--surface-light);
    border-bottom: 1px solid var(--border-light);
    color: var(--primary-dark);
}

.sidebar-actions {
    display: flex;
    gap: 10px;
    align-items: center;
}

/* 用户信息组件 */
.user-info {
    position: relative;
    display: flex;
    align-items: center;
}

.user-avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    cursor: pointer;
    transition: var(--transition-default);
    overflow: hidden;
    border: 2px solid var(--border-light);
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: var(--surface-lightest);
}

.user-avatar:hover {
    transform: scale(1.05);
    box-shadow: var(--shadow-md);
    border-color: var(--accent-blue);
}

/* 用户头像图片 */
.user-avatar-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 50%;
}

/* 用户菜单下拉框 */
.user-menu {
    position: absolute;
    top: 100%;
    right: 0;
    margin-top: 8px;
    background-color: var(--surface-lightest);
    border: 1px solid var(--border-light);
    border-radius: 8px;
    box-shadow: var(--shadow-lg);
    min-width: 200px;
    z-index: 1000;
    overflow: hidden;
}

.user-menu-header {
    padding: 16px;
    background-color: var(--surface-light);
    border-bottom: 1px solid var(--border-light);
}

.user-details {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.username {
    font-weight: 600;
    color: var(--text-dark);
    font-size: 14px;
}

.user-status {
    font-size: 12px;
    color: var(--text-light);
}

.user-menu-divider {
    height: 1px;
    background-color: var(--border-light);
}

.user-menu-items {
    padding: 8px 0;
}

.user-menu-item {
    width: 100%;
    padding: 12px 16px;
    background: none;
    border: none;
    display: flex;
    align-items: center;
    gap: 12px;
    cursor: pointer;
    transition: var(--transition-default);
    color: var(--text-dark);
    font-size: 14px;
    text-align: left;
}

.user-menu-item:hover {
    background-color: var(--surface-light);
}

.user-menu-item.logout-item {
    color: var(--danger-color);
}

.user-menu-item.logout-item:hover {
    background-color: #fef2f2;
}

.user-menu-item i {
    width: 16px;
    font-size: 14px;
}

/* 退出登录确认弹窗 */
.logout-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 2000;
}

.logout-modal {
    background-color: var(--surface-lightest);
    border-radius: 12px;
    box-shadow: var(--shadow-lg);
    width: 90%;
    max-width: 400px;
    overflow: hidden;
}

.logout-modal-header {
    padding: 20px;
    border-bottom: 1px solid var(--border-light);
    display: flex;
    justify-content: space-between;
    align-items: center;
    background-color: var(--surface-light);
}

.logout-modal-header h3 {
    margin: 0;
    color: var(--text-dark);
    font-size: 18px;
    font-weight: 600;
}

.close-btn {
    background: none;
    border: none;
    font-size: 16px;
    color: var(--text-light);
    cursor: pointer;
    padding: 4px;
    border-radius: 4px;
    transition: var(--transition-default);
}

.close-btn:hover {
    background-color: var(--surface-dark);
    color: var(--text-dark);
}

.logout-modal-content {
    padding: 20px;
}

.logout-modal-content p {
    margin: 0;
    color: var(--text-mid);
    line-height: 1.5;
}

.logout-modal-actions {
    padding: 20px;
    display: flex;
    gap: 12px;
    justify-content: flex-end;
    border-top: 1px solid var(--border-light);
}

.btn-secondary {
    padding: 8px 16px;
    border: 1px solid var(--border-light);
    background-color: var(--surface-lightest);
    color: var(--text-dark);
    border-radius: 6px;
    cursor: pointer;
    transition: var(--transition-default);
    font-size: 14px;
}

.btn-secondary:hover {
    background-color: var(--surface-light);
}

.btn-danger {
    padding: 8px 16px;
    border: 1px solid var(--danger-color);
    background-color: var(--danger-color);
    color: white;
    border-radius: 6px;
    cursor: pointer;
    transition: var(--transition-default);
    font-size: 14px;
}

.btn-danger:hover {
    background-color: var(--danger-hover);
    border-color: var(--danger-hover);
}

.sidebar-content {
    flex: 1;
    overflow: auto;
    padding: 0px 0;
}

.file-explorer {
    padding-left: 5px;
}

/* 主布局容器，使用flex布局 */
.ide-main-layout {
    display: flex;
    height: 100%;
    width: 100%;
}

/* 让编辑器和面板容器占据中间区域 */
.editor-and-panel-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

/* 侧边栏拖动控件 - 右侧边框 */
.sidebar-resizer {
    width: 8px;
    height: 100%;
    position: absolute;
    right: -4px;
    top: 0;
    cursor: col-resize;
    background-color: transparent;
    transition: var(--transition-default);
    z-index: 10;
}

/* 拖动时的视觉反馈 */
.sidebar-resizer:hover,
.sidebar-resizer:active {
    background-color: rgba(49, 130, 206, 0.6);
}

/* 拖动时的遮罩层 */
.resize-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 9999;
    background-color: transparent;
}

/* 根据拖动类型设置不同的光标 */
.resize-overlay.dragging-sidebar,
.resize-overlay.dragging-activity-bar {
    cursor: col-resize;
}

.resize-overlay.dragging-panel {
    cursor: ns-resize;
}

/* 移动端适配 */
@media (max-width: 768px) {
    .ide-container {
        font-size: 12px;
    }

    /* 活动栏在移动端保持最小宽度 */
    .ide-main-layout {
        position: relative;
    }

    /* 侧边栏移动端适配 */
    .sidebar {
        width: 100vw;
        position: absolute;
        left: 50px; /* 活动栏宽度 */
        top: 0;
        z-index: 100;
        transform: translateX(-100%);
        transition: transform 0.3s ease;
        box-shadow: var(--shadow-lg);
    }

    .sidebar.mobile-open {
        transform: translateX(0);
    }

    .sidebar-header {
        padding: 15px;
        font-size: 14px;
    }

    .sidebar-header span {
        font-size: 14px;
    }

    /* 用户信息移动端适配 */
    .user-avatar {
        width: 28px;
        height: 28px;
    }

    .user-menu {
        right: -10px;
        min-width: 180px;
        max-width: 240px;
    }

    .user-menu-header {
        padding: 12px;
    }

    .username {
        font-size: 13px;
    }

    .user-status {
        font-size: 11px;
    }

    .user-menu-item {
        padding: 10px 12px;
        font-size: 13px;
    }

    /* 编辑器和面板容器适配 */
    .editor-and-panel-container {
        margin-left: 50px; /* 活动栏宽度 */
        width: calc(100vw - 50px);
    }

    /* AI面板移动端适配 */
    .ai-chat-panel {
        position: absolute;
        right: -100%;
        top: 0;
        height: 100%;
        z-index: 101;
        transition: right 0.3s ease;
        box-shadow: var(--shadow-lg);
    }

    .ai-chat-panel.mobile-open {
        right: 0;
    }

    /* 禁用拖动功能 */
    .sidebar-resizer,
    .ai-panel-resizer,
    .panel-resizer {
        display: none;
    }

    /* 退出登录弹窗适配 */
    .logout-modal {
        width: 95%;
        max-width: 350px;
        margin: 0 10px;
    }

    .logout-modal-header {
        padding: 15px;
    }

    .logout-modal-header h3 {
        font-size: 16px;
    }

    .logout-modal-content {
        padding: 15px;
    }

    .logout-modal-actions {
        padding: 15px;
        gap: 8px;
    }

    .btn-secondary,
    .btn-danger {
        padding: 8px 12px;
        font-size: 13px;
    }
}

/* 超小屏幕适配 */
@media (max-width: 480px) {
    .ide-container {
        font-size: 11px;
    }

    .sidebar-header {
        padding: 12px;
        flex-direction: column;
        gap: 8px;
        align-items: flex-start;
    }

    .sidebar-header span {
        font-size: 13px;
    }

    .user-avatar {
        width: 24px;
        height: 24px;
    }

    .user-menu {
        right: 0;
        left: auto;
        min-width: 160px;
        max-width: 200px;
    }

    .user-menu-header {
        padding: 10px;
    }

    .username {
        font-size: 12px;
    }

    .user-status {
        font-size: 10px;
    }

    .user-menu-item {
        padding: 8px 10px;
        font-size: 12px;
        gap: 8px;
    }

    .user-menu-item i {
        width: 14px;
        font-size: 12px;
    }

    /* 弹窗进一步缩小 */
    .logout-modal {
        width: 98%;
        margin: 0 5px;
    }

    .logout-modal-header {
        padding: 12px;
    }

    .logout-modal-header h3 {
        font-size: 15px;
    }

    .logout-modal-content {
        padding: 12px;
    }

    .logout-modal-content p {
        font-size: 13px;
        line-height: 1.4;
    }

    .logout-modal-actions {
        padding: 12px;
        gap: 6px;
        flex-direction: column;
    }

    .btn-secondary,
    .btn-danger {
        width: 100%;
        padding: 10px;
        font-size: 12px;
        text-align: center;
    }
}

/* 横屏模式适配 */
@media (max-width: 768px) and (orientation: landscape) {
    .sidebar {
        width: 60vw;
        max-width: 350px;
    }

    .editor-and-panel-container {
        width: calc(100vw - 50px);
    }

    .ai-chat-panel {
        width: 50vw;
        max-width: 300px;
    }
}

/* 触摸优化 */
@media (hover: none) and (pointer: coarse) {
    .user-avatar,
    .user-menu-item,
    .btn-secondary,
    .btn-danger,
    .close-btn {
        min-height: 44px;
        min-width: 44px;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .user-menu-item {
        padding: 12px 16px;
    }
}
</style>