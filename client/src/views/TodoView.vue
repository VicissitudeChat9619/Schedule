<template>
  <div class="page-container">
    <div class="page-header">
      <h2>待办事项</h2>
      <div class="header-actions">
        <el-radio-group v-model="filter" size="small" @change="fetchTodos">
          <el-radio-button value="">未安排</el-radio-button>
          <el-radio-button value="ALL">全部</el-radio-button>
          <el-radio-button value="DONE">已完成</el-radio-button>
        </el-radio-group>
        <el-button type="primary" @click="openCreateDialog">新建待办</el-button>
        <el-button
          v-if="filter === ''"
          type="success"
          :disabled="selectedIds.length === 0"
          @click="handleArrange"
        >
          一键安排日程 ({{ selectedIds.length }})
        </el-button>
      </div>
    </div>

    <el-card>
      <el-table
        :data="todos"
        style="width: 100%"
        v-loading="loading"
        empty-text="暂无待办"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" v-if="filter === ''" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'UNARRANGED'" type="warning" size="small">未安排</el-tag>
            <el-tag v-else-if="row.status === 'ARRANGED'" type="success" size="small">已安排</el-tag>
            <el-tag v-else-if="row.status === 'DONE'" type="info" size="small">已完成</el-tag>
            <el-tag v-else size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180">
          <template #default="{ row }">
            <span :class="{ 'done-title': row.status === 'DONE' }">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="优先级" width="80">
          <template #default="{ row }">
            <el-tag :type="priorityType(row.priority)" size="small">
              {{ priorityLabel(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="截止时间" width="170">
          <template #default="{ row }">
            {{ row.dueTime ? formatTime(row.dueTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="提醒" width="100">
          <template #default="{ row }">
            提前{{ row.reminderBeforeMinutes }}分
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEditDialog(row)" :disabled="row.status === 'DONE'">
              编辑
            </el-button>
            <el-popconfirm title="确认删除？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <TodoFormDialog
      v-model:visible="dialogVisible"
      :todo="editingTodo"
      @saved="fetchTodos"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'
import TodoFormDialog from '../components/TodoForm.vue'

const todos = ref([])
const loading = ref(false)
const filter = ref('')
const dialogVisible = ref(false)
const editingTodo = ref(null)
const selectedIds = ref([])

function formatTime(time) {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 16)
}

function priorityLabel(p) {
  const map = { 1: '高', 2: '中', 3: '低' }
  return map[p] || '中'
}

function priorityType(p) {
  const map = { 1: 'danger', 2: 'warning', 3: 'info' }
  return map[p] || 'warning'
}

function handleSelectionChange(selection) {
  selectedIds.value = selection.map(item => item.id)
}

async function fetchTodos() {
  loading.value = true
  try {
    const params = filter.value && filter.value !== 'ALL' ? { status: filter.value } : {}
    const res = await api.get('/todos', { params })
    if (res.data.code === 200) {
      todos.value = res.data.data
    }
  } catch (e) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  editingTodo.value = null
  dialogVisible.value = true
}

function openEditDialog(todo) {
  editingTodo.value = { ...todo }
  dialogVisible.value = true
}

async function handleDelete(id) {
  try {
    await api.delete(`/todos/${id}`)
    await fetchTodos()
  } catch (e) {
    // error handled by interceptor
  }
}

async function handleArrange() {
  if (selectedIds.value.length === 0) return
  loading.value = true
  try {
    const res = await api.post('/todos/arrange', { ids: selectedIds.value })
    if (res.data.code === 200) {
      ElMessage.success(`已创建 ${res.data.data.length} 条日程`)
      selectedIds.value = []
      await fetchTodos()
    }
  } catch (e) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}

onMounted(fetchTodos)
</script>

<style scoped>
.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.done-title {
  text-decoration: line-through;
  color: #c0c4cc;
}
</style>
