<template>
  <header class="login-header">
    <div class="header-left">
      <img src="@/images/logo.svg" alt="CloudStack Pro Logo" class="logo-img">
      <span class="brand-name">CloudStack</span>
      <span class="user-type">Pro</span>
    </div>
    <div class="header-right">
      <a href="#" @click="goBack">返回首页</a>
    </div>
  </header>
  
  <div class="login-container">
    <div class="forms-container">
      <div class="signin-signup">
        <form @submit.prevent="handleSignIn" class="sign-in-form">
          <h2 class="title">登录</h2>
          <div class="input-field">
            <i class="fas fa-user"></i>
            <input 
              type="text" 
              placeholder="用户名" 
              v-model="loginForm.username" 
              required
            />
          </div>
          <div class="input-field">
            <i class="fas fa-lock"></i>
            <input 
              type="password" 
              placeholder="密码" 
              v-model="loginForm.password" 
              required
            />
          </div>
          
          <!-- 错误提示 -->
          <div v-if="loginError" class="error-message">
            {{ loginError }}
          </div>
          
          <input 
            type="submit" 
            value="立即登录" 
            class="btn solid" 
            :disabled="loginLoading"
          />
          
          <p class="social-text">通过其他方式</p>
          <div class="social-media">
            <a href="#" class="social-icon">
              <img src="@/images/icons8-qq.svg" alt="QQ登录" class="social-icon-img">
            </a>
            <a href="#" class="social-icon">
              <img src="@/images/icons8-微信.svg" alt="微信登录" class="social-icon-img">
            </a>
            <a href="#" class="social-icon">
              <img src="@/images/icons8-微博.svg" alt="微博登录" class="social-icon-img">
            </a>
            <a href="#" class="social-icon">
              <img src="@/images/icons8-支付宝.svg" alt="支付宝登录" class="social-icon-img">
            </a>
          </div>
        </form>
        
        <form @submit.prevent="handleSignUp" class="sign-up-form">
          <h2 class="title">注册</h2>
          <div class="input-field">
            <i class="fas fa-user"></i>
            <input 
              type="text" 
              placeholder="用户名" 
              v-model="registerForm.username" 
              required
            />
          </div>
          <div class="input-field">
            <i class="fas fa-phone"></i>
            <input 
              type="tel" 
              placeholder="手机号" 
              v-model="registerForm.phone" 
              required
            />
          </div>
          <div class="input-field">
            <i class="fas fa-lock"></i>
            <input 
              type="password" 
              placeholder="密码" 
              v-model="registerForm.password" 
              required
            />
          </div>
          
          <!-- 错误提示 -->
          <div v-if="registerError" class="error-message">
            {{ registerError }}
          </div>
          
          <input 
            type="submit" 
            class="btn" 
            value="立即注册" 
            :disabled="registerLoading"
          />
          
          <p class="social-text">通过其他方式</p>
          <div class="social-media">
            <a href="#" class="social-icon">
              <img src="@/images/icons8-qq.svg" alt="QQ登录" class="social-icon-img">
            </a>
            <a href="#" class="social-icon">
              <img src="@/images/icons8-微信.svg" alt="微信登录" class="social-icon-img">
            </a>
            <a href="#" class="social-icon">
              <img src="@/images/icons8-微博.svg" alt="微博登录" class="social-icon-img">
            </a>
            <a href="#" class="social-icon">
              <img src="@/images/icons8-支付宝.svg" alt="支付宝登录" class="social-icon-img">
            </a>
          </div>
        </form>
      </div>
    </div>

    <div class="panels-container">
      <div class="panel left-panel">
        <div class="content">
          <h3>加入我们</h3>
          <p>
            加入CloudStack Pro，我们期待你的加入。
          </p>
          <button class="btn transparent" @click="toggleSignUp">
            去注册
          </button>
        </div>
        <img src="@/images/log.svg" class="image" alt="" />
      </div>
      <div class="panel right-panel">
        <div class="content">
          <h3>已有帐号？</h3>
          <p>
            欢迎回到CloudStack Pro。
          </p>
          <button class="btn transparent" @click="toggleSignIn">
            去登录
          </button>
        </div>
        <img src="@/images/register.svg" class="image" alt="" />
      </div>
    </div>
  </div>
  
  <footer class="login-footer">
    <div class="footer-links">
      <a href="#">帮助中心</a>
      <span>|</span>
      <a href="#">联系我们</a>
      <span>|</span>
      <a href="#">反馈问题</a>
    </div>
    <div class="copyright">
      <p>Copyright © 2025-2026 Yaonikaixin999999. All Rights Reserved.</p>
      <p>奶龙公司 版权所有</p>
    </div>
  </footer>
</template>

<script>
import { userService } from '@/services/userService'

export default {
  name: 'LoginNEPS',
  data() {
    return {
      loginForm: {
        username: '',
        password: ''
      },
      registerForm: {
        username: '',
        phone: '',
        password: ''
      },
      loginError: '',
      registerError: '',
      loginLoading: false,
      registerLoading: false
    };
  },
  methods: {
    async handleSignIn() {
      this.loginError = ''
      this.loginLoading = true
      
      try {
        const response = await userService.login(this.loginForm)
        
        if (response.success) {
          // 保存token和用户信息
          localStorage.setItem('token', response.data.token)
          localStorage.setItem('username', response.data.username)
          
          // 跳转到主页或编辑器页面
          this.$router.push('/editor')
        } else {
          this.loginError = response.message
        }
      } catch (error) {
        this.loginError = error.message || '登录失败，请重试'
      } finally {
        this.loginLoading = false
      }
    },
    
    async handleSignUp() {
      this.registerError = ''
      this.registerLoading = true
      
      // 表单验证
      if (!this.validateRegisterForm()) {
        this.registerLoading = false
        return
      }
      
      try {
        const response = await userService.register(this.registerForm)
        
        if (response.success) {
          // 保存token和用户信息
          localStorage.setItem('token', response.data.token)
          localStorage.setItem('username', response.data.username)
          
          // 跳转到主页或编辑器页面
          this.$router.push('/editor')
        } else {
          this.registerError = response.message
        }
      } catch (error) {
        this.registerError = error.message || '注册失败，请重试'
      } finally {
        this.registerLoading = false
      }
    },
    
    validateRegisterForm() {
      if (!this.registerForm.username.trim()) {
        this.registerError = '用户名不能为空'
        return false
      }
      
      if (this.registerForm.username.length < 2) {
        this.registerError = '用户名至少2个字符'
        return false
      }
      
      if (!this.registerForm.phone.trim()) {
        this.registerError = '手机号不能为空'
        return false
      }
      
      // 简单的手机号验证
      const phoneRegex = /^\d{9,11}$/;
      if (!phoneRegex.test(this.registerForm.phone)) {
        this.registerError = '请输入有效的手机号'
        return false
      }
      
      if (!this.registerForm.password.trim()) {
        this.registerError = '密码不能为空'
        return false
      }
      
      if (this.registerForm.password.length < 6) {
        this.registerError = '密码至少6个字符'
        return false
      }
      
      return true
    },
    
    toggleSignUp() {
      this.clearErrors()
      const container = document.querySelector('.login-container');
      container.classList.add('sign-up-mode');
    },
    
    toggleSignIn() {
      this.clearErrors()
      const container = document.querySelector('.login-container');
      container.classList.remove('sign-up-mode');
    },
    
    clearErrors() {
      this.loginError = ''
      this.registerError = ''
    },
    
    goBack() {
      this.$router.push('/');
    }
  }
};
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@100;200;300;400;500;600;700;800;900&display=swap');

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Poppins', sans-serif;
}

/* 错误提示样式 */
.error-message {
  background-color: #fee2e2;
  color: #b91c1c;
  padding: 10px;
  border-radius: 5px;
  margin: 10px 0;
  font-size: 14px;
  text-align: center;
  border: 1px solid #f87171;
}

/* 按钮禁用状态 */
.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 头部样式 */
.login-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 40px;
  border-bottom: 1px solid #e8e8e8;
  background: rgba(255, 255, 255, 0.9);
  position: relative;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.logo-img {
  height: 35px;
  width: auto;
}

.brand-name {
  font-size: 1.5rem;
  font-weight: 600;
  color: #4a90e2;
}

.header-right a {
  color: #4a90e2;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s;
}

.header-right a:hover {
  color: #3a7bc8;
}

/* 底部样式 */
.login-footer {
  padding: 30px 20px;
  font-size: 0.8rem;
  color: #888;
  text-align: center;
  background: rgba(255, 255, 255, 0.7);
  border-top: 1px solid #e8e8e8;
  position: relative;
  z-index: 10;
}

.footer-links {
  margin-bottom: 15px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
}

.footer-links a {
  color: #888;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-links a:hover {
  color: #4a90e2;
}

.copyright p {
  margin: 5px 0;
}

.login-container {
  position: relative;
  width: 100%;
  background-color: #f5f9ff;
  min-height: calc(100vh - 140px);
  overflow: hidden;
}

.forms-container {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
}

.signin-signup {
  position: absolute;
  top: 50%;
  transform: translate(-50%, -50%);
  left: 75%;
  width: 50%;
  transition: 1s 0.7s ease-in-out;
  display: grid;
  grid-template-columns: 1fr;
  z-index: 5;
}

form {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  padding: 0rem 5rem;
  transition: all 0.2s 0.7s;
  overflow: hidden;
  grid-column: 1 / 2;
  grid-row: 1 / 2;
}

form.sign-up-form {
  opacity: 0;
  z-index: 1;
}

form.sign-in-form {
  z-index: 2;
}

.title {
  font-size: 2.2rem;
  color: #4a90e2;
  margin-bottom: 10px;
  font-weight: 600;
}

.input-field {
  max-width: 380px;
  width: 100%;
  background-color: #ffffff;
  margin: 10px 0;
  height: 55px;
  border-radius: 55px;
  display: grid;
  grid-template-columns: 15% 85%;
  padding: 0 0.4rem;
  position: relative;
  border: 2px solid #e6f2ff;
  box-shadow: 0 2px 10px rgba(74, 144, 226, 0.1);
}

.input-field i {
  text-align: center;
  line-height: 55px;
  color: #4a90e2;
  transition: 0.5s;
  font-size: 1.1rem;
}

.input-field input {
  background: none;
  outline: none;
  border: none;
  line-height: 1;
  font-weight: 600;
  font-size: 1.1rem;
  color: #333;
}

.input-field input::placeholder {
  color: #aaa;
  font-weight: 500;
}

.social-text {
  padding: 0.7rem 0;
  font-size: 1rem;
  color: #666;
}

.social-media {
  display: flex;
  justify-content: center;
}

.social-icon {
  height: 46px;
  width: 46px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 0.45rem;
  color: #4a90e2;
  border-radius: 50%;
  border: 2px solid #e6f2ff;
  text-decoration: none;
  font-size: 1.1rem;
  transition: 0.3s;
  background-color: #ffffff;
}

.user-type {
  background: linear-gradient(45deg, #4a90e2, #3a7bc8);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
}

.social-icon:hover {
  color: #ffffff;
  background-color: #4a90e2;
  border-color: #4a90e2;
}

.social-icon-img {
  width: 24px;
  height: 24px;
  object-fit: contain;
  transition: 0.3s;
}

.social-icon:hover .social-icon-img {
  filter: brightness(0) saturate(100%) invert(100%);
}

.btn {
  width: 150px;
  background-color: #4a90e2;
  border: none;
  outline: none;
  height: 49px;
  border-radius: 49px;
  color: #fff;
  text-transform: uppercase;
  font-weight: 600;
  margin: 10px 0;
  cursor: pointer;
  transition: 0.5s;
  box-shadow: 0 4px 15px rgba(74, 144, 226, 0.3);
}

.btn:hover {
  background-color: #3a7bc8;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(74, 144, 226, 0.4);
}

.panels-container {
  position: absolute;
  height: 100%;
  width: 100%;
  top: 0;
  left: 0;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
}

.login-container:before {
  content: '';
  position: absolute;
  height: 2000px;
  width: 2000px;
  top: -10%;
  right: 48%;
  transform: translateY(-50%);
  background: linear-gradient(135deg, #e6f2ff 0%, #b8daff 100%);
  transition: 1.8s ease-in-out;
  border-radius: 50%;
  z-index: 6;
}

.image {
  width: 100%;
  transition: transform 1.1s ease-in-out;
  transition-delay: 0.4s;
}

.panel {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: space-around;
  text-align: center;
  z-index: 6;
}

.left-panel {
  pointer-events: all;
  padding: 3rem 17% 2rem 12%;
}

.right-panel {
  pointer-events: none;
  padding: 3rem 12% 2rem 17%;
}

.panel .content {
  color: #4a90e2;
  transition: transform 0.9s ease-in-out;
  transition-delay: 0.6s;
}

.panel h3 {
  font-weight: 600;
  line-height: 1;
  font-size: 1.5rem;
  margin-bottom: 1rem;
}

.panel p {
  font-size: 0.95rem;
  padding: 0.7rem 0;
  margin-bottom: 1.5rem;
}

.btn.transparent {
  margin: 0;
  background: none;
  border: 2px solid #4a90e2;
  width: 130px;
  height: 41px;
  font-weight: 600;
  font-size: 0.8rem;
  color: #4a90e2;
  box-shadow: 0 2px 10px rgba(74, 144, 226, 0.2);
}

.btn.transparent:hover {
  background-color: #4a90e2;
  color: #fff;
}

.right-panel .image,
.right-panel .content {
  transform: translateX(800px);
}

.login-container.sign-up-mode:before {
  transform: translate(100%, -50%);
  right: 52%;
}

.login-container.sign-up-mode .left-panel .image,
.login-container.sign-up-mode .left-panel .content {
  transform: translateX(-1100px);
}

.login-container.sign-up-mode .signin-signup {
  left: 25%;
}

.login-container.sign-up-mode form.sign-up-form {
  opacity: 1;
  z-index: 2;
}

.login-container.sign-up-mode form.sign-in-form {
  opacity: 0;
  z-index: 1;
}

.login-container.sign-up-mode .right-panel .image,
.login-container.sign-up-mode .right-panel .content {
  transform: translateX(0%);
}

.login-container.sign-up-mode .left-panel {
  pointer-events: none;
}

.login-container.sign-up-mode .right-panel {
  pointer-events: all;
}

@media (max-width: 870px) {
  .login-header {
    padding: 15px 20px;
  }
  
  .login-container {
    min-height: calc(100vh - 120px);
  }
  
  .signin-signup {
    width: 100%;
    top: 95%;
    transform: translate(-50%, -100%);
    transition: 1s 0.8s ease-in-out;
  }

  .signin-signup,
  .login-container.sign-up-mode .signin-signup {
    left: 50%;
  }

  .panels-container {
    grid-template-columns: 1fr;
    grid-template-rows: 1fr 2fr 1fr;
  }

  .panel {
    flex-direction: row;
    justify-content: space-around;
    align-items: center;
    padding: 2.5rem 8%;
    grid-column: 1 / 2;
  }

  .right-panel {
    grid-row: 3 / 4;
  }

  .left-panel {
    grid-row: 1 / 2;
  }

  .image {
    width: 200px;
    transition: transform 0.9s ease-in-out;
    transition-delay: 0.6s;
  }

  .panel .content {
    padding-right: 15%;
    transition: transform 0.9s ease-in-out;
    transition-delay: 0.8s;
  }

  .panel h3 {
    font-size: 1.2rem;
  }

  .panel p {
    font-size: 0.7rem;
    padding: 0.5rem 0;
  }

  .btn.transparent {
    width: 110px;
    height: 35px;
    font-size: 0.7rem;
  }

  .login-container:before {
    width: 1500px;
    height: 1500px;
    transform: translateX(-50%);
    left: 30%;
    bottom: 68%;
    right: initial;
    top: initial;
    transition: 2s ease-in-out;
  }

  .login-container.sign-up-mode:before {
    transform: translate(-50%, 100%);
    bottom: 32%;
    right: initial;
  }

  .login-container.sign-up-mode .left-panel .image,
  .login-container.sign-up-mode .left-panel .content {
    transform: translateY(-300px);
  }

  .login-container.sign-up-mode .right-panel .image,
  .login-container.sign-up-mode .right-panel .content {
    transform: translateY(0px);
  }

  .right-panel .image,
  .right-panel .content {
    transform: translateY(300px);
  }

  .login-container.sign-up-mode .signin-signup {
    top: 5%;
    transform: translate(-50%, 0);
  }
}

@media (max-width: 570px) {
  .login-header {
    padding: 15px 10px;
  }
  
  .brand-name {
    font-size: 1.2rem;
  }
  
  .logo-img {
    height: 30px;
  }
  
  .login-footer {
    padding: 20px 10px;
    font-size: 0.7rem;
  }
  
  form {
    padding: 0 1.5rem;
  }

  .image {
    display: none;
  }
  
  .panel .content {
    padding: 0.5rem 1rem;
  }
  
  .login-container {
    min-height: calc(100vh - 100px);
    padding: 1.5rem;
  }

  .login-container:before {
    bottom: 72%;
    left: 50%;
  }

  .login-container.sign-up-mode:before {
    bottom: 28%;
    left: 50%;
  }
}
</style>