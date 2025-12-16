<template>
    <div>
      <!-- 按钮 -->
      <button
        class="text-blue-600 flex items-center gap-1 hover:underline"
        :style="{
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          color: 'var(--primary-blue)',
          padding: '6px 10px',
          borderRadius: '4px',
          border: 'none',
          backgroundColor: 'transparent',
          cursor: 'pointer',
          transition: 'all 0.2s ease',
        }"
        @mouseenter="handleButtonMouseEnter"
        @mouseleave="handleButtonMouseLeave"
        @click="showModal = true"
      >
        <!-- 使用指定的图标 -->
        <img
          :src="inviteIcon"
          alt="邀请协作者"
          :style="{
            width: '20px',
            height: '20px',
            objectFit: 'contain'
          }"
        />
        添加协作者
      </button>
  
      <!-- 弹框 -->
      <div
        v-if="showModal"
        :style="{
          position: 'fixed',
          inset: '0',
          backgroundColor: 'rgba(0, 0, 0, 0.4)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 50
        }"
      >
        <div
          :style="{
            backgroundColor: 'var(--surface-lightest)',
            padding: '24px',
            borderRadius: '8px',
            boxShadow: '0 10px 25px rgba(0, 0, 0, 0.1)',
            width: '320px',
            border: '1px solid var(--border-light)'
          }"
        >
          <h2 :style="{
            fontSize: '18px',
            fontWeight: 600,
            marginBottom: '16px',
            color: 'var(--primary-dark)',
            display: 'flex',
            alignItems: 'center',
            gap: '10px'
          }">
            <img
              :src="inviteIcon"
              alt="邀请"
              :style="{ width: '24px', height: '24px', objectFit: 'contain' }"
            />
            邀请协作者
          </h2>
  
          <div :style="{
            marginBottom: '20px',
            position: 'relative'
          }">
            <label
              for="email-input"
              :style="{
                display: 'block',
                marginBottom: '6px',
                fontSize: '13px',
                color: 'var(--text-mid)'
              }"
            >
              协作者邮箱：
            </label>
            <input
              id="email-input"
              type="email"
              placeholder="请输入邮箱地址"
              v-model="email"
              :style="{
                width: '100%',
                padding: '10px 12px',
                border: '1px solid var(--border-light)',
                borderRadius: '4px',
                fontSize: '14px',
                color: 'var(--text-dark)',
                backgroundColor: 'var(--surface-light)',
                transition: 'all 0.2s ease',
                outline: 'none'
              }"
              @focus="handleInputFocus"
              @blur="handleInputBlur"
            />
            <p :style="{
              fontSize: '12px',
              color: 'var(--text-light)',
              marginTop: '6px'
            }">
              他们将收到一封邀请邮件，获得对此项目的访问权限
            </p>
          </div>
  
          <div :style="{
            display: 'flex',
            justifyContent: 'flex-end',
            gap: '10px'
          }">
            <button
              @click="showModal = false"
              :style="{
                padding: '8px 16px',
                backgroundColor: 'var(--surface-mid)',
                color: 'var(--text-mid)',
                border: '1px solid var(--border-light)',
                borderRadius: '4px',
                cursor: 'pointer',
                fontSize: '14px',
                transition: 'all 0.2s ease'
              }"
              @mouseenter="handleCancelMouseEnter"
              @mouseleave="handleCancelMouseLeave"
            >
              取消
            </button>
            <button
              @click="handleSendInvite"
              :style="{
                padding: '8px 16px',
                backgroundColor: 'var(--primary-blue)',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
                fontSize: '14px',
                transition: 'all 0.2s ease'
              }"
              @mouseenter="handleSendMouseEnter"
              @mouseleave="handleSendMouseLeave"
            >
              发送邀请
            </button>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref } from 'vue'
  import inviteIcon from '@/images/icons8-邀请-80.png'
  
  // 响应式数据
  const showModal = ref(false)
  const email = ref('')
  
  // 事件处理函数
  const handleButtonMouseEnter = (e: Event) => {
    const target = e.currentTarget as HTMLElement
    target.style.backgroundColor = 'var(--primary-light)'
  }
  
  const handleButtonMouseLeave = (e: Event) => {
    const target = e.currentTarget as HTMLElement
    target.style.backgroundColor = 'transparent'
  }
  
  const handleInputFocus = (e: Event) => {
    const target = e.target as HTMLInputElement
    target.style.borderColor = 'var(--primary-blue)'
    target.style.boxShadow = '0 0 0 2px rgba(49, 130, 206, 0.2)'
  }
  
  const handleInputBlur = (e: Event) => {
    const target = e.target as HTMLInputElement
    target.style.borderColor = 'var(--border-light)'
    target.style.boxShadow = 'none'
  }
  
  const handleCancelMouseEnter = (e: Event) => {
    const target = e.currentTarget as HTMLElement
    target.style.backgroundColor = 'var(--surface-dark)'
  }
  
  const handleCancelMouseLeave = (e: Event) => {
    const target = e.currentTarget as HTMLElement
    target.style.backgroundColor = 'var(--surface-mid)'
  }
  
  const handleSendMouseEnter = (e: Event) => {
    const target = e.currentTarget as HTMLElement
    target.style.backgroundColor = 'var(--primary-dark)'
  }
  
  const handleSendMouseLeave = (e: Event) => {
    const target = e.currentTarget as HTMLElement
    target.style.backgroundColor = 'var(--primary-blue)'
  }
  
  const handleSendInvite = () => {
    // 处理邀请逻辑
    console.log('邀请：', email.value)
    showModal.value = false
  }
  </script>