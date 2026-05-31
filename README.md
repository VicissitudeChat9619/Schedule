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
| 后端 | Java 21 / Spring Boot 3.2 / Spring Security + JWT / JPA / MySQL |
| 客户端 | Electron 30 / Vue 3.4 / Vite 5 / Element Plus / Pinia |
| 消息推送 | NapCat (NTQQ HTTP API) |
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
│       ├── config/               # NapCat HTTP 客户端配置
│       ├── controller/           # Auth / Schedule / Todo / User API
│       ├── service/              # 业务逻辑 + 定时提醒调度
│       ├── entity/               # JPA 实体
│       ├── repository/           # JPA 仓库
│       ├── dto/                  # 数据传输对象
│       └── security/             # JWT 生成/验证/过滤器
├── client/                       # Electron + Vue 3 客户端
│   ├── electron/                 # Electron 主进程
│   └── src/
│       ├── views/                # 登录/注册/日程/待办/设置
│       ├── components/           # 表单对话框组件
│       ├── api/                  # Axios 封装
│       ├── router/               # Vue Router + 路由守卫
│       └── stores/               # Pinia 状态管理
└── napcat/                       # NapCat 部署指南
    └── README.md
```

## 功能特性

- **用户系统**: 注册/登录，JWT 无状态认证
- **日程管理**: 创建、编辑、删除日程，支持一次性/每天/每周/每月重复
- **待办管理**: 创建、编辑、删除待办，支持优先级(高/中/低)，一键标记完成
- **QQ 提醒**: 绑定 QQ 号后，服务器定时检测到期日程/待办，通过 NapCat 发送私聊提醒
- **提醒配置**: 每个日程/待办可独立设置提前提醒时间

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
| GET | `/api/todos` | 待办列表 | 是 |
| POST | `/api/todos` | 新建待办 | 是 |
| PUT | `/api/todos/{id}` | 修改待办 | 是 |
| PUT | `/api/todos/{id}/done` | 标记完成 | 是 |
| DELETE | `/api/todos/{id}` | 删除待办 | 是 |
| GET | `/api/user/profile` | 用户信息 | 是 |
| PUT | `/api/user/bind-qq` | 绑定 QQ | 是 |

## 快速开始

### 1. 数据库初始化

在服务器 MySQL 中执行 `sql/init.sql`：

```bash
mysql -u root -p < sql/init.sql
```

### 2. 启动后端

修改 `server/src/main/resources/application.yml` 中的数据库连接信息和 NapCat API 地址，然后：

```bash
cd server
mvn clean package -DskipTests
java -jar target/myschedule-server-1.0.0.jar
```

服务默认运行在 `http://localhost:8080`。

### 3. 部署 NapCat

参考 `napcat/README.md` 在服务器上部署 NapCat，确保 HTTP Server 在 3000 端口运行。

### 4. 启动客户端

```bash
cd client
npm install
npm run electron:dev
```

开发模式运行 Vue 开发服务器 + Electron 窗口。

### 5. 使用流程

1. 客户端注册/登录账户
2. 在「设置」页面绑定 QQ 号
3. 在「日程管理」创建日程，设置提醒时间
4. 在「待办事项」创建待办，设置截止时间和优先级
5. 到达提醒时间时，QQ 将收到私聊消息推送

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

## 提醒机制

服务端每分钟执行定时任务：

1. 查询所有未发送提醒的活跃日程/待办
2. 判断当前时间是否已达到「日程开始时间 - 提前提醒分钟数」或「待办截止时间 - 提前提醒分钟数」
3. 调用 NapCat `/send_private_msg` API 向绑定的 QQ 号发送私聊消息
4. 记录提醒日志到 `reminder_log` 表
