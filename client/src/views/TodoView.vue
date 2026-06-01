<template>
  <div class="page-container">
    <div class="page-header">
      <h2>待办事项</h2>
      <div class="header-actions">
        <el-button type="primary" @click="openCreateDialog">新建待办</el-button>
      </div>
    </div>

    <el-card class="todo-section" shadow="hover">
      <template #header>
        <span class="section-title">未完成</span>
        <span class="section-count">{{ activeTodos.length }}</span>
      </template>
      <el-table
        :data="activeTodos"
        style="width: 100%"
        v-loading="loading"
        empty-text="暂无待办"
        :row-style="getRowStyle"
      >
        <el-table-column width="50">
          <template #default="{ row }">
            <el-checkbox
              :model-value="markingIds.has(row.id) ? true : false"
              :disabled="markingIds.has(row.id)"
              @change="() => handleMarkDone(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag v-if="isExpired(row)" type="danger" size="small">已过期</el-tag>
            <el-tag v-else type="warning" size="small">活跃</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180">
          <template #default="{ row }">
            {{ row.title }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="优先级" width="80">
          <template #default="{ row }">
            <span :style="{ color: getPriorityStyle(row) }" class="priority-dot">●</span>
            {{ priorityLabel(row.priority) }}
          </template>
        </el-table-column>
        <el-table-column label="截止时间" width="170">
          <template #default="{ row }">
            <span :class="{ 'expired-time': isExpired(row) }">
              {{ row.dueTime ? formatTime(row.dueTime) : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="提醒" width="100">
          <template #default="{ row }">
            提前{{ row.reminderBeforeMinutes }}分
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEditDialog(row)">
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

    <el-card class="todo-section done-section" shadow="hover">
      <template #header>
        <span class="section-title">已完成</span>
        <span class="section-count">{{ doneTodos.length }}</span>
      </template>
      <el-table
        :data="doneTodos"
        style="width: 100%"
        empty-text="暂无已完成的待办"
        row-class-name="done-row"
      >
        <el-table-column width="50">
          <template #default>
            <span class="done-check">✓</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default>
            <el-tag type="success" size="small">已完成</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180">
          <template #default="{ row }">
            <span class="done-title">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="优先级" width="80">
          <template #default="{ row }">
            {{ priorityLabel(row.priority) }}
          </template>
        </el-table-column>
        <el-table-column label="截止时间" width="170">
          <template #default="{ row }">
            {{ row.dueTime ? formatTime(row.dueTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleUndo(row.id)">恢复</el-button>
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
import { ref, reactive, onMounted } from 'vue'
import dayjs from 'dayjs'
import api from '../api'
import { getTodoColor, getTodoBgColor } from '../utils/colors.js'
import { useTodoRefresh } from '../stores/refresh.js'
import TodoFormDialog from '../components/TodoForm.vue'

const { triggerRefresh } = useTodoRefresh()

const activeTodos = ref([])
const doneTodos = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingTodo = ref(null)
const markingIds = reactive(new Set())

function formatTime(time) {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 16)
}

function isExpired(todo) {
  if (!todo.dueTime) return false
  return dayjs().isAfter(dayjs(todo.dueTime))
}

function priorityLabel(p) {
  const map = { 1: '高', 2: '中', 3: '低' }
  return map[p] || '中'
}

function getPriorityStyle(todo) {
  return getTodoColor(todo)
}

function getRowStyle({ row }) {
  const bgColor = getTodoBgColor(row)
  if (isExpired(row)) {
    return { background: '#fef0f0', color: '#f56c6c', textDecoration: 'line-through' }
  }
  return { background: bgColor }
}

async function fetchTodos() {
  loading.value = true
  try {
    const [activeRes, doneRes] = await Promise.all([
      api.get('/todos', { params: { status: 'ACTIVE' } }),
      api.get('/todos', { params: { status: 'DONE' } })
    ])
    if (activeRes.data.code === 200) {
      activeTodos.value = activeRes.data.data
    }
    if (doneRes.data.code === 200) {
      doneTodos.value = doneRes.data.data
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

async function handleMarkDone(todo) {
  markingIds.add(todo.id)
  try {
    await api.put(`/todos/${todo.id}/done`)
    setTimeout(() => {
      markingIds.delete(todo.id)
      fetchTodos()
      triggerRefresh()
    }, 300)
  } catch (e) {
    markingIds.delete(todo.id)
  }
}

async function handleDelete(id) {
  try {
    await api.delete(`/todos/${id}`)
    await fetchTodos()
    triggerRefresh()
  } catch (e) {
    // error handled by interceptor
  }
}

async function handleUndo(id) {
  try {
    await api.put(`/todos/${id}/undo`)
    await fetchTodos()
    triggerRefresh()
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
  flex-wrap: wrap;
}

.todo-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.section-count {
  margin-left: 8px;
  font-size: 13px;
  color: #909399;
  background: #f4f4f5;
  border-radius: 10px;
  padding: 0 8px;
}

.done-section :deep(.done-row) {
  opacity: 0.85;
}

.done-title {
  text-decoration: line-through;
  color: #c0c4cc;
}

.done-check {
  color: #67c23a;
  font-size: 16px;
  font-weight: bold;
}

.expired-time {
  color: #f56c6c;
  font-weight: 500;
}

.priority-dot {
  font-size: 14px;
  margin-right: 2px;
}
</style>