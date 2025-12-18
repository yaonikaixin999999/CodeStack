<template>
    <div :style="{ marginLeft: `${depth * 12}px` }">
      <div
        :class="['file-item', { active: activeFile === node.path }]"
        @click="handleClick"
        :style="{
          display: 'flex',
          alignItems: 'center',
          padding: '4px 8px',
          cursor: 'pointer',
          borderRadius: '4px',
          backgroundColor: activeFile === node.path ? 'var(--primary-light)' : 'transparent'
        }"
      >
        <div v-if="isDirectory">
          <img 
            :src="getFolderIcon(isExpanded)" 
            alt="Folder" 
            width="16" 
            height="16" 
          />
        </div>
        <div v-else style="display: flex; align-items: center; position: relative">
          <img 
            :src="getFileIcon(node.name)" 
            alt="File" 
            width="16" 
            height="16" 
          />
          <span
            v-if="isFileLoading"
            class="file-loading-spinner"
            style="
              position: absolute;
              right: -8px;
              top: -8px;
              font-size: 10px;
              animation: spin 1s infinite linear;
            "
          >⟳</span>
        </div>
        <span style="margin-left: 6px">{{ node.name }}</span>
        <span
          v-if="fileContentCache[node.path]"
          title="已缓存"
          style="margin-left: 4px; font-size: 10px; opacity: 0.5"
        >✓</span>
      </div>
  
      <div v-if="isDirectory && isExpanded && node.children" class="folder-children">
        <FileTreeNode
          v-for="child in node.children"
          :key="child.path"
          :node="child"
          :activeFile="activeFile"
          :expandedFolders="expandedFolders"
          :loadingFiles="loadingFiles"
          :fileContentCache="fileContentCache"
          :depth="depth + 1"
          @toggle-folder="$emit('toggle-folder', $event)"
          @file-click="$emit('file-click', $event)"
        />
      </div>
    </div>
  </template>
  
  <script setup lang="ts">
  import { computed } from 'vue';
  
  // 导入图标
  import defaultFolderIcon from '@/images/Coding_icons/default_folder.svg';
  import defaultFolderOpenedIcon from '@/images/Coding_icons/default_folder_opened.svg';
  import cFileIcon from '@/images/Coding_icons/file_type_c.svg';
  import cppFileIcon from '@/images/Coding_icons/file_type_cpp2.svg';
  import javaFileIcon from '@/images/Coding_icons/file_type_java.svg';
  import pythonFileIcon from '@/images/Coding_icons/file_type_python.svg';
  import defaultFileIcon from '@/images/Coding_icons/icons8-文件-80.png';
  
  interface FileNode {
    name: string;
    path: string;
    type: 'directory' | 'file';
    children?: FileNode[];
    size?: number;
    modifyTime?: string;
  }
  
  interface Props {
    node: FileNode;
    activeFile: string;
    expandedFolders: Record<string, boolean>;
    loadingFiles: Record<string, boolean>;
    fileContentCache: Record<string, { content: string; timestamp: number }>;
    depth: number;
  }
  
  const props = defineProps<Props>();
  
  const emit = defineEmits<{
    'toggle-folder': [folderPath: string];
    'file-click': [file: FileNode];
  }>();
  
  const isDirectory = computed(() => props.node.type === 'directory');
  const isExpanded = computed(() => props.expandedFolders[props.node.path] || false);
  const isFileLoading = computed(() => props.loadingFiles[props.node.path] || false);
  
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
  
  const getFolderIcon = (isExpanded: boolean) => {
    return isExpanded ? defaultFolderOpenedIcon : defaultFolderIcon;
  };
  
  const handleClick = () => {
    if (isDirectory.value) {
      emit('toggle-folder', props.node.path);
    } else {
      emit('file-click', props.node);
    }
  };
  </script>