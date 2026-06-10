import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 创建 axios 实例
const request = axios.create({
    baseURL: '/api',
    timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
    config => {
        const userStore = useUserStore()
        if (userStore.token) {
            config.headers.Authorization = `Bearer ${userStore.token}`
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 响应拦截器
request.interceptors.response.use(
    response => {
        const res = response.data

        if (res.code !== 200) {
            ElMessage.error(res.message || '请求失败')
            return Promise.reject(new Error(res.message || '请求失败'))
        }

        return res.data
    },
    error => {
        if (error.response) {
            if (error.response.status === 401) {
                ElMessage.error('未登录或登录已过期')
                const userStore = useUserStore()
                userStore.logout()
                router.push('/login')
            } else {
                ElMessage.error(error.response.data.message || '请求失败')
            }
        } else {
            ElMessage.error('网络错误，请稍后重试')
        }
        return Promise.reject(error)
    }
)

export default request
