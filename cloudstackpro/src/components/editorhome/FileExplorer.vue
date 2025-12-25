<template>
    <div class="file-explorer" style="padding: 4px 0">
      <!-- 文件操作工具栏 -->
      <div class="file-explorer-actions">
        <div style="display: flex; gap: 4px">
          <button
            class="action-button"
            title="新建文件"
            @click="showNewFileInput = true; showNewFolderInput = false; newItemName = ''"
          >
            <img :src="newFileIcon" alt="新建文件" />
          </button>
          <button
            class="action-button"
            title="新建文件夹"
            @click="showNewFolderInput = true; showNewFileInput = false; newItemName = ''"
          >
            <img :src="newFolderIcon" alt="新建文件夹" />
          </button>
        </div>
        <div style="font-size: 12px; color: var(--text-mid)">
          {{ currentPath.replace(rootDir, '') || '/' }}
        </div>
        <button
          class="action-button"
          title="刷新文件列表"
          @click="refreshFileTree"
        >
          <span style="font-size: 16px">↻</span>
        </button>
      </div>
  
      <!-- 新建文件输入框 -->
      <div v-if="showNewFileInput" class="new-item-input-container">
        <input
          type="text"
          class="new-item-input"
          placeholder="输入文件名..."
          v-model="newItemName"
          :disabled="creatingItem"
          ref="fileInput"
        />
        <div class="new-item-actions">
          <button
            class="new-item-button confirm-button"
            @click="handleCreateFile"
            :disabled="creatingItem"
          >
            {{ creatingItem ? '创建中...' : '确定' }}
          </button>
          <button
            class="new-item-button cancel-button"
            @click="showNewFileInput = false"
            :disabled="creatingItem"
          >
            取消
          </button>
        </div>
      </div>
  
      <!-- 新建文件夹输入框 -->
      <div v-if="showNewFolderInput" class="new-item-input-container">
        <input
          type="text"
          class="new-item-input"
          placeholder="输入文件夹名..."
          v-model="newItemName"
          :disabled="creatingItem"
          ref="folderInput"
        />
        <div class="new-item-actions">
          <button
            class="new-item-button confirm-button"
            @click="handleCreateFolder"
            :disabled="creatingItem"
          >
            {{ creatingItem ? '创建中...' : '确定' }}
          </button>
          <button
            class="new-item-button cancel-button"
            @click="showNewFolderInput = false"
            :disabled="creatingItem"
          >
            取消
          </button>
        </div>
      </div>
  
      <div style="flex: 1; overflow-y: auto">
        <div v-if="isLoading" class="loading-message">加载文件中...</div>
  
        <div
          v-if="error"
          class="error-message"
          style="color: red; padding: 8px"
        >
          {{ error }}
          <button @click="refreshFileTree" style="margin-left: 8px; padding: 4px 8px; cursor: pointer;">重试</button>
        </div>
  
        <div v-if="!isLoading && !error && files.length > 0" @click="handleEmptyAreaClick">
          <div style="padding: 4px 8px; font-weight: bold; color: var(--text-mid); cursor: pointer;" @click.stop="backToRoot">
            <!-- 📁 /My_Desktop/User_Coding -->
            <span v-if="currentPath !== rootDir" style="font-size: 12px; opacity: 0.7; margin-left: 8px">
              /Desktop/User_Coding
            </span>
          </div>
          
          <!-- 显示当前路径 -->
          <!-- <div v-if="currentPath !== rootDir" style="padding: 4px 8px; font-size: 12px; color: var(--text-light); border-bottom: 1px solid var(--border-light)">
            当前位置: {{ currentPath.replace(rootDir, '') || '/' }}
            <button @click.stop="backToRoot" style="margin-left: 8px; font-size: 11px; padding: 2px 6px; border: 1px solid #ccc; background: #f5f5f5; cursor: pointer; border-radius: 3px;">
              返回根目录
            </button>
          </div> -->
          
          <FileTreeNode
            v-for="node in files"
            :key="node.path"
            :node="node"
            :activeFile="activeFile"
            :expandedFolders="expandedFolders"
            :loadingFiles="loadingFiles"
            :fileContentCache="fileContentCache"
            :depth="0"
            @toggle-folder="toggleFolder"
            @file-click="handleFileClick"
          />
        </div>
  
        <div
          v-if="!isLoading && !error && files.length === 0"
          class="empty-directory"
          style="padding: 8px"
          @click="handleEmptyAreaClick"
        >
          目录为空
          <div style="font-size: 12px; color: var(--text-light); margin-top: 4px">
            点击上方的按钮创建新文件或文件夹，或点击此处返回根目录
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref, reactive, onMounted, nextTick, watch } from 'vue';
  import axios from 'axios';
  import { throttle } from 'lodash';
  import FileTreeNode from './FileTreeNode.vue';
  
  // 导入自定义图标
  import defaultFolderIcon from '@/images/Coding_icons/default_folder.svg';
  import defaultFolderOpenedIcon from '@/images/Coding_icons/default_folder_opened.svg';
  import cFileIcon from '@/images/Coding_icons/file_type_c.svg';
  import cppFileIcon from '@/images/Coding_icons/file_type_cpp2.svg';
  import javaFileIcon from '@/images/Coding_icons/file_type_java.svg';
  import pythonFileIcon from '@/images/Coding_icons/file_type_python.svg';
  import defaultFileIcon from '@/images/Coding_icons/icons8-文件-80.png';
  import newFileIcon from '@/images/icons8-添加文件-80.png';
  import newFolderIcon from '@/images/icons8-新增文件夹-80.png';
  
  // API 基础 URL - 动态获取
  const getApiBaseUrl = () => {
    const hostname = window.location.hostname;
    if (hostname === 'localhost' || hostname === '127.0.0.1') {
      return 'http://localhost:3001/api';
    }
    return `http://${hostname}:3001/api`;
  }
  const API_BASE_URL = getApiBaseUrl();
  
  // 文件类型接口
  interface FileNode {
    name: string;
    path: string;
    type: 'directory' | 'file';
    children?: FileNode[];
    size?: number;
    modifyTime?: string;
  }
  
  // Props 定义
  interface Props {
    activeFile: string;
    setActiveFile: (filePath: string) => void;
    openFileTab: (filePath: string, content?: string) => Promise<void>;
    updateEditorContent: (content: string) => void;
  }
  
  const props = defineProps<Props>();
  
  // 响应式数据
  const files = ref<FileNode[]>([]);
  const expandedFolders = ref<Record<string, boolean>>({});
  const isLoading = ref<boolean>(true);
  const error = ref<string | null>(null);
  const rootDir = ref<string>('/Desktop/User_Coding');
  const loadingFiles = ref<Record<string, boolean>>({});
  const fileContentCache = ref<Record<string, {
    content: string;
    timestamp: number;
  }>>({});
  
  // 新建文件/文件夹相关状态
  const currentPath = ref<string>(rootDir.value);
  const showNewFileInput = ref<boolean>(false);
  const showNewFolderInput = ref<boolean>(false);
  const newItemName = ref<string>('');
  const creatingItem = ref<boolean>(false);
  
  // refs
  const fileInput = ref<HTMLInputElement>();
  const folderInput = ref<HTMLInputElement>();
  
  // 获取文件图标
  const getFileIcon = (fileName: string) => {
    const extension = fileName.split('.').pop()?.toLowerCase();
  
    switch (extension) {
      case 'c':
        return cFileIcon;
      case 'cpp':
      case 'cc':
      case 'cxx':
      case 'h':
      case 'hpp':
        return cppFileIcon;
      case 'java':
        return javaFileIcon;
      case 'py':
      case 'python':
        return pythonFileIcon;
      default:
        return defaultFileIcon;
    }
  };
  
  // 获取文件夹图标
  const getFolderIcon = (isExpanded: boolean) => {
    return isExpanded ? defaultFolderOpenedIcon : defaultFolderIcon;
  };
    // 新建文件处理函数
  const handleCreateFile = async () => {
    if (!newItemName.value.trim()) {
      alert('文件名不能为空');
      return;
    }

    creatingItem.value = true;
    try {
      const newFilePath = `${currentPath.value}/${newItemName.value}`;
      
      console.log('创建文件 - 当前路径:', currentPath.value);
      console.log('创建文件 - 新文件路径:', newFilePath);

      await axios.post(`${API_BASE_URL}/files/create`, {
        path: newFilePath,
        type: 'file',
        content: ''
      });

      console.log('文件创建成功，刷新文件树');
      await refreshFileTree();
      
      console.log('打开新创建的文件');
      if (props.openFileTab) {
        await props.openFileTab(newFilePath, '');
      }

      newItemName.value = '';
      showNewFileInput.value = false;
    } catch (err: any) {
      console.error('创建文件失败:', err);
      const errorMsg = err?.response?.data?.error || err?.message || '创建文件失败';
      alert(`创建文件失败: ${errorMsg}`);
    } finally {
      creatingItem.value = false;
    }
  };
    // 新建文件夹处理函数
  const handleCreateFolder = async () => {
    if (!newItemName.value.trim()) {
      alert('文件夹名不能为空');
      return;
    }

    creatingItem.value = true;
    try {
      const newFolderPath = `${currentPath.value}/${newItemName.value}`;
      
      console.log('创建文件夹 - 当前路径:', currentPath.value);
      console.log('创建文件夹 - 新文件夹路径:', newFolderPath);

      await axios.post(`${API_BASE_URL}/files/create`, {
        path: newFolderPath,
        type: 'directory'
      });

      console.log('文件夹创建成功，刷新文件树');
      await refreshFileTree();

      expandedFolders.value = {
        ...expandedFolders.value,
        [newFolderPath]: true
      };

      newItemName.value = '';
      showNewFolderInput.value = false;
    } catch (err: any) {
      console.error('创建文件夹失败:', err);
      const errorMsg = err?.response?.data?.error || err?.message || '创建文件夹失败';
      alert(`创建文件夹失败: ${errorMsg}`);
    } finally {
      creatingItem.value = false;
    }
  };
  
  // 刷新文件树
  const refreshFileTree = async () => {
    isLoading.value = true;
    error.value = null;
  
    try {
      const response = await axios.get(`${API_BASE_URL}/files/tree`, {
        params: { path: rootDir.value }
      });
      files.value = response.data;
    } catch (err) {
      console.error('刷新文件树失败:', err);
      error.value = '无法刷新文件树，请检查服务器连接';
    } finally {
      isLoading.value = false;
    }
  };
  
  // 处理文件点击
  const handleFileClick = async (file: FileNode) => {
    // 点击文件时不改变当前路径，保持在当前目录
    await throttledOpenFile(file);
  };
    // 打开文件函数
  const openFile = async (file: FileNode) => {
    try {
      loadingFiles.value = { ...loadingFiles.value, [file.path]: true };
      
      console.log('FileExplorer: 正在打开文件', file.path);
      
      const cacheEntry = fileContentCache.value[file.path];
      const isCacheValid = cacheEntry &&
        (Date.now() - cacheEntry.timestamp < 5 * 60 * 1000);

      let content = '';
      
      if (isCacheValid) {
        content = cacheEntry.content;
        console.log('FileExplorer: 使用缓存内容');
      } else {
        console.log('FileExplorer: 从服务器获取文件内容');
        const response = await axios.get(`${API_BASE_URL}/files/content`, {
          params: { path: file.path },
          timeout: 5000
        });

        content = response.data.content || '';
        
        fileContentCache.value = {
          ...fileContentCache.value,
          [file.path]: {
            content: content,
            timestamp: Date.now()
          }
        };
      }

      console.log('FileExplorer: 调用 openFileTab，文件:', file.path, '内容长度:', content.length);
      
      // 确保调用父组件的 openFileTab 方法
      if (props.openFileTab) {
        await props.openFileTab(file.path, content);
      } else {
        console.error('FileExplorer: openFileTab 方法未定义');
      }
      
    } catch (err: any) {
      console.error('FileExplorer: 打开文件失败:', err);
      const errorMessage = `// 加载文件内容失败: ${file.path}\n// 错误: ${err?.response?.data?.error || err?.message || '未知错误'}`;
      
      if (props.updateEditorContent) {
        props.updateEditorContent(errorMessage);
      }
    } finally {
      loadingFiles.value = { ...loadingFiles.value, [file.path]: false };
    }
  };
  
  // 创建节流版本的openFile函数
  const throttledOpenFile = throttle((file: FileNode) => openFile(file), 500);
  
  // 切换文件夹展开状态
  const toggleFolder = (folderPath: string) => {
    const wasExpanded = expandedFolders.value[folderPath];
    
    expandedFolders.value = {
      ...expandedFolders.value,
      [folderPath]: !wasExpanded
    };
    
    // 始终设置当前路径为被点击的文件夹路径，不管是展开还是收起
    // 这样在文件夹内创建文件时，路径就是正确的
    currentPath.value = folderPath;
    
    console.log('切换文件夹:', folderPath, '当前路径设置为:', currentPath.value);
  };

  // 返回根目录
  const backToRoot = async () => {
    console.log('返回根目录');
    currentPath.value = rootDir.value;
    
    // 清空展开状态
    expandedFolders.value = {};
    
    // 重新加载根目录的文件树
    await refreshFileTree();
  };

  // 处理空白区域点击
  const handleEmptyAreaClick = (event: Event) => {
    // 如果点击的是空白区域（不是文件或文件夹），返回根目录
    if (event.target === event.currentTarget) {
      backToRoot();
    }
  };
  
  // 加载文件树
  const fetchFileTree = async () => {
    isLoading.value = true;
    error.value = null;
  
    try {
      const response = await axios.get(`${API_BASE_URL}/files/tree`, {
        params: { path: rootDir.value }
      });
      files.value = response.data;
    } catch (err) {
      console.error('加载文件树失败:', err);
      error.value = '无法加载文件树，请检查服务器连接';
  
      if (process.env.NODE_ENV === 'development') {
        console.log('使用模拟数据...');
        files.value = [
          {
            name: 'projects',
            path: `${rootDir.value}/projects`,
            type: 'directory',
            children: [
              { name: 'hello.c', path: `${rootDir.value}/projects/hello.c`, type: 'file' },
              { name: 'example.cpp', path: `${rootDir.value}/projects/example.cpp`, type: 'file' }
            ]
          },
          {
            name: 'python_examples',
            path: `${rootDir.value}/python_examples`,
            type: 'directory',
            children: [
              { name: 'hello.py', path: `${rootDir.value}/python_examples/hello.py`, type: 'file' }
            ]
          }
        ];
      }
    } finally {
      isLoading.value = false;
    }
  };
  
  // 监听输入框显示状态，自动聚焦
  watch(showNewFileInput, async (newVal) => {
    if (newVal) {
      await nextTick();
      fileInput.value?.focus();
    }
  });
  
  watch(showNewFolderInput, async (newVal) => {
    if (newVal) {
      await nextTick();
      folderInput.value?.focus();
    }
  });
  
  // 组件挂载时加载文件树
  onMounted(() => {
    fetchFileTree();
  });
  </script>

<style>
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.file-explorer-actions {
  display: flex;
  padding: 8px;
  border-bottom: 1px solid var(--border-light);
  background-color: var(--surface-light);
  justify-content: space-between;
  align-items: center;
}

.action-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 4px;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: background-color 0.2s;
}

.action-button:hover {
  background-color: var(--primary-light);
}

.action-button img {
  width: 18px;
  height: 18px;
  object-fit: contain;
}

.new-item-input-container {
  display: flex;
  padding: 8px;
  border-bottom: 1px solid var(--border-light);
  background-color: var(--surface-lightest);
  align-items: center;
}

.new-item-input {
  flex: 1;
  padding: 6px 8px;
  border: 1px solid var(--border-light);
  border-radius: 4px;
  font-size: 13px;
}

.new-item-input:focus {
  outline: none;
  border-color: var(--primary-blue);
}

.new-item-actions {
  display: flex;
  gap: 4px;
  margin-left: 8px;
}

.new-item-button {
  padding: 4px 8px;
  border-radius: 4px;
  border: none;
  font-size: 12px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.confirm-button {
  background-color: var(--primary-blue);
  color: white;
}

.confirm-button:hover {
  background-color: var(--primary-dark);
}

.cancel-button {
  background-color: var(--surface-mid);
  color: var(--text-dark);
}

.cancel-button:hover {
  background-color: var(--surface-dark);
}
</style>