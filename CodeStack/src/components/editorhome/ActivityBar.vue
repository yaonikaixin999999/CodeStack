<template>
    <div
      class="activity-bar"
      ref="activityBarRef"
      :style="{ width: `${width}px` }"
    >
      <div
        :class="['activity-icon', { active: activeTab === 'explorer' }]"
        @click="handleClick('explorer')"
      >
        <img :src="explorerIcon" alt="Explorer" />
      </div>
      <div
        :class="['activity-icon', { active: activeTab === 'search' }]"
        @click="handleClick('search')"
      >
        <img :src="searchIcon" alt="Search" />
      </div>
      <div
        :class="['activity-icon', { active: activeTab === 'git' }]"
        @click="handleClick('git')"
      >
        <img :src="gitIcon" alt="Git" />
      </div>
      <div
        :class="['activity-icon', { active: activeTab === 'debug' }]"
        @click="handleClick('debug')"
      >
        <img :src="debugIcon" alt="Debug" />
      </div>
      <div
        :class="['activity-icon', { active: activeTab === 'extensions' }]"
        @click="handleClick('extensions')"
      >
        <a href="https://yaonikaixin999999.xyz" target="_blank" rel="noopener noreferrer">
          <img :src="extensionsIcon" alt="Extensions" />
        </a>
      </div>
      
      <div class="spacer"></div>
      
      <div
        class="activity-icon"
        @click="toggleSettingsMenu"
        ref="settingsRef"
      >
        <img :src="settingsIcon" alt="Settings" />
      </div>
      
      <!-- 设置菜单弹窗 -->
      <div
        v-if="showSettingsMenu && settingsRef"
        class="settings-menu"
        :style="{
          position: 'absolute',
          left: settingsRef.getBoundingClientRect().right + 8 + 'px',
          top: settingsRef.getBoundingClientRect().top - 280 + 'px',
          width: '220px',
          backgroundColor: 'var(--surface-lightest)',
          color: 'var(--text-dark)',
          borderRadius: '6px',
          boxShadow: 'var(--shadow-md)',
          border: '1px solid var(--border-light)',
          zIndex: 9999,
          transition: 'var(--transition-default)'
        }"
      >
        <div
          v-for="(item, index) in settingsMenuItems"
          :key="index"
          class="settings-menu-item"
          @mouseenter="handleMenuItemHover"
          @mouseleave="handleMenuItemLeave"
        >
          {{ item }}
        </div>
      </div>
      
      <!-- 活动栏拖动控件
      <div
        class="activity-bar-resizer"
        ref="activityBarResizerRef"
        @mousedown="handleActivityBarMouseDown"
      ></div> -->
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref, onMounted, onUnmounted } from 'vue'
  
  // 导入图标
  import explorerIcon from '@/images/icons8-文件夹-40.png'
  import searchIcon from '@/images/icons8-搜索-40.png'
  import gitIcon from '@/images/icons8-代码叉-40.png'
  import debugIcon from '@/images/icons8-播放-40.png'
  import extensionsIcon from '@/images/icons8-用户组-40.png'
  import settingsIcon from '@/images/icons8-设置-40.png'
  
  // Props 定义
  interface Props {
    activeTab: string
    width: number
    onTabChange: (tab: string) => void
    onDragStart: () => void
  }
  
  const props = defineProps<Props>()
  
  // 模板引用
  const activityBarRef = ref<HTMLDivElement>()
  const activityBarResizerRef = ref<HTMLDivElement>()
  const settingsRef = ref<HTMLDivElement>()
  
  // 响应式数据
  const showSettingsMenu = ref(false)
  
  // 设置菜单项
  const settingsMenuItems = [
    '配置文件（默认）',
    '设置',
    '扩展',
    '键盘快捷方式',
    '代码片段',
    '任务',
    '主题',
    '备份和同步设置',
    '下载更新(1)',
  ]
  
  // 事件处理函数
  const handleClick = (tabName: string) => {
    props.onTabChange(tabName)
  }
  
  const toggleSettingsMenu = () => {
    showSettingsMenu.value = !showSettingsMenu.value
  }
  
  const handleActivityBarMouseDown = (e: MouseEvent) => {
    e.preventDefault()
    props.onDragStart()
  }
  
  // 菜单项悬停效果
  const handleMenuItemHover = (e: Event) => {
    const target = e.currentTarget as HTMLElement
    target.style.backgroundColor = 'var(--primary-light)'
  }
  
  const handleMenuItemLeave = (e: Event) => {
    const target = e.currentTarget as HTMLElement
    target.style.backgroundColor = 'transparent'
  }
  
  // 点击外部关闭设置菜单
  const handleClickOutside = (event: MouseEvent) => {
    if (settingsRef.value && !settingsRef.value.contains(event.target as Node)) {
      showSettingsMenu.value = false
    }
  }
  
  // 生命周期钩子
  onMounted(() => {
    document.addEventListener('mousedown', handleClickOutside)
  })
  
  onUnmounted(() => {
    document.removeEventListener('mousedown', handleClickOutside)
  })
  </script>
  
  <style>
  /* 活动栏样式 */
  .activity-bar {
    width: 50px;
    background-color: var(--surface-mid);
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 8px 0;
    z-index: 10;
    box-shadow: var(--shadow-sm);
    position: relative;
  }
  
  .activity-icon {
    width: 36px;
    height: 36px;
    display: flex;
    justify-content: center;
    align-items: center;
    margin-bottom: 10px;
    font-size: 18px;
    color: var(--text-light);
    cursor: pointer;
    border-radius: 4px;
    transition: var(--transition-default);
  }
  
  .activity-icon:hover {
    color: var(--primary-blue);
    background-color: var(--primary-light);
  }
  
  .activity-icon.active {
    color: var(--primary-blue);
    background-color: var(--primary-light);
    border-left: 2px solid var(--primary-blue);
  }
  
  .activity-icon img {
    width: 24px;
    height: 24px;
    object-fit: contain;
    transition: var(--transition-default);
  }
  
  /* 活动栏图标激活状态 */
  .activity-icon.active img {
    filter: brightness(1.2);
  }
  
  /* 图标悬停效果 */
  .activity-icon:hover img {
    transform: scale(1.1);
  }
  
  /* 扩展链接样式 */
  .activity-icon a {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    text-decoration: none;
  }
  
  .spacer {
    flex: 1;
  }
  
  /* 设置菜单样式 */
  .settings-menu {
    position: absolute;
    background-color: var(--surface-lightest);
    color: var(--text-dark);
    border-radius: 6px;
    box-shadow: var(--shadow-md);
    border: 1px solid var(--border-light);
    z-index: 9999;
    transition: var(--transition-default);
  }
  
  .settings-menu-item {
    padding: 8px 16px;
    cursor: pointer;
    transition: var(--transition-default);
  }
  
  .settings-menu-item:hover {
    background-color: var(--primary-light);
  }
  
  /* 活动栏拖动控件 */
  .activity-bar-resizer {
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
/*   
  .activity-bar-resizer:hover,
  .activity-bar-resizer:active {
    background-color: rgba(49, 130, 206, 0.6);
  } */
  
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
  
  /* 响应式设计 */
  @media (max-width: 768px) {
    .activity-bar {
      width: 40px;
    }
  }
  
  @media (max-width: 600px) {
    .activity-bar {
      width: 30px;
    }
  
    .activity-icon {
      width: 28px;
      height: 28px;
      font-size: 14px;
    }
  }
  </style>