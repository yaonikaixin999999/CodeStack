<template>
    <div class="search-container">
      <div class="search-header">
        <div class="search-title">
          搜索
          <div class="search-actions">
            <button class="search-action" title="刷新" @click="handleRefresh">
              <span>⟳</span>
            </button>
            <button class="search-action" title="清除" @click="searchQuery = ''">
              <span>✕</span>
            </button>
            <button class="search-action" title="折叠" @click="handleCollapse">
              <span>⤢</span>
            </button>
            <button class="search-action" title="更多选项" @click="toggleReplace">
              <span>⋮</span>
            </button>
          </div>
        </div>
        
        <form @submit="handleSearch">
          <div class="search-input-container">
            <img
              :src="searchIconPath"
              alt="搜索"
              class="search-icon-img"
            />
            <input
              type="text"
              class="search-input"
              v-model="searchQuery"
              placeholder="搜索"
              ref="searchInput"
              autofocus
            />
          </div>
        </form>
  
        <div v-if="isReplaceVisible" class="search-input-container">
          <input
            type="text"
            class="search-input"
            v-model="replaceQuery"
            placeholder="替换"
          />
        </div>
  
        <div class="search-options">
          <div class="search-options-left">
            <div
              :class="['search-option', { active: caseSensitive }]"
              @click="caseSensitive = !caseSensitive"
              title="区分大小写 (Alt+C)"
            >
              <span>Aa</span>
            </div>
            <div
              :class="['search-option', { active: wholeWord }]"
              @click="wholeWord = !wholeWord"
              title="全字匹配 (Alt+W)"
            >
              <span>ab</span>
            </div>
            <div
              :class="['search-option', { active: useRegExp }]"
              @click="useRegExp = !useRegExp"
              title="使用正则表达式 (Alt+R)"
            >
              <span>.*</span>
            </div>
          </div>
          <div class="search-options-right">
            <template v-if="isReplaceVisible">
              <button
                class="search-option"
                @click="handleReplace"
                title="替换 (Ctrl+Shift+1)"
              >
                替换
              </button>
              <button
                class="search-option"
                @click="handleReplaceAll"
                title="全部替换 (Ctrl+Alt+Enter)"
              >
                全部替换
              </button>
            </template>
          </div>
        </div>
      </div>
  
      <div class="search-toggle" @click="toggleReplace">
        <div class="search-toggle-label">替换</div>
        <div class="search-toggle-shortcut">AB</div>
      </div>
  
      <div class="search-results">
        <!-- 搜索结果会在这里显示 -->
        <div class="search-more">暂无搜索结果</div>
      </div>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref } from 'vue'
  import searchIconPath from '@/images/icons8-搜索-40.png'
  
  // 响应式数据
  const searchQuery = ref('')
  const isReplaceVisible = ref(false)
  const replaceQuery = ref('')
  const caseSensitive = ref(false)
  const wholeWord = ref(false)
  const useRegExp = ref(false)
  
  // 模板引用
  const searchInput = ref<HTMLInputElement>()
  
  // 切换替换区域显示
  const toggleReplace = () => {
    isReplaceVisible.value = !isReplaceVisible.value
  }
  
  // 模拟搜索处理函数
  const handleSearch = (e: Event) => {
    e.preventDefault()
    console.log('搜索:', searchQuery.value)
    // 实际应用中这里应该执行搜索逻辑
  }
  
  // 模拟替换处理函数
  const handleReplace = () => {
    console.log('替换:', searchQuery.value, '替换为:', replaceQuery.value)
    // 实际应用中这里应该执行替换逻辑
  }
  
  // 模拟全部替换处理函数
  const handleReplaceAll = () => {
    console.log('全部替换:', searchQuery.value, '替换为:', replaceQuery.value)
    // 实际应用中这里应该执行全部替换逻辑
  }
  
  // 刷新处理函数
  const handleRefresh = () => {
    console.log('刷新')
  }
  
  // 折叠处理函数
  const handleCollapse = () => {
    console.log('折叠')
  }
  </script>

  <style>
        /* 搜索组件样式 - 符合淡蓝白色主题 */
.search-container {
    display: flex;
    flex-direction: column;
    height: 100%;
    color: var(--text-dark);
    background-color: var(--surface-light);
    overflow: hidden;
}

.search-header {
    padding: 8px;
    display: flex;
    flex-direction: column;
    gap: 8px;
    border-bottom: 1px solid var(--border-light);
}

.search-title {
    font-weight: 500;
    color: var(--text-mid);
    padding: 0 8px;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.search-actions {
    display: flex;
    gap: 8px;
}

.search-action {
    background: none;
    border: none;
    cursor: pointer;
    display: flex;
    align-items: center;
    color: var(--text-light);
    font-size: 14px;
    padding: 2px;
}

.search-action:hover {
    color: var(--primary-blue);
}

.search-input-container {
    position: relative;
    padding: 0 8px;
}

.search-input {
    width: 100%;
    padding: 6px 8px 6px 32px;
    /* 稍微调整左边距以适应图片图标 */
    border: 1px solid var(--border-light);
    border-radius: 4px;
    background-color: var(--surface-lightest);
    font-size: 13px;
    color: var(--text-dark);
}

.search-input:focus {
    outline: none;
    border-color: var(--primary-blue);
    box-shadow: 0 0 0 1px rgba(43, 108, 176, 0.2);
}

.search-icon {
    position: absolute;
    left: 16px;
    top: 50%;
    transform: translateY(-50%);
    color: var(--text-light);
    font-size: 14px;
}

.search-options {
    display: flex;
    justify-content: space-between;
    padding: 4px 8px;
    font-size: 12px;
}

.search-options-left {
    display: flex;
    gap: 12px;
    align-items: center;
}

.search-options-right {
    display: flex;
    gap: 6px;
    align-items: center;
}

.search-option {
    display: flex;
    align-items: center;
    gap: 4px;
    color: var(--text-light);
    cursor: pointer;
    padding: 2px 4px;
    border-radius: 3px;
}

.search-option:hover {
    background-color: var(--surface-mid);
}

.search-option.active {
    color: var(--primary-blue);
}

.search-toggle {
    padding: 6px 8px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border: 1px solid var(--border-light);
    border-radius: 4px;
    background-color: var(--surface-lightest);
    cursor: pointer;
    margin: 4px 8px;
}

.search-toggle:hover {
    background-color: var(--surface-mid);
}

.search-toggle-label {
    font-weight: 500;
    color: var(--text-mid);
    font-size: 12px;
}

.search-toggle-shortcut {
    color: var(--text-light);
    font-size: 11px;
    display: flex;
    align-items: center;
    gap: 2px;
}

.search-results {
    flex: 1;
    overflow: auto;
    padding: 8px;
}

.search-more {
    padding: 8px;
    display: flex;
    justify-content: center;
    color: var(--text-light);
    font-size: 12px;
}

.search-icon-img {
    position: absolute;
    left: 12px;
    top: 50%;
    transform: translateY(-50%);
    width: 16px;
    height: 16px;
    opacity: 0.7;
}


</style>