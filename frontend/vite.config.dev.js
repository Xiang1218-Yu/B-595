import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// 开发环境配置
export default defineConfig({
    plugins: [vue()],
    resolve: {
        alias: {
            '@': path.resolve(__dirname, 'src')
        }
    },
    server: {
        host: '0.0.0.0',  // 允许Docker容器外部访问
        port: 3000,
        strictPort: true,
        watch: {
            usePolling: true  // Docker中需要启用轮询
        },
        proxy: {
            '/api': {
                target: 'http://backend:8080',
                changeOrigin: true,
                pathRewrite: { '^/api': '' } // 有时后端 API 不需要 /api 前缀，但我发现后端代码是有 /api 的，如果是这样则不需要这行
            }
        }
    }
})
