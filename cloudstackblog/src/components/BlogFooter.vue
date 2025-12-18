<template>
  <footer class="blog-footer">
    <div class="footer-container">
      <div class="footer-main">
        <div class="footer-brand">
          <div class="brand-logo">
            <span class="logo-text">CloudStack</span>
            <span class="logo-badge">Blog</span>
          </div>
          <p class="brand-desc">
            一个专注于技术分享和知识交流的博客平台，让创作更简单，让知识更有价值。
          </p>
          <div class="social-links">
            <a href="#" class="social-link" title="GitHub">
              <img src="@/assets/icons/code.svg" alt="GitHub" />
            </a>
            <a href="#" class="social-link" title="邮箱">
              <img src="@/assets/icons/comment.svg" alt="邮箱" />
            </a>
            <a href="#" class="social-link" title="分享">
              <img src="@/assets/icons/share.svg" alt="分享" />
            </a>
          </div>
        </div>
        
        <div class="footer-links">
          <div class="link-group">
            <h4 class="link-title">快速导航</h4>
            <router-link to="/" class="link-item">首页</router-link>
            <router-link to="/category/tech" class="link-item">技术文章</router-link>
            <router-link to="/search" class="link-item">热门话题</router-link>
            <router-link to="/write" class="link-item">写文章</router-link>
          </div>
          
          <div class="link-group">
            <h4 class="link-title">帮助支持</h4>
            <a href="#" class="link-item">使用帮助</a>
            <a href="#" class="link-item">意见反馈</a>
            <a href="#" class="link-item">联系我们</a>
            <a href="#" class="link-item">关于我们</a>
          </div>
          
          <div class="link-group">
            <h4 class="link-title">法律声明</h4>
            <a href="#" class="link-item">服务条款</a>
            <a href="#" class="link-item">隐私政策</a>
            <a href="#" class="link-item">版权声明</a>
            <a href="#" class="link-item">社区规范</a>
          </div>
        </div>
      </div>
      
      <div class="footer-bottom">
        <p class="copyright">
          Copyright © 2025-2026 Yaonikaixin999999. All Rights Reserved.
        </p>
        <p class="company">奶龙公司 版权所有</p>
      </div>
    </div>
  </footer>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'BlogFooter'
})
</script>

<style scoped>
.blog-footer {
  background: linear-gradient(180deg, #f8fafc 0%, #ffffff 100%);
  border-top: 1px solid var(--border-color);
  margin-top: 60px;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 48px 24px 24px;
}

.footer-main {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 60px;
  padding-bottom: 32px;
  border-bottom: 1px solid var(--border-color);
}

.footer-brand {
  max-width: 300px;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.logo-text {
  font-size: 24px;
  font-weight: 700;
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.logo-badge {
  font-size: 12px;
  font-weight: 500;
  padding: 2px 8px;
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
  color: white;
  border-radius: 12px;
}

.brand-desc {
  font-size: 14px;
  color: var(--text-light);
  line-height: 1.7;
  margin-bottom: 20px;
}

.social-links {
  display: flex;
  gap: 12px;
}

.social-link {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--border-light);
  border-radius: 50%;
  transition: var(--transition-default);
}

.social-link:hover {
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
}

.social-link img {
  width: 18px;
  height: 18px;
  opacity: 0.6;
}

.social-link:hover img {
  opacity: 1;
  filter: brightness(0) invert(1);
}

.footer-links {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 40px;
}

.link-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-dark);
  margin-bottom: 16px;
}

.link-item {
  display: block;
  font-size: 14px;
  color: var(--text-light);
  margin-bottom: 10px;
  transition: var(--transition-default);
}

.link-item:hover {
  color: var(--primary-color);
}

.footer-bottom {
  padding-top: 24px;
  text-align: center;
}

.copyright {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 4px;
}

.company {
  font-size: 12px;
  color: var(--text-muted);
}

@media (max-width: 768px) {
  .footer-main {
    grid-template-columns: 1fr;
    gap: 40px;
  }
  
  .footer-brand {
    max-width: 100%;
    text-align: center;
  }
  
  .social-links {
    justify-content: center;
  }
  
  .footer-links {
    grid-template-columns: repeat(2, 1fr);
    gap: 24px;
  }
}

@media (max-width: 480px) {
  .footer-links {
    grid-template-columns: 1fr;
  }
  
  .link-group {
    text-align: center;
  }
}
</style>
