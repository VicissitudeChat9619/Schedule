<template>
  <div class="page-container">
    <div class="page-header">
      <h2>日程管理</h2>
      <el-button type="primary" @click="openCreateDialog">新建日程</el-button>
    </div>

    <el-card>
      <el-table :data="schedules" style="width: 100%" v-loading="loading" empty-text="暂无日程">
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="开始时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column label="结束时间" width="170">
          <template #default="{ row }">
            {{ row.endTime ? formatTime(row.endTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="重复" width="80">
          <template #default="{ row }">
            {{ repeatLabel(row.repeatType) }}
          </template>
        </el-table-column>
        <el-table-column label="提醒" width="100">
          <template #default="{ row }">
            提前{{ row.reminderBeforeMinutes }}分
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-popconfirm title="确认删除此日程？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <ScheduleFormDialog
      v-model:visible="dialogVisible"
      :schedule="editingSchedule"
      @saved="fetchSchedules"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'
import ScheduleFormDialog from '../components/ScheduleForm.vue'

const schedules = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingSchedule = ref(null)

function formatTime(time) {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 16)
}

function repeatLabel(type) {
  const map = { NONE: '不重复', DAILY: '每天', WEEKLY: '每周', MONTHLY: '每月' }
  return map[type] || type
}

async function fetchSchedules() {
  loading.value = true
  try {
    const res = await api.get('/schedules')
    if (res.data.code === 200) {
      schedules.value = res.data.data
    }
  } catch (e) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  editingSchedule.value = null
  dialogVisible.value = true
}

function openEditDialog(schedule) {
  editingSchedule.value = { ...schedule }
  dialogVisible.value = true
}

async function handleDelete(id) {
  try {
    await api.delete(`/schedules/${id}`)
    await fetchSchedules()
  } catch (e) {
    // error handled by interceptor
  }
}

onMounted(fetchSchedules)
</script>
