# NapCat 部署指南

NapCat 是基于 NTQQ 的机器人框架，可暴露 HTTP API 供后端调用发送 QQ 消息。

## 1. 安装 NapCat

在服务器 (your-server-ip) 上安装 NapCat：

```bash
# 使用 Docker 部署（推荐）
docker run -d \
  --name napcat \
  --restart always \
  -p 3000:3000 \
  -v /root/napcat/config:/app/config \
  -v /root/napcat/data:/app/data \
  napneko/napcat:latest
```

或参考官方文档：https://napneko.github.io/

## 2. 配置 HTTP Server

登录 NapCat WebUI 后，配置 HTTP Server：

- **监听地址**: 0.0.0.0
- **监听端口**: 3000
- **启用**: 是

## 3. QQ 登录

在 NapCat WebUI 中使用目标 QQ 号扫码登录。

## 4. 后端对接

后端已配置 NapCat API 地址为 `http://your-server-ip:3000`。

当提醒触发时，后端会调用：
```
POST http://your-server-ip:3000/send_private_msg
Content-Type: application/json

{
  "user_id": "目标QQ号",
  "message": "提醒消息内容"
}
```

### API 响应格式

```json
{
  "status": "ok",
  "retcode": 0,
  "data": {
    "message_id": 123456
  }
}
```

## 5. 验证

部署 NapCat 后，测试通信：

```bash
curl -X POST http://your-server-ip:3000/send_private_msg \
  -H "Content-Type: application/json" \
  -d '{"user_id":"你的QQ号","message":"测试消息"}'
```

## 6. 注意事项

- 确保服务器防火墙开放 3000 端口
- QQ 号登录后需保持在线状态
- 首次登录可能需要手机 QQ 扫码确认
