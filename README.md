# X Timeline Digest Bot 🤖

An intelligent bot that crawls your X (Twitter) timeline, summarizes content using LLM, clusters topics, and pushes a beautiful digest to Discord.

## ✨ Features

- **Timeline Crawling**: Uses Playwright to securely browse and fetch tweets from your "For You" timeline.
- **AI-Powered Analysis**: Integrates with Kimi LLM (Moonshot AI) to:
  - Summarize individual tweets.
  - Cluster tweets into topics (e.g., "AI Tools", "Crypto", "Coding").
  - Generate professional trend insights.
- **Rich Discord Digest**: Sends a structured, emoji-rich digest to your Discord server via Webhook.
- **Docker Ready**: Full Docker support for easy deployment on any server.

## 🚀 Getting Started

### Prerequisites

- Java 21+ (for local dev)
- Maven
- Docker (recommended for deployment)
- X.com account (cookies required)
- Kimi API Key
- Discord Webhook URL

### Configuration

Copy the example configuration:

```bash
cp digest-app-boot/src/main/resources/application.properties.example \
   digest-app-boot/src/main/resources/application.properties
```

Or set environment variables (recommended for production):

- `KIMI_API_KEY`
- `DISCORD_WEBHOOK_URL`
- `TWITTER_COOKIES_PATH`

See [DEPLOYMENT.md](DEPLOYMENT.md) for detailed deployment instructions.

## 🛠️ Tech Stack

- **Core**: Java 21, Spring Boot
- **Browser Automation**: Playwright (Java)
- **LLM**: Kimi (Moonshot AI)
- **Architecture**: Hexagonal Architecture (Domain, Adapters, App)

## 📦 Deployment

```bash
docker-compose up -d
```

## 🗺️ Roadmap

- [ ] **Advanced Source Management**
  - Support fetching from specific X Lists (high S/N ratio)
  - Monitor specific users or keywords
- **Reporting & Archiving**
  - Weekly/Monthly trend reports
  - Sync high-value content to Notion/Obsidian
- **Web Dashboard**
  - Cookie file management & auto-renewal
  - Subscription list management
  - Prompt engineering UI
- **More Channels**
  - Telegram Bot integration
  - Email Newsletter

## 📄 License

MIT
