<template>
  <div class="page-container">
    <div class="page-header">
      <h2>待办事项</h2>
      <div class="header-actions">
        <el-radio-group v-model="filter" size="small" @change="fetchTodos">
          <el-radio-button value="ALL">全部</el-radio-button>
          <el-radio-button value="PENDING">待完成</el-radio-button>
        </el-radio-group>
        <el-button type="primary" @click="openCreateDialog">新建待办</el-button>
      </div>
    </div>

    <el-card>
      <el-table :data="todos" style="width: 100%" v-loading="loading" empty-text="暂无待办"
                row-class-name="todo-row">
        <el-table-column label="状态" width="70">
          <template #default="{ row }">
            <el-checkbox
              v-if="row.status === 'PENDING'"
              :model-value="false"
              @change="handleMarkDone(row.id)"
            />
            <el-tag v-else type="success" size="small">已完成</el-tag>
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
import api from '../api'
import TodoFormDialog from '../components/TodoForm.vue'

const todos = ref([])
const loading = ref(false)
const filter = ref('ALL')
const dialogVisible = ref(false)
const editingTodo = ref(null)

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

async function fetchTodos() {
  loading.value = true
  try {
    const params = filter.value === 'PENDING' ? { status: 'PENDING' } : {}
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

async function handleMarkDone(id) {
  try {
    await api.put(`/todos/${id}/done`)
    await fetchTodos()
  } catch (e) {
    // error handled by interceptor
  }
}

async function handleDelete(id) {
  try {
    await api.delete(`/todos/${id}`)
    await fetchTodos()
  } catch (e) {
    // error handled by interceptor
  }
}

onMounted(fetchTodos)
</script>

<style scoped>
.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.done-title {
  text-decoration: line-through;
  color: #c0c4cc;
}
</style>
