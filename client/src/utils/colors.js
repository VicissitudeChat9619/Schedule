import dayjs from 'dayjs'

const PRIORITY_COLORS = {
  1: { r: 245, g: 108, b: 108 },
  2: { r: 230, g: 162, b: 60 },
  3: { r: 64, g: 158, b: 255 }
}

const PRIORITY_DARK_TARGET = {
  1: { r: 198, g: 40, b: 40 },
  2: { r: 205, g: 100, b: 0 },
  3: { r: 21, g: 101, b: 192 }
}

export function getTodoColor(todo) {
  const priority = todo.priority || 2
  const dueTime = todo.dueTime
  if (!dueTime) {
    const base = PRIORITY_COLORS[priority]
    return `rgb(${base.r},${base.g},${base.b})`
  }

  const days = dayjs(dueTime).diff(dayjs(), 'day')
  const factor = Math.max(0, Math.min(1, 1 - days / 30))

  const base = PRIORITY_COLORS[priority]
  const target = PRIORITY_DARK_TARGET[priority]

  const r = Math.round(base.r + (target.r - base.r) * factor)
  const g = Math.round(base.g + (target.g - base.g) * factor)
  const b = Math.round(base.b + (target.b - base.b) * factor)

  return `rgb(${r},${g},${b})`
}

export function getTodoBgColor(todo) {
  const color = getTodoColor(todo)
  return color.replace('rgb', 'rgba').replace(')', ', 0.12)')
}

export function getTodoBorderColor(todo) {
  return getTodoColor(todo)
}
