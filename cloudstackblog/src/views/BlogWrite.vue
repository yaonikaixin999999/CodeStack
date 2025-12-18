<template>
  <div class="blog-write">
    <BlogHeader />
    
    <main class="main-content">
      <div class="content-container">
        <div class="write-header">
          <h1 class="page-title">
            <img src="@/assets/icons/edit.svg" alt="写文章" class="title-icon" />
            写文章
          </h1>
          <div class="header-actions">
            <button class="btn-draft" @click="saveDraft">
              <img src="@/assets/icons/bookmark.svg" alt="保存草稿" class="btn-icon" />
              保存草稿
            </button>
            <button class="btn-publish" @click="publishPost">
              <img src="@/assets/icons/upload.svg" alt="发布" class="btn-icon" />
              发布文章
            </button>
          </div>
        </div>
        
        <div class="write-content">
          <!-- 标题输入 -->
          <div class="title-input-wrapper">
            <input 
              v-model="postData.title" 
              type="text" 
              placeholder="请输入文章标题（最多100字）" 
              maxlength="100"
              class="title-input"
            />
            <span class="title-count">{{ postData.title.length }}/100</span>
          </div>
          
          <!-- 编辑区域 -->
          <div class="editor-container">
            <!-- 工具栏 -->
            <div class="editor-toolbar">
              <div class="toolbar-group">
                <button class="toolbar-btn" title="加粗" @click="insertFormat('bold')">
                  <span class="toolbar-text">B</span>
                </button>
                <button class="toolbar-btn" title="斜体" @click="insertFormat('italic')">
                  <span class="toolbar-text italic">I</span>
                </button>
                <button class="toolbar-btn" title="删除线" @click="insertFormat('strike')">
                  <span class="toolbar-text strike">S</span>
                </button>
              </div>
              
              <div class="toolbar-divider"></div>
              
              <div class="toolbar-group">
                <button class="toolbar-btn" title="一级标题" @click="insertHeading(1)">
                  <span class="toolbar-text">H1</span>
                </button>
                <button class="toolbar-btn" title="二级标题" @click="insertHeading(2)">
                  <span class="toolbar-text">H2</span>
                </button>
                <button class="toolbar-btn" title="三级标题" @click="insertHeading(3)">
                  <span class="toolbar-text">H3</span>
                </button>
              </div>
              
              <div class="toolbar-divider"></div>
              
              <div class="toolbar-group">
                <button class="toolbar-btn" title="无序列表" @click="insertList('ul')">
                  <img src="@/assets/icons/menu.svg" alt="无序列表" />
                </button>
                <button class="toolbar-btn" title="有序列表" @click="insertList('ol')">
                  <span class="toolbar-text">1.</span>
                </button>
                <button class="toolbar-btn" title="引用" @click="insertQuote">
                  <span class="toolbar-text">"</span>
                </button>
              </div>
              
              <div class="toolbar-divider"></div>
              
              <div class="toolbar-group">
                <button class="toolbar-btn" title="代码块" @click="insertCode">
                  <img src="@/assets/icons/code.svg" alt="代码" />
                </button>
                <button class="toolbar-btn" title="链接" @click="insertLink">
                  <img src="@/assets/icons/share.svg" alt="链接" />
                </button>
                <button class="toolbar-btn" title="图片" @click="insertImage">
                  <img src="@/assets/icons/image.svg" alt="图片" />
                </button>
              </div>
            </div>
            
            <!-- 编辑器主体 -->
            <div class="editor-body">
              <textarea 
                ref="editorRef"
                v-model="postData.content" 
                placeholder="开始写作你的文章...

支持 Markdown 语法：
- 使用 # 创建标题
- 使用 ** 加粗文字
- 使用 * 斜体文字
- 使用 ``` 插入代码块
- 使用 > 创建引用
- 使用 - 或 * 创建列表"
                class="editor-textarea"
              ></textarea>
            </div>
          </div>
          
          <!-- 文章设置 -->
          <div class="post-settings">
            <div class="settings-card">
              <h3 class="settings-title">
                <img src="@/assets/icons/category.svg" alt="分类" class="settings-icon" />
                文章分类
              </h3>
              <div class="category-options">
                <label 
                  v-for="category in categories" 
                  :key="category.id" 
                  class="category-option"
                  :class="{ selected: postData.category === category.id }"
                >
                  <input 
                    type="radio" 
                    :value="category.id" 
                    v-model="postData.category"
                  />
                  <span>{{ category.name }}</span>
                </label>
              </div>
            </div>
            
            <div class="settings-card">
              <h3 class="settings-title">
                <img src="@/assets/icons/filter.svg" alt="标签" class="settings-icon" />
                文章标签
              </h3>
              <div class="tags-input-wrapper">
                <div class="selected-tags">
                  <span 
                    v-for="tag in postData.tags" 
                    :key="tag" 
                    class="selected-tag"
                  >
                    {{ tag }}
                    <button class="remove-tag" @click="removeTag(tag)">
                      <img src="@/assets/icons/close.svg" alt="删除" />
                    </button>
                  </span>
                  <input 
                    v-if="postData.tags.length < 5"
                    v-model="tagInput" 
                    type="text" 
                    placeholder="输入标签并回车"
                    class="tag-input"
                    @keyup.enter="addTag"
                  />
                </div>
                <p class="tags-hint">最多添加5个标签，每个标签最多15字</p>
              </div>
              
              <div class="suggested-tags">
                <span class="hint-text">推荐标签：</span>
                <button 
                  v-for="tag in suggestedTags" 
                  :key="tag"
                  class="suggested-tag"
                  :disabled="postData.tags.includes(tag)"
                  @click="addSuggestedTag(tag)"
                >
                  {{ tag }}
                </button>
              </div>
            </div>
            
            <div class="settings-card">
              <h3 class="settings-title">
                <img src="@/assets/icons/image.svg" alt="封面" class="settings-icon" />
                文章封面
              </h3>
              <div 
                class="cover-upload"
                :class="{ 'has-cover': postData.coverImage }"
                @click="triggerCoverUpload"
              >
                <input 
                  type="file" 
                  ref="coverInputRef"
                  accept="image/*"
                  @change="handleCoverUpload"
                  style="display: none;"
                />
                <template v-if="postData.coverImage">
                  <img :src="postData.coverImage" alt="封面预览" class="cover-preview" />
                  <div class="cover-overlay">
                    <span>点击更换封面</span>
                  </div>
                </template>
                <template v-else>
                  <img src="@/assets/icons/upload.svg" alt="上传" class="upload-icon" />
                  <span>上传封面图片</span>
                  <span class="upload-hint">建议尺寸：1200 x 500</span>
                </template>
              </div>
            </div>
            
            <div class="settings-card">
              <h3 class="settings-title">
                <img src="@/assets/icons/info.svg" alt="摘要" class="settings-icon" />
                文章摘要
              </h3>
              <textarea 
                v-model="postData.excerpt"
                placeholder="请输入文章摘要，将在文章列表中展示（最多200字）"
                maxlength="200"
                class="excerpt-input"
              ></textarea>
              <div class="excerpt-count">{{ postData.excerpt.length }}/200</div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import BlogHeader from '@/components/BlogHeader.vue'

export default defineComponent({
  name: 'BlogWrite',
  components: {
    BlogHeader
  },
  setup() {
    const router = useRouter()
    const editorRef = ref<HTMLTextAreaElement | null>(null)
    const coverInputRef = ref<HTMLInputElement | null>(null)
    const tagInput = ref('')
    
    const postData = reactive({
      title: '',
      content: '',
      category: '',
      tags: [] as string[],
      coverImage: '',
      excerpt: ''
    })
    
    const categories = ref([
      { id: 'frontend', name: '前端开发' },
      { id: 'backend', name: '后端开发' },
      { id: 'ai', name: '人工智能' },
      { id: 'mobile', name: '移动开发' },
      { id: 'devops', name: '运维部署' },
      { id: 'other', name: '其他' }
    ])
    
    const suggestedTags = ref([
      'Vue.js', 'React', 'TypeScript', 'Node.js', 'Python', 
      '前端', '后端', '全栈', '算法', '面试'
    ])
    
    const insertFormat = (type: string) => {
      const textarea = editorRef.value
      if (!textarea) return
      
      const start = textarea.selectionStart
      const end = textarea.selectionEnd
      const text = postData.content
      const selectedText = text.substring(start, end)
      
      let newText = ''
      switch (type) {
        case 'bold':
          newText = `**${selectedText || '粗体文字'}**`
          break
        case 'italic':
          newText = `*${selectedText || '斜体文字'}*`
          break
        case 'strike':
          newText = `~~${selectedText || '删除线文字'}~~`
          break
      }
      
      postData.content = text.substring(0, start) + newText + text.substring(end)
    }
    
    const insertHeading = (level: number) => {
      const textarea = editorRef.value
      if (!textarea) return
      
      const start = textarea.selectionStart
      const text = postData.content
      const prefix = '#'.repeat(level) + ' '
      
      postData.content = text.substring(0, start) + '\n' + prefix + '标题' + '\n' + text.substring(start)
    }
    
    const insertList = (type: string) => {
      const textarea = editorRef.value
      if (!textarea) return
      
      const start = textarea.selectionStart
      const text = postData.content
      const prefix = type === 'ul' ? '- ' : '1. '
      
      postData.content = text.substring(0, start) + '\n' + prefix + '列表项\n' + text.substring(start)
    }
    
    const insertQuote = () => {
      const textarea = editorRef.value
      if (!textarea) return
      
      const start = textarea.selectionStart
      const text = postData.content
      
      postData.content = text.substring(0, start) + '\n> 引用文字\n' + text.substring(start)
    }
    
    const insertCode = () => {
      const textarea = editorRef.value
      if (!textarea) return
      
      const start = textarea.selectionStart
      const text = postData.content
      
      postData.content = text.substring(0, start) + '\n```javascript\n// 代码\n```\n' + text.substring(start)
    }
    
    const insertLink = () => {
      const url = prompt('请输入链接地址：')
      if (url) {
        const textarea = editorRef.value
        if (!textarea) return
        
        const start = textarea.selectionStart
        const text = postData.content
        
        postData.content = text.substring(0, start) + `[链接文字](${url})` + text.substring(start)
      }
    }
    
    const insertImage = () => {
      const url = prompt('请输入图片地址：')
      if (url) {
        const textarea = editorRef.value
        if (!textarea) return
        
        const start = textarea.selectionStart
        const text = postData.content
        
        postData.content = text.substring(0, start) + `![图片描述](${url})` + text.substring(start)
      }
    }
    
    const addTag = () => {
      const tag = tagInput.value.trim()
      if (tag && tag.length <= 15 && postData.tags.length < 5 && !postData.tags.includes(tag)) {
        postData.tags.push(tag)
        tagInput.value = ''
      }
    }
    
    const removeTag = (tag: string) => {
      const index = postData.tags.indexOf(tag)
      if (index > -1) {
        postData.tags.splice(index, 1)
      }
    }
    
    const addSuggestedTag = (tag: string) => {
      if (postData.tags.length < 5 && !postData.tags.includes(tag)) {
        postData.tags.push(tag)
      }
    }
    
    const triggerCoverUpload = () => {
      coverInputRef.value?.click()
    }
    
    const handleCoverUpload = (event: Event) => {
      const input = event.target as HTMLInputElement
      if (input.files && input.files[0]) {
        const file = input.files[0]
        const reader = new FileReader()
        reader.onload = (e) => {
          postData.coverImage = e.target?.result as string
        }
        reader.readAsDataURL(file)
      }
    }
    
    const saveDraft = () => {
      // 保存草稿逻辑
      localStorage.setItem('blogDraft', JSON.stringify(postData))
      alert('草稿已保存')
    }
    
    const publishPost = () => {
      if (!postData.title.trim()) {
        alert('请输入文章标题')
        return
      }
      if (!postData.content.trim()) {
        alert('请输入文章内容')
        return
      }
      if (!postData.category) {
        alert('请选择文章分类')
        return
      }
      
      // 发布逻辑
      console.log('发布文章:', postData)
      alert('文章发布成功！')
      router.push('/')
    }
    
    return {
      editorRef,
      coverInputRef,
      tagInput,
      postData,
      categories,
      suggestedTags,
      insertFormat,
      insertHeading,
      insertList,
      insertQuote,
      insertCode,
      insertLink,
      insertImage,
      addTag,
      removeTag,
      addSuggestedTag,
      triggerCoverUpload,
      handleCoverUpload,
      saveDraft,
      publishPost
    }
  }
})
</script>

<style scoped>
.blog-write {
  min-height: 100vh;
  background: #f5f7fa;
}

.main-content {
  padding-top: 64px;
}

.content-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 32px 24px;
}

.write-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: 600;
  color: var(--text-dark);
}

.title-icon {
  width: 28px;
  height: 28px;
  opacity: 0.8;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.btn-draft {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: white;
  color: var(--text-light);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  font-size: 14px;
  transition: var(--transition-default);
}

.btn-draft:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.btn-publish {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
  color: white;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  transition: var(--transition-default);
}

.btn-publish:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(58, 156, 255, 0.3);
}

.btn-icon {
  width: 18px;
  height: 18px;
}

.btn-draft .btn-icon {
  opacity: 0.6;
}

.btn-publish .btn-icon {
  filter: brightness(0) invert(1);
}

/* Title Input */
.title-input-wrapper {
  position: relative;
  margin-bottom: 20px;
}

.title-input {
  width: 100%;
  padding: 20px 80px 20px 24px;
  background: white;
  border: none;
  border-radius: 12px;
  font-size: 24px;
  font-weight: 600;
  color: var(--text-dark);
  box-shadow: var(--shadow-sm);
}

.title-input::placeholder {
  color: var(--text-muted);
  font-weight: 400;
}

.title-count {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 13px;
  color: var(--text-muted);
}

/* Editor Container */
.editor-container {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  margin-bottom: 24px;
}

.editor-toolbar {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 12px 16px;
  background: #fafbfc;
  border-bottom: 1px solid var(--border-light);
}

.toolbar-group {
  display: flex;
  gap: 4px;
}

.toolbar-divider {
  width: 1px;
  height: 24px;
  background: var(--border-color);
  margin: 0 8px;
}

.toolbar-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border-radius: 6px;
  transition: var(--transition-default);
}

.toolbar-btn:hover {
  background: var(--border-light);
}

.toolbar-btn img {
  width: 18px;
  height: 18px;
  opacity: 0.6;
}

.toolbar-text {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-light);
}

.toolbar-text.italic {
  font-style: italic;
}

.toolbar-text.strike {
  text-decoration: line-through;
}

.editor-body {
  padding: 0;
}

.editor-textarea {
  width: 100%;
  min-height: 400px;
  padding: 24px;
  border: none;
  font-size: 15px;
  line-height: 1.8;
  color: var(--text-dark);
  resize: vertical;
  font-family: inherit;
}

.editor-textarea::placeholder {
  color: var(--text-muted);
}

/* Post Settings */
.post-settings {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.settings-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: var(--shadow-sm);
}

.settings-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-dark);
  margin-bottom: 16px;
}

.settings-icon {
  width: 20px;
  height: 20px;
  opacity: 0.7;
}

/* Category Options */
.category-options {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.category-option {
  display: flex;
  align-items: center;
  padding: 10px 18px;
  background: var(--border-light);
  border-radius: 8px;
  cursor: pointer;
  transition: var(--transition-default);
}

.category-option:hover {
  background: linear-gradient(120deg, rgba(58, 156, 255, 0.1), rgba(168, 213, 255, 0.2));
}

.category-option.selected {
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
  color: white;
}

.category-option input {
  display: none;
}

.category-option span {
  font-size: 14px;
}

/* Tags Input */
.tags-input-wrapper {
  margin-bottom: 16px;
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 12px;
  background: var(--border-light);
  border-radius: 8px;
  min-height: 48px;
}

.selected-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
  color: white;
  font-size: 13px;
  border-radius: 16px;
}

.remove-tag {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
}

.remove-tag img {
  width: 10px;
  height: 10px;
  filter: brightness(0) invert(1);
}

.tag-input {
  flex: 1;
  min-width: 120px;
  padding: 6px;
  background: transparent;
  border: none;
  font-size: 14px;
}

.tags-hint {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 8px;
}

.suggested-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.hint-text {
  font-size: 13px;
  color: var(--text-muted);
}

.suggested-tag {
  padding: 6px 12px;
  background: var(--border-light);
  color: var(--text-light);
  font-size: 12px;
  border-radius: 16px;
  transition: var(--transition-default);
}

.suggested-tag:hover:not(:disabled) {
  background: linear-gradient(120deg, rgba(58, 156, 255, 0.1), rgba(168, 213, 255, 0.2));
  color: var(--primary-color);
}

.suggested-tag:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Cover Upload */
.cover-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  background: var(--border-light);
  border: 2px dashed var(--border-color);
  border-radius: 12px;
  cursor: pointer;
  transition: var(--transition-default);
  position: relative;
  overflow: hidden;
}

.cover-upload:hover {
  border-color: var(--primary-color);
  background: linear-gradient(120deg, rgba(58, 156, 255, 0.05), rgba(168, 213, 255, 0.1));
}

.cover-upload.has-cover {
  border-style: solid;
}

.upload-icon {
  width: 48px;
  height: 48px;
  opacity: 0.4;
  margin-bottom: 12px;
}

.cover-upload span {
  font-size: 14px;
  color: var(--text-light);
}

.upload-hint {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
}

.cover-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: var(--transition-default);
}

.cover-upload:hover .cover-overlay {
  opacity: 1;
}

.cover-overlay span {
  color: white;
}

/* Excerpt Input */
.excerpt-input {
  width: 100%;
  height: 100px;
  padding: 16px;
  background: var(--border-light);
  border: 1px solid transparent;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.6;
  resize: none;
  transition: var(--transition-default);
}

.excerpt-input:focus {
  border-color: var(--primary-color);
  background: white;
}

.excerpt-count {
  text-align: right;
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 8px;
}

@media (max-width: 768px) {
  .write-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .header-actions {
    width: 100%;
  }
  
  .btn-draft, .btn-publish {
    flex: 1;
    justify-content: center;
  }
  
  .title-input {
    font-size: 18px;
    padding: 16px 60px 16px 16px;
  }
}
</style>
