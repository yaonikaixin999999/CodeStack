import axios from 'axios'

const API_BASE_URL = 'http://192.168.22.16:8080/api/auth'

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
}

interface CurrentUser {
    token: string
    username: string
}

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
        return token && username ? { token, username } : null
    },

    // 登出
    logout: (): void => {
        localStorage.removeItem('token')
        localStorage.removeItem('username')
    }
}