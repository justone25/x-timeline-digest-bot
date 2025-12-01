# X Timeline Digest Bot 🤖

[English](README.md) | [中文](README_CN.md)

一个智能机器人，自动抓取你的 X (Twitter) 时间线，使用 LLM 进行总结和聚类，并将精美的日报推送到 Discord。

## ✨ 功能特性

- **时间线抓取**: 使用 Playwright 安全地浏览并获取 "For You" 时间线的推文。
- **AI 智能分析**: 集成 Kimi LLM (Moonshot AI) 实现：
  - 单条推文精准总结。
  - 话题自动聚类 (如 "AI 工具", "加密货币", "编程开发")。
  - 生成专业的趋势洞察报告。
- **Discord 推送**: 通过 Webhook 发送结构清晰、包含 Emoji 的富文本日报。
- **Docker 支持**: 提供完整的 Docker 支持，轻松部署到任何服务器。

## 🚀 快速开始

### 前置要求

- Java 21+ (本地开发)
- Maven
- Docker (推荐部署方式)
- X.com 账号 (需要导出 cookies)
- Kimi API Key
- Discord Webhook URL

### 配置

复制示例配置文件：

```bash
cp digest-app-boot/src/main/resources/application.properties.example \
   digest-app-boot/src/main/resources/application.properties
```

或者设置环境变量 (生产环境推荐):

- `KIMI_API_KEY`
- `DISCORD_WEBHOOK_URL`
- `TWITTER_COOKIES_PATH`

详细部署说明请参考 [DEPLOYMENT.md](DEPLOYMENT.md)。

## 🛠️ 技术栈

- **核心**: Java 21, Spring Boot
- **浏览器自动化**: Playwright (Java)
- **LLM**: Kimi (Moonshot AI)
- **架构**: 六边形架构 (Domain, Adapters, App)

## 📦 部署

```bash
docker-compose up -d
```

## 🗺️ 路线图 (Roadmap)

- [ ] **高级数据源管理**
  - 支持抓取特定的 X Lists (列表)，获取高质量信息源
  - 监控特定用户或关键词
- **报告与归档**
  - 自动生成周报/月报趋势总结
  - 高价值内容同步至 Notion/Obsidian
- **Web 管理后台**
  - Cookie 文件上传与管理
  - 关注列表/任务管理
  - Prompt 在线调整
- **更多推送渠道**
  - Telegram Bot 集成
  - 邮件订阅 (Newsletter)

## 📄 许可证

MIT
