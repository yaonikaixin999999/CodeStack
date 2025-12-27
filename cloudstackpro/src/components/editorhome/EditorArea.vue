<template>
  <div class="main-content">
    <div class="editor-area">
      <div class="editor-tabs">
        <!-- 文件标签区 -->
        <div class="tabs-container">
          <div v-if="openFiles.length > 0" class = 'tabs-list'>
            <div
              v-for="file in openFiles"
              :key="file"
              :class="['editor-tab', { active: activeFile === file }]"
              @click="switchToFile(file)"
            >
              <span class="tab-filename">{{ file.split('/').pop() }}</span>
              <span
                class="tab-close"
                @click="closeFileTab(file, $event)"
                title="关闭"
              >
                ×
              </span>
            </div>
          </div>
          <div v-else class="editor-tab active">
            <span>未打开文件</span>
          </div>
        </div>

        <!-- 编辑器操作按钮 -->
        <div v-if="activeFile" class="editor-actions">
                    <!-- 协作状态指示器 -->
          <div v-if="isCollaborating" class="collaboration-indicator">
            <div class="collaborators-list">
              <span class="collaboration-status">
                <span v-if="isUserTyping">✏️ </span>
                <span v-if="isReceivingRemoteChange">📥 </span>
                 在线{{ collaborators.length }}人:
              </span>
              <span 
                v-for="user in collaborators" 
                :key="user.userId" 
                class="collaborator-badge"
              >
                {{ user.userName }}
              </span>
            </div>
          </div>
          <div
            class="editor-action-button"
            @click="saveFile"
            title="保存 (Ctrl+S)"
          >
            💾 保存
          </div>
          <div
            class="editor-action-button"
            @click="compileCurrentFile"
            title="编译 (F6)"
          >
            🔨 编译
          </div>
          <div
            class="editor-action-button"
            @click="runCurrentFile"
            title="运行 (F5)"
          >
            ▶️ 运行
          </div>
        </div>
      </div>
      
      <div class="editor-content" style="height: 400px;min-height: 400px;">
        <!-- 使用原生 Monaco Editor -->
        <div
          ref="editorContainer"
          style="width: 100%; height: 100%;"
        ></div>
      </div>
    </div>
    
    <!-- 面板区域 -->
    <div
      class="panel-area"
      ref="panelRef"
      :style="{ height: `300px` }"
    >
      <!-- 面板拖动控件 -->
      <div
        class="panel-resizer"
        ref="panelResizerRef"
        @mousedown="handlePanelMouseDown"
      ></div>
      
      <div class="panel-tabs">
        <div
          :class="['panel-tab', { active: activePanelTab === '问题' }]"
          @click="activePanelTab = '问题'"
        >
          问题
        </div>
        <div
          :class="['panel-tab', { active: activePanelTab === '输出' }]"
          @click="activePanelTab = '输出'"
        >
          输出
        </div>
        <div
          :class="['panel-tab', { active: activePanelTab === '调试控制台' }]"
          @click="activePanelTab = '调试控制台'"
        >
          调试控制台
        </div>
        <div
          :class="['panel-tab', { active: activePanelTab === '终端' }]"
          @click="activePanelTab = '终端'"
        >
          终端
        </div>
      </div>
      
      <div class="panel-content">
        <!-- 终端面板 -->
        <div
          v-if="activePanelTab === '终端'"
          class="terminal"
          ref="terminalRef"
          :style="{ height: '100%', overflow: 'auto' }"
        >
          <div
            v-for="(line, index) in terminalOutput"
            :key="index"
            class="terminal-line"
          >
            {{ line }}
          </div>
          <div class="terminal-input-line" :style="{ display: 'flex', alignItems: 'center' }">
            <span>root@yaonikaixin9999:~# </span>
            <input
              type="text"
              v-model="terminalCommand"
              @keypress="handleTerminalKeypress" 
              placeholder="输入命令..."
              :style="{
                background: 'transparent',
                border: 'none',
                color: 'inherit',
                fontFamily: 'inherit',
                fontSize: 'inherit',
                width: 'calc(100% - 20px)',
                outline: 'none'
              }"
            />
          </div>
        </div>
        
        <!-- 问题面板 -->
        <div v-if="activePanelTab === '问题'" class="problems-panel">
          <div>没有发现问题</div>
        </div>
        
        <!-- 输出面板 -->
        <div v-if="activePanelTab === '输出'" class="output-panel">
          <div>已成功连接到远程服务器</div>
          <div>远程服务器: 120.46.182.160</div>
        </div>
        
        <!-- 调试控制台 -->
        <div v-if="activePanelTab === '调试控制台'" class="debug-console">
          <div>调试会话未启动</div>
          <div>按 F5 运行当前文件</div>
          <div>按 F6 编译当前文件</div>
        </div>
      </div>
    </div>

    <!-- 状态栏 -->
    <div class="status-bar">
      <div class="status-items-left">
        <div class="status-item status-branch">
          <span class="status-git-icon">⑂</span>main
        </div>
        <div class="status-item status-metrics">
          <span class="error">0</span>
          <span class="warning">0</span>
        </div>
      </div>
      <div class="status-items-right">
        <div class="status-item">远程: 120.46.182.160</div>
        <div class="status-item">UTF-8</div>
        <div class="status-item">LF</div>
        <div class="status-item">{{ editorLanguage }}</div>
        <div class="status-item status-position">行 1, 列 1</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, nextTick ,onBeforeUnmount} from 'vue'
import axios from 'axios'
import * as monaco from 'monaco-editor'
// 预先引入语言包，避免运行时按需加载 chunk 失败
import 'monaco-editor/esm/vs/basic-languages/javascript/javascript.contribution'
import 'monaco-editor/esm/vs/language/typescript/monaco.contribution'
// import loader from '@monaco-editor/loader'

// API基础URL - 动态获取
const getApiBaseUrl = () => {
  const hostname = window.location.hostname;
  if (hostname === 'localhost' || hostname === '127.0.0.1') {
    return 'http://localhost:3001/api';
  }
  return `http://${hostname}:3001/api`;
}
const API_BASE_URL = getApiBaseUrl()

//协同编辑部分
import * as Y from 'yjs'
import { WebsocketProvider } from 'y-websocket'
import { MonacoBinding } from 'y-monaco'
import { collaborationService, CollaborationUser, FileChange } from '@/services/collaborationService';

// Yjs 文档和共享文本
// 修改Yjs相关变量声明
let ydoc: Y.Doc | null = null
let yText: Y.Text | null = null
let provider: WebsocketProvider | null = null
let monacoBinding: MonacoBinding | null = null
let hasInitializedYDoc = false

// WebSocket 连接
// const provider = new WebsocketProvider('ws://yaonikaixin999999.store:1234', 'my-room', ydoc)

// 添加协同编辑状态
const collaborators = ref<CollaborationUser[]>([])
const isCollaborating = ref(false)
const isReceivingRemoteChange = ref(false)
const lastRemoteChangeTime = ref(0)
const isUserTyping = ref(false)
let typingTimeoutId: NodeJS.Timeout | null = null
// let monacoBinding: MonacoBinding | null = null

// 添加当前房间追踪
const currentRoom = ref<string>('')

// Props 定义
interface Props {
  activeFile?: string
  openFiles?: string[]
  fileContents?: Record<string, string>
  editorContent?: string
  editorLanguage?: string
  isCollaborating?: boolean
  isReceivingRemoteChange?: boolean
  isUserTyping?: boolean
  collaborators?: any[]
  // panelHeight?: number
  isDraggingPanel?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  activeFile: '',
  openFiles: () => [],
  fileContents: () => ({}),
  editorContent: '',
  editorLanguage: 'javascript',
  isCollaborating: false,
  isReceivingRemoteChange: false,
  isUserTyping: false,
  collaborators: () => [],
  // panelHeight: 200,
  isDraggingPanel: false
})

// Emits 定义
const emit = defineEmits<{
  'update:activeFile': [file: string]
  'update:openFiles': [files: string[]]
  'update:fileContents': [contents: Record<string, string>]
  'update:editorContent': [content: string]
  'update:editorLanguage': [language: string]
  'update:activePanelTab': [tab: string]
  'update:terminalOutput': [output: string[]]
  // 'update:panelHeight': [height: number]
  'update:isDraggingPanel': [isDragging: boolean]
  'file-save': [filePath: string, content: string]
  'file-compile': [filePath: string]
  'file-run': [filePath: string]
  'terminal-execute': [command: string]
  'collaboration-content-change': [content: string]
  'collaboration-cursor-change': [position: any, selection: any]
}>()

// 响应式数据
const editorLanguage = ref<string>(props.editorLanguage || 'javascript')
const activeFile = ref<string>(props.activeFile || '')
const openFiles = ref<string[]>(props.openFiles || [])
const fileContents = ref<Record<string, string>>(props.fileContents || {})
const activePanelTab = ref('终端')
// const panelHeight = ref(props.panelHeight)

// SSH连接状态
const sshConnectionId = ref<string>('')
const isSSHConnected = ref(false)
const isConnecting = ref(false)

// 终端状态
const terminalOutput = ref<string[]>([
  'CloudStack Pro [版本 0.25.7.11]',
  '(c) Yaonikaixin999999。保留所有权利。',
  ' '
])
const terminalCommand = ref('')

// DOM引用
const panelRef = ref<HTMLDivElement>()
const panelResizerRef = ref<HTMLDivElement>()
const terminalRef = ref<HTMLDivElement>()
const editorContainer = ref<HTMLDivElement>()
let editor: monaco.editor.IStandaloneCodeEditor | null = null

// 编辑器实例
const editorInstance = ref<monaco.editor.IStandaloneCodeEditor | null>(null)

// 计算当前编辑器内容
const currentContent = computed({
  get() {
    console.log('currentContent get 被调用')
    console.log('activeFile.value:', activeFile.value)
    console.log('fileContents.value:', fileContents.value)
    
    if (activeFile.value && fileContents.value[activeFile.value] !== undefined) {
      console.log('返回文件内容:', fileContents.value[activeFile.value])
      return fileContents.value[activeFile.value]
    }
    console.log('返回默认内容')
    return ''
  },
  set(value: string) {
    console.log('currentContent set 被调用:', value)
    if (activeFile.value) {
      fileContents.value[activeFile.value] = value
      emit('update:fileContents', fileContents.value)
      emit('update:editorContent', value)
    }
  }
})

// Monaco Editor 配置选项
const editorOptions: monaco.editor.IStandaloneEditorConstructionOptions = {
  minimap: { enabled: false },
  automaticLayout: true,
  fontSize: 14,
  wordWrap: 'on',
  lineNumbers: 'on',
  readOnly: false,
  theme: 'vs',
  scrollBeyondLastLine: false,
  selectOnLineNumbers: true,
  roundedSelection: false,
  cursorStyle: 'line',
  contextmenu: true,
  value: '',
  language: 'javascript'
}

// 创建新的Yjs连接的函数
const createYjsConnection = (filePath: string) => {
  console.log('为文件创建Yjs连接:', filePath)
  
  // 清理之前的连接
  if (monacoBinding) {
    console.log('销毁之前的Monaco绑定')
    monacoBinding.destroy()
    monacoBinding = null
  }
  
  if (provider) {
    console.log('断开之前的WebSocket连接')
    provider.disconnect()
    provider = null
  }
  
  // 创建文件特定的房间名
  const roomName = `file:${filePath.replace(/[\/\\]/g, '_')}`
  console.log('创建房间:', roomName)
  
  // 创建新的Yjs文档和连接
  ydoc = new Y.Doc()
  yText = ydoc.getText('monaco')
  hasInitializedYDoc = false
  // 动态获取 Yjs WebSocket URL
  const getYjsUrl = () => {
    const hostname = window.location.hostname;
    if (hostname === 'localhost' || hostname === '127.0.0.1') {
      return 'ws://localhost:1234';
    }
    return `ws://${hostname}:1234`;
  }
  const yjsUrl = process.env.VUE_APP_YJS_WS_URL || getYjsUrl()
  provider = new WebsocketProvider(yjsUrl, roomName, ydoc)
  currentRoom.value = roomName
  
  // 监听连接状态
  provider.on('status', (event: { status: string }) => {
    console.log(`WebSocket Status for ${filePath}:`, event.status)
    if (event.status === 'connected') {
      console.log(`✅ 文件 ${filePath} 的协作连接建立成功`)
    }
  })
  
  // 监听同步状态
  provider.on('sync', (isSynced: boolean) => {
    console.log(`文件 ${filePath} 同步状态:`, isSynced ? '已同步' : '同步中')
    if (isSynced && yText && editor) {
      // 同步完成后，如果Yjs文档为空且本地有内容，则初始化Yjs文档
      const yjsContent = yText?.toString() || ''
      const localContent = fileContents.value[filePath] || ''

      // 首次同步：用服务器文件内容作为唯一真源，重置 Yjs，避免多客户端同时注入造成重复
      if (!hasInitializedYDoc) {
        hasInitializedYDoc = true
        isReceivingRemoteChange.value = true
        ydoc!.transact(() => {
          yText.delete(0, yText.length)
          if (localContent) {
            yText.insert(0, localContent)
          }
        })
        fileContents.value[filePath] = localContent || ''
        emit('update:fileContents', fileContents.value)
        emit('update:editorContent', localContent || '')
        editor.setValue(localContent || '')
        if (!monacoBinding) {
          createMonacoBinding()
        }
        setTimeout(() => {
          isReceivingRemoteChange.value = false
        }, 50)
        return
      }
      
      if (!yjsContent && localContent && editor && yText) {
        console.log('用本地内容初始化Yjs文档')
        // 临时禁用本地更新，避免循环
        isReceivingRemoteChange.value = true
        yText.insert(0, localContent)
        setTimeout(() => {
          isReceivingRemoteChange.value = false
        }, 100)
      } else if (yjsContent && yjsContent !== localContent) {
        // 如果Yjs有内容且与本地不同，则更新本地内容
        console.log('用Yjs内容更新本地文档')
        isReceivingRemoteChange.value = true
        fileContents.value[filePath] = yjsContent
        // 由 MonacoBinding 自动更新编辑器，无需再次 setValue（避免回声）
        emit('update:fileContents', fileContents.value)
        setTimeout(() => {
          isReceivingRemoteChange.value = false
        }, 100)
      }
    }
  })
  
  // 监听Yjs文档变化 - 改进版本
  yText.observe((event) => {
    if (!isReceivingRemoteChange.value) {
      console.log('Yjs文档远程变化:', event)
      const content = yText?.toString() || ''
      
      // 只在内容真正不同时更新
      if (content !== fileContents.value[filePath]) {
        console.log('更新本地文件内容:', filePath)
        isReceivingRemoteChange.value = true
        
        fileContents.value[filePath] = content
        emit('update:fileContents', fileContents.value)
        emit('update:editorContent', content)
        
        // 短暂延迟后重置标志
        setTimeout(() => {
          isReceivingRemoteChange.value = false
        }, 50)
      }
    }
  })
  
  // 监听awareness变化（其他用户的光标）
  provider.awareness.on('change', (changes: any) => {
    const states = provider!.awareness.getStates()
    const collaboratorsList: CollaborationUser[] = []
    
    states.forEach((state, clientId) => {
      if (clientId !== provider!.awareness.clientID && state.user && state.user.file === filePath) {
        collaboratorsList.push({
          userId: state.user.id || clientId.toString(),
          userName: state.user.name || `用户${clientId}`
        })
      }
    })
    
    collaborators.value = collaboratorsList
    isCollaborating.value = collaboratorsList.length > 0
    console.log(`文件 ${filePath} 当前协作者:`, collaboratorsList)
  })
  
  // 设置当前用户信息到awareness
  provider.awareness.setLocalStateField('user', {
    id: collaborationService.getUserId(),
    name: collaborationService.getUserName(),
    color: getRandomColor(),
    file: filePath // 标记当前用户正在编辑的文件
  })
  
  // 如果编辑器已存在，创建新的绑定
  // 延后到 sync 完成后再绑定，避免初始内容双向重复
}

// 添加颜色生成函数
const getRandomColor = () => {
  const colors = [
    '#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', 
    '#FECA57', '#FF9FF3', '#54A0FF', '#5F27CD',
    '#00D2D3', '#FF9F43', '#10AC84', '#EE5A24'
  ]
  return colors[Math.floor(Math.random() * colors.length)]
}

// 创建Monaco绑定的函数
const createMonacoBinding = () => {
  if (!editor || !yText || !provider) {
    console.warn('编辑器或Yjs组件未准备好')
    return
  }
  
  console.log('创建Monaco-Yjs绑定')
  monacoBinding = new MonacoBinding(yText, editor.getModel()!, new Set([editor]), provider.awareness)
}

// 销毁编辑器
const disposeEditor = () => {
  if (editor) {
    editor.dispose()
    editor = null
  }
}

// 编辑器事件处理
const updateEditorContent = (content: string) => {
  if (editor && editor.getValue() !== content) {
    editor.setValue(content)
  }
}

const updateEditorLanguage = (language: string) => {
  if (editor) {
    const model = editor.getModel()
    if (model) {
      monaco.editor.setModelLanguage(model, language)
    }
  }
}



// 文件操作方法
const openFileTab = async (filePath: string, content?: string) => {
  console.log('打开文件:', filePath, '内容长度:', content?.length || 0)
  
  // 添加到打开文件列表
  if (!openFiles.value.includes(filePath)) {
    openFiles.value.push(filePath)
    emit('update:openFiles', openFiles.value)
  }
  
  // 设置文件内容
  if (content !== undefined) {
    fileContents.value[filePath] = content
    emit('update:fileContents', fileContents.value)
  } else {
    // 从服务器获取文件内容
    let fileContent = ''
    try {
      const response = await axios.get(`${API_BASE_URL}/files/content`, {
        params: { path: filePath },
        timeout: 5000
      })
      fileContent = response.data.content || ''
      fileContents.value[filePath] = fileContent
      emit('update:fileContents', fileContents.value)
    } catch (error: any) {
      console.error('获取文件内容失败:', error)
      fileContent = `// 无法加载文件内容: ${filePath}\n// 错误: ${error.message || '未知错误'}`
      fileContents.value[filePath] = fileContent
      emit('update:fileContents', fileContents.value)
    }
  }
  
  // 设置为活动文件
  activeFile.value = filePath
  emit('update:activeFile', filePath)
  
  // 设置语言
  editorLanguage.value = getLanguageFromFileName(filePath)
  emit('update:editorLanguage', editorLanguage.value)
  
  // 立即更新编辑器内容
  await nextTick()
  if (editor && fileContents.value[filePath] !== undefined) {
    updateEditorContent(fileContents.value[filePath])
    updateEditorLanguage(editorLanguage.value)
  }
  
  console.log('文件打开完成')
}

const closeFileTab = (filePath: string, event?: Event) => {
  if (event) {
    event.stopPropagation()
  }

  const newOpenFiles = openFiles.value.filter(file => file !== filePath)
  openFiles.value = newOpenFiles
  emit('update:openFiles', openFiles.value)

  if (filePath === activeFile.value) {
    if (newOpenFiles.length > 0) {
      const nextFile = newOpenFiles[newOpenFiles.length - 1]
      activeFile.value = nextFile
      emit('update:activeFile', nextFile)
    } else {
      activeFile.value = ''
      emit('update:activeFile', '')
    }
  }
}

const switchToFile = (filePath: string) => {
  if (openFiles.value.includes(filePath)) {
    activeFile.value = filePath
        // activeFile.value = "/Desktop/User_Coding"
    editorLanguage.value = getLanguageFromFileName(filePath)
    
    emit('update:activeFile', filePath)
    emit('update:editorLanguage', editorLanguage.value)
  }
}

const getLanguageFromFileName = (filename: string): string => {
  const ext = filename.split('.').pop()?.toLowerCase()
  switch (ext) {
    case 'js': return 'javascript'
    case 'ts': return 'typescript'
    case 'tsx': return 'typescript'
    case 'jsx': return 'javascript'
    case 'html': return 'html'
    case 'css': return 'css'
    case 'py': return 'python'
    case 'java': return 'java'
    case 'c': return 'c'
    case 'cpp': case 'cc': case 'cxx': return 'cpp'
    case 'h': return 'c'
    case 'hpp': return 'cpp'
    case 'md': return 'markdown'
    case 'json': return 'json'
    case 'txt': return 'plaintext'
    case 'sh': return 'shell'
    case 'xml': return 'xml'
    case 'sql': return 'sql'
    case 'go': return 'go'
    case 'rb': return 'ruby'
    case 'php': return 'php'
    default: return 'plaintext'
  }
}

// SSH连接函数
const connectToSSH = async () => {
  if (isConnecting.value || isSSHConnected.value) return
  
  isConnecting.value = true
  
  try {
    const response = await axios.post(`${API_BASE_URL}/files/ssh/connect`, {
      host: '120.46.182.160',
      username: 'root',
      password: 'Yaonikaixin999999',
      port: 22
    })
    
    if (response.data.success) {
      sshConnectionId.value = response.data.connectionId
      isSSHConnected.value = true
    } else {
      terminalOutput.value.push(`✗ SSH连接失败: ${response.data.message}`)
    }
  } catch (error: any) {
    console.error('SSH连接错误:', error)
    terminalOutput.value.push(`✗ SSH连接失败: ${error.response?.data?.message || error.message}`)
  } finally {
    isConnecting.value = false
    emit('update:terminalOutput', terminalOutput.value)
  }
}

// 文件操作
const saveFile = async () => {
  if (!activeFile.value) {
    alert('没有打开的文件')
    return
  }

  try {
    const content = fileContents.value[activeFile.value]
    await axios.post(
      `${API_BASE_URL}/files/save?path=${encodeURIComponent(activeFile.value)}`,
      content,
      { headers: { 'Content-Type': 'text/plain' } }
    )
    
    terminalOutput.value.push(`已保存文件: ${activeFile.value}`)
    emit('update:terminalOutput', terminalOutput.value)
    emit('file-save', activeFile.value, content)
  } catch (error) {
    console.error('保存文件失败:', error)
    terminalOutput.value.push(`保存失败: ${activeFile.value}`)
    emit('update:terminalOutput', terminalOutput.value)
  }
}

const compileCurrentFile = async () => {
  if (!activeFile.value) {
    terminalOutput.value.push('错误: 没有打开的文件')
    emit('update:terminalOutput', terminalOutput.value)
    return
  }

  if (!isSSHConnected.value) {
    await connectToSSH()
    if (!isSSHConnected.value) {
      terminalOutput.value.push('✗ 无法建立SSH连接，编译失败')
      emit('update:terminalOutput', terminalOutput.value)
      return
    }
  }

  const fileName = activeFile.value.split('/').pop() || ''
  const ext = fileName.split('.').pop()?.toLowerCase()
  const outputName = fileName.replace(`.${ext}`, '')

  let command = ''
  switch (ext) {
    case 'c':
      command = `gcc "/Desktop/User_Coding/${fileName}" -o "${outputName}" && echo "编译成功: ${outputName}"`
      break
    case 'cpp': case 'cc': case 'cxx':
      command = `g++ "/Desktop/User_Coding/${fileName}" -o "${outputName}" && echo "编译成功: ${outputName}"`
      break
    case 'java':
      command = `javac "/Desktop/User_Coding/${fileName}" && echo "编译成功: ${outputName}.class"`
      break
    case 'py':
      command = `python3 -m py_compile "/Desktop/User_Coding/${fileName}" && echo "Python文件语法检查通过"`
      break
    default:
      terminalOutput.value.push(`不支持编译此类型的文件: ${ext}`)
      emit('update:terminalOutput', terminalOutput.value)
      return
  }

  activePanelTab.value = '终端'
  emit('update:activePanelTab', activePanelTab.value)
  await executeCommand(command)
  emit('file-compile', activeFile.value)
}

const runCurrentFile = async () => {
  if (!activeFile.value) {
    terminalOutput.value.push('错误: 没有打开的文件')
    emit('update:terminalOutput', terminalOutput.value)
    return
  }

  if (!isSSHConnected.value) {
    await connectToSSH()
    if (!isSSHConnected.value) {
      terminalOutput.value.push('✗ 无法建立SSH连接，运行失败')
      emit('update:terminalOutput', terminalOutput.value)
      return
    }
  }

  const fileName = activeFile.value.split('/').pop() || ''
  const ext = fileName.split('.').pop()?.toLowerCase()
  const outputName = fileName.replace(`.${ext}`, '')

  let command = ''
  switch (ext) {
    case 'c': case 'cpp': case 'cc': case 'cxx':
      command = `./${outputName}`
      break
    case 'java':
      command = `cd /Desktop/User_Coding/ && java ${outputName}`
      break
    case 'py':
      command = `python3 /Desktop/User_Coding/"${fileName}"`
      break
    case 'js':
      command = `node /Desktop/User_Coding/"${fileName}"`
      break
    case 'sh':
      command = `bash /Desktop/User_Coding/"${fileName}"`
      break
    default:
      terminalOutput.value.push(`不支持运行此类型的文件: ${ext}`)
      emit('update:terminalOutput', terminalOutput.value)
      return
  }

  activePanelTab.value = '终端'
  emit('update:activePanelTab', activePanelTab.value)
  await executeCommand(command)
  emit('file-run', activeFile.value)
}

// 终端命令执行
const executeCommand = async (command: string) => {
  if (!command.trim()) return

  terminalOutput.value.push(`root@yaonikaixin9999:~$ ${command}`)
  terminalCommand.value = ''

  if (!isSSHConnected.value) {
    terminalOutput.value.push('SSH连接已断开，正在重新连接...')
    await connectToSSH()
    if (!isSSHConnected.value) {
      terminalOutput.value.push('✗ 无法建立SSH连接，命令执行失败')
      emit('update:terminalOutput', terminalOutput.value)
      return
    }
  }

  try {
    const response = await axios.post(`${API_BASE_URL}/files/ssh/execute`, {
      connectionId: sshConnectionId.value,
      command: command
    })

    if (response.data.success) {
      const { stdout, stderr } = response.data
      
      if (stdout && stdout.trim()) {
        const lines = stdout.split('\n').filter((line: string) => line.trim() !== '')
        terminalOutput.value.push(...lines)
      }
      if (stderr && stderr.trim()) {
        const errorLines = stderr.split('\n').filter((line: string) => line.trim() !== '')
        terminalOutput.value.push(...errorLines.map((line: string) => `错误: ${line}`))
      }
    } else {
      terminalOutput.value.push(`✗ 命令执行失败: ${response.data.message}`)
      
      if (response.data.message?.includes('连接')) {
        isSSHConnected.value = false
        sshConnectionId.value = ''
      }
    }
  } catch (error: any) {
    console.error('执行命令失败:', error)
    terminalOutput.value.push(`✗ 命令执行失败: ${error.response?.data?.message || error.message}`)
    
    if (error.response?.status === 400 || error.message?.includes('连接')) {
      isSSHConnected.value = false
      sshConnectionId.value = ''
      terminalOutput.value.push('SSH连接已断开')
    }
  }

  emit('update:terminalOutput', terminalOutput.value)
  emit('terminal-execute', command)

  await nextTick()
  if (terminalRef.value) {
    terminalRef.value.scrollTop = terminalRef.value.scrollHeight
  }
}

// 终端键盘事件
const handleTerminalKeypress = async (e: KeyboardEvent) => {
  if (e.key === 'Enter') {
    e.preventDefault()
    
    const cmd = terminalCommand.value.trim()
    
    if (cmd === 'clear') {
      terminalOutput.value = [
        'CloudStack Pro [版本 0.25.7.11]',
        '(c) Yaonikaixin999999。保留所有权利。',
        ' '
      ]
      terminalCommand.value = ''
      emit('update:terminalOutput', terminalOutput.value)
      return
    }
    
    if (cmd === 'exit') {
      if (sshConnectionId.value) {
        try {
          await axios.post(`${API_BASE_URL}/files/ssh/disconnect`, {
            connectionId: sshConnectionId.value
          })
        } catch (error) {
          console.error('断开SSH连接失败:', error)
        }
      }
      
      isSSHConnected.value = false
      sshConnectionId.value = ''
      terminalOutput.value.push('SSH连接已断开')
      terminalCommand.value = ''
      emit('update:terminalOutput', terminalOutput.value)
      return
    }
    
    if (cmd === 'connect' || cmd === 'reconnect') {
      await connectToSSH()
      return
    }
    
    await executeCommand(cmd)
  }
  
  if (e.ctrlKey && e.key === 'c') {
    e.preventDefault()
    terminalOutput.value.push('^C')
    terminalCommand.value = ''
    emit('update:terminalOutput', terminalOutput.value)
  }
}

// 面板拖动处理
const handlePanelMouseDown = (e: MouseEvent) => {
  e.preventDefault()
  emit('update:isDraggingPanel', true)
}

// 键盘快捷键
const handleKeyDown = (e: KeyboardEvent) => {
  if ((e.ctrlKey || e.metaKey) && e.key === 's') {
    e.preventDefault()
    saveFile()
  }
  if (e.key === 'F5') {
    e.preventDefault()
    runCurrentFile()
  }
  if (e.key === 'F6') {
    e.preventDefault()
    compileCurrentFile()
  }
}

// 智能防抖发送内容变更（已禁用，避免与 Yjs 双通道重复）
const smartDebouncedSendContentChange = debounce((_content: string) => {
  /* no-op */
}, 800)

// 处理协作内容更新（仅更新缓存，避免重复 setValue 回声）
const handleRemoteContentChange = (change: FileChange) => {
  // 只处理当前活动文件的变更，且不是自己发送的
  if (change.filePath === activeFile.value &&
      change.userId !== collaborationService.getCurrentUser().userId) {
    
    console.log('收到远程内容变更:', change)
    
    isReceivingRemoteChange.value = true
    lastRemoteChangeTime.value = Date.now()

    // 仅更新本地缓存，Yjs/MonacoBinding 会负责把内容同步到编辑器
    fileContents.value[activeFile.value] = change.content
    emit('update:fileContents', fileContents.value)
    emit('update:editorContent', change.content)

    setTimeout(() => {
      isReceivingRemoteChange.value = false
    }, 100)
  }
}

// 处理协作者更新
const handleCollaboratorsUpdate = (newCollaborators: CollaborationUser[]) => {
  nextTick(() => { 
      collaborators.value = newCollaborators
  })
  nextTick(() => {
    isCollaborating.value = newCollaborators.length > 1
  })
  console.log('协作者更新:', newCollaborators, '是否协作中:', isCollaborating.value)
}

// 处理光标位置变化
const handleCursorPositionChange = debounce(() => {
  if (!isReceivingRemoteChange.value && editor && activeFile.value && isCollaborating.value) {
    const position = editor.getPosition()
    const selection = editor.getSelection()

    if (position) {
      collaborationService.sendCursorPosition(position, selection)
      emit('collaboration-cursor-change', position, selection)
    }
  }
}, 200)

// 处理用户输入检测
const handleUserTyping = () => {
  isUserTyping.value = true

  // 清除之前的超时
  if (typingTimeoutId) {
    clearTimeout(typingTimeoutId)
  }

  // 设置新的超时，1秒后认为用户停止输入
  typingTimeoutId = setTimeout(() => {
    isUserTyping.value = false
  }, 1000)
}

// 工具函数
function debounce<T extends (...args: any[]) => any>(func: T, delay: number): (...args: Parameters<T>) => void {
  let timeoutId: NodeJS.Timeout | null = null
  
  return (...args: Parameters<T>) => {
    if (timeoutId) {
      clearTimeout(timeoutId)
    }
    timeoutId = setTimeout(() => {
      func(...args)
    }, delay) as NodeJS.Timeout
  }
}

// 监听器
watch(() => props.activeFile, (newActiveFile) => {
  if (newActiveFile !== activeFile.value) {
    activeFile.value = newActiveFile
  }
})

// 监听活动文件变化，加入协作
watch(() => activeFile.value, async (newFile, oldFile) => {
  console.log('活动文件变化:', oldFile, '->', newFile)
  
  // 重置协作状态
  isReceivingRemoteChange.value = false
  lastRemoteChangeTime.value = 0
  isUserTyping.value = false

  // 离开之前的Socket.IO房间
  if (oldFile) {
    collaborationService.leaveFileCollaboration()
  }

  if (newFile) {
    // 创建新的Yjs连接
    createYjsConnection(newFile)
    
    // 更新编辑器内容和语言
    if (fileContents.value[newFile] !== undefined) {
      console.log('更新编辑器内容:', fileContents.value[newFile])
      updateEditorContent(fileContents.value[newFile])
      updateEditorLanguage(getLanguageFromFileName(newFile))
    }

    // 不再加入 Socket 协作房间，避免与 Yjs 双通道重复
    // collaborationService.joinFileCollaboration(newFile)
  } else {
    // 没有活动文件时，清理连接
    if (monacoBinding) {
      monacoBinding.destroy()
      monacoBinding = null
    }
    if (provider) {
      provider.disconnect()
      provider = null
    }
    currentRoom.value = ''
    collaborators.value = []
    isCollaborating.value = false
  }
})

watch(() => currentContent.value, (newContent) => {
  updateEditorContent(newContent)
})

watch(() => props.openFiles, (newOpenFiles) => {
  if (JSON.stringify(newOpenFiles) !== JSON.stringify(openFiles.value)) {
    openFiles.value = [...(newOpenFiles || [])]
  }
}, { deep: true })

watch(() => props.fileContents, (newFileContents) => {
  if (JSON.stringify(newFileContents) !== JSON.stringify(fileContents.value)) {
    fileContents.value = { ...(newFileContents || {}) }
  }
}, { deep: true })

watch(terminalOutput, async () => {
  await nextTick()
  if (terminalRef.value) {
    terminalRef.value.scrollTop = terminalRef.value.scrollHeight
  }
}, { deep: true })

// 生命周期
// 修改Monaco Editor初始化部分
onMounted(async () => {
  await nextTick()
  console.log('EditorArea: 组件挂载')

  if (!editorContainer.value) {
    console.error('编辑器容器未找到')
    return
  }

  // 创建Monaco编辑器
  editor = monaco.editor.create(editorContainer.value, {
    value: currentContent.value || '',
    language: editorLanguage.value,
    theme: 'vs-light',
    minimap: { enabled: false },
    automaticLayout: true,
    fontSize: 14,
    wordWrap: 'on',
    lineNumbers: 'on',
    readOnly: false,
    scrollBeyondLastLine: false,
    selectOnLineNumbers: true,
    roundedSelection: false,
    cursorStyle: 'line',
    contextmenu: true,
  })

  // 监听编辑器内容变化 - 只处理本地状态更新
  editor.onDidChangeModelContent((e) => {
    if (!isReceivingRemoteChange.value) {
      const value = editor?.getValue() || ''
      handleEditorContentChange(value)
    }
  })

  // 监听光标位置变化
  editor.onDidChangeCursorPosition((e) => {
    if (!isReceivingRemoteChange.value && activeFile.value && provider && isCollaborating.value) {
      // 更新awareness中的光标信息
      provider.awareness.setLocalStateField('cursor', {
        position: e.position,
        selection: editor!.getSelection(),
        file: activeFile.value
      })
    }
  })

  // 监听键盘输入
  editor.onKeyDown(handleUserTyping)

  // 协作状态改由 Yjs awareness 维护，避免双通道重复；不再订阅 Socket 内容通道
  // collaborationService.onContentChange(handleRemoteContentChange)
  // collaborationService.onCollaboratorsUpdate(handleCollaboratorsUpdate)

  console.log('Monaco编辑器创建完成')
  
  // 如果有活动文件，立即创建连接
  if (activeFile.value) {
    createYjsConnection(activeFile.value)
  }
  
  document.addEventListener('keydown', handleKeyDown)
  
  // 延迟连接SSH
  setTimeout(async () => {
    await connectToSSH()
  }, 2000)
})

// 修改组件卸载处理
onUnmounted(() => {
  console.log('EditorArea: 组件卸载')
  
  // 清理超时
  if (typingTimeoutId) {
    clearTimeout(typingTimeoutId)
  }

  // 离开协作
  if (activeFile.value) {
    collaborationService.leaveFileCollaboration()
  }

  // 销毁Monaco绑定
  if (monacoBinding) {
    monacoBinding.destroy()
    monacoBinding = null
  }

  // 断开Yjs连接
  if (provider) {
    provider.disconnect()
    provider = null
  }

  document.removeEventListener('keydown', handleKeyDown)
  
  // 销毁编辑器
  disposeEditor()
  
  // 断开SSH连接
  if (sshConnectionId.value) {
    axios.post(`${API_BASE_URL}/files/ssh/disconnect`, {
      connectionId: sshConnectionId.value
    }).catch(console.error)
  }
})

// 修改编辑器内容变化处理
const handleEditorContentChange = (content: string) => {
  // 只在内容真正变化时更新本地状态，Yjs会自动处理同步
  if (activeFile.value && fileContents.value[activeFile.value] !== content) {
    console.log('本地内容更新:', activeFile.value, '长度:', content.length)
    fileContents.value[activeFile.value] = content
    emit('update:fileContents', fileContents.value)
    emit('update:editorContent', content)
  }
}

// 暴露方法给父组件
defineExpose({
  openFileTab,
  closeFileTab,
  switchToFile,
  saveFile,
  compileCurrentFile,
  runCurrentFile,
    // 协作相关
  collaborators: collaborators.value,
  isCollaborating: isCollaborating.value,
  isReceivingRemoteChange: isReceivingRemoteChange.value,
  isUserTyping: isUserTyping.value
})
</script>

<style>
/* 主体内容区域 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: var(--surface-lightest);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

/* 编辑器区域 */
.editor-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: var(--surface-lightest);
}

.editor-tabs {
  display: flex;
  background-color: var(--surface-light);
  border-bottom: 1px solid var(--border-light);
  justify-content: space-between;
  align-items: center;
}

.tabs-container {
  display: flex;
  flex-direction: row; /* 明确设置水平排列 */
  flex: 1;
  overflow-x: auto;
}

.tabs-list {
  display: flex;
  flex-direction: row; /* 确保标签水平排列 */
  align-items: center;
}

.editor-tab {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  border-right: 1px solid var(--border-light);
  cursor: pointer;
  background-color: var(--surface-mid);
  color: var(--text-mid);
  transition: var(--transition-default);
  white-space: nowrap;
  min-width: 120px;
}

.editor-tab:hover {
  background-color: var(--surface-dark);
}

.editor-tab.active {
  background-color: var(--surface-lightest);
  color: var(--text-dark);
  border-bottom: 2px solid var(--primary-blue);
}

.tab-filename {
  flex: 1;
  margin-right: 8px;
}

.tab-close {
  opacity: 0.6;
  transition: opacity 0.2s;
  font-size: 16px;
  font-weight: bold;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 2px;
}

.tab-close:hover {
  opacity: 1;
  background-color: var(--surface-dark);
}

.editor-actions {
  display: flex;
  gap: 8px;
  padding: 0 8px;
}

.editor-action-button {
  padding: 4px 8px;
  background-color: var(--primary-blue);
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: var(--transition-default);
}

.editor-action-button:hover {
  background-color: var(--primary-dark);
}

.editor-content {
    flex: 1;
    overflow: hidden;
    min-height: 400px; /* 确保最小高度 */
    height: 100%;
}

.editor-content > div {
  height: 100% !important;
  min-height: 400px !important;
}

.monaco-editor {
  background-color: inherit !important;
  height: 100% !important;
  min-height: 400px !important;
}

/* 面板区域 */
.panel-area {
  background-color: var(--surface-light);
  border-top: 1px solid var(--border-light);
  display: flex;
  flex-direction: column;
  position: relative;
}

.panel-resizer {
  width: 100%;
  height: 4px;
  cursor: row-resize;
  background-color: var(--border-light);
  position: absolute;
  top: -2px;
  z-index: 10;
  transition: var(--transition-default);
}

.panel-resizer:hover,
.panel-resizer:active {
  background-color: var(--primary-blue);
}

.panel-tabs {
  display: flex;
  background-color: var(--surface-mid);
  border-bottom: 1px solid var(--border-light);
}

.panel-tab {
  padding: 8px 16px;
  cursor: pointer;
  color: var(--text-mid);
  transition: var(--transition-default);
  border-right: 1px solid var(--border-light);
}

.panel-tab:hover {
  background-color: var(--surface-dark);
}

.panel-tab.active {
  background-color: var(--surface-lightest);
  color: var(--text-dark);
  border-bottom: 2px solid var(--primary-blue);
}

.panel-content {
  flex: 1;
  overflow: hidden;
  padding: 8px;
}

/* 终端样式 */
.terminal {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
  color: #000000;
  padding: 8px;
  border-radius: 4px;
}

.terminal-line {
  margin: 2px 0;
  /* color: #ffffff; */
  white-space: pre-wrap;
  word-wrap: break-word;
}

.terminal-input-line {
  margin-top: 4px;
  color: #569cd6;
}

/* 状态栏 */
.status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 12px;
  background-color: var(--primary-blue);
  color: white;
  font-size: 12px;
  border-top: 1px solid var(--border-light);
  height: 24px;
}

.status-items-left,
.status-items-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 2px 6px;
  border-radius: 2px;
  cursor: pointer;
  transition: var(--transition-default);
}

.status-item:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.status-git-icon {
  font-weight: bold;
}

.status-metrics .error {
  color: #f14c4c;
}

.status-metrics .warning {
  color: #fcf84a;
}

.status-position {
  font-family: monospace;
}

/* 面板内容样式 */
.problems-panel,
.output-panel,
.debug-console {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
  color: var(--text-dark);
  line-height: 1.5;
}

.problems-panel div,
.output-panel div,
.debug-console div {
  margin: 4px 0;
}

/* CSS 变量定义 */
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
  --transition-default: all 0.2s ease;
}

.collaboration-indicator {
  margin-right: 12px;
  padding: 4px 8px;
  background-color: rgba(0, 255, 0, 0.1);
  border-radius: 4px;
  border: 1px solid rgba(0, 255, 0, 0.3);
}

.collaborators-list {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.collaboration-status {
  color: #059669;
  font-weight: 500;
  white-space: nowrap;
}

.collaborator-badge {
  background-color: var(--primary-blue);
  color: white;
  padding: 2px 6px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
  white-space: nowrap;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .collaborators-list {
    flex-direction: column;
    gap: 4px;
  }
  
  .collaboration-status {
    font-size: 11px;
  }
  
  .collaborator-badge {
    font-size: 10px;
    padding: 1px 4px;
  }
}
</style>
