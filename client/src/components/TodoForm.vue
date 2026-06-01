<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="$emit('update:visible', $event)"
    :title="isEdit ? '编辑待办' : '新建待办'"
    width="540px"
    @close="resetForm"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="待办事项标题" />
      </el-form-item>

      <el-form-item label="描述">
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="详细描述（可选）" />
      </el-form-item>

      <el-form-item label="优先级">
        <el-radio-group v-model="form.priority">
          <el-radio :value="1">高</el-radio>
          <el-radio :value="2">中</el-radio>
          <el-radio :value="3">低</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="截止时间">
        <el-date-picker
          v-model="form.dueTime"
          type="datetime"
          placeholder="选择截止时间（可选）"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="提前提醒">
        <el-select v-model="form.reminderBeforeMinutes" style="width: 100%">
          <el-option label="10 分钟" :value="10" />
          <el-option label="15 分钟" :value="15" />
          <el-option label="30 分钟" :value="30" />
          <el-option label="1 小时" :value="60" />
          <el-option label="2 小时" :value="120" />
          <el-option label="1 天" :value="1440" />
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
import { ref, reactive, watch } from 'vue'
import api from '../api'

const props = defineProps({
  visible: Boolean,
  todo: Object
})

const emit = defineEmits(['update:visible', 'saved'])

const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  title: '',
  description: '',
  priority: 2,
  dueTime: null,
  reminderBeforeMinutes: 30,
  autoDelete: false
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }]
}

watch(() => props.todo, (val) => {
  if (val) {
    isEdit.value = true
    form.title = val.title || ''
    form.description = val.description || ''
    form.priority = val.priority || 2
    form.dueTime = val.dueTime || null
    form.reminderBeforeMinutes = val.reminderBeforeMinutes || 30
    form.autoDelete = val.autoDelete || false
  } else {
    isEdit.value = false
    resetForm()
  }
})

function resetForm() {
  form.title = ''
  form.description = ''
  form.priority = 2
  form.dueTime = null
  form.reminderBeforeMinutes = 30
  form.autoDelete = false
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
      priority: form.priority,
      dueTime: form.dueTime || null,
      reminderBeforeMinutes: form.reminderBeforeMinutes,
      autoDelete: form.autoDelete
    }

    if (isEdit.value && props.todo?.id) {
      await api.put(`/todos/${props.todo.id}`, payload)
    } else {
      await api.post('/todos', payload)
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
