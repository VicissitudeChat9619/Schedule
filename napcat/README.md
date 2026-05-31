# NapCat 部署指南

NapCat 是基于 NTQQ 的机器人框架，可暴露 HTTP API 供后端调用发送 QQ 消息。

## 1. 安装 NapCat

在你的服务器上安装 NapCat：

```bash
# 使用 Docker 部署（推荐）
docker run -d \
  --name napcat \
  --restart always \
  -p 3000:3000 \
  -p 6099:6099 \
  -v /root/napcat/config:/app/config \
  -v /root/napcat/data:/app/data \
  -e ACCOUNT=<你的QQ号> \
  mlikiowa/napcat-docker:latest
```

## 2. 登录 QQ

启动后查看容器日志获取二维码：

```bash
docker logs napcat
```

用手机 QQ 扫码登录，或在浏览器打开 `http://<服务器IP>:6099/webui` 扫码。

## 3. 启用 HTTP API

登录后进入 WebUI → 网络配置 → 添加 HTTP 服务器：

- **名称**: schedule-http
- **地址**: 0.0.0.0
- **端口**: 3000
- **启用**: 是

或直接修改 OneBot11 配置文件 `onebot11_<QQ号>.json`：

```json
{
  "network": {
    "httpServers": [
      {
        "name": "schedule-http",
        "enable": true,
        "host": "0.0.0.0",
        "port": 3000,
        "enablePost": true,
        "token": ""
      }
    ]
  }
}
```

## 4. 后端对接

在 `server/src/main/resources/application.yml` 中修改 NapCat API 地址：

```yaml
napcat:
  api-url: http://localhost:3000
```

后端会调用 `POST <napcat-api-url>/send_private_msg` 发送消息：

```json
{
  "user_id": "目标QQ号",
  "message": "提醒消息内容"
}
```

## 5. 验证

部署后测试通信：

```bash
curl -X POST http://localhost:3000/send_private_msg \
  -H "Content-Type: application/json" \
  -d '{"user_id":"你的QQ号","message":"测试消息"}'
```

## 6. 注意事项

- 确保服务器防火墙开放 3000 端口
- QQ 号登录后需保持在线状态
- 首次登录可能需要手机 QQ 扫码确认
