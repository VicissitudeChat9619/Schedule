<template>
  <div class="page-container">
    <div class="page-header">
      <h2>系统设置</h2>
    </div>

    <el-card>
      <el-form label-width="120px" style="max-width: 500px">
        <el-form-item label="用户名">
          <span>{{ auth.user?.username }}</span>
        </el-form-item>

        <el-form-item label="绑定QQ">
          <div v-if="auth.user?.qqNumber" style="display: flex; align-items: center; gap: 12px">
            <el-tag type="success">{{ auth.user.qqNumber }}</el-tag>
            <el-button size="small" @click="showBindDialog">修改绑定</el-button>
          </div>
          <el-button v-else type="primary" @click="showBindDialog">绑定QQ号</el-button>
        </el-form-item>

        <el-form-item label="服务器地址">
          <el-tag>http://your-server-ip:8080</el-tag>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog v-model="bindVisible" title="绑定QQ号" width="400px">
      <el-form :model="bindForm" :rules="bindRules" ref="bindFormRef">
        <el-form-item label="QQ号" prop="qqNumber">
          <el-input v-model="bindForm.qqNumber" placeholder="请输入你的QQ号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bindVisible = false">取消</el-button>
        <el-button type="primary" :loading="bindLoading" @click="handleBindQQ">确认绑定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import api from '../api'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const bindVisible = ref(false)
const bindLoading = ref(false)
const bindFormRef = ref(null)

const bindForm = reactive({
  qqNumber: ''
})

const bindRules = {
  qqNumber: [
    { required: true, message: '请输入QQ号', trigger: 'blur' },
    { pattern: /^\d{5,15}$/, message: '请输入有效的QQ号', trigger: 'blur' }
  ]
}

function showBindDialog() {
  bindForm.qqNumber = ''
  bindVisible.value = true
}

async function handleBindQQ() {
  const valid = await bindFormRef.value.validate().catch(() => false)
  if (!valid) return

  bindLoading.value = true
  try {
    const res = await api.put('/user/bind-qq', { qqNumber: bindForm.qqNumber })
    if (res.data.code === 200) {
      bindVisible.value = false
      await auth.fetchProfile()
    }
  } catch (e) {
    // error handled by interceptor
  } finally {
    bindLoading.value = false
  }
}
</script>
