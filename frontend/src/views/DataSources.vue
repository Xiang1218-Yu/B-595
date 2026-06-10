<template>
  <div class="datasources-page">
    <div class="page-header">
      <h2 class="page-title">数据源管理</h2>
      <el-button type="primary" @click="showDialog = true">
        ➕ 新建数据源
      </el-button>
    </div>
    
    <el-table :data="dataSourceList" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="数据源名称" min-width="150" />
      <el-table-column prop="type" label="类型" width="120">
        <template #default="{ row }">
          <el-tag>{{ row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="host" label="主机地址" min-width="180" />
      <el-table-column prop="port" label="端口" width="100" />
      <el-table-column prop="database" label="数据库" min-width="150" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="success" size="small" @click="handleTest(row.id)">测试</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 新建/编辑对话框 -->
    <el-dialog 
      v-model="showDialog" 
      :title="editingId ? '编辑数据源' : '新建数据源'"
      width="600px"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="数据源名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入数据源名称" />
        </el-form-item>
        
        <el-form-item label="类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="MySQL" value="MySQL" />
            <el-option label="PostgreSQL" value="PostgreSQL" />
            <el-option label="Oracle" value="Oracle" />
            <el-option label="Hive" value="Hive" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="主机地址" prop="host">
          <el-input v-model="formData.host" placeholder="请输入主机地址" />
        </el-form-item>
        
        <el-form-item label="端口" prop="port">
          <el-input-number v-model="formData.port" :min="1" :max="65535" style="width: 100%" />
        </el-form-item>
        
        <el-form-item label="数据库" prop="database">
          <el-input v-model="formData.database" placeholder="请输入数据库名" />
        </el-form-item>
        
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input v-model="formData.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDataSources, createDataSource, updateDataSource, deleteDataSource, testConnection } from '@/api'

const loading = ref(false)
const saving = ref(false)
const showDialog = ref(false)
const formRef = ref()
const editingId = ref(null)
const dataSourceList = ref([])

const formData = reactive({
  name: '',
  type: 'MySQL',
  host: '',
  port: 3306,
  database: '',
  username: '',
  password: '',
  description: '',
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入数据源名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  host: [{ required: true, message: '请输入主机地址', trigger: 'blur' }],
  port: [{ required: true, message: '请输入端口', trigger: 'blur' }],
  database: [{ required: true, message: '请输入数据库名', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }]
}

const loadDataSources = async () => {
  loading.value = true
  try {
    dataSourceList.value = await getDataSources()
  } catch (error) {
    console.error('加载数据源失败:', error)
  } finally {
    loading.value = false
  }
}

const handleEdit = (row) => {
  editingId.value = row.id
  Object.assign(formData, row)
  showDialog.value = true
}

const handleSave = async () => {
  try {
    await formRef.value.validate()
    saving.value = true
    
    if (editingId.value) {
      await updateDataSource(editingId.value, formData)
      ElMessage.success('更新成功')
    } else {
      await createDataSource(formData)
      ElMessage.success('创建成功')
    }
    
    showDialog.value = false
    loadDataSources()
    resetForm()
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    saving.value = false
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除此数据源吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteDataSource(id)
    ElMessage.success('删除成功')
    loadDataSources()
  })
}

const handleTest = async (id) => {
  try {
    const success = await testConnection(id)
    if (success) {
      ElMessage.success('连接测试成功')
    } else {
      ElMessage.error('连接测试失败')
    }
  } catch (error) {
    console.error('测试连接失败:', error)
  }
}

const resetForm = () => {
  editingId.value = null
  Object.assign(formData, {
    name: '',
    type: 'MySQL',
    host: '',
    port: 3306,
    database: '',
    username: '',
    password: '',
    description: '',
    status: 1
  })
  formRef.value?.resetFields()
}

onMounted(() => {
  loadDataSources()
})
</script>

<style scoped>
.datasources-page {
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

.el-table {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}
</style>
