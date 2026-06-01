<template>
  <div class="calendar-view">
    <div class="calendar-toolbar">
      <el-button-group>
        <el-button size="small" @click="prevMonth">◀</el-button>
        <el-button size="small" @click="goToday">今天</el-button>
        <el-button size="small" @click="nextMonth">▶</el-button>
      </el-button-group>
      <span class="calendar-title">{{ year }}年 {{ month }}月</span>
    </div>

    <div class="calendar-grid">
      <div class="calendar-header">
        <div v-for="d in dayHeaders" :key="d" class="header-cell">{{ d }}</div>
      </div>
      <div class="calendar-body">
        <div
          v-for="(day, idx) in days"
          :key="idx"
          class="day-cell"
          :class="{ 'is-today': day.isToday, 'is-other-month': day.isOtherMonth }"
          @click="onDayClick(day)"
        >
          <div class="day-number">{{ day.date }}</div>
          <div class="day-events">
            <div
              v-for="evt in day.scheduleEvents"
              :key="'s-' + evt.id"
              class="event-bar schedule-bar"
              :class="{ 'is-expired': isExpired(evt) }"
              :style="scheduleBarStyle(evt, day.scheduleEvents)"
              :title="formatScheduleTooltip(evt)"
              @click.stop="onScheduleClick(evt)"
            >
              {{ evt.title }}
            </div>
            <div
              v-for="todo in day.todoEvents"
              :key="'t-' + todo.id"
              class="event-bar todo-bar"
              :class="{ 'is-expired': isTodoExpired(todo) }"
              :style="todoBarStyle(todo)"
              :title="formatTodoTooltip(todo)"
              @click.stop="onTodoClick(todo)"
            >
              ◼ {{ todo.title }}
            </div>
            <div
              v-if="day.eventsTotal.length > maxVisible"
              class="more-events"
              @click.stop="onDayClick(day)"
            >
              +{{ day.eventsTotal.length - maxVisible }} 更多
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import dayjs from 'dayjs'
import { getTodoColor } from '../utils/colors.js'

const props = defineProps({
  schedules: { type: Array, default: () => [] },
  todos: { type: Array, default: () => [] }
})

const emit = defineEmits(['dayClick', 'eventClick', 'todoClick'])

const currentDate = ref(dayjs())
const maxVisible = ref(3)

const COLORS = [
  '#409eff', '#67c23a', '#e6a23c', '#f56c6c',
  '#e040fb', '#00bcd4', '#ff9800', '#795548',
  '#607d8b', '#009688', '#cddc39', '#ff5722'
]

const dayHeaders = ['日', '一', '二', '三', '四', '五', '六']

const year = computed(() => currentDate.value.year())
const month = computed(() => currentDate.value.month() + 1)

function getScheduleColor(schedule) {
  let hash = 0
  for (let i = 0; i < schedule.title.length; i++) {
    hash = schedule.title.charCodeAt(i) + ((hash << 5) - hash)
  }
  return COLORS[Math.abs(hash) % COLORS.length]
}

function formatScheduleTooltip(evt) {
  let t = dayjs(evt.startTime).format('HH:mm')
  if (evt.endTime) {
    t += ' - ' + dayjs(evt.endTime).format('HH:mm')
  }
  t += ' ' + evt.title
  if (evt.description) {
    t += '\n' + evt.description
  }
  return t
}

function formatTodoTooltip(todo) {
  const prioMap = { 1: '高', 2: '中', 3: '低' }
  let t = '[待办] ' + todo.title
  if (todo.dueTime) {
    t += '\n截止: ' + dayjs(todo.dueTime).format('YYYY-MM-DD HH:mm')
  }
  t += '\n优先级: ' + (prioMap[todo.priority] || '中')
  if (todo.description) {
    t += '\n' + todo.description
  }
  return t
}

function isExpired(evt) {
  const now = dayjs()
  const expireTime = evt.endTime ? dayjs(evt.endTime) : dayjs(evt.startTime)
  return now.isAfter(expireTime)
}

function isTodoExpired(todo) {
  if (!todo.dueTime) return false
  return dayjs().isAfter(dayjs(todo.dueTime))
}

function scheduleBarStyle(evt, allEvents) {
  const color = getScheduleColor(evt)
  const chars = (evt.title || '').length
  const pct = Math.min(90, Math.max(35, chars * 8))
  const idx = allEvents.indexOf(evt)
  if (isExpired(evt)) {
    return {
      borderColor: '#f56c6c',
      color: '#f56c6c',
      background: 'transparent',
      width: pct + '%',
      zIndex: allEvents.length - idx
    }
  }
  return {
    background: color,
    width: pct + '%',
    opacity: 0.85,
    zIndex: allEvents.length - idx
  }
}

function todoBarStyle(todo) {
  const color = getTodoColor(todo)
  if (isTodoExpired(todo)) {
    return {
      borderColor: '#f56c6c',
      color: '#f56c6c',
      background: 'transparent',
      width: '85%'
    }
  }
  return {
    background: color,
    width: '85%',
    opacity: 0.9
  }
}

function matchesScheduleDate(schedule, dateStr) {
  const sStart = dayjs(schedule.startTime)
  const sEnd = schedule.endTime ? dayjs(schedule.endTime) : null
  const target = dayjs(dateStr)

  if (target.isBefore(sStart, 'day')) return false
  if (sEnd && target.isAfter(sEnd, 'day')) return false

  const repeatType = schedule.repeatType || 'NONE'

  if (repeatType === 'NONE' || repeatType === 'DAILY') {
    return true
  }

  if (repeatType === 'WEEKLY') {
    return target.day() === sStart.day()
  }

  if (repeatType === 'MONTHLY') {
    const startDay = sStart.date()
    const lastDayOfMonth = target.daysInMonth()
    return target.date() === Math.min(startDay, lastDayOfMonth)
  }

  return true
}

const days = computed(() => {
  const start = currentDate.value.startOf('month').startOf('week')
  const end = currentDate.value.endOf('month').endOf('week')
  const today = dayjs().format('YYYY-MM-DD')
  const result = []

  let d = start
  while (d.isBefore(end) || d.isSame(end, 'day')) {
    const dateStr = d.format('YYYY-MM-DD')

    const scheduleEvents = props.schedules.filter(s => {
      if (s.status === false) return false
      return matchesScheduleDate(s, dateStr)
    })

    const todoEvents = props.todos.filter(t => {
      if (t.status !== 'ACTIVE' || !t.dueTime) return false
      return dayjs(t.dueTime).format('YYYY-MM-DD') === dateStr
    })

    const eventsTotal = [...scheduleEvents, ...todoEvents]

    result.push({
      date: d.date(),
      dateStr,
      scheduleEvents,
      todoEvents,
      eventsTotal,
      isToday: dateStr === today,
      isOtherMonth: d.month() !== currentDate.value.month()
    })
    d = d.add(1, 'day')
  }
  return result
})

function prevMonth() {
  currentDate.value = currentDate.value.subtract(1, 'month')
}

function nextMonth() {
  currentDate.value = currentDate.value.add(1, 'month')
}

function goToday() {
  currentDate.value = dayjs()
}

function onDayClick(day) {
  emit('dayClick', day.dateStr)
}

function onScheduleClick(evt) {
  emit('eventClick', evt)
}

function onTodoClick(todo) {
  emit('todoClick', todo)
}
</script>

<style scoped>
.calendar-view {
  background: #fff;
  border-radius: 4px;
  overflow: hidden;
}

.calendar-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
}

.calendar-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.calendar-grid {
  display: flex;
  flex-direction: column;
}

.calendar-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  border-bottom: 1px solid #ebeef5;
}

.header-cell {
  text-align: center;
  padding: 8px 0;
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

.calendar-body {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
}

.day-cell {
  min-height: 110px;
  padding: 4px;
  border-right: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: background 0.15s;
  position: relative;
  overflow: hidden;
}

.day-cell:nth-child(7n) {
  border-right: none;
}

.day-cell:hover {
  background: #f5f7fa;
}

.day-cell.is-other-month .day-number {
  color: #c0c4cc;
}

.day-cell.is-today {
  background: #ecf5ff;
}

.day-cell.is-today .day-number {
  background: #409eff;
  color: #fff;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

.day-number {
  font-size: 13px;
  color: #606266;
  margin-bottom: 2px;
}

.day-events {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.event-bar {
  border-radius: 3px;
  padding: 2px 5px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  cursor: pointer;
  transition: opacity 0.15s;
  font-size: 11px;
  color: #fff;
  line-height: 1.6;
  font-weight: 500;
}

.event-bar:hover {
  opacity: 1 !important;
  z-index: 99 !important;
  filter: brightness(1.1);
}

.event-bar.is-expired {
  background: transparent !important;
  color: #f56c6c;
  border: 1.5px dashed #f56c6c;
  opacity: 1;
  font-weight: 600;
}

.event-bar.is-expired:hover {
  background: rgba(245, 108, 108, 0.1) !important;
  filter: none;
}

.todo-bar {
  border-radius: 0;
  border-left: 3px solid currentColor;
}

.schedule-bar {
  border-radius: 3px;
}

.more-events {
  font-size: 11px;
  color: #909399;
  padding: 2px 4px;
  cursor: pointer;
}
</style>