# Schedule - 日程管理系统

基于 Spring Boot + Electron + NapCat 的日程与待办提醒系统。用户在 Windows 客户端上管理日程/待办，服务器定时检测到期提醒，通过 NapCat (NTQQ) 推送 QQ 消息通知。

## 架构

```
Windows客户端 (Electron+Vue3)
        │  REST API (JWT)
        ▼
Spring Boot 服务端 (Java 21)
        │  HTTP
        ▼
     NapCat (NTQQ)
        │
        ▼
     QQ 消息提醒
```

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Java 17 / Spring Boot 3.2 / Spring Security + JWT / JPA / MySQL |
| 客户端 | Electron 30 / Vue 3.4 / Vite 5 / Element Plus / Pinia / Day.js |
| 消息推送 | NapCat (NTQQ Docker) + OneBot11 HTTP API |
| 数据库 | MySQL 8.x |

## 项目结构

```
myschedule/
├── sql/                          # 数据库初始化脚本
│   └── init.sql
├── server/                       # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/java/com/myschedule/
│       ├── MyscheduleApplication.java
│       ├── config/               # NapCat HTTP 客户端 / Jackson 日期格式
│       ├── controller/           # Auth / Schedule / Todo / User API
│       ├── service/              # 业务逻辑 + 定时提醒调度
│       ├── entity/               # JPA 实体 (User/Schedule/Todo/ReminderLog)
│       ├── repository/           # JPA 仓库
│       ├── dto/                  # 请求/响应 DTO
│       └── security/             # JWT 生成/验证/过滤器
├── client/                       # Electron + Vue 3 客户端
│   ├── electron/                 # Electron 主进程
│   └── src/
│       ├── views/                # 登录/注册/日程/待办/设置
│       ├── components/           # CalendarView / ScheduleForm / TodoForm
│       ├── api/                  # Axios 封装 + 拦截器
│       ├── router/               # Vue Router + 路由守卫
│       └── stores/               # Pinia 状态管理
├── napcat/                       # NapCat 部署指南
│   └── README.md
├── start-client.bat              # 一键启动（浏览器）
└── start-client-electron.bat     # 一键启动（Electron）
```

## 功能特性

### 日程管理
- 列表视图 + **日历视图**（可切换）
- 创建、编辑、删除日程，支持一次性/每天/每周/每月重复
- **持续时间**自由输入（X天 Y小时 Z分），自动计算结束时间
- **批量删除**：勾选多个日程一键批量删除
- **清除过期**：一键列出并确认删除所有已过期日程
- **过期自动删除**：新建/编辑时可勾选，过期后自动清理不发送提醒
- **跨天日程**：跨越多日的日程在日历中覆盖所有日期显示

### 日历可视化
- 月视图网格，每格显示当天日程色条
- **12 种配色**按标题自动分配，不同日程颜色显著区分
- **色条宽度**按持续时间等比缩放，长短一目了然
- **过期日程**红色虚线空心边框，与活跃日程形成鲜明对比

### 待办管理
- 创建、编辑、删除待办，支持优先级（高/中/低）
- 默认状态为「未安排」，安排后变为「已安排」
- **一键安排日程**：勾选多个待办 → 点击按钮 → 自动创建对应日程

### QQ 提醒
- **定时提醒**：服务端每分钟检测到期日程/待办，通过 NapCat 发送私聊消息
- **过期提醒**：日程到期后 QQ 通知「您的日程已过期，请及时处理」
- **过期自动删除**：勾选后过期不通知，直接自动清理

## API 接口

| 方法 | 路径 | 说明 | 认证 |
|---|---|---|---|
| POST | `/api/auth/register` | 注册 | 否 |
| POST | `/api/auth/login` | 登录 | 否 |
| GET | `/api/auth/me` | 当前用户信息 | 是 |
| GET | `/api/schedules` | 日程列表 | 是 |
| POST | `/api/schedules` | 新建日程 | 是 |
| PUT | `/api/schedules/{id}` | 修改日程 | 是 |
| DELETE | `/api/schedules/{id}` | 删除日程 | 是 |
| POST | `/api/schedules/batch-delete` | 批量删除日程 | 是 |
| GET | `/api/todos` | 待办列表（默认未安排） | 是 |
| POST | `/api/todos` | 新建待办 | 是 |
| PUT | `/api/todos/{id}` | 修改待办 | 是 |
| DELETE | `/api/todos/{id}` | 删除待办 | 是 |
| POST | `/api/todos/arrange` | 一键安排日程 | 是 |
| GET | `/api/user/profile` | 用户信息 | 是 |
| PUT | `/api/user/bind-qq` | 绑定 QQ | 是 |

## 快速开始

### 0. 配置服务器连接

使用前需修改两处配置：

**客户端 API 地址** (`client/src/api/index.js`)：
```js
baseURL: 'http://<你的服务器IP>:8080/api',
```

**服务端数据库** (`server/src/main/resources/application.yml`)：
```yaml
spring:
  datasource:
    url: jdbc:mysql://<你的服务器IP>:3306/schedule
    password: <你的MySQL密码>

napcat:
  api-url: http://localhost:3000
```

### 1. 启动客户端

双击 `start-client.bat` 或桌面快捷方式：

```bash
cd client
npm install       # 首次运行
npm run dev       # 浏览器 http://localhost:5173
# 或
npm run electron:dev   # Electron 桌面版
```

### 2. 数据库初始化

在服务器 MySQL 中执行：

```bash
mysql -u root -p < sql/init.sql
```

### 3. 启动后端

```bash
cd server
mvn clean package -DskipTests
java -jar target/myschedule-server-1.0.0.jar
```

服务默认运行在 `http://localhost:8080`。

### 4. 部署 NapCat

```bash
docker run -d --name napcat --restart=always \
  -e ACCOUNT=<你的QQ号> \
  -p 3000:3000 -p 6099:6099 \
  -v /root/napcat/config:/app/napcat/config \
  -v /root/napcat/data:/app/napcat/data \
  -v /root/napcat/QQ:/app/QQ \
  mlikiowa/napcat-docker:latest
```

打开 WebUI `http://<服务器IP>:6099/webui` 扫码登录，然后在 OneBot11 配置中启用 HTTP 服务器（端口 3000）。

### 5. 使用流程

1. 客户端注册/登录账户
2. 在「设置」页面绑定 QQ 号
3. 在「日程管理」创建日程 → 日历视图可视化查看
4. 在「待办事项」创建待办 → 勾选后一键安排为日程
5. 到达提醒时间 / 过期时，QQ 收到消息推送

## 提醒机制

服务端每分钟执行定时任务：

1. **日程提醒**：检测 `startTime - reminderBeforeMinutes <= now` 的未提醒日程，发送 QQ 消息
2. **待办提醒**：检测 `dueTime - reminderBeforeMinutes <= now` 的未提醒待办，发送 QQ 消息
3. **过期检测**：
   - 有结束时间 → `endTime < now` 时过期
   - 无结束时间 → `startTime < now` 时过期
   - 勾选自动删除 → 直接删除
   - 未勾选 → 发送 QQ 过期提醒
4. 所有提醒记录写入 `reminder_log` 表

## 配置说明

`server/src/main/resources/application.yml` 关键配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://<服务器IP>:3306/schedule
    username: root
    password: <密码>

napcat:
  api-url: http://localhost:3000    # NapCat HTTP API 地址

reminder:
  cron: "0 * * * * *"               # 每分钟检查一次
```
