import axios from 'axios'

// 动态获取 API 地址
const getApiBaseUrl = () => {
    const hostname = window.location.hostname
    if (hostname === 'localhost' || hostname === '127.0.0.1') {
        return 'http://localhost:8082/api/auth'
    }
    return `http://${hostname}:8082/api/auth`
}

const API_BASE_URL = getApiBaseUrl()

// 定义接口类型
interface LoginForm {
    username: string
    password: string
}

interface RegisterForm {
    username: string
    password: string
    phone: string
}

interface ApiResponse<T> {
    success: boolean
    message: string
    data: T
}

interface AuthData {
    token: string
    username: string
    userId?: number
    isAdmin?: boolean
    role?: string
    avatar?: string
}

interface CurrentUser extends AuthData { }

// 创建axios实例
const api = axios.create({
    baseURL: API_BASE_URL,
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
})

// 请求拦截器
api.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

export const userService = {
    // 登录
    login: async (loginForm: LoginForm): Promise<ApiResponse<AuthData>> => {
        try {
            const response = await api.post('/login', loginForm)
            const data: ApiResponse<AuthData> = response.data
            if (data.success && data.data) {
                localStorage.setItem('token', data.data.token)
                localStorage.setItem('username', data.data.username)
                if (data.data.userId) localStorage.setItem('userId', String(data.data.userId))
                if (data.data.avatar) localStorage.setItem('avatar', data.data.avatar)
                if (data.data.isAdmin) localStorage.setItem('isAdmin', 'true')
                if (data.data.role) localStorage.setItem('role', data.data.role)
            }
            return data
        } catch (error: any) {
            if (error.response?.status === 401) {
                localStorage.removeItem('token')
                localStorage.removeItem('username')
                window.location.href = '/login'
            }
            throw new Error(error.response?.data?.message || '登录失败')
        }
    },

    // 注册
    register: async (registerForm: RegisterForm): Promise<ApiResponse<AuthData>> => {
        try {
            const response = await api.post('/register', registerForm)
            const data: ApiResponse<AuthData> = response.data
            return data
        } catch (error: any) {
            if (error.response?.status === 401) {
                localStorage.removeItem('token')
                localStorage.removeItem('username')
                window.location.href = '/login'
            }
            throw new Error(error.response?.data?.message || '注册失败')
        }
    },

    // 获取当前用户信息
    getCurrentUser: (): CurrentUser | null => {
        const token = localStorage.getItem('token')
        const username = localStorage.getItem('username')
        const userId = localStorage.getItem('userId')
        const isAdmin = localStorage.getItem('isAdmin') === 'true'
        const role = localStorage.getItem('role') || undefined
        const avatar = localStorage.getItem('avatar') || undefined
        return token && username ? { token, username, userId: userId ? Number(userId) : undefined, isAdmin, role, avatar } : null
    },

    // 登出
    logout: (): void => {
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        localStorage.removeItem('userId')
        localStorage.removeItem('isAdmin')
        localStorage.removeItem('role')
        localStorage.removeItem('avatar')
    }
}
