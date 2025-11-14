# GitHub Pages + Local Server Setup

## Problem
When hosting the client on GitHub Pages (HTTPS), browsers block insecure WebSocket connections (ws://) due to mixed content security policies. To connect to a local server, you need secure WebSocket connections (wss://).

## Solution Overview
The client now automatically detects the page protocol and uses:
- `wss://` when loaded from HTTPS (GitHub Pages)
- `ws://` when loaded from HTTP (local development)

## Setup Options

### Option 1: Use a Reverse Proxy with SSL (Recommended for LAN)

Use a reverse proxy like **ngrok**, **Cloudflare Tunnel**, or **nginx** to provide SSL termination for your local server.

#### Using ngrok (Easiest)

1. **Install ngrok**: Download from https://ngrok.com/download

2. **Start your game server** (port 8080)
   ```bash
   cd server
   mvn clean compile exec:java
   ```

3. **Start ngrok tunnel**:
   ```bash
   ngrok http 8080
   ```

4. **Connect from GitHub Pages**:
   - ngrok will give you a URL like: `https://abc123.ngrok.io`
   - Enter in the game: `abc123.ngrok.io` (without https://)
   - The client will automatically use `wss://abc123.ngrok.io:8080/game`

**Note**: Free ngrok URLs change each restart. Get a free static domain at ngrok.com

#### Using Cloudflare Tunnel (Free Static URL)

1. **Install cloudflared**: https://developers.cloudflare.com/cloudflare-one/connections/connect-apps/install-and-setup/

2. **Start tunnel**:
   ```bash
   cloudflared tunnel --url http://localhost:8080
   ```

3. Use the provided URL in your game client

### Option 2: Self-Signed Certificate (For Testing Only)

⚠️ **Warning**: Browsers will show security warnings. Not recommended for production.

1. **Generate certificate**:
   ```bash
   keytool -genkeypair -keyalg RSA -keysize 2048 -keystore keystore.jks \
     -alias minitank -validity 365 -storepass changeit
   ```

2. **Modify GameServer.java** to use SSLServerSocket

3. **Accept certificate** in browser (visit https://your-lan-ip:8080 first)

### Option 3: Local Development Only

For local testing without HTTPS:

1. **Host client locally** (not on GitHub Pages):
   ```bash
   cd client
   python -m http.server 3000
   ```

2. **Access via HTTP**: http://localhost:3000

3. **Client will use**: `ws://` protocol automatically

## Current Configuration

The client (`network-manager.js`) now includes:

```javascript
getWebSocketProtocol() {
    return window.location.protocol === 'https:' ? 'wss:' : 'ws:';
}
```

This ensures the correct protocol is used based on how the page is loaded.

## Recommended Setup for LAN Gaming with GitHub Pages

1. **Use ngrok or Cloudflare Tunnel** for SSL termination
2. **Share the tunnel URL** with other players on your LAN
3. **Everyone connects** via GitHub Pages using the tunnel hostname
4. **Advantage**: No certificate warnings, works from any network

## Testing

### Test Local Connection:
1. Start server: `make run-server`
2. Open: http://localhost:8080 (or use index.html locally)
3. Enter server: `localhost`
4. Should connect via `ws://localhost:8080/game`

### Test GitHub Pages Connection:
1. Start server with ngrok: `ngrok http 8080`
2. Open: https://th33k.github.io/minitankfire.game/
3. Enter server: `abc123.ngrok.io` (your ngrok domain)
4. Should connect via `wss://abc123.ngrok.io:8080/game`

## Port Considerations

The server runs on port **8080**. When using a tunnel service:
- The tunnel handles SSL on the public side
- Forwards to your local server on port 8080
- You only need to enter the tunnel hostname (e.g., `abc123.ngrok.io`)
- Don't include the port in the server address field
- The client automatically appends `:8080/game`

## Troubleshooting

### "Mixed Content" Error
- ✅ Fixed: Client now uses correct protocol
- If still occurring: clear browser cache

### "Connection Refused"
- Check server is running: `netstat -an | findstr 8080`
- Verify firewall allows port 8080
- For tunnels: check tunnel is active

### Certificate Warnings
- With ngrok/Cloudflare: No warnings (they provide valid certs)
- With self-signed: Expected, must accept in browser

### Can't Connect from Other Devices
- Use tunnel service (ngrok/Cloudflare)
- OR ensure your LAN IP is accessible and firewall allows connections
