<template>
  <div id="app-container">
    <header class="app-header">
      <div class="header-left">
        <img src="@/images/logo.svg" alt="CloudStack Pro Logo" class="logo-img">
        <a href="#">首页</a>
        <a href="#">下载</a>
      </div>
      <div class="header-right">
        <a href="#">CSP安全</a>
      </div>
    </header>

    <main class="app-main">
      <div class="title-group">
        <p class="main-title">AI全栈协作开发平台</p>
        <p class="subtitle">你的想法应该获得最佳工具来实现</p>
      </div>
      <div class="CloudStack-Pro-logo">CloudStack Pro</div>
      <div class="login-buttons">
        <button v-for="os in operatingSystems" :key="os.name" class="login-btn" @click="handleLogin(os.name)">
          <img :src="os.icon" :alt="os.name" class="icon">
          <span>{{ os.name }}</span>
        </button>
      </div>
    </main>

    <footer class="app-footer">
      <div class="footer-links">
        <a href="#">恢复账户</a>
        <span>|</span>
        <a href="#">账号冻结和举报</a>
        <span>|</span>
        <a href="#">EG客服</a>
        <span>|</span>
        <a href="#">反馈问题</a>
      </div>
      <div class="copyright">
        <p>Copyright © 2025-2026 Yaonikaixin999999. All Rights Reserved.</p>
        <p>奶龙公司 版权所有</p>
      </div>
    </footer>
  </div>
</template>

<script>
export default {
  name: 'HomePage',
  data() {
    return {
      operatingSystems: [
        { name: 'Web', icon: require('@/images/icons8-网页-100.png') },
        { name: 'MacOS', icon: require('@/images/icons8-macos-100.png') },
        { name: 'Windows', icon: require('@/images/icons8-视窗11-100.png') },
        { name: 'Android', icon: require('@/images/icons8-android-144.png') },
        { name: 'Linux', icon: require('@/images/icons8-linux-100.png') },
        { name: 'IOS', icon: require('@/images/icons8-macos-100.png') }
      ]
    };
  },
  methods: {
    handleLogin(osName) {
      // 如果点击的是Web，跳转到登录页面
      if (osName === 'Web') {
        this.$router.push('/login');
      }
      else {
        // 其他端暂时显示提示信息
        console.log(`点击了${osName}，功能开发中...`);
        alert(`${osName}端正在开发中...`);
      }
    }
  }
};
</script>

<style>
#app-container {
  font-family: -apple-system, BlinkMacSystemFont, "Helvetica Neue", "PingFang SC", "Microsoft YaHei", "Source Han Sans SC", "Noto Sans CJK SC", "WenQuanYi Micro Hei", sans-serif;
  display: flex;
  flex-direction: column;
  height: 100vh;
  min-height: 800px;
  background: linear-gradient(135deg, #e6f2ff 0%, #ffffff 100%);
  color: #333;
  text-align: center;
  overflow-y: auto;
}

a {
  text-decoration: none;
  color: #555;
  transition: color 0.3s;
}

a:hover {
  color: #007bff;
}

/* 头部 */
.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 40px;
  width: 100%;
  box-sizing: border-box;
  flex-shrink: 0;
  border-bottom: 1px solid #e8e8e8;
}

.header-left, .header-right {
  display: flex;
  align-items: center;
  gap: 25px;
}

.logo-img {
  height: 35px;
  width: auto;
}

/* 主要内容 */
.app-main {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.title-group {
  margin-bottom: 30px;
}

.main-title {
  font-size: 2.5rem;
  letter-spacing: 10px;
  color: #666;
  font-weight: 300;
  margin: 0;
}

.subtitle {
  font-size: 3.5rem;
  font-weight: 500;
  color: #333;
  margin: 10px 0;
}

.CloudStack-Pro-logo {
  font-size: 10rem;
  font-weight: 700;
  line-height: 1.8;
  background: linear-gradient(120deg, #3a9cff, #a8d5ff);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  margin-bottom: 60px;
  user-select: none;
}

/* 下载按钮 */
.login-buttons {
  display: flex;
  gap: 70px;
  justify-content: center;
  flex-wrap: wrap;
}

.login-btn {
  background: none;
  border: none;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  color: #555;
  font-size: 1rem;
  padding: 10px;
  transition: all 0.3s ease;
}

.login-btn .icon {
  width: 40px;
  height: 40px;
  opacity: 0.8;
  object-fit: contain;
}

.login-btn:hover {
  color: #007bff;
}

.login-btn:hover .icon {
  opacity: 1;
}

/* 底部 */
.app-footer {
  padding: 30px 20px;
  font-size: 0.8rem;
  color: #888;
  flex-shrink: 0;
}

.footer-links {
  margin-bottom: 15px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.footer-links a {
  color: #888;
}

.footer-links a:hover {
  color: #333;
}

.copyright p {
  margin: 5px 0;
}

/* 移动端适配 */
@media (max-width: 768px) {
  #app-container {
    min-height: 100vh;
  }

  /* 头部移动端适配 */
  .app-header {
    padding: 15px 20px;
    flex-direction: column;
    gap: 15px;
  }

  .header-left, .header-right {
    gap: 15px;
  }

  .header-left a, .header-right a {
    font-size: 0.9rem;
  }

  .logo-img {
    height: 30px;
  }

  /* 主要内容移动端适配 */
  .app-main {
    padding: 20px 15px;
  }

  .title-group {
    margin-bottom: 20px;
  }

  .main-title {
    font-size: 1.2rem;
    letter-spacing: 3px;
  }

  .subtitle {
    font-size: 1.5rem;
    line-height: 1.4;
  }

  .CloudStack-Pro-logo {
    font-size: 3rem;
    margin-bottom: 30px;
    line-height: 1.2;
  }

  /* 按钮区域移动端适配 */
  .login-buttons {
    gap: 20px;
    width: 100%;
    padding: 0 20px;
  }

  .login-btn {
    gap: 8px;
    font-size: 0.8rem;
    min-width: 60px;
  }

  .login-btn .icon {
    width: 32px;
    height: 32px;
  }

  /* 底部移动端适配 */
  .app-footer {
    padding: 20px 15px;
    font-size: 0.7rem;
  }

  .footer-links {
    gap: 8px;
    line-height: 1.5;
  }

  .footer-links a {
    font-size: 0.7rem;
  }

  .copyright p {
    font-size: 0.65rem;
    margin: 3px 0;
  }
}

/* 超小屏幕适配 */
@media (max-width: 480px) {
  .app-header {
    padding: 10px 15px;
  }

  .header-left, .header-right {
    gap: 10px;
  }

  .main-title {
    font-size: 1rem;
    letter-spacing: 2px;
  }

  .subtitle {
    font-size: 1.2rem;
  }

  .CloudStack-Pro-logo {
    font-size: 2.5rem;
    margin-bottom: 25px;
  }

  .login-buttons {
    gap: 15px;
    padding: 0 10px;
  }

  .login-btn {
    font-size: 0.7rem;
    min-width: 50px;
  }

  .login-btn .icon {
    width: 28px;
    height: 28px;
  }

  .footer-links {
    flex-direction: column;
    gap: 5px;
  }

  .footer-links span {
    display: none;
  }
}

/* 横屏适配 */
@media (max-width: 768px) and (orientation: landscape) {
  .CloudStack-Pro-logo {
    font-size: 2rem;
    margin-bottom: 20px;
  }

  .subtitle {
    font-size: 1.2rem;
  }

  .main-title {
    font-size: 1rem;
  }

  .app-main {
    padding: 15px;
  }
}
</style>