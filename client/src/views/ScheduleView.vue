<template>
  <div class="page-container">
    <div class="page-header">
      <h2>日程管理</h2>
      <div class="header-actions">
        <el-radio-group v-model="viewMode" size="small">
          <el-radio-button value="list">列表</el-radio-button>
          <el-radio-button value="calendar">日历</el-radio-button>
        </el-radio-group>
        <el-button v-if="viewMode === 'list'" type="danger" plain :disabled="selectedIds.length === 0" @click="handleBatchDelete">
          批量删除 ({{ selectedIds.length }})
        </el-button>
        <el-button v-if="viewMode === 'list' && expiredSchedules.length > 0" type="warning" plain @click="handleClearExpired">
          清除过期 ({{ expiredSchedules.length }})
        </el-button>
        <el-button type="primary" @click="openCreateDialog">新建日程</el-button>
      </div>
    </div>

    <el-card v-if="viewMode === 'list'">
      <el-table :data="schedules" style="width: 100%" v-loading="loading" empty-text="暂无日程"
                :row-class-name="getRowClass"
                @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
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

    <CalendarView
      v-else
      :schedules="schedules"
      @day-click="onCalendarDayClick"
      @event-click="onCalendarEventClick"
    />

    <ScheduleFormDialog
      v-model:visible="dialogVisible"
      :schedule="editingSchedule"
      @saved="fetchSchedules"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import api from '../api'
import ScheduleFormDialog from '../components/ScheduleForm.vue'
import CalendarView from '../components/CalendarView.vue'

const schedules = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingSchedule = ref(null)
const selectedIds = ref([])
const viewMode = ref('calendar')

const expiredSchedules = computed(() => {
  const now = dayjs()
  return schedules.value.filter(s => {
    const expireTime = s.endTime ? dayjs(s.endTime) : dayjs(s.startTime)
    return now.isAfter(expireTime) && s.status !== false
  })
})

function formatTime(time) {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 16)
}

function repeatLabel(type) {
  const map = { NONE: '不重复', DAILY: '每天', WEEKLY: '每周', MONTHLY: '每月' }
  return map[type] || type
}

function handleSelectionChange(selection) {
  selectedIds.value = selection.map(item => item.id)
}

function getRowClass({ row }) {
  const now = dayjs()
  const expireTime = row.endTime ? dayjs(row.endTime) : dayjs(row.startTime)
  return now.isAfter(expireTime) ? 'row-expired' : ''
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

function openCreateDialog(startTime) {
  editingSchedule.value = startTime ? { startTime } : null
  dialogVisible.value = true
}

function openEditDialog(schedule) {
  editingSchedule.value = { ...schedule }
  dialogVisible.value = true
}

function onCalendarDayClick(dateStr) {
  openCreateDialog(dateStr + ' 09:00:00')
}

function onCalendarEventClick(evt) {
  openEditDialog(evt)
}

async function handleDelete(id) {
  try {
    await api.delete(`/schedules/${id}`)
    await fetchSchedules()
  } catch (e) {
    // error handled by interceptor
  }
}

async function handleBatchDelete() {
  if (selectedIds.value.length === 0) return
  try {
    await api.post('/schedules/batch-delete', { ids: selectedIds.value })
    ElMessage.success(`已删除 ${selectedIds.value.length} 条日程`)
    selectedIds.value = []
    await fetchSchedules()
  } catch (e) {
    // error handled by interceptor
  }
}

async function handleClearExpired() {
  const titles = expiredSchedules.value.map(s => s.title)
  try {
    await ElMessageBox.confirm(
      '以下日程已过期，确认删除？\n\n' + titles.map((t, i) => `${i + 1}. ${t}`).join('\n'),
      '清除过期日程',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' }
    )
    const ids = expiredSchedules.value.map(s => s.id)
    await api.post('/schedules/batch-delete', { ids })
    ElMessage.success(`已清除 ${ids.length} 条过期日程`)
    await fetchSchedules()
  } catch (e) {
    // cancelled or error
  }
}

onMounted(fetchSchedules)
</script>

<style scoped>
.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

:deep(.row-expired) {
  background: #fef0f0 !important;
  color: #f56c6c;
  text-decoration: line-through;
}
</style>
