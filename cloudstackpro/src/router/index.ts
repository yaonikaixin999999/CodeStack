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
const BlogMessages = () => import('../views/blog/BlogMessages.vue')

// 管理后台页面
const AdminDashboard = () => import('../views/admin/AdminDashboard.vue')
const AdminModeration = () => import('../views/admin/AdminModeration.vue')
const AdminUserModeration = () => import('../views/admin/AdminUserModeration.vue')
const AdminBlog = () => import('../views/admin/AdminBlog.vue')
const AdminBlogPost = () => import('../views/admin/AdminBlogPost.vue')
const AdminBlogProfile = () => import('../views/admin/AdminBlogProfile.vue')
const AdminBlogSearch = () => import('../views/admin/AdminBlogSearch.vue')

// 导入用户服务
import { userService } from '@/services/userService'
import { blogService } from '@/services/blogService'

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
  // 管理后台
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: AdminDashboard,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/moderation',
    name: 'AdminModeration',
    component: AdminModeration,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/users',
    name: 'AdminUserModeration',
    component: AdminUserModeration,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/profile',
    name: 'AdminProfile',
    component: AdminBlogProfile,
    meta: { requiresAuth: true, requiresAdmin: true, isBlog: true }
  },
  {
    path: '/admin/blog',
    name: 'AdminBlogForward',
    component: AdminBlog,
    meta: { requiresAuth: true, requiresAdmin: true, isBlog: true }
  },
  {
    path: '/admin/blog/post/:id',
    name: 'AdminBlogPost',
    component: AdminBlogPost,
    meta: { requiresAuth: true, requiresAdmin: true, isBlog: true }
  },
  {
    path: '/admin/blog/profile/:id?',
    name: 'AdminBlogProfile',
    component: AdminBlogProfile,
    meta: { requiresAuth: true, requiresAdmin: true, isBlog: true }
  },
  {
    path: '/admin/blog/search',
    name: 'AdminBlogSearch',
    component: AdminBlogSearch,
    meta: { requiresAuth: true, requiresAdmin: true, isBlog: true }
  },
  // 博客系统路由
  {
    path: '/blog',
    name: 'BlogHome',
    component: BlogHome,
    meta: { requiresAuth: true, isBlog: true }
  },
  {
    path: '/blog/post/:id',
    name: 'BlogPost',
    component: BlogPost,
    meta: { requiresAuth: true, isBlog: true }
  },
  {
    path: '/blog/write',
    name: 'BlogWrite',
    component: BlogWrite,
    meta: { requiresAuth: true, isBlog: true }
  },
  {
    path: '/blog/category/:name',
    name: 'BlogCategory',
    component: BlogCategory,
    meta: { requiresAuth: true, isBlog: true }
  },
  // 支持不带 /blog 前缀的分类路由（从侧边栏跳转）
  {
    path: '/category/:name',
    name: 'CategoryRedirect',
    component: BlogCategory,
    meta: { requiresAuth: true, isBlog: true }
  },
  // 支持 /archive 路由
  {
    path: '/archive/:month',
    name: 'BlogArchive',
    component: BlogSearch,
    meta: { requiresAuth: true, isBlog: true }
  },
  {
    path: '/blog/search',
    name: 'BlogSearch',
    component: BlogSearch,
    meta: { requiresAuth: true, isBlog: true }
  },
  {
    path: '/blog/profile',
    name: 'BlogProfile',
    component: BlogProfile,
    meta: { requiresAuth: true, isBlog: true }
  },
  {
    path: '/blog/messages',
    name: 'BlogMessages',
    component: BlogMessages,
    meta: { requiresAuth: true, isBlog: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const requiresAuth = to.matched.some(record => record.meta?.requiresAuth)
  const isBlogRoute = to.matched.some(record => record.meta?.isBlog)
  const requiresAdmin = to.matched.some(record => (record.meta as any)?.requiresAdmin)

  // 检查博客系统登录状态
  const blogLoggedIn = blogService.auth.isLoggedIn()
  // 检查旧系统登录状态
  const currentUser = userService.getCurrentUser()
  const isAdminUser = !!currentUser?.isAdmin || currentUser?.role === 'admin'

  // 管理员不允许访问编辑器或普通博客页面，统一重定向到后台
  if (currentUser && isAdminUser && to.name === 'Editor') {
    next('/admin')
    return
  }
  if (currentUser && isAdminUser && isBlogRoute && !(to.meta as any)?.requiresAdmin) {
    next('/admin')
    return
  }

  // 已登录访问登录页时直接分流到对应入口
  if (to.path === '/login') {
    if (isAdminUser) {
      next('/admin')
      return
    }
    // if (blogLoggedIn) {
    //   next('/blog')
    //   return
    // }
    // if (currentUser) {
    //   next('/editor')
    //   return
    // }
  }

  if (requiresAuth) {
    if (isBlogRoute) {
      // 博客路由优先检查博客登录状态
      if (!blogLoggedIn && !currentUser) {
        next('/login')
        return
      }
    } else {
      // 非博客路由检查旧系统登录状态
      if (!currentUser) {
        next('/login')
        return
      }
    }
  }

  if (requiresAdmin) {
    if (!isAdminUser) {
      next('/login')
      return
    }
  }

  // 已登录用户访问登录页，重定向
  // if (to.path === '/login') {
  //   if (blogLoggedIn) {
  //     next('/blog')
  //     return
  //   } else if (currentUser) {
  //     next('/editor')
  //     return
  //   }
  // }

  next()
})

export default router