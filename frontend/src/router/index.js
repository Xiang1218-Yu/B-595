import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/Login.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/Register.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/',
        component: () => import('@/layout/MainLayout.vue'),
        redirect: '/dashboard',
        meta: { requiresAuth: true },
        children: [
            {
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('@/views/Dashboard.vue'),
                meta: { title: '工作台' }
            },
            {
                path: 'datasources',
                name: 'DataSources',
                component: () => import('@/views/DataSources.vue'),
                meta: { title: '数据源管理' }
            },
            {
                path: 'metadata',
                name: 'Metadata',
                component: () => import('@/views/Metadata.vue'),
                meta: { title: '元数据管理' }
            },
            {
                path: 'lineage',
                name: 'Lineage',
                component: () => import('@/views/Lineage.vue'),
                meta: { title: '血缘可视化' }
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
    const userStore = useUserStore()

    if (to.meta.requiresAuth && !userStore.token) {
        next('/login')
    } else if (to.path === '/login' && userStore.token) {
        next('/')
    } else {
        next()
    }
})

export default router
