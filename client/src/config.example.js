// 复制此文件为 config.js 并填入你的服务器地址
export default {
  serverHost: 'localhost',     // 你的服务器 IP 或域名
  serverPort: 8080,             // 后端端口
  get apiBase() {
    return `http://${this.serverHost}:${this.serverPort}/api`
  },
  get serverUrl() {
    return `http://${this.serverHost}:${this.serverPort}`
  }
}
