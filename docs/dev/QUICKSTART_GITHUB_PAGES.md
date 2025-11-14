# 🚀 Quick Setup: GitHub Pages + Local Server

## The Problem
✗ GitHub Pages uses HTTPS  
✗ Your server uses WS (insecure WebSocket)  
✗ Browsers block mixed content (HTTPS → WS)

## The Solution
✓ Use WSS (secure WebSocket) with a tunnel service  
✓ Client auto-detects and uses correct protocol

---

## 3-Step Setup with ngrok (Easiest)

### 1. Download & Install ngrok
https://ngrok.com/download

### 2. Start Your Server
```bash
cd server
mvn clean compile exec:java
```

### 3. Start ngrok Tunnel
```bash
ngrok http 8080
```

**You'll see:**
```
Forwarding   https://abc123.ngrok.io -> http://localhost:8080
```

---

## Connect & Play

### From GitHub Pages:
1. Open: **https://th33k.github.io/minitankfire.game/**
2. Enter server: **abc123.ngrok.io** (your ngrok URL)
3. Click Join Game!

### Share with Friends:
- Give them the ngrok URL: `abc123.ngrok.io`
- They visit: https://th33k.github.io/minitankfire.game/
- Everyone plays together!

---

## What Changed?

### ✅ Client (network-manager.js)
```javascript
// Auto-detects protocol
getWebSocketProtocol() {
    return window.location.protocol === 'https:' ? 'wss:' : 'ws:';
}

// Uses correct protocol
const protocol = this.getWebSocketProtocol();
this.ws = new WebSocket(`${protocol}//${serverAddress}:8080/game`);
```

**Result:**
- HTTPS page → Uses `wss://`
- HTTP page → Uses `ws://`

---

## Alternative: Cloudflare Tunnel (Free Forever)

```bash
# Install
npm install -g cloudflared

# Run tunnel
cloudflared tunnel --url http://localhost:8080
```

**Advantage:** Free static URL that doesn't change

---

## Local Development (No Tunnel Needed)

Host client locally instead of GitHub Pages:

```bash
cd client
python -m http.server 3000
```

Open: **http://localhost:3000**  
Server: **localhost**

Uses `ws://` automatically since page is HTTP.

---

## Troubleshooting

### Still getting "Mixed Content" errors?
- Clear browser cache (Ctrl+Shift+Delete)
- Hard refresh (Ctrl+F5)
- Check console: should show `wss://` not `ws://`

### ngrok URL keeps changing?
- Free plan gives new URL on restart
- Get free static domain: https://ngrok.com/pricing (forever free tier)
- OR use Cloudflare Tunnel

### Connection fails?
- Check server is running: `netstat -an | findstr 8080`
- Check ngrok is running and shows "online"
- Try URL in browser: `https://abc123.ngrok.io` (should show some response)

### Firewall blocking?
- ngrok bypasses firewall (tunnels through HTTPS)
- Allow Java through Windows Firewall if needed

---

## Files Changed

✓ `client/js/managers/network-manager.js` - Auto protocol detection  
✓ `client/index.html` - Added favicon  
✓ `client/favicon.svg` - New icon (no more 404)

---

## For More Details

📖 [Full Setup Guide](GITHUB_PAGES_SETUP.md)  
📖 [Development Guide](DEVELOPMENT.md)  
📖 [Architecture](ARCHITECTURE.md)
