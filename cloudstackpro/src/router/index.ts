import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

// 动态导入组件
const HomeView = () => import('../views/HomeView.vue')
const Login = () => import('../views/Login.vue')
const EditorHome = () => import('../views/EditorHome.vue')

// 博客系统页面
const BlogHome = () => import('../views/blog/BlogHome.vue')
const BlogPost = () => import('../views/blog/BlogPost.vue')
const BlogWrite = () => import('../views/blog/BlogWrite.vue')
const BlogCategory = () => import('../views/blog/BlogCategory.vue')
const BlogSearch = () => import('../views/blog/BlogSearch.vue')
const BlogProfile = () => import('../views/blog/BlogProfile.vue')

// 导入用户服务
import { userService } from '@/services/userService'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: HomeView
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/editor',
    name: 'Editor',
    component: EditorHome,
    meta: { requiresAuth: true }
  },
  // 博客系统路由
  {
    path: '/blog',
    name: 'BlogHome',
    component: BlogHome,
    meta: { requiresAuth: true }
  },
  {
    path: '/blog/post/:id',
    name: 'BlogPost',
    component: BlogPost,
    meta: { requiresAuth: true }
  },
  {
    path: '/blog/write',
    name: 'BlogWrite',
    component: BlogWrite,
    meta: { requiresAuth: true }
  },
  {
    path: '/blog/category/:name',
    name: 'BlogCategory',
    component: BlogCategory,
    meta: { requiresAuth: true }
  },
  {
    path: '/blog/search',
    name: 'BlogSearch',
    component: BlogSearch,
    meta: { requiresAuth: true }
  },
  {
    path: '/blog/profile',
    name: 'BlogProfile',
    component: BlogProfile,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const requiresAuth = to.matched.some(record => record.meta?.requiresAuth)
  const currentUser = userService.getCurrentUser()

  if (requiresAuth && !currentUser) {
    next('/login')
  } else if (to.path === '/login' && currentUser) {
    next('/editor')
  } else {
    next()
  }
})

export default router