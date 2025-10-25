# 🎮 GAMEPLAY GUIDE

## Getting Started

### 1. Launch the Game
- Open `http://localhost:3000` in your browser
- You'll see the join screen with a neon-themed interface

### 2. Enter Your Callsign
- Type a unique name (max 20 characters)
- Press Enter or click "Deploy to Battle"

### 3. Game HUD Overview

```
┌────────────────────────────────────────────────────────┐
│ [Health] [Kills] [Deaths]    [Power-up]    [🎤] [⚙️]  │
├────────────────────────────────────────────────────────┤
│                                               ┌──────┐ │
│  [Kill Feed]                                  │ TOP  │ │
│  ✗ Player1 killed Player2                    │  10  │ │
│                                               └──────┘ │
│                                                        │
│           [GAME CANVAS 1200×800]                       │
│                                                        │
│  ┌──────────┐                           ┌──────────┐  │
│  │  CHAT    │                           │ MINIMAP  │  │
│  │ Player: │                           │  ◉ ○ ○   │  │
│  │ Hi all! │                           └──────────┘  │
│  └──────────┘                                         │
└────────────────────────────────────────────────────────┘
```

## Controls

### Movement
- **W / ↑** - Move forward
- **S / ↓** - Move backward
- **A / ←** - Move left
- **D / →** - Move right

> **Tip**: You can move diagonally by pressing two keys at once (e.g., W+D)

### Combat
- **Mouse Movement** - Aim turret
- **Left Click** - Fire bullet
- **Spacebar** - Alternative fire (hold for rapid fire)

> **Cooldown**: 500ms between shots (2 shots per second)

### Communication
- **Enter** - Open chat input
- **Type & Enter** - Send message
- **ESC** - Close chat input
- **Microphone Icon** - Toggle voice chat

## Game Mechanics

### Health System
- Start with **100 HP**
- One hit = instant elimination
- Respawn after 3 seconds at random location
- **Shield power-up** blocks one hit

### Combat Tips
1. **Lead your shots** - Bullets travel at 8 units/tick
2. **Strafe while firing** - Keep moving to avoid hits
3. **Use cover** - No physical walls, but stay unpredictable
4. **Watch the minimap** - See all players' positions

### Power-ups

#### 🛡️ Shield (Cyan Diamond)
- **Effect**: Blocks next bullet hit
- **Duration**: 5 seconds
- **Visual**: Cyan glowing border around tank

#### ⚡ Speed Boost (Yellow Diamond)
- **Effect**: +50% movement speed
- **Duration**: 3 seconds
- **Visual**: Yellow speed lines behind tank

#### 🔥 Double Fire (Magenta Diamond)
- **Effect**: Fire 2 bullets per shot
- **Duration**: 10 seconds
- **Visual**: Orange power-up indicator in HUD

> **Strategy**: Rush for power-ups early in the game!

## Scoring

| Action | Points | Notes |
|--------|--------|-------|
| Kill Enemy | **+1** | Eliminate another player |
| Get Eliminated | **−1** | Respawn in 3 seconds |
| Collect Power-up | **+0.5** | First to grab gets it |

### Leaderboard
- Top 10 players shown
- Real-time updates
- Your rank highlighted in green
- Sorted by score (high to low)

## Advanced Tactics

### 1. Crossfire Strategy
- Position yourself between two enemies
- Force them to shoot each other
- Clean up the survivor

### 2. Power-up Control
- Memorize spawn locations
- Camp near power-ups
- Deny enemy power-ups

### 3. Hit and Run
- Fire once, then immediately strafe
- Don't stay in one place
- Use speed boost to escape

### 4. Ambush Tactics
- Watch minimap for approaching enemies
- Pre-aim at corners
- Fire as soon as they appear

### 5. Team Communication (Voice)
- Call out enemy positions
- Coordinate attacks
- Warn about power-ups

## Voice Chat Usage

### Setup
1. Click microphone icon in top-right
2. Allow browser to access microphone
3. Green icon = transmitting
4. Red icon = muted

### Best Practices
- **Push-to-talk**: Click to toggle, don't leave always-on
- **Keep brief**: Call-outs, not conversations
- **Be respectful**: Team communication only

### Troubleshooting
- **No audio?** Check browser permissions
- **Echo?** Use headphones
- **Lag?** Voice is P2P, check your connection

## Chat Commands

### Regular Chat
```
Press Enter → Type message → Enter to send
```

### Quick Messages
- "gg" - Good game
- "1v1?" - Challenge someone
- "power up" - Call attention to power-up
- "help" - Ask for assistance

### Chat Tips
- Messages stay for ~30 seconds
- Max 100 characters per message
- Your name is highlighted in green
- Press ESC to close without sending

## Visual Indicators

### Your Tank
- **Color**: Neon green (#00ff88)
- **Name Tag**: Above tank (white text)
- **Health Bar**: Green/yellow/red based on HP

### Enemy Tanks
- **Color**: Red (#ff4444)
- **Name Tag**: Above tank (white text)
- **Turret**: Shows aim direction

### Effects
- **Bullet Trail**: Red line showing projectile path
- **Explosion**: Particle burst on death
- **Shield Glow**: Cyan aura around protected tank
- **Screen Shake**: Slight shake when firing

## Minimap

### Legend
- **Green Dot** (◉): Your position
- **Red Dots** (○): Enemy positions
- **Grid**: Map coordinates

### Usage
- Quick glance to see enemy locations
- Plan escape routes
- Coordinate with team

## Performance Tips

### Optimal Settings
- **Browser**: Chrome or Edge recommended
- **Resolution**: 1920×1080 or higher
- **Connection**: Stable internet (>1 Mbps)

### If Lagging
1. Close other tabs
2. Disable browser extensions
3. Reduce window size
4. Check network connection

## Game Modes

### Free-for-All (Current)
- Everyone vs everyone
- First to highest score wins
- No teams, no mercy

### Coming Soon
- **Team Deathmatch**: Red vs Blue
- **Capture the Flag**: Base defense
- **Battle Royale**: Last tank standing

## Etiquette

### Do's ✅
- Say "gg" after matches
- Help newcomers
- Use voice for tactics
- Report bugs

### Don'ts ❌
- Spawn camping (boring)
- Trash talk (be respectful)
- Spam chat (annoying)
- Rage quit (stay for respawn)

## Achievements (Unofficial)

### Beginner
- [ ] First Kill
- [ ] 5 Kills in one session
- [ ] Collect all 3 power-up types

### Intermediate
- [ ] Kill 3 players in 30 seconds
- [ ] Win a match (highest score)
- [ ] 10+ Kill streak

### Expert
- [ ] Double kill (2 kills within 5 seconds)
- [ ] Win without dying
- [ ] Top the leaderboard for 5 minutes

## Troubleshooting

### Common Issues

#### "Connection lost"
- Server might be down
- Check `ws://localhost:8080/game` is running
- Refresh page to reconnect

#### Can't move/fire
- Click on canvas to focus
- Check keys aren't stuck
- Refresh if unresponsive

#### Voice not working
- Grant microphone permission
- Check browser console (F12)
- Toggle voice on/off to reset

#### Laggy gameplay
- Close other applications
- Check server terminal for errors
- Reduce browser zoom level

## Keyboard Shortcuts

| Key | Action |
|-----|--------|
| **WASD** | Movement |
| **Space** | Fire |
| **Enter** | Chat toggle |
| **ESC** | Close chat |
| **F5** | Refresh (reconnect) |
| **F11** | Fullscreen |
| **F12** | Developer tools |

## Pro Tips 🏆

1. **Always keep moving** - Stationary tanks are easy targets
2. **Watch the kill feed** - Learn who's dangerous
3. **Control the center** - More power-up spawns
4. **Count bullets** - Track enemy fire rate
5. **Use voice for callouts** - "Enemy at 3 o'clock!"
6. **Predict movement** - Aim where they'll be
7. **Don't chase kills** - Avoid tunnel vision
8. **Respawn strategy** - Use 3 seconds to plan
9. **Shield vs Speed** - Shield is safer, speed is aggressive
10. **Have fun!** - It's a game, enjoy it!

---

**Good luck on the battlefield, soldier!** 🎖️