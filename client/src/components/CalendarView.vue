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
              v-for="evt in day.events"
              :key="evt.id"
              class="event-bar"
              :style="eventBarStyle(evt, day.events)"
              :title="evt.title + (evt.description ? '\n' + evt.description : '')"
              @click.stop="onEventClick(evt)"
            >
              <span class="event-time">{{ formatEventTime(evt) }}</span>
              <span class="event-title">{{ evt.title }}</span>
            </div>
            <div v-if="day.events.length > maxVisible" class="more-events" @click.stop="onDayClick(day)">
              +{{ day.events.length - maxVisible }} 更多
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

const props = defineProps({
  schedules: { type: Array, default: () => [] }
})

const emit = defineEmits(['dayClick', 'eventClick'])

const currentDate = ref(dayjs())
const maxVisible = ref(2)

const COLORS = [
  '#409eff', '#67c23a', '#e6a23c', '#f56c6c',
  '#e040fb', '#00bcd4', '#ff9800', '#795548',
  '#607d8b', '#009688', '#cddc39', '#ff5722'
]

const dayHeaders = ['日', '一', '二', '三', '四', '五', '六']

const year = computed(() => currentDate.value.year())
const month = computed(() => currentDate.value.month() + 1)

function getColor(schedule) {
  let hash = 0
  for (let i = 0; i < schedule.title.length; i++) {
    hash = schedule.title.charCodeAt(i) + ((hash << 5) - hash)
  }
  return COLORS[Math.abs(hash) % COLORS.length]
}

function formatEventTime(evt) {
  return dayjs(evt.startTime).format('HH:mm')
}

function durationMinutes(evt) {
  if (!evt.endTime) return 60
  return dayjs(evt.endTime).diff(dayjs(evt.startTime), 'minute')
}

function eventBarStyle(evt, allEvents) {
  const color = getColor(evt)
  const dur = durationMinutes(evt)
  const pct = Math.min(100, Math.max(20, (dur / 480) * 100))
  const idx = allEvents.indexOf(evt)
  return {
    background: color,
    width: pct + '%',
    opacity: 0.85,
    zIndex: allEvents.length - idx
  }
}

const days = computed(() => {
  const start = currentDate.value.startOf('month').startOf('week')
  const end = currentDate.value.endOf('month').endOf('week')
  const today = dayjs().format('YYYY-MM-DD')
  const result = []

  let d = start
  while (d.isBefore(end) || d.isSame(end, 'day')) {
    const dateStr = d.format('YYYY-MM-DD')
    const events = props.schedules.filter(s => {
      const sDate = dayjs(s.startTime).format('YYYY-MM-DD')
      return sDate === dateStr && s.status !== false
    })

    result.push({
      date: d.date(),
      dateStr,
      events,
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

function onEventClick(evt) {
  emit('eventClick', evt)
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
  min-height: 100px;
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
  padding: 1px 4px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  cursor: pointer;
  transition: opacity 0.15s;
  display: flex;
  gap: 2px;
  align-items: center;
  font-size: 11px;
  color: #fff;
  line-height: 1.4;
}

.event-bar:hover {
  opacity: 1 !important;
  z-index: 99 !important;
}

.event-time {
  font-size: 10px;
  opacity: 0.85;
  flex-shrink: 0;
}

.event-title {
  overflow: hidden;
  text-overflow: ellipsis;
}

.more-events {
  font-size: 11px;
  color: #909399;
  padding: 2px 4px;
  cursor: pointer;
}
</style>
