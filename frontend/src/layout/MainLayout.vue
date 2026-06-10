<template>
  <el-container class="main-layout">
    <el-aside width="240px" class="sidebar">
      <div class="logo">
        <h2>数据血缘系统</h2>
      </div>
      
      <el-menu
        :default-active="$route.path"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/dashboard">
          <span class="menu-icon">📊</span>
          <span>工作台</span>
        </el-menu-item>
        
        <el-menu-item index="/datasources">
          <span class="menu-icon">🗄️</span>
          <span>数据源管理</span>
        </el-menu-item>
        
        <el-menu-item index="/metadata">
          <span class="menu-icon">📝</span>
          <span>元数据管理</span>
        </el-menu-item>
        
        <el-menu-item index="/lineage">
          <span class="menu-icon">🔗</span>
          <span>血缘可视化</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <h3>{{ $route.meta.title || '数据血缘管理系统' }}</h3>
        </div>
        
        <div class="header-right">
          <span class="username">{{ userStore.userInfo.username }}</span>
          <el-dropdown @command="handleCommand">
            <div class="user-avatar">
              <span>👤</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      router.push('/login')
    })
  }
}
</script>

<style scoped>
.main-layout {
  height: 100vh;
}

.sidebar {
  background: linear-gradient(180deg, #1a1f3a 0%, #2d3561 100%);
  color: #fff;
  overflow-x: hidden;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo h2 {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.sidebar-menu {
  background: transparent;
  border: none;
  margin-top: 20px;
}

.menu-icon {
  font-size: 18px;
  margin-right: 8px;
}

:deep(.sidebar-menu .el-menu-item) {
  color: rgba(255, 255, 255, 0.7);
  margin: 4px 12px;
  border-radius: 8px;
  transition: all 0.3s;
}

:deep(.sidebar-menu .el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

:deep(.sidebar-menu .el-menu-item.is-active) {
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e6e8eb;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.header-left h3 {
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.username {
  font-size: 14px;
  color: #666;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  cursor: pointer;
  transition: transform 0.3s;
}

.user-avatar:hover {
  transform: scale(1.1);
}

.main-content {
  background: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}
</style>
