import { ref } from 'vue'

const todoRefresh = ref(0)

export function useTodoRefresh() {
  return {
    todoRefresh,
    triggerRefresh() {
      todoRefresh.value++
    }
  }
}
