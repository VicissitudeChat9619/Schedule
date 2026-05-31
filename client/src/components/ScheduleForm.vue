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
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 100%"
          @change="onStartTimeChange"
        />
      </el-form-item>

      <el-form-item label="持续时间">
        <el-select v-model="form.durationMinutes" style="width: 100%" @change="onDurationChange">
          <el-option label="不设置结束" :value="0" />
          <el-option label="30 分钟" :value="30" />
          <el-option label="1 小时" :value="60" />
          <el-option label="1.5 小时" :value="90" />
          <el-option label="2 小时" :value="120" />
          <el-option label="3 小时" :value="180" />
          <el-option label="半天 (4h)" :value="240" />
          <el-option label="全天 (8h)" :value="480" />
        </el-select>
      </el-form-item>

      <el-form-item v-if="form.startTime && form.durationMinutes > 0" label="预计结束">
        <span style="color: #409eff; font-weight: 500">{{ calcEndTime() }}</span>
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
import { ref, reactive, watch } from 'vue'
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
  durationMinutes: 0,
  repeatType: 'NONE',
  reminderBeforeMinutes: 15
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
    form.durationMinutes = calcDuration(val.startTime, val.endTime)
  } else {
    isEdit.value = false
    resetForm()
  }
})

function calcDuration(start, end) {
  if (!start || !end) return 0
  const diff = dayjs(end).diff(dayjs(start), 'minute')
  const presets = [30, 60, 90, 120, 180, 240, 480]
  const closest = presets.reduce((a, b) => Math.abs(b - diff) < Math.abs(a - diff) ? b : a)
  return Math.abs(closest - diff) <= 5 ? closest : 0
}

function calcEndTime() {
  if (!form.startTime || form.durationMinutes <= 0) return ''
  return dayjs(form.startTime).add(form.durationMinutes, 'minute').format('YYYY-MM-DD HH:mm')
}

function onStartTimeChange() {
  if (form.durationMinutes > 0) {
    form.endTime = calcEndTime()
  }
}

function onDurationChange(val) {
  if (val > 0 && form.startTime) {
    form.endTime = calcEndTime()
  } else {
    form.endTime = null
  }
}

function resetForm() {
  form.title = ''
  form.description = ''
  form.startTime = null
  form.endTime = null
  form.durationMinutes = 0
  form.repeatType = 'NONE'
  form.reminderBeforeMinutes = 15
  formRef.value?.resetFields()
  isEdit.value = false
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload = {
      title: form.title,
      description: form.description || null,
      startTime: form.startTime,
      endTime: form.endTime || null,
      repeatType: form.repeatType,
      reminderBeforeMinutes: form.reminderBeforeMinutes
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
