<template>
  <div class="metadata-page">
    <div class="page-header">
      <h2 class="page-title">元数据管理</h2>
      <div class="header-actions">
        <el-input 
          v-model="searchKeyword" 
          placeholder="🔍 搜索表名" 
          style="width: 300px"
          @input="handleSearch"
        />
        <el-button type="primary" @click="showDialog = true">
          ➕ 新建数据表
        </el-button>
      </div>
    </div>
    
    <el-table :data="tableList" style="width: 100%" v-loading="loading">
      <el-table-column type="expand">
        <template #default="{ row }">
          <div class="expand-content">
            <h4>列信息</h4>
            <el-table :data="row.columns" size="small">
              <el-table-column prop="columnName" label="列名" />
              <el-table-column prop="dataType" label="数据类型" />
              <el-table-column prop="columnComment" label="注释" />
              <el-table-column prop="isPrimaryKey" label="主键" width="80">
                <template #default="{ row: col }">
                  <el-tag v-if="col.isPrimaryKey" type="success" size="small">是</el-tag>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column prop="isNullable" label="可空" width="80">
                <template #default="{ row: col }">
                  {{ col.isNullable ? '是' : '否' }}
                </template>
              </el-table-column>
            </el-table>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="tableName" label="表名" min-width="180" />
      <el-table-column prop="tableComment" label="表注释" min-width="200" />
      <el-table-column prop="tableType" label="类型" width="100">
        <template #default="{ row }">
          <el-tag>{{ row.tableType || 'TABLE' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="columnCount" label="列数" width="100" />
      <el-table-column prop="rowCount" label="行数" width="120">
        <template #default="{ row }">
          {{ row.rowCount ? row.rowCount.toLocaleString() : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleViewDetail(row)">详情</el-button>
          <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 新建/编辑对话框 -->
    <el-dialog 
      v-model="showDialog" 
      :title="editingId ? '编辑数据表' : '新建数据表'"
      width="600px"
    >
      <el-form :model="formData" ref="formRef" label-width="100px">
        <el-form-item label="数据源" prop="sourceId">
          <el-select v-model="formData.sourceId" placeholder="请选择数据源" style="width: 100%">
            <el-option 
              v-for="ds in dataSources" 
              :key="ds.id" 
              :label="ds.name" 
              :value="ds.id" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="表名" prop="tableName">
          <el-input v-model="formData.tableName" placeholder="请输入表名" />
        </el-form-item>
        
        <el-form-item label="表注释">
          <el-input v-model="formData.tableComment" placeholder="请输入表注释" />
        </el-form-item>
        
        <el-form-item label="表类型">
          <el-select v-model="formData.tableType" style="width: 100%">
            <el-option label="TABLE" value="TABLE" />
            <el-option label="VIEW" value="VIEW" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="表详情" width="800px">
      <el-descriptions :column="2" border v-if="currentDetail.table">
        <el-descriptions-item label="表名">{{ currentDetail.table.tableName }}</el-descriptions-item>
        <el-descriptions-item label="表注释">{{ currentDetail.table.tableComment }}</el-descriptions-item>
        <el-descriptions-item label="表类型">{{ currentDetail.table.tableType }}</el-descriptions-item>
        <el-descriptions-item label="列数">{{ currentDetail.table.columnCount }}</el-descriptions-item>
        <el-descriptions-item label="行数">{{ currentDetail.table.rowCount }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentDetail.table.createTime }}</el-descriptions-item>
      </el-descriptions>
      
      <h4 style="margin-top: 20px; margin-bottom: 10px;">列信息</h4>
      <el-table :data="currentDetail.columns" border>
        <el-table-column prop="columnName" label="列名" />
        <el-table-column prop="dataType" label="数据类型" />
        <el-table-column prop="columnLength" label="长度" width="80" />
        <el-table-column prop="columnComment" label="注释" />
        <el-table-column prop="isPrimaryKey" label="主键" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isPrimaryKey" type="success" size="small">是</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="isNullable" label="可空" width="80">
          <template #default="{ row }">
            {{ row.isNullable ? '是' : '否' }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllTables, searchTables, getTableDetail, createTable, updateTable, deleteTable, getDataSources } from '@/api'

const loading = ref(false)
const showDialog = ref(false)
const showDetailDialog = ref(false)
const formRef = ref()
const editingId = ref(null)
const searchKeyword = ref('')
const tableList = ref([])
const dataSources = ref([])
const currentDetail = ref({ table: null, columns: [] })

const formData = reactive({
  sourceId: null,
  tableName: '',
  tableComment: '',
  tableType: 'TABLE',
  description: ''
})

const loadTables = async () => {
  loading.value = true
  try {
    const data = await getAllTables()
    // 为每个表加载列信息
    for (const table of data) {
      const detail = await getTableDetail(table.id)
      table.columns = detail.columns || []
    }
    tableList.value = data
  } catch (error) {
    console.error('加载数据表失败:', error)
  } finally {
    loading.value = false
  }
}

const loadDataSources = async () => {
  try {
    dataSources.value = await getDataSources()
  } catch (error) {
    console.error('加载数据源失败:', error)
  }
}

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    loadTables()
    return
  }
  
  loading.value = true
  try {
    const data = await searchTables(searchKeyword.value)
    for (const table of data) {
      const detail = await getTableDetail(table.id)
      table.columns = detail.columns || []
    }
    tableList.value = data
  } catch (error) {
    console.error('搜索失败:', error)
  } finally {
    loading.value = false
  }
}

const handleViewDetail = async (row) => {
  try {
    currentDetail.value = await getTableDetail(row.id)
    showDetailDialog.value = true
  } catch (error) {
    console.error('加载详情失败:', error)
  }
}

const handleEdit = (row) => {
  editingId.value = row.id
  Object.assign(formData, row)
  showDialog.value = true
}

const handleSave = async () => {
  try {
    if (editingId.value) {
      await updateTable(editingId.value, formData)
      ElMessage.success('更新成功')
    } else {
      await createTable(formData)
      ElMessage.success('创建成功')
    }
    
    showDialog.value = false
    loadTables()
    resetForm()
  } catch (error) {
    console.error('保存失败:', error)
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除此数据表吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteTable(id)
    ElMessage.success('删除成功')
    loadTables()
  })
}

const resetForm = () => {
  editingId.value = null
  Object.assign(formData, {
    sourceId: null,
    tableName: '',
    tableComment: '',
    tableType: 'TABLE',
    description: ''
  })
}

onMounted(() => {
  loadTables()
  loadDataSources()
})
</script>

<style scoped>
.metadata-page {
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

.header-actions {
  display: flex;
  gap: 12px;
}

.el-table {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.expand-content {
  padding: 12px 48px;
}

.expand-content h4 {
  margin-bottom: 12px;
  font-size: 14px;
  color: #333;
}
</style>
