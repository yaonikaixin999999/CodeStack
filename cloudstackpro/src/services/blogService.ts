import axios from 'axios'

// API 基础配置
const API_BASE_URL = 'http://localhost:8082/api'

// 创建 axios 实例
const api = axios.create({
    baseURL: API_BASE_URL,
    timeout: 15000,
    headers: {
        'Content-Type': 'application/json'
    }
})

// 请求拦截器 - 自动添加 token
api.interceptors.request.use(
    config => {
        const token = localStorage.getItem('blog_token') || localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    error => Promise.reject(error)
)

// 响应拦截器 - 统一处理错误
api.interceptors.response.use(
    response => response.data,
    error => {
        if (error.response?.status === 401) {
            // token 过期，清除登录状态
            localStorage.removeItem('blog_token')
            localStorage.removeItem('blog_user')
            window.location.href = '/login'
        }
        const message = error.response?.data?.message || '请求失败'
        return Promise.reject(new Error(message))
    }
)

// ============== 类型定义 ==============

// 通用响应
export interface ApiResponse<T> {
    success: boolean
    message: string
    data: T
}

// 分页响应
export interface PageResponse<T> {
    content: T[]
    page: number
    size: number
    totalElements: number
    totalPages: number
    last?: boolean
    first?: boolean
}

// 用户相关
export interface User {
    id: number
    username: string
    nickname: string
    avatar: string
    bio: string
    email: string
    phone: string
    website: string
    location: string
    company: string
    github: string
    level: number
    experience: number
    role: string
    isAdmin: boolean
    postCount: number
    followerCount: number
    followingCount: number
    likeReceivedCount: number
    createdAt: string
    followed?: boolean
    bookmarkCount?: number
    viewCount?: number
    likeCount?: number
    profession?: string
}

export interface LoginRequest {
    username: string
    password: string
}

export interface RegisterRequest {
    username: string
    password: string
    email?: string
    phone?: string
}

export interface LoginResponse {
    token: string
    user: User
}

// 文章相关
export interface Post {
    id: number
    title: string
    slug: string
    excerpt: string
    content: string
    contentHtml: string
    coverImage: string
    wordCount: number
    readTime: number
    viewCount: number
    likeCount: number
    commentCount: number
    bookmarkCount: number
    shareCount: number
    isTop: boolean
    isFeatured: boolean
    isOriginal: boolean
    sourceUrl: string
    allowComment: boolean
    status: number
    publishedAt: string
    createdAt: string
    updatedAt: string
    author: {
        id: number
        username: string
        nickname: string
        avatar: string
    }
    category: {
        id: number
        name: string
        slug: string
    }
    tags: Array<{
        id: number
        name: string
        slug: string
        color: string
    }>
    liked: boolean
    bookmarked: boolean
}

export interface PostListItem {
    id: number
    title: string
    slug: string
    excerpt: string
    coverImage: string
    viewCount: number
    likeCount: number
    commentCount: number
    bookmarkCount?: number
    isTop: boolean
    isFeatured: boolean
    publishedAt: string
    createdAt?: string
    status?: number
    content?: string
    liked?: boolean
    bookmarked?: boolean
    author: {
        id: number
        username: string
        nickname: string
        avatar: string
    }
    category: {
        id: number
        name: string
    }
    tags: Array<{
        id: number
        name: string
        color: string
    }>
}

export interface PostRequest {
    title: string
    excerpt?: string
    content: string
    coverImage?: string
    categoryId?: number
    tagIds?: number[]
    tags?: string[]
    newTags?: string[]
    isOriginal?: boolean
    sourceUrl?: string
    allowComment?: boolean
    status?: number | string // 0: 草稿, 1: 发布 或 'DRAFT'/'PUBLISHED'
}

// 分类相关
export interface Category {
    id: number
    name: string
    slug: string
    description: string
    icon: string
    coverImage: string
    postCount: number
    sortOrder: number
    viewCount?: number
    children?: Category[]
}

// 标签相关
export interface Tag {
    id: number
    name: string
    slug: string
    description: string
    color: string
    postCount: number
}

// 评论相关
export interface Comment {
    id: number
    content: string
    likeCount: number
    isTop: boolean
    createdAt: string
    author: {
        id: number
        username: string
        nickname: string
        avatar: string
    }
    replyTo?: {
        id: number
        username: string
        nickname: string
    }
    replies?: Comment[]
    liked: boolean
}

export interface CommentRequest {
    postId: number
    content: string
    parentId?: number
    replyToUserId?: number
}

// 收藏相关
export interface Bookmark {
    id: number
    post: PostListItem
    createdAt: string
}

// 通知相关
export interface Notification {
    id: number
    type: string
    title: string
    content: string
    targetId: number
    targetType: string
    isRead: boolean
    createdAt: string
    sender?: {
        id: number
        username: string
        nickname: string
        avatar: string
    }
}

// 私信消息相关
export interface Conversation {
    otherUserId: number
    lastMessageContent: string
    lastMessageAt: string
    otherUser: {
        id: number
        username: string
        nickname: string
        avatar: string
    }
}

export interface Message {
    id: number
    content: string
    createdAt: string
    isMine: boolean
    sender: {
        id: number
        username: string
        nickname: string
        avatar: string
    }
}

// 搜索到的用户
export interface SearchUser {
    id: number
    username: string
    nickname: string
    avatar: string
}

// ============== API 服务 ==============

export const blogService = {
    // ========== 认证相关 ==========
    auth: {
        // 登录
        login: async (data: LoginRequest): Promise<ApiResponse<LoginResponse>> => {
            return api.post('/blog/auth/login', data)
        },

        // 注册
        register: async (data: RegisterRequest): Promise<ApiResponse<LoginResponse>> => {
            return api.post('/blog/auth/register', data)
        },

        // 获取当前用户信息
        getCurrentUser: async (): Promise<ApiResponse<User>> => {
            return api.get('/blog/auth/me')
        },

        // 更新用户资料
        updateProfile: async (data: Partial<User>): Promise<ApiResponse<User>> => {
            return api.put('/blog/auth/profile', data)
        },

        // 登出
        logout: () => {
            localStorage.removeItem('blog_token')
            localStorage.removeItem('blog_user')
        },

        // 检查是否已登录
        isLoggedIn: (): boolean => {
            return !!localStorage.getItem('blog_token')
        },

        // 获取本地存储的用户信息
        getLocalUser: (): User | null => {
            const userStr = localStorage.getItem('blog_user')
            return userStr ? JSON.parse(userStr) : null
        },

        // 保存登录信息
        saveLoginInfo: (token: string, user: User) => {
            localStorage.setItem('blog_token', token)
            localStorage.setItem('blog_user', JSON.stringify(user))
        }
    },

    // ========== 文章相关 ==========
    posts: {
        // 获取文章列表
        getList: async (params?: {
            page?: number
            size?: number
            categoryId?: number
            tagId?: number
            authorId?: number
            keyword?: string
            sort?: string
        }): Promise<ApiResponse<PageResponse<PostListItem>>> => {
            return api.get('/posts', { params })
        },

        // 获取文章详情
        getById: async (id: number): Promise<ApiResponse<Post>> => {
            return api.get(`/posts/${id}`)
        },

        // 创建文章
        create: async (data: PostRequest): Promise<ApiResponse<Post>> => {
            return api.post('/posts', data)
        },

        // 更新文章
        update: async (id: number, data: PostRequest): Promise<ApiResponse<Post>> => {
            return api.put(`/posts/${id}`, data)
        },

        // 删除文章
        delete: async (id: number): Promise<ApiResponse<void>> => {
            return api.delete(`/posts/${id}`)
        },

        // 获取热门文章
        getHot: async (limit?: number): Promise<ApiResponse<PostListItem[]>> => {
            return api.get('/posts/hot', { params: { limit } })
        },

        // 获取推荐文章
        getFeatured: async (limit?: number): Promise<ApiResponse<PostListItem[]>> => {
            return api.get('/posts/featured', { params: { limit } })
        },

        // 获取用户的文章
        getByUser: async (userId: number, params?: {
            page?: number
            size?: number
        }): Promise<ApiResponse<PageResponse<PostListItem>>> => {
            return api.get(`/posts/user/${userId}`, { params })
        },

        // 搜索文章
        search: async (keyword: string, params?: {
            page?: number
            size?: number
        }): Promise<ApiResponse<PageResponse<PostListItem>>> => {
            return api.get('/posts/search', { params: { keyword, ...params } })
        },

        globalSearch: async (keyword: string, params?: {
            page?: number
            size?: number
        }): Promise<ApiResponse<{ posts: PageResponse<PostListItem>; users: PageResponse<User> }>> => {
            return api.get('/posts/global-search', { params: { keyword, ...params } })
        },

        // 点赞文章
        like: async (id: number): Promise<ApiResponse<void>> => {
            return api.post(`/posts/${id}/like`)
        },

        // 取消点赞
        unlike: async (id: number): Promise<ApiResponse<void>> => {
            return api.delete(`/posts/${id}/like`)
        },

        // 收藏文章
        bookmark: async (id: number): Promise<ApiResponse<void>> => {
            return api.post(`/posts/${id}/bookmark`)
        },

        // 取消收藏
        unbookmark: async (id: number): Promise<ApiResponse<void>> => {
            return api.delete(`/posts/${id}/bookmark`)
        }
    },

    // ========== 分类相关 ==========
    categories: {
        // 获取所有分类（树形结构）
        getAll: async (): Promise<ApiResponse<Category[]>> => {
            return api.get('/categories')
        },

        // 获取分类详情
        getById: async (id: number): Promise<ApiResponse<Category>> => {
            return api.get(`/categories/${id}`)
        },

        // 获取分类下的文章
        getPosts: async (id: number | string, page?: number, size?: number, sort?: string): Promise<ApiResponse<PageResponse<PostListItem>>> => {
            return api.get(`/categories/${id}/posts`, { params: { page, size, sort } })
        }
    },

    // ========== 标签相关 ==========
    tags: {
        // 获取所有标签
        getAll: async (): Promise<ApiResponse<Tag[]>> => {
            return api.get('/tags')
        },

        // 获取热门标签
        getHot: async (limit?: number): Promise<ApiResponse<Tag[]>> => {
            return api.get('/tags/hot', { params: { limit } })
        },

        // 搜索标签
        search: async (keyword: string): Promise<ApiResponse<Tag[]>> => {
            return api.get('/tags/search', { params: { keyword } })
        },

        // 获取标签下的文章
        getPosts: async (idOrName: number | string, page?: number, size?: number): Promise<ApiResponse<PageResponse<PostListItem>>> => {
            return api.get(`/tags/${idOrName}/posts`, { params: { page, size } })
        }
    },

    // ========== 评论相关 ==========
    comments: {
        // 获取文章评论
        getByPost: async (postId: number, params?: {
            page?: number
            size?: number
        }): Promise<ApiResponse<PageResponse<Comment>>> => {
            return api.get(`/comments/post/${postId}`, { params })
        },

        // 创建评论
        create: async (data: CommentRequest): Promise<ApiResponse<Comment>> => {
            return api.post('/comments', data)
        },

        // 删除评论
        delete: async (id: number): Promise<ApiResponse<void>> => {
            return api.delete(`/comments/${id}`)
        },

        // 点赞评论
        like: async (id: number): Promise<ApiResponse<void>> => {
            return api.post(`/comments/${id}/like`)
        },

        // 取消点赞评论
        unlike: async (id: number): Promise<ApiResponse<void>> => {
            return api.delete(`/comments/${id}/like`)
        }
    },

    // ========== 通知相关 ==========
    notifications: {
        // 获取通知列表
        getList: async (params?: {
            type?: string
            page?: number
            size?: number
        }): Promise<ApiResponse<PageResponse<Notification>>> => {
            return api.get('/notifications', { params })
        },

        // 获取未读数量
        getUnreadCount: async (): Promise<ApiResponse<number>> => {
            return api.get('/notifications/unread-count')
        },

        // 标记为已读
        markAsRead: async (id: number): Promise<ApiResponse<void>> => {
            return api.put(`/notifications/${id}/read`)
        },

        // 全部标记为已读
        markAllAsRead: async (): Promise<ApiResponse<void>> => {
            return api.put('/notifications/read-all')
        }
    },

    // ========== 收藏相关 ==========
    bookmarks: {
        // 获取收藏列表
        getList: async (params?: {
            page?: number
            size?: number
        }): Promise<ApiResponse<PageResponse<Bookmark>>> => {
            return api.get('/bookmarks', { params })
        },

        // 检查是否已收藏
        check: async (postId: number): Promise<ApiResponse<boolean>> => {
            return api.get(`/bookmarks/check/${postId}`)
        }
    },

    // ========== 用户相关 ==========
    users: {
        // 获取用户信息
        getById: async (id: number): Promise<ApiResponse<User>> => {
            return api.get(`/users/${id}`)
        },

        // 关注用户
        follow: async (id: number): Promise<ApiResponse<void>> => {
            return api.post(`/users/${id}/follow`)
        },

        // 取消关注
        unfollow: async (id: number): Promise<ApiResponse<void>> => {
            return api.delete(`/users/${id}/follow`)
        },

        // 获取关注列表
        getFollowing: async (userId: number, params?: {
            page?: number
            size?: number
        }): Promise<ApiResponse<PageResponse<User>>> => {
            return api.get(`/users/${userId}/following`, { params })
        },

        // 获取粉丝列表
        getFollowers: async (userId: number, params?: {
            page?: number
            size?: number
        }): Promise<ApiResponse<PageResponse<User>>> => {
            return api.get(`/users/${userId}/followers`, { params })
        }
    },

    // ========== 私信消息相关 ==========
    messages: {
        // 发送私信
        send: async (data: {
            receiverId: number
            content: string
        }): Promise<ApiResponse<Message>> => {
            return api.post('/messages', data)
        },

        // 获取会话列表
        getConversations: async (): Promise<ApiResponse<Conversation[]>> => {
            return api.get('/messages/conversations')
        },

        // 获取与指定用户的消息列表
        getMessages: async (otherUserId: number): Promise<ApiResponse<Message[]>> => {
            return api.get(`/messages/user/${otherUserId}`)
        },

        // 获取与指定用户的消息列表（分页）
        getMessagesPaged: async (otherUserId: number, params?: {
            page?: number
            size?: number
        }): Promise<ApiResponse<PageResponse<Message>>> => {
            return api.get(`/messages/user/${otherUserId}/paged`, { params })
        },

        // 删除与指定用户的会话
        deleteConversation: async (otherUserId: number): Promise<ApiResponse<void>> => {
            return api.delete(`/messages/user/${otherUserId}`)
        },

        // 删除单条消息
        deleteMessage: async (messageId: number): Promise<ApiResponse<void>> => {
            return api.delete(`/messages/${messageId}`)
        },

        // 搜索用户（用于发起新私信）
        searchUsers: async (keyword: string): Promise<ApiResponse<SearchUser[]>> => {
            return api.get('/messages/search-users', { params: { keyword } })
        }
    }
}

export default blogService
