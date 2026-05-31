<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="$emit('update:visible', $event)"
    :title="isEdit ? '编辑日程' : '新建日程'"
    width="540px"
    @close="resetForm"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="日程标题" />
      </el-form-item>

      <el-form-item label="描述">
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="日程描述（可选）" />
      </el-form-item>

      <el-form-item label="开始时间" prop="startTime">
        <el-date-picker
          v-model="form.startTime"
          type="datetime"
          placeholder="选择开始时间"
          format="YYYY-MM-DD HH:mm:ss"
          value-format="YYYY-MM-DD HH:mm:ss"
          @change="onStartTimeChange"
        />
      </el-form-item>

      <el-form-item label="结束时间">
        <el-radio-group v-model="form.endTimeMode" size="small" @change="onEndTimeModeChange">
          <el-radio-button value="duration">持续时间</el-radio-button>
          <el-radio-button value="datetime">截止日期</el-radio-button>
          <el-radio-button value="none">不设置</el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="form.endTimeMode === 'duration'" label="">
        <div style="display: flex; gap: 6px; align-items: center; width: 100%">
          <el-input-number v-model="form.durationDays" :min="0" :max="365" placeholder="0" size="default" controls-position="right" style="width: 80px" @change="onDurationChange" />
          <span style="white-space: nowrap; color: #606266">天</span>
          <el-input-number v-model="form.durationHours" :min="0" :max="23" placeholder="0" size="default" controls-position="right" style="width: 80px" @change="onDurationChange" />
          <span style="white-space: nowrap; color: #606266">小时</span>
          <el-input-number v-model="form.durationMinutes" :min="0" :max="59" placeholder="0" size="default" controls-position="right" style="width: 80px" @change="onDurationChange" />
          <span style="white-space: nowrap; color: #606266">分</span>
        </div>
      </el-form-item>

      <el-form-item v-if="form.endTimeMode === 'datetime'" label="">
        <el-date-picker
          v-model="form.directEndTime"
          type="datetime"
          placeholder="选择截止日期"
          format="YYYY-MM-DD HH:mm:ss"
          value-format="YYYY-MM-DD HH:mm:ss"
          @change="updateEndTime"
        />
      </el-form-item>

      <el-form-item v-if="computedEndTime" label="预计结束">
        <span style="color: #409eff; font-weight: 500">{{ computedEndTime }}</span>
      </el-form-item>

      <el-form-item label="重复类型">
        <el-select v-model="form.repeatType" style="width: 100%">
          <el-option label="不重复" value="NONE" />
          <el-option label="每天" value="DAILY" />
          <el-option label="每周" value="WEEKLY" />
          <el-option label="每月" value="MONTHLY" />
        </el-select>
      </el-form-item>

      <el-form-item label="提前提醒">
        <el-select v-model="form.reminderBeforeMinutes" style="width: 100%">
          <el-option label="5 分钟" :value="5" />
          <el-option label="10 分钟" :value="10" />
          <el-option label="15 分钟" :value="15" />
          <el-option label="30 分钟" :value="30" />
          <el-option label="1 小时" :value="60" />
          <el-option label="2 小时" :value="120" />
        </el-select>
      </el-form-item>

      <el-form-item label="">
        <el-checkbox v-model="form.autoDelete">过期后自动删除（不发送提醒）</el-checkbox>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
        {{ isEdit ? '保存修改' : '创建' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import dayjs from 'dayjs'
import api from '../api'

const props = defineProps({
  visible: Boolean,
  schedule: Object
})

const emit = defineEmits(['update:visible', 'saved'])

const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  title: '',
  description: '',
  startTime: null,
  endTime: null,
  endTimeMode: 'none',
  durationDays: 0,
  durationHours: 0,
  durationMinutes: 0,
  directEndTime: null,
  repeatType: 'NONE',
  reminderBeforeMinutes: 15,
  autoDelete: false
})

const computedDuration = computed(() => {
  return form.durationDays * 1440 + form.durationHours * 60 + form.durationMinutes
})

const computedEndTime = computed(() => {
  if (form.endTimeMode === 'datetime' && form.directEndTime) {
    return dayjs(form.directEndTime).format('YYYY-MM-DD HH:mm')
  }
  if (form.endTimeMode === 'duration' && form.startTime && computedDuration.value > 0) {
    return dayjs(form.startTime).add(computedDuration.value, 'minute').format('YYYY-MM-DD HH:mm')
  }
  return ''
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }]
}

watch(() => props.schedule, (val) => {
  if (val) {
    isEdit.value = true
    form.title = val.title || ''
    form.description = val.description || ''
    form.startTime = val.startTime || null
    form.endTime = val.endTime || null
    form.repeatType = val.repeatType || 'NONE'
    form.reminderBeforeMinutes = val.reminderBeforeMinutes || 15
    form.autoDelete = val.autoDelete || false
    if (val.endTime) {
      form.endTimeMode = 'datetime'
      form.directEndTime = val.endTime
      const diff = calcDurationMinutes(val.startTime, val.endTime)
      form.durationDays = Math.floor(diff / 1440)
      form.durationHours = Math.floor((diff % 1440) / 60)
      form.durationMinutes = diff % 60
    } else {
      form.endTimeMode = 'none'
      form.directEndTime = null
      form.durationDays = 0
      form.durationHours = 0
      form.durationMinutes = 0
    }
  } else {
    isEdit.value = false
    resetForm()
  }
})

function calcDurationMinutes(start, end) {
  if (!start || !end) return 0
  return dayjs(end).diff(dayjs(start), 'minute')
}

function onStartTimeChange() {
  updateEndTime()
}

function onDurationChange() {
  updateEndTime()
}

function onEndTimeModeChange(mode) {
  if (mode === 'none') {
    form.endTime = null
    form.directEndTime = null
    form.durationDays = 0
    form.durationHours = 0
    form.durationMinutes = 0
  } else if (mode === 'datetime') {
    form.durationDays = 0
    form.durationHours = 0
    form.durationMinutes = 0
    if (form.directEndTime) {
      form.endTime = form.directEndTime
    }
  } else if (mode === 'duration') {
    form.directEndTime = null
    updateEndTime()
  }
}

function updateEndTime() {
  if (form.endTimeMode === 'duration' && form.startTime && computedDuration.value > 0) {
    form.endTime = dayjs(form.startTime).add(computedDuration.value, 'minute').format('YYYY-MM-DD HH:mm:ss')
  } else if (form.endTimeMode === 'datetime' && form.directEndTime) {
    form.endTime = form.directEndTime
  } else {
    form.endTime = null
  }
}

function resetForm() {
  form.title = ''
  form.description = ''
  form.startTime = null
  form.endTime = null
  form.endTimeMode = 'none'
  form.durationDays = 0
  form.durationHours = 0
  form.durationMinutes = 0
  form.directEndTime = null
  form.repeatType = 'NONE'
  form.reminderBeforeMinutes = 15
  form.autoDelete = false
  formRef.value?.resetFields()
  isEdit.value = false
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    let payEndTime = null
    if (form.endTimeMode === 'duration' && form.startTime && computedDuration.value > 0) {
      payEndTime = dayjs(form.startTime).add(computedDuration.value, 'minute').format('YYYY-MM-DD HH:mm:ss')
    } else if (form.endTimeMode === 'datetime' && form.directEndTime) {
      payEndTime = form.directEndTime
    }

    const payload = {
      title: form.title,
      description: form.description || null,
      startTime: form.startTime,
      endTime: payEndTime,
      repeatType: form.repeatType,
      reminderBeforeMinutes: form.reminderBeforeMinutes,
      autoDelete: form.autoDelete
    }

    if (isEdit.value && props.schedule?.id) {
      await api.put(`/schedules/${props.schedule.id}`, payload)
    } else {
      await api.post('/schedules', payload)
    }

    emit('update:visible', false)
    emit('saved')
  } catch (e) {
    // error handled by interceptor
  } finally {
    submitting.value = false
  }
}
</script>
