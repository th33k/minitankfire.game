# 🚀 QUICK START GUIDE

## ⚡ 10-Second Setup (EASIEST)

### Option 1: One-Click Launch
```cmd
run.bat
```
✅ **Automatically starts both servers and opens browser!**

### Option 2: Manual Setup

#### 1. Start Backend
```bash
cd backend
mvn exec:java
```
✅ Server running on `ws://localhost:8080`

#### 2. Start Frontend
```bash
cd frontend
python -m http.server 3000
```
✅ Game at `http://localhost:3000`

#### 3. Play
- Open browser → `http://localhost:3000`
- Enter name → Click "Deploy to Battle"
- Use **WASD** to move, **mouse** to aim, **click** to fire

---

### 🛑 Stop the Game
```cmd
stop.bat
```
✅ **Stops all servers cleanly**

### 📊 Check Server Status
```cmd
status.bat
```
✅ **Shows if servers are running**

---

## 🎮 Controls

```
Movement:   W A S D  or  ↑ ← ↓ →
Aim:        Mouse cursor
Fire:       Left Click  or  Spacebar
Chat:       Enter (open) → Type → Enter (send)
Voice:      Click 🎤 icon (top-right)
```

---

## 🎯 Objectives

1. **Eliminate enemies** → +1 point
2. **Avoid being hit** → −1 point when eliminated
3. **Collect power-ups** → Gain advantage
4. **Top the leaderboard** → Highest score wins!

---

## 💡 Tips

- **Keep moving** - Don't stand still
- **Watch minimap** - Bottom-right corner
- **Grab power-ups** - Rotating colored squares
- **Use voice chat** - Coordinate with others
- **Check kill feed** - Top-left corner

---

## 🛡️ Power-ups

| Icon | Name | Effect | Duration |
|------|------|--------|----------|
| 🟦 | Shield | Block 1 hit | 5 sec |
| 🟨 | Speed | +50% speed | 3 sec |
| 🟪 | Double Fire | 2 bullets/shot | 10 sec |

---

## 🎤 Voice Chat

1. Click microphone icon (top-right)
2. Allow browser permission
3. Start talking!
4. Click again to mute

---

## ❓ Troubleshooting

### Server won't start
```bash
# Make sure you're in the backend folder
cd Game/backend
mvn clean compile
mvn exec:java
```

### Can't connect
- Check server is running (see terminal output)
- Try `http://localhost:3000` (not `127.0.0.1`)
- Refresh page (Ctrl+F5)

### Game is laggy
- Close other programs
- Check server terminal for errors
- Use wired connection instead of WiFi

---

## 📱 Multi-Player Testing

### Same Computer
1. Open multiple browser tabs
2. Use different names in each
3. Play against yourself!

### Different Computers (LAN)
1. Find server IP: `ipconfig` (Windows) or `ifconfig` (Mac/Linux)
2. On other computers, open `http://[SERVER_IP]:3000`
3. All players connect to same server

---

## 🔧 Advanced

### Change Port
**Backend:**
```java
// GameServer.java, line 10
Server server = new Server(8080); // Change to your port
```

**Frontend:**
```javascript
// game.js, line 309
this.ws = new WebSocket('ws://localhost:8080/game'); // Update port
```

### Adjust Game Speed
```java
// GameRoom.java, line 4-7
private static final int PLAYER_SPEED = 3;     // Movement speed
private static final int BULLET_SPEED = 8;     // Bullet speed
```

### Change Map Size
```java
// GameRoom.java, line 2-3
private static final int MAP_WIDTH = 1200;
private static final int MAP_HEIGHT = 800;
```

```html
<!-- index.html, line 10 -->
<canvas id="game-canvas" width="1200" height="800"></canvas>
```

---

## 📖 Full Documentation

- **README.md** - Complete feature list, architecture
- **GAMEPLAY.md** - Detailed tactics, strategies, tips
- **Code Comments** - Inline documentation in source files

---

## 🎯 First Match Checklist

- [ ] Server running (check terminal)
- [ ] Frontend accessible (check browser)
- [ ] Entered callsign
- [ ] Can move with WASD
- [ ] Can aim with mouse
- [ ] Can fire with click
- [ ] Chat works (press Enter)
- [ ] See other players
- [ ] Leaderboard updating

---

## 🏆 Enjoy the Game!

**You're all set!** Deploy to the battlefield and dominate! 🎖️

---

*For issues, check the terminal output or browser console (F12).*