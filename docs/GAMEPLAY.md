# 🎮 Gameplay Guide - Mini Tank Fire

## Table of Contents

1. [Getting Started](#getting-started)
2. [Basic Controls](#basic-controls)
3. [Game Mechanics](#game-mechanics)
4. [Combat System](#combat-system)
5. [Power-ups](#power-ups)
6. [Scoring System](#scoring-system)
7. [Tips & Strategies](#tips--strategies)
8. [Game Modes](#game-modes)
9. [User Interface](#user-interface)

---

## Getting Started

### First Time Setup

1. **Create Your Callsign**
   - Enter your player name on the join screen (max 20 characters)
   - Your name appears above your tank in-game
   - Visible on leaderboard and kill feed

2. **Choose Server**
   - Default: `localhost` (local machine)
   - Remote: Enter server IP address
   - Port: 8080 (fixed, no entry needed)

3. **Click "Deploy to Battle"**
   - Your tank spawns at random location
   - Green outline = your tank
   - Red outline = enemies
   - Blue outline = dead/respawning

### The Game Arena

```
┌─────────────────────────────────────────┐
│  1920 pixels wide × 1080 pixels tall     │
│                                         │
│  ┌─────────────────────────────────────┐│
│  │                                     ││
│  │    Spawn points: Scattered randomly ││
│  │    Safe zone: None (entire map)     ││
│  │    Boundaries: Wrap around (TBD)    ││
│  │                                     ││
│  └─────────────────────────────────────┘│
└─────────────────────────────────────────┘
```

---

## Basic Controls

### Movement

| Key | Action | Effect |
|-----|--------|--------|
| **W** | Move Up | Tank moves upward on screen |
| **A** | Move Left | Tank moves left on screen |
| **S** | Move Down | Tank moves downward on screen |
| **D** | Move Right | Tank moves right on screen |
| **Arrow Keys** | Alternate movement | Same as WASD |

**Movement Speed**:
- Base: 3 pixels per frame (60 FPS = 180px/sec)
- With Speed Boost: 4.5 pixels per frame (+50%)

**Movement Behavior**:
- Can move diagonally (combine keys)
- Momentum: Changes direction instantly
- Wrapping: Tank wraps at map edges (TBD)

### Aiming & Firing

| Input | Action |
|-------|--------|
| **Mouse Move** | Aim turret at cursor |
| **Left Click** | Fire bullet |
| **Spacebar** | Alternative fire button (TBD) |

**Firing Mechanics**:
- Rate of fire: 1 shot per 500ms (2 shots/sec)
- Bullet speed: 8 pixels per frame
- Bullet range: Infinite (until hit)
- Max bullets per player: Unlimited (server-side limit)
- Double Fire power-up: 2 bullets per shot at 45° angle

### User Interface

| Key | Action | Effect |
|-----|--------|--------|
| **Enter** | Toggle Chat | Opens/closes chat input |
| **Type & Enter** | Send Message | Broadcasts to all players |
| **Escape** | Close Chat | Hides chat input |
| **Microphone Icon** | Toggle Voice | P2P voice chat with all players |

---

## Game Mechanics

### Tank Movement

```
Your Tank (Green):
  ┌─────┐
  │ 🎯  │ ← Turret rotates to mouse
  │ │░│ │ ← Tank body
  └─────┘

Direction: Always faces forward
Rotation Speed: Instant (no acceleration)
Collision: Tank vs Tank = Push apart (TBD)
```

### Bullets

**Bullet Behavior**:
- Travels in straight line at fired angle
- Doesn't affected by bullet owner's tank
- **Friendly Fire**: Disabled (bullets pass through allies)
- **Hit Detection**: Circles based on positions
- **Piercing**: Single bullet = single hit

**Bullet Properties**:
```json
{
  "owner": "Player UUID",
  "position": [x, y],
  "velocity": [vx, vy],
  "speed": 8,
  "lifetime": "infinite (until map edge or hit)",
  "damage": 1
}
```

### Collision Detection

**Bullet vs Player**:
- Server calculates: `distance < (bullet_size + tank_size)`
- Hit registration: Server-authoritative
- Response: Player takes damage, bullet destroyed
- Kill credit: Given to bullet owner

**Tank vs Tank**:
- Collision: Server-side physics
- Response: Tanks pushed apart slightly
- No damage: Collision doesn't cause damage

**Tank vs Map Edge**:
- Behavior: TBD (wrap around or bounce)
- Currently: Assumed to wrap

### Player Death & Respawn

**Death Conditions**:
- Health reaches 0 (single bullet hit)
- Kill credited to bullet owner

**Respawn Process**:
1. Player marked as dead (3-second cooldown)
2. Respawn timer displayed on screen
3. Tank invisible during respawn
4. Random spawn location (no safe zone)
5. Respawn: Health restored to 100

**Score Impact**:
- Killer: +1 point
- Killed: -1 point
- Death feed shows: "Player1 killed Player2"

---

## Combat System

### Damage Model

```
Action              Damage    Notes
─────────────────────────────────────
Direct Hit (bullet)    1      Instant kill
Head-on collision      0      No damage
Friendly fire          0      Disabled
```

### Health System

- **Health Points**: 100 (per player)
- **Display**: Health bar above tank in-game + HUD
- **Damage per hit**: 100 (one-hit kill)
- **Regeneration**: None (killed instantly)
- **Shield Protection**: Negates one hit

### Shield Power-up

When you have a Shield:
- Visual indicator: Cyan glowing border
- Effect: Absorb next incoming hit
- Health protection: Takes damage as usual but shield absorbs
- Activation: Automatic on next hit
- Loss: Shield disappears after protecting once

---

## Power-ups

### Power-up Types

#### 🛡️ Shield
- **Duration**: 5 seconds
- **Effect**: Absorb one hit without damage
- **Visual**: Cyan glow around tank
- **Use**: Defensive play, risky encounters
- **Rarity**: Common

**Strategy**:
- Grab before engaging enemies
- Hold ground more confidently
- Still avoid multiple hits in short time

#### ⚡ Speed Boost
- **Duration**: 3 seconds
- **Effect**: +50% movement speed
- **Visual**: Yellow glow + trailing effect
- **Use**: Escape danger, chase enemies
- **Rarity**: Common

**Strategy**:
- Use to dodge incoming fire
- Chase fleeing enemies
- Reposition quickly before speed decays

#### 🔥 Double Fire
- **Duration**: 10 seconds
- **Effect**: Fire 2 bullets per shot
- **Spread**: ±45° from aim angle
- **Visual**: Magenta glow
- **Use**: Offense, area denial
- **Rarity**: Rare (valuable!)

**Strategy**:
- Best for raw offensive power
- Increases hit probability
- Cover more area with shots
- Longest duration = most impactful

### Collecting Power-ups

1. Power-up appears on map at random location
2. Tank touches it (no click needed)
3. Effect applies immediately
4. Duration timer starts
5. HUD shows which power-ups are active
6. When duration ends, effect disappears

**Spawn Rate**:
- New power-up spawns: Every 10 seconds (1 per spawn)
- Max active power-ups: 5 on map
- Types are random

---

## Scoring System

### Points

| Event | Points |
|-------|--------|
| Kill enemy player | +1 |
| Die/Get killed | -1 |
| Collect power-up | +0.5 |
| **Current record** | Leaderboard |

### Leaderboard

**Displayed**:
- Top 10 players by score
- Real-time updates
- Your rank highlighted
- Color-coded: Gold (1st), Silver (2nd), Bronze (3rd)

**Updated**:
- Every game update (20 Hz)
- Kill/death events immediately
- Power-up collection on next update

### Kill Feed

**Shows**:
- "Player1 killed Player2"
- Timestamp
- Appears top-right for 5 seconds
- Fades out gradually
- Stacks for multiple kills

---

## Tips & Strategies

### Beginner Tips

1. **Master Movement First**
   - Learn to move smoothly in all directions
   - Get comfortable with WASD + mouse combination
   - Practice circle strafing

2. **Positioning > Aiming**
   - Don't stand still (easy target)
   - Use map terrain mentally
   - Predict enemy movements

3. **Peek-a-Boo Tactics**
   - Peek to see enemy
   - Duck back to safety
   - Return fire when they're vulnerable

4. **Power-up Priority**
   - In trouble? Grab shield
   - Chasing? Use speed boost
   - Going aggressive? Double fire

### Intermediate Strategies

#### 1. **Kiting**
```
Technique: 
  1. Get close to enemy
  2. Fire
  3. Immediately move away
  4. Rotate 180°
  5. Repeat while maintaining distance

Advantage: You see enemy, they don't
```

#### 2. **Predict Dodging**
- Don't aim where tank is
- Aim where tank is GOING
- Watch movement patterns
- Lead shots for moving targets

#### 3. **Resource Management**
- Know where power-ups spawn
- Time your rotations
- Defend power-up locations
- Build up shield before engaging

#### 4. **Map Control**
- Keep central areas if winning
- Stay mobile if losing
- Use corners for cover
- Minimize exposure time

### Advanced Strategies

#### 1. **Double Fire Combos**
```
With Double Fire Power-up:
- Increased coverage area
- Higher hit probability
- Use in crowded areas
- Spam near chokepoints
```

#### 2. **Group Tactics**
- Coordinate with allies in chat
- Crossfire enemies
- Guard power-up spawns together
- Rotate who grabs power-ups

#### 3. **Psychological Play**
- Use chat strategically
- Fake retreats
- Bait aggressive players
- Build pressure with presence

#### 4. **Health Awareness**
- Always know your status
- Don't risk with low health
- Grab shield when weak
- Use speed boost to reset

---

## Game Modes

### Current

**Free For All (FFA)**
- All players vs. everyone
- Last player standing wins match
- Continuous spawning after death
- Scoring based on kills
- No teams

### Planned

- [ ] Team Deathmatch (Red vs Blue)
- [ ] Capture The Flag (CTF)
- [ ] King of the Hill
- [ ] Payload (Escort mode)

---

## User Interface

### In-Game HUD

```
┌─────────────────────────────────────────────────────┐
│  KILLS: 5    DEATHS: 2    HEALTH: ████████░░ 80% │  ← Top bar
├──────────────────────────────────────────────────────┤
│  ┌──────────────────────────────────────────────┐  │
│  │                                              │  │
│  │         GAME CANVAS (1920×1080)               │  │
│  │                                              │  │
│  │         [Your tank and enemy tanks]          │  │
│  │                                              │  │
│  └──────────────────────────────────────────────┘  │
│                                                    │
│  Right Side:                      Left Side:       │
│  ┌─────────────────┐             ┌──────────────┐ │
│  │  POWER-UPS      │             │ LEADERBOARD  │ │
│  │  ⚡ 3s left     │             │              │ │
│  │  🛡️ 2s left     │             │ 🥇 Player2 8 │ │
│  │                 │             │ 🥈 Player1 5 │ │
│  │                 │             │ 🥉 You    3  │ │
│  └─────────────────┘             │ 4. Player3 2 │ │
│                                  │ 5. Player4 0 │ │
│  Bottom Left:                    └──────────────┘ │
│  ┌──────────────────┐                             │
│  │ CHAT             │            MINIMAP:         │
│  │ Player1: Attack! │            ┌──────────────┐ │
│  │ You: Let's go!   │            │ ▓▓▓▓▓▓▓▓▓▓▓▓ │ │
│  │ [Type message]   │            │ ▓ ● ▓ ▓ ○ ▓▓ │ │
│  └──────────────────┘            │ ▓▓ ○ ▓ ▓ ▓▓▓ │ │
│                                  │ ▓▓▓▓▓▓▓▓▓▓▓▓ │ │
│                                  └──────────────┘ │
└─────────────────────────────────────────────────────┘
```

### Minimap

- **Size**: ~150×100 pixels
- **Position**: Bottom-right corner
- **Shows**: 
  - Your tank: Green circle (●)
  - Enemies: Red circles (●)
  - Power-ups: Yellow dots
  - Map boundaries
- **Update**: Real-time

### Chat System

- **Position**: Bottom-left corner
- **History**: Last 20 messages
- **Colors**: Different color per player
- **Activation**: Press Enter
- **Controls**: 
  - Type message
  - Press Enter to send
  - Press Escape to close without sending

---

## Common Questions

**Q: What happens when I die?**
A: You respawn after 3 seconds at a random location on the map.

**Q: Can I friendly fire?**
A: No, bullets pass through your tank. You can only damage enemies.

**Q: How long do power-ups last?**
A: Shield (5s), Speed Boost (3s), Double Fire (10s).

**Q: Can I have multiple power-ups?**
A: Yes! Stack effects. You can have Shield + Speed Boost + Double Fire simultaneously.

**Q: What's my health?**
A: 100 HP. One bullet = instant death (no partial damage). Exception: Shield absorbs first hit.

**Q: How do I join with friends?**
A: Have them connect to same server IP. They'll appear in the game automatically.

**Q: Can I customize my tank?**
A: Currently no. Planned for future updates.

**Q: Is there voice chat?**
A: Yes! Click the microphone icon to toggle P2P voice.

---

## Game Statistics

### Performance Targets

| Metric | Value |
|--------|-------|
| Server Updates | 20 FPS (50ms) |
| Client Renders | 60 FPS |
| Latency | < 100ms typical |
| Max Players | 100 concurrent |
| Bullet Speed | 8 px/frame |
| Tank Speed | 3 px/frame (base) |
| Fire Rate | 2 shots/sec |

### Balance Numbers

| Parameter | Value |
|-----------|-------|
| Bullet Damage | 100 HP |
| Player Health | 100 HP |
| Shield Duration | 5 seconds |
| Speed Boost | +50% for 3 seconds |
| Double Fire | 2 bullets for 10 seconds |
| Map Width | 1920 pixels |
| Map Height | 1080 pixels |

---

## Conclusion

**Mini Tank Fire** is about:
- ✅ **Fast-paced action** - Constant combat
- ✅ **Skill-based gameplay** - Positioning > luck
- ✅ **Team cooperation** - Chat & coordination
- ✅ **Strategic depth** - Power-up management
- ✅ **Accessibility** - Easy to learn, hard to master

**Have fun and dominate the arena!** 🎮
