<template>
  <div class="lineage-page">
    <div class="page-header">
      <h2 class="page-title">血缘可视化</h2>
      <div class="header-actions">
        <el-input 
          v-model="searchText" 
          placeholder="🔍 输入表名搜索血缘关系" 
          style="width: 300px"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch" :loading="searching">搜索</el-button>
          </template>
        </el-input>
      </div>
    </div>
    
    <el-card class="graph-card" shadow="never">
      <div v-if="!graphData.nodes.length" class="empty-state">
        <el-empty description="请搜索表名查看血缘关系图谱">
          <div style="font-size: 80px; color: #dcdfe6;">🔗</div>
        </el-empty>
      </div>
      
      <div v-else class="graph-container">
        <v-chart 
          ref="chartRef"
          class="chart"
          :option="chartOption"
          autoresize
        />
        
        <div class="graph-legend">
          <div class="legend-item">
            <div class="legend-color" style="background: #5470c6;"></div>
            <span>数据表节点</span>
          </div>
          <div class="legend-item">
            <div class="legend-color" style="background: #91cc75;"></div>
            <span>血缘关系</span>
          </div>
        </div>
      </div>
    </el-card>
    
    <el-card class="lineage-list-card" shadow="never" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>血缘关系列表</span>
          <el-button type="primary" size="small" @click="showDialog = true">
            ➕ 新建血缘关系
          </el-button>
        </div>
      </template>
      
      <el-table :data="lineageList" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="源表" min-width="150">
          <template #default="{ row }">
            {{ getTableName(row.sourceTableId) }}
          </template>
        </el-table-column>
        <el-table-column label="目标表" min-width="150">
          <template #default="{ row }">
            {{ getTableName(row.targetTableId) }}
          </template>
        </el-table-column>
        <el-table-column prop="lineageType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ row.lineageType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="transformRule" label="转换规则" min-width="200" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 新建血缘关系对话框 -->
    <el-dialog v-model="showDialog" title="新建血缘关系" width="600px">
      <el-form :model="formData" ref="formRef" label-width="100px">
        <el-form-item label="源表" prop="sourceTableId">
          <el-select v-model="formData.sourceTableId" placeholder="请选择源表" style="width: 100%">
            <el-option 
              v-for="table in allTables" 
              :key="table.id" 
              :label="table.tableName" 
              :value="table.id" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="目标表" prop="targetTableId">
          <el-select v-model="formData.targetTableId" placeholder="请选择目标表" style="width: 100%">
            <el-option 
              v-for="table in allTables" 
              :key="table.id" 
              :label="table.tableName" 
              :value="table.id" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="血缘类型" prop="lineageType">
          <el-select v-model="formData.lineageType" style="width: 100%">
            <el-option label="表级血缘" value="TABLE" />
            <el-option label="列级血缘" value="COLUMN" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="转换规则">
          <el-input v-model="formData.transformRule" type="textarea" :rows="3" placeholder="请输入转换规则" />
        </el-form-item>
        
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="2" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { GraphChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { getAllLineages, createLineage, deleteLineage, searchLineage, getAllTables } from '@/api'

use([CanvasRenderer, GraphChart, TitleComponent, TooltipComponent, LegendComponent])

const loading = ref(false)
const searching = ref(false)
const showDialog = ref(false)
const searchText = ref('')
const chartRef = ref()

const graphData = ref({
  nodes: [],
  edges: []
})

const lineageList = ref([])
const allTables = ref([])

const formData = reactive({
  sourceTableId: null,
  targetTableId: null,
  lineageType: 'TABLE',
  transformRule: '',
  description: ''
})

const chartOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    formatter: (params) => {
      if (params.dataType === 'node') {
        return `<b>${params.data.name}</b><br/>数据源: ${params.data.sourceName || '-'}`
      } else {
        return `${params.data.label || '血缘关系'}`
      }
    }
  },
  series: [{
    type: 'graph',
    layout: 'force',
    data: graphData.value.nodes.map(node => ({
      id: node.id,
      name: node.label,
      sourceName: node.sourceName,
      symbolSize: 50,
      itemStyle: {
        color: '#5470c6'
      },
      label: {
        show: true,
        position: 'bottom',
        fontSize: 12
      }
    })),
    edges: graphData.value.edges.map(edge => ({
      source: edge.source,
      target: edge.target,
      label: {
        show: true,
        formatter: edge.label || '',
        fontSize: 10
      },
      lineStyle: {
        color: '#91cc75',
        curveness: 0.2,
        width: 2
      }
    })),
    roam: true,
    force: {
      repulsion: 300,
      edgeLength: 150
    },
    emphasis: {
      focus: 'adjacency',
      lineStyle: {
        width: 4
      }
    }
  }]
}))

const tableNameMap = computed(() => {
  const map = {}
  allTables.value.forEach(table => {
    map[table.id] = table.tableName
  })
  return map
})

const getTableName = (tableId) => {
  return tableNameMap.value[tableId] || `表${tableId}`
}

const loadLineages = async () => {
  loading.value = true
  try {
    lineageList.value = await getAllLineages()
  } catch (error) {
    console.error('加载血缘关系失败:', error)
  } finally {
    loading.value = false
  }
}

const loadTables = async () => {
  try {
    allTables.value = await getAllTables()
  } catch (error) {
    console.error('加载数据表失败:', error)
  }
}

const handleSearch = async () => {
  if (!searchText.value.trim()) {
    ElMessage.warning('请输入表名')
    return
  }
  
  searching.value = true
  try {
    graphData.value = await searchLineage(searchText.value)
    if (!graphData.value.nodes.length) {
      ElMessage.info('未找到相关血缘关系')
    }
  } catch (error) {
    console.error('搜索失败:', error)
  } finally {
    searching.value = false
  }
}

const handleSave = async () => {
  try {
    await createLineage(formData)
    ElMessage.success('创建成功')
    showDialog.value = false
    loadLineages()
    Object.assign(formData, {
      sourceTableId: null,
      targetTableId: null,
      lineageType: 'TABLE',
      transformRule: '',
      description: ''
    })
  } catch (error) {
    console.error('保存失败:', error)
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除此血缘关系吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteLineage(id)
    ElMessage.success('删除成功')
    loadLineages()
  })
}

onMounted(() => {
  loadLineages()
  loadTables()
})
</script>

<style scoped>
.lineage-page {
  width: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.graph-card {
  border-radius: 12px;
}

.graph-container {
  position: relative;
  height: 600px;
}

.chart {
  width: 100%;
  height: 100%;
}

.empty-state {
  height: 600px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.graph-legend {
  position: absolute;
  top: 20px;
  right: 20px;
  background: rgba(255, 255, 255, 0.9);
  padding: 12px 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 14px;
}

.legend-item:last-child {
  margin-bottom: 0;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 4px;
}

.lineage-list-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
