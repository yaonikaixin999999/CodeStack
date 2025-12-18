<template>
    <div class="run-debug-container">
      <!-- 运行部分 -->
      <div class="run-debug-section">
        <div class="run-debug-header">
          <div class="run-debug-header-title">
            <span :class="['chevron', { open: expanded }]">
              {{ expanded ? '▼' : '▶' }}
            </span>
            运行
          </div>
          <div class="run-debug-header-actions">
            <div class="run-debug-header-action">...</div>
          </div>
        </div>
  
        <div v-if="expanded" class="run-debug-content">
          <button class="run-debug-button">
            运行和调试
          </button>
  
          <div class="run-debug-message">
            要自定义运行和调试<a href="#" class="run-debug-link">创建 launch.json 文件</a>。
          </div>
  
          <div class="run-debug-help">
            无论是基于 <a href="#" class="run-debug-link">终端命令</a> 还是使用 <a href="#" class="run-debug-link">交互式聊天</a>，
            AI助手都可帮助你开始调试。
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref } from 'vue'
  
  // 响应式数据
  const expanded = ref(true)
  // 如果需要切换功能，可以启用下面的代码
const toggleExpanded = () => {
    //handleClick
    expanded.value = !expanded.value
  }
  </script>

<style>
.run-debug-container {
    display: flex;
    flex-direction: column;
    height: 100%;
    color: var(--text-light);
    font-size: 13px;
}

.run-debug-section {
    margin-bottom: 10px;
}

.run-debug-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 6px 10px;
    font-weight: 600;
    color: var(--text-light);
    cursor: pointer;
    border-radius: 4px;
}

.run-debug-header:hover {
    background-color: var(--surface-light);
}

.run-debug-header.expanded {
    background-color: var(--surface-light);
}

.run-debug-header-title {
    display: flex;
    align-items: center;
    gap: 6px;
}

.run-debug-header-actions {
    display: flex;
    gap: 6px;
}

.run-debug-header-action {
    cursor: pointer;
    width: 16px;
    height: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0.8;
}

.run-debug-header-action:hover {
    opacity: 1;
}

.run-debug-content {
    padding: 0 10px 0 32px;
}

.run-config-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 6px 0;
    cursor: pointer;
    border-radius: 4px;
}

.run-config-item:hover {
    background-color: var(--surface-light);
}

.run-config-item-name {
    display: flex;
    align-items: center;
    gap: 6px;
}

.run-config-item-actions {
    display: flex;
    gap: 8px;
    opacity: 0;
}

.run-config-item:hover .run-config-item-actions {
    opacity: 0.7;
}

.run-config-item-actions span {
    cursor: pointer;
}

.run-config-item-actions span:hover {
    opacity: 1;
}

.run-button {
    background-color: var(--primary);
    color: white;
    border: none;
    border-radius: 4px;
    padding: 4px 10px;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
}

.run-button:hover {
    background-color: var(--primary-dark);
}

.run-button-icon {
    font-size: 14px;
}

.breakpoints-section {
    margin-top: 20px;
}

.no-breakpoints {
    color: var(--text-muted);
    padding: 10px 0 10px 32px;
    font-style: italic;
    font-size: 12px;
}

.chevron {
    transition: transform 0.2s ease;
}

.chevron.open {
    transform: rotate(90deg);
}

.run-debug-container {
    display: flex;
    flex-direction: column;
    height: 100%;
    color: var(--text-light);
    font-size: 13px;
}

.run-debug-section {
    margin-bottom: 10px;
}

.run-debug-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 6px 10px;
    font-weight: 600;
    color: var(--text-light);
}

.run-debug-header-title {
    display: flex;
    align-items: center;
    gap: 6px;
}

.run-debug-content {
    padding: 0 10px;
}

.run-debug-button {
    width: 100%;
    background-color: #0078d4;
    color: white;
    border: none;
    border-radius: 2px;
    padding: 8px 0;
    cursor: pointer;
    text-align: center;
    margin: 10px 0;
    font-size: 13px;
}

.run-debug-button:hover {
    background-color: #106ebe;
}

.run-debug-message {
    margin-bottom: 10px;
    line-height: 1.5;
}

.run-debug-help {
    color: var(--text-muted);
    line-height: 1.5;
    margin-bottom: 10px;
}

.run-debug-link {
    color: #3794ff;
    text-decoration: none;
}

.run-debug-link:hover {
    text-decoration: underline;
}

.chevron {
    font-size: 10px;
}

.chevron.open {
    transform: rotate(90deg);
}


</style>


