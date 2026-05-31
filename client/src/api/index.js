import axios from 'axios'
import { ElMessage } from 'element-plus'
import config from '../config.js'

const api = axios.create({
  baseURL: config.apiBase,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  response => response,
  error => {
    const msg = error.response?.data?.message || '请求失败'
    ElMessage.error(msg)

    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.hash = '#/login'
    }

    return Promise.reject(error)
  }
)

export default api
