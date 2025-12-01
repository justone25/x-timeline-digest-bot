# Deployment Guide - X Timeline Digest Bot

## 🐳 Docker Deployment (Recommended)

### Quick Start

#### 1. Build Docker Image

```bash
docker build -t x-digest-bot:latest .
```

#### 2. Run with Docker Compose (Easiest)

Create a `.env` file in the project root:

```env
KIMI_API_KEY=sk-your-kimi-api-key
DISCORD_WEBHOOK_URL=https://discord.com/api/webhooks/your-webhook
KIMI_MODEL=kimi-k2-turbo-preview
```

Then start the container:

```bash
# Make sure cookies.json is in the project root
docker-compose up -d
```

Check logs:
```bash
docker-compose logs -f
```

Stop the container:
```bash
docker-compose down
```

#### 3. Run with Docker CLI

```bash
docker run -d \
  --name x-digest-bot \
  --restart unless-stopped \
  -e KIMI_API_KEY="sk-your-key" \
  -e DISCORD_WEBHOOK_URL="https://discord.com/..." \
  -e TWITTER_HEADLESS=true \
  -v $(pwd)/cookies.json:/app/config/cookies.json:ro \
  x-digest-bot:latest
```

View logs:
```bash
docker logs -f x-digest-bot
```

---

### Push to Remote Server with GUI

#### Option A: Using Docker Registry (Docker Hub / GitHub Container Registry)

**1. Tag your image:**
```bash
# For Docker Hub
docker tag x-digest-bot:latest yourusername/x-digest-bot:latest

# For GitHub Container Registry (ghcr.io)
docker tag x-digest-bot:latest ghcr.io/yourusername/x-digest-bot:latest
```

**2. Login to registry:**
```bash
# Docker Hub
docker login

# GitHub Container Registry
echo $GITHUB_TOKEN | docker login ghcr.io -u yourusername --password-stdin
```

**3. Push image:**
```bash
# Docker Hub
docker push yourusername/x-digest-bot:latest

# GitHub Container Registry
docker push ghcr.io/yourusername/x-digest-bot:latest
```

**4. On remote server (in GUI):**
- Image: `yourusername/x-digest-bot:latest` or `ghcr.io/yourusername/x-digest-bot:latest`
- Add environment variables in GUI:
  - `KIMI_API_KEY`: Your Kimi API key
  - `DISCORD_WEBHOOK_URL`: Your Discord webhook URL
  - `TWITTER_HEADLESS`: `true`
  - `KIMI_MODEL`: `kimi-k2-turbo-preview`
  - `TZ`: `Asia/Shanghai`
- Mount `cookies.json` as volume:
  - Host path: `/path/to/cookies.json`
  - Container path: `/app/config/cookies.json`
  - Read-only: ✅

#### Option B: Export/Import Image (No Registry Needed)

**1. Save image to tar file:**
```bash
docker save -o x-digest-bot.tar x-digest-bot:latest
```

**2. Transfer to remote server:**
```bash
scp x-digest-bot.tar user@remote-server:/tmp/
```

**3. On remote server, load image:**
```bash
docker load -i /tmp/x-digest-bot.tar
```

**4. Configure in GUI** (same as Option A step 4)

---

### Environment Variables Reference

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `KIMI_API_KEY` | Kimi API key from platform.moonshot.cn | - | ✅ Yes |
| `DISCORD_WEBHOOK_URL` | Discord webhook URL | - | ✅ Yes |
| `TWITTER_COOKIES_PATH` | Path to cookies.json inside container | `/app/config/cookies.json` | No |
| `TWITTER_HEADLESS` | Run browser in headless mode | `true` | No |
| `KIMI_MODEL` | Kimi model to use | `kimi-k2-turbo-preview` | No |
| `TZ` | Timezone | `Asia/Shanghai` | No |
| `JAVA_OPTS` | JVM options | - | No |

---

### Volumes

**Required:**
- `cookies.json` must be mounted to `/app/config/cookies.json`

**Optional:**
- Logs: `/app/logs` (if you want to persist logs)

---

### Updating the Application

**With Docker Compose:**
```bash
# Pull latest code
git pull

# Rebuild and restart
docker-compose up -d --build
```

**With Docker CLI:**
```bash
# Rebuild image
docker build -t x-digest-bot:latest .

# Stop old container
docker stop x-digest-bot
docker rm x-digest-bot

# Start new container (same run command as before)
docker run -d --name x-digest-bot ...
```

---

### Troubleshooting

**Container won't start:**
```bash
# Check logs
docker logs x-digest-bot

# Check if cookies.json is accessible
docker exec x-digest-bot ls -la /app/config/
```

**Playwright errors:**
- The Docker image includes Chromium dependencies
- If you see browser errors, the image should have everything needed
- Check if `TWITTER_HEADLESS=true` is set

**Out of memory:**
```bash
# Increase memory limit
docker run --memory=1g --memory-swap=1g ...
```

**Cookies expired:**
1. Export fresh cookies from browser
2. Update mounted `cookies.json` file
3. Restart container: `docker restart x-digest-bot`

---

### Health Monitoring

**Check if container is healthy:**
```bash
docker ps
docker inspect x-digest-bot | grep -A 10 Health
```

**Resource usage:**
```bash
docker stats x-digest-bot
```

---

## Alternative: JAR Deployment (Non-Docker)

<details>
<summary>Click to expand JAR deployment instructions</summary>

### Environment Variables Setup

```bash
export KIMI_API_KEY="sk-your-key"
export DISCORD_WEBHOOK_URL="https://discord.com/..."
export TWITTER_COOKIES_PATH="/path/to/cookies.json"
export TWITTER_HEADLESS="true"
export KIMI_MODEL="kimi-k2-turbo-preview"
```

### Build and Run

```bash
# Build
mvn clean package -DskipTests

# Run
java -jar digest-app-boot/target/digest-app-boot-*.jar
```

### Systemd Service

Create `/etc/systemd/system/x-digest-bot.service`:

```ini
[Unit]
Description=X Timeline Digest Bot
After=network.target

[Service]
Type=simple
User=your-user
WorkingDirectory=/opt/x-digest-bot
Environment="KIMI_API_KEY=sk-your-key"
Environment="DISCORD_WEBHOOK_URL=https://discord.com/...."
Environment="TWITTER_COOKIES_PATH=/etc/x-digest/cookies.json"
Environment="TWITTER_HEADLESS=true"
ExecStart=/usr/bin/java -jar digest-app-boot-*.jar
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable x-digest-bot
sudo systemctl start x-digest-bot
```

</details>

---

## Security Best Practices

1. ✅ Never commit sensitive data to Git
2. ✅ Use environment variables for secrets
3. ✅ Set restrictive permissions on `cookies.json` (chmod 600)
4. ✅ Use private Docker registry or encrypt image tar files
5. ✅ Rotate credentials regularly
6. ✅ Monitor container logs for suspicious activity
7. ✅ Consider using Docker secrets in production
8. ✅ Keep base images updated

---

## Quick Reference

```bash
# Build
docker build -t x-digest-bot .

# Run with compose
docker-compose up -d

# View logs
docker-compose logs -f

# Stop
docker-compose down

# Update
git pull && docker-compose up -d --build
```
