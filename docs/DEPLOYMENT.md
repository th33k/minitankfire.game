# 🚀 PRODUCTION DEPLOYMENT GUIDE

## Overview

This guide covers deploying **Mini Tank Fire: Online** to production servers for real-world usage.

---

## 📋 Prerequisites

### Server Requirements
- **OS**: Linux (Ubuntu 20.04+ recommended)
- **Java**: OpenJDK 11 or higher
- **RAM**: 2GB minimum (4GB recommended)
- **CPU**: 2 cores minimum
- **Storage**: 1GB
- **Network**: Static IP, open ports

### Software
- Maven 3.6+
- Nginx (for client hosting)
- SSL certificate (Let's Encrypt recommended)
- Domain name (optional but recommended)

---

## 🔧 Server Deployment

### 1. Build Production JAR

```bash
cd server
mvn clean package
```

This creates: `target/minitankfire-server-1.0-SNAPSHOT.jar`

### 2. Create Systemd Service

Create `/etc/systemd/system/minitankfire.service`:

```ini
[Unit]
Description=Mini Tank Fire Game Server
After=network.target

[Service]
Type=simple
User=gameserver
WorkingDirectory=/opt/minitankfire
ExecStart=/usr/bin/java -jar /opt/minitankfire/minitankfire-server-1.0-SNAPSHOT.jar
Restart=always
RestartSec=10
StandardOutput=syslog
StandardError=syslog
SyslogIdentifier=minitankfire

[Install]
WantedBy=multi-user.target
```

### 3. Deploy and Start

```bash
# Create user
sudo useradd -r -s /bin/false gameserver

# Create directory
sudo mkdir -p /opt/minitankfire
sudo cp target/minitankfire-server-1.0-SNAPSHOT.jar /opt/minitankfire/
sudo chown -R gameserver:gameserver /opt/minitankfire

# Enable and start service
sudo systemctl daemon-reload
sudo systemctl enable minitankfire
sudo systemctl start minitankfire

# Check status
sudo systemctl status minitankfire
```

### 4. Configure Firewall

```bash
# Allow WebSocket port
sudo ufw allow 8080/tcp

# If using reverse proxy (recommended)
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
```

---

## 🌐 Client Deployment

### 1. Update WebSocket URL

Edit `client/js/game.js` line 309:

```javascript
// Change from
this.ws = new WebSocket('ws://localhost:8080/game');

// To production URL
this.ws = new WebSocket('wss://yourdomain.com/game');
```

### 2. Nginx Configuration

Create `/etc/nginx/sites-available/minitankfire`:

```nginx
upstream game_backend {
    server localhost:8080;
}

server {
    listen 80;
    server_name yourdomain.com;
    
    # Redirect to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name yourdomain.com;
    
    # SSL Configuration
    ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    
    # Client static files
    root /var/www/minitankfire;
    index index.html;
    
    location / {
        try_files $uri $uri/ =404;
    }
    
    # WebSocket proxy
    location /game {
        proxy_pass http://game_backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 86400;
    }
    
    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header Referrer-Policy "no-referrer-when-downgrade" always;
    add_header Content-Security-Policy "default-src 'self' https: wss: data: 'unsafe-inline' 'unsafe-eval';" always;
    
    # Caching
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

### 3. Deploy Client

```bash
# Copy files
sudo mkdir -p /var/www/minitankfire
sudo cp -r client/* /var/www/minitankfire/
sudo chown -R www-data:www-data /var/www/minitankfire

# Enable site
sudo ln -s /etc/nginx/sites-available/minitankfire /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### 4. SSL Certificate (Let's Encrypt)

```bash
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d yourdomain.com
sudo certbot renew --dry-run
```

---

## ⚙️ Production Optimizations

### 1. JVM Tuning

Edit systemd service:

```ini
ExecStart=/usr/bin/java \
  -Xms1G \
  -Xmx2G \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -jar /opt/minitankfire/minitankfire-server-1.0-SNAPSHOT.jar
```

### 2. Game Configuration

Create `config.properties`:

```properties
# Server
server.port=8080
server.maxPlayers=50
server.tickRate=20

# Game
game.mapWidth=1200
game.mapHeight=800
game.playerSpeed=3
game.bulletSpeed=8

# Power-ups
powerup.spawnChance=0.005
powerup.maxActive=10
```

### 3. Logging

Add SLF4J configuration (`logback.xml`):

```xml
<configuration>
  <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>/var/log/minitankfire/game.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>/var/log/minitankfire/game-%d{yyyy-MM-dd}.log</fileNamePattern>
      <maxHistory>30</maxHistory>
    </rollingPolicy>
    <encoder>
      <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>
  <root level="INFO">
    <appender-ref ref="FILE" />
  </root>
</configuration>
```

---

## 📊 Monitoring

### 1. Server Health Check

```bash
# Check if server is running
curl -I http://localhost:8080

# Check WebSocket
wscat -c ws://localhost:8080/game
```

### 2. Application Logs

```bash
# Systemd logs
sudo journalctl -u minitankfire -f

# Application logs
tail -f /var/log/minitankfire/game.log
```

### 3. Performance Monitoring

Install and configure:
- **Prometheus** for metrics
- **Grafana** for dashboards
- **Uptime Robot** for availability monitoring

Example metrics to track:
- Active players count
- Messages per second
- Memory usage
- CPU usage
- WebSocket connections

---

## 🔒 Security

### 1. Rate Limiting (Nginx)

```nginx
http {
    limit_req_zone $binary_remote_addr zone=game:10m rate=10r/s;
    
    server {
        location / {
            limit_req zone=game burst=20;
        }
    }
}
```

### 2. Input Validation

Add to `GameWebSocket.java`:

```java
// Validate player names
if (name.length() > 20 || !name.matches("[a-zA-Z0-9_]+")) {
    session.close(1008, "Invalid name");
    return;
}

// Validate coordinates
if (x < 0 || x > MAP_WIDTH || y < 0 || y > MAP_HEIGHT) {
    return; // Ignore invalid moves
}
```

### 3. DDoS Protection

- Use Cloudflare for DDoS protection
- Configure fail2ban for repeated failed connections
- Implement connection limits per IP

---

## 🚀 Scaling

### Horizontal Scaling (Multiple Instances)

1. **Load Balancer** (Nginx):

```nginx
upstream game_backends {
    least_conn;
    server game1.internal:8080;
    server game2.internal:8080;
    server game3.internal:8080;
}
```

2. **Session Stickiness**:

```nginx
upstream game_backends {
    ip_hash;
    server game1.internal:8080;
    server game2.internal:8080;
}
```

3. **Redis for State Sharing** (requires code changes):
   - Store game state in Redis
   - All instances read/write to shared state
   - Enables true horizontal scaling

### Vertical Scaling

- Increase RAM allocation
- Add more CPU cores
- Optimize game tick rate
- Use connection pooling

---

## 📈 Capacity Planning

### Expected Performance

| Players | RAM | CPU | Bandwidth |
|---------|-----|-----|-----------|
| 10 | 512MB | 1 core | 1 Mbps |
| 50 | 2GB | 2 cores | 5 Mbps |
| 100 | 4GB | 4 cores | 10 Mbps |
| 500 | 16GB | 8 cores | 50 Mbps |

### Network Calculations

- **Update rate**: 20 packets/sec per player
- **Packet size**: ~500 bytes
- **Per player**: 10 KB/s upload, 10 KB/s download
- **50 players**: ~500 KB/s (~4 Mbps)

---

## 🔄 CI/CD Pipeline

### GitHub Actions Example

```yaml
name: Deploy to Production

on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Set up JDK 11
        uses: actions/setup-java@v2
        with:
          java-version: '11'
          
      - name: Build with Maven
        run: cd server && mvn clean package
        
      - name: Deploy to server
        uses: appleboy/scp-action@master
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SERVER_SSH_KEY }}
          source: "server/target/*.jar,client/*"
          target: "/opt/minitankfire"
          
      - name: Restart service
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SERVER_SSH_KEY }}
          script: sudo systemctl restart minitankfire
```

---

## 🧪 Testing

### Load Testing

Use Artillery:

```yaml
# artillery.yml
config:
  target: 'wss://yourdomain.com'
  phases:
    - duration: 60
      arrivalRate: 10
scenarios:
  - engine: ws
    flow:
      - send:
          data: '{"type":"join","name":"LoadTest{{$randomNumber}}"}'
      - think: 1
      - send:
          data: '{"type":"move","x":400,"y":300,"angle":45}'
      - think: 0.5
      - send:
          data: '{"type":"fire"}'
```

Run: `artillery run artillery.yml`

---

## 📱 Mobile Considerations

For mobile deployment:
1. Add touch controls in JavaScript
2. Adjust canvas size for mobile screens
3. Optimize network for higher latency
4. Add vibration feedback
5. Test on various devices

---

## 🛠️ Maintenance

### Regular Tasks

**Daily:**
- Check error logs
- Monitor player count
- Review performance metrics

**Weekly:**
- Update dependencies
- Review security logs
- Backup database (if added)

**Monthly:**
- Security patches
- Performance optimization
- Feature updates

### Backup Strategy

```bash
# Backup script
#!/bin/bash
DATE=$(date +%Y%m%d)
tar -czf /backups/minitankfire-$DATE.tar.gz \
  /opt/minitankfire \
  /var/www/minitankfire \
  /etc/nginx/sites-available/minitankfire

# Keep last 30 days
find /backups -mtime +30 -delete
```

---

## 📞 Support

### Health Endpoint

Add to `GameServer.java`:

```java
@GET
@Path("/health")
public Response health() {
    return Response.ok()
        .entity("{\"status\":\"UP\",\"players\":" + gameRoom.getPlayerCount() + "}")
        .build();
}
```

### Metrics Endpoint

```java
@GET
@Path("/metrics")
public Response metrics() {
    return Response.ok()
        .entity(new Metrics(
            gameRoom.getPlayerCount(),
            gameRoom.getBulletCount(),
            Runtime.getRuntime().freeMemory()
        ))
        .build();
}
```

---

## ✅ Pre-Launch Checklist

- [ ] SSL certificate configured
- [ ] Domain DNS configured
- [ ] Firewall rules set
- [ ] Monitoring setup
- [ ] Backups configured
- [ ] Load testing completed
- [ ] Security audit passed
- [ ] Documentation updated
- [ ] Emergency contacts listed
- [ ] Rollback plan ready

---

## 🎉 Go Live!

```bash
# Final checks
sudo systemctl status minitankfire
sudo systemctl status nginx
curl -I https://yourdomain.com
wscat -c wss://yourdomain.com/game

# If all green, announce launch!
echo "🚀 Mini Tank Fire: Online is LIVE!"
```

---

**Your game is now production-ready!** 🎮🚀

For issues, check:
- `/var/log/minitankfire/game.log`
- `sudo journalctl -u minitankfire`
- Nginx error logs: `/var/log/nginx/error.log`