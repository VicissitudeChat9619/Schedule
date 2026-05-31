<template>
  <div id="app-root">
    <el-container v-if="isLoggedIn">
      <el-header class="app-header">
        <div class="header-left">
          <h3>日程管理系统</h3>
        </div>
        <div class="header-menu">
          <el-menu
            mode="horizontal"
            :default-active="activeMenu"
            router
            background-color="#667eea"
            text-color="#fff"
            active-text-color="#fff"
          >
            <el-menu-item index="/schedules">日程管理</el-menu-item>
            <el-menu-item index="/todos">待办事项</el-menu-item>
            <el-menu-item index="/settings">设置</el-menu-item>
          </el-menu>
        </div>
        <div class="header-right">
          <span class="username">{{ auth.user?.username }}</span>
          <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
    <router-view v-else />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from './stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const isLoggedIn = computed(() => !!auth.token)
const activeMenu = computed(() => route.path)

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.app-header {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 0 24px;
  height: 60px;
}

.header-left h3 {
  color: #fff;
  font-size: 18px;
  white-space: nowrap;
  margin-right: 20px;
}

.header-menu {
  flex: 1;
}

.header-menu .el-menu {
  border-bottom: none;
}

.header-menu .el-menu--horizontal > .el-menu-item {
  border-bottom: 2px solid transparent;
}

.header-menu .el-menu--horizontal > .el-menu-item.is-active {
  border-bottom-color: #fff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #fff;
}

.header-right .username {
  font-size: 14px;
}
</style>
