<template>
  <div class="dashboard">
    <h2 class="page-title">工作台</h2>
    
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <div class="stat-card" style="border-left-color: #409eff;">
          <div class="stat-icon" style="background: #ecf5ff; color: #409eff;">
            <svg viewBox="0 0 1024 1024" width="32" height="32" fill="currentColor">
              <path d="M896 128H128c-35.3 0-64 28.7-64 64v640c0 35.3 28.7 64 64 64h768c35.3 0 64-28.7 64-64V192c0-35.3-28.7-64-64-64z m0 704H128V256h768v576z"/>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.dataSourceCount }}</div>
            <div class="stat-label">数据源</div>
          </div>
        </div>
      </el-col>
      
      <el-col :span="6">
        <div class="stat-card" style="border-left-color: #67c23a;">
          <div class="stat-icon" style="background: #f0f9ff; color: #67c23a;">
            <svg viewBox="0 0 1024 1024" width="32" height="32" fill="currentColor">
              <path d="M832 64H192c-17.7 0-32 14.3-32 32v832c0 17.7 14.3 32 32 32h640c17.7 0 32-14.3 32-32V96c0-17.7-14.3-32-32-32z m-40 824H232V136h560v752z"/>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.tableCount }}</div>
            <div class="stat-label">数据表</div>
          </div>
        </div>
      </el-col>
      
      <el-col :span="6">
        <div class="stat-card" style="border-left-color: #e6a23c;">
          <div class="stat-icon" style="background: #fdf6ec; color: #e6a23c;">
            <svg viewBox="0 0 1024 1024" width="32" height="32" fill="currentColor">
              <path d="M880 112H144c-17.7 0-32 14.3-32 32v736c0 17.7 14.3 32 32 32h736c17.7 0 32-14.3 32-32V144c0-17.7-14.3-32-32-32z m-40 728H184V184h656v656z"/>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.lineageCount }}</div>
            <div class="stat-label">血缘关系</div>
          </div>
        </div>
      </el-col>
      
      <el-col :span="6">
        <div class="stat-card" style="border-left-color: #f56c6c;">
          <div class="stat-icon" style="background: #fef0f0; color: #f56c6c;">
            <svg viewBox="0 0 1024 1024" width="32" height="32" fill="currentColor">
              <path d="M858.5 763.6c-18.9-44.8-46.1-85-80.6-119.5-34.5-34.5-74.7-61.6-119.5-80.6-0.4-0.2-0.8-0.3-1.2-0.5C719.5 518 760 444.7 760 362c0-137-111-248-248-248S264 225 264 362c0 82.7 40.5 156 102.8 201.1-0.4 0.2-0.8 0.3-1.2 0.5-44.8 18.9-85 46-119.5 80.6-34.5 34.5-61.6 74.7-80.6 119.5C146.9 807.5 137 854 136 901.8c-0.1 4.5 3.5 8.2 8 8.2h60c4.4 0 7.9-3.5 8-7.8 2-77.2 33-149.5 87.8-204.3 56.7-56.7 132-87.9 212.2-87.9s155.5 31.2 212.2 87.9C779 752.7 810 825 812 902.2c0.1 4.4 3.6 7.8 8 7.8h60c4.5 0 8.1-3.7 8-8.2-1-47.8-10.9-94.3-29.5-138.2z"/>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.userCount }}</div>
            <div class="stat-label">用户数</div>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <el-card class="welcome-card" shadow="never">
      <h3>欢迎使用数据血缘管理系统</h3>
      <p>本系统提供完整的数据血缘追踪和元数据管理功能，帮助您更好地理解和管理数据资产。</p>
      
      <div class="feature-list">
        <div class="feature-item">
          <span class="check-icon" style="color: #409eff;">✓</span>
          <span>支持多种数据源类型（MySQL、PostgreSQL、Hive等）</span>
        </div>
        <div class="feature-item">
          <span class="check-icon" style="color: #67c23a;">✓</span>
          <span>自动化元数据采集和管理</span>
        </div>
        <div class="feature-item">
          <span class="check-icon" style="color: #e6a23c;">✓</span>
          <span>可视化血缘关系图谱展示</span>
        </div>
        <div class="feature-item">
          <span class="check-icon" style="color: #f56c6c;">✓</span>
          <span>强大的搜索和过滤功能</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDashboardStats } from '@/api'

const stats = ref({
  dataSourceCount: 0,
  tableCount: 0,
  lineageCount: 0,
  userCount: 0
})

const loadStats = async () => {
  try {
    const data = await getDashboardStats()
    stats.value = data
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.dashboard {
  width: 100%;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 24px;
}

.stats-cards {
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  border-left: 4px solid;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #333;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

.welcome-card {
  border-radius: 12px;
}

.welcome-card h3 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.welcome-card p {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 24px;
}

.feature-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #666;
}

.check-icon {
  font-size: 24px;
  font-weight: bold;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
