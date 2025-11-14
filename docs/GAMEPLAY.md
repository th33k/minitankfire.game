# Gameplay Documentation

## Game Overview

Tank Arena is a fast-paced, real-time multiplayer tank battle arena game where players compete to achieve the highest score by eliminating opponents and collecting power-ups. The game features smooth top-down tank combat with mouse-based aiming and WASD movement controls.

## Core Gameplay

### Objective

- **Primary Goal**: Destroy enemy tanks to earn points
- **Scoring**: Each kill increases your score by 1
- **Win Condition**: Reach the target score (configurable, default: 10 kills)
- **Survival**: Avoid enemy fire and manage your health (100 HP)

### Game Mechanics

#### Movement System

**Controls:**
- `W` - Move forward
- `S` - Move backward  
- `A` - Strafe left
- `D` - Strafe right

**Movement Properties:**
- **Normal Speed**: 12 pixels/tick
- **With Speed Boost**: 20 pixels/tick
- **Movement Type**: 8-directional (WASD combinations)
- **Map Boundaries**: 1920x1080 pixels with collision detection

**Movement Features:**
- Smooth acceleration/deceleration
- Wall collision prevention
- Speed boost power-up effect
- Independent of aiming direction

#### Aiming & Shooting System

**Aiming:**
- Mouse cursor controls tank turret direction
- Tank body rotates independently from turret
- Optional aim line shows firing trajectory
- 360-degree aiming range

**Firing Mechanics:**
- **Fire Method**: Left mouse button click
- **Base Fire Rate**: 500ms between shots
- **Bullet Speed**: 50 pixels/tick
- **Bullet Damage**: 20 HP per hit
- **Bullet Lifetime**: 1.5 seconds before despawning
- **Range**: Approximately 75 units (1500 pixels at 50 px/tick)

#### Heat Management System

The weapon system includes a heat mechanic to prevent spam and encourage strategic shooting:

**Heat Mechanics:**
- **Heat Generation**: +20 heat per shot fired
- **Heat Decay**: -0.2 heat per game tick (when not firing)
- **Maximum Heat**: 100 (overheated)

**Fire Rate Penalties:**
| Heat Level | Fire Rate | Delay |
|-----------|-----------|-------|
| 0-39% | Normal | 500ms |
| 40-59% | Reduced | 800ms |
| 60-79% | Slow | 1200ms |
| 80-100% | Critical | 2000ms |

**Strategy Tips:**
- Burst fire instead of holding fire
- Let weapon cool between engagements
- Heat resets to 0 on death
- Monitor heat indicator in UI

### Power-Up System

Power-ups spawn randomly across the battlefield and provide temporary advantages.

#### Power-Up Types

##### 🛡️ Shield Power-Up
- **Effect**: Grants invincibility
- **Duration**: 5 seconds
- **Visual**: Blue aura around tank
- **Behavior**: 
  - Absorbs all incoming damage
  - Can still move and fire normally
  - Shield expires after duration or on death

##### ⚡ Speed Boost Power-Up
- **Effect**: Increases movement speed
- **Duration**: 3 seconds
- **Visual**: Yellow/orange trail effect
- **Speed Increase**: 12 → 20 pixels/tick (66% faster)
- **Behavior**:
  - Affects all movement directions
  - Stacks with normal controls
  - Good for dodging or closing distance

##### 🔫 Double Fire Power-Up
- **Effect**: Fire two bullets simultaneously
- **Duration**: 10 seconds
- **Visual**: Purple/magenta tank highlight
- **Behavior**:
  - Bullets spread at slight angles (±10 degrees)
  - Same heat generation as single shot
  - Doubled damage potential
  - Most powerful offensive power-up

#### Power-Up Spawning

- **Spawn Rate**: Random intervals
- **Spawn Locations**: Random coordinates on map
- **Lifetime**: 10 seconds (despawns if not collected)
- **Visual Indicator**: Pulsing glow effect
- **Collection**: Automatic on collision with tank

**Power-Up Collection Rules:**
- Only alive players can collect
- One power-up active per type simultaneously
- Power-ups of same type refresh duration
- All power-ups lost on death

### Health & Damage System

#### Health Mechanics

- **Starting Health**: 100 HP
- **Health Display**: HUD bar at top of screen
- **Color Indicators**:
  - Green: 70-100 HP (healthy)
  - Yellow: 40-69 HP (damaged)
  - Red: 1-39 HP (critical)

#### Damage Sources

| Source | Damage | Notes |
|--------|--------|-------|
| Bullet Hit | 20 HP | Standard projectile |
| Double Fire Hit | 20 HP each | Can hit with both bullets (40 HP total) |
| Shield Protected | 0 HP | No damage while shielded |

#### Death & Respawn

**Death Occurs When:**
- Health reaches 0
- Tank explodes with particle effect
- Killer gains +1 score
- Victim gains +1 death count

**Respawn System:**
- **Respawn Timer**: 3 seconds
- **Respawn Location**: Random safe position on map
- **Respawn State**: 
  - Full health (100 HP) restored
  - All power-ups removed
  - Heat reset to 0
  - Brief invincibility period (0.5s)

### Combat Mechanics

#### Bullet Physics

**Properties:**
- Straight-line trajectory
- No gravity or drop
- Instant velocity
- Small hitbox (8px diameter)

**Collision Detection:**
- Hits enemy players (not self)
- Despawns on hit
- Despawns after lifetime expires
- No bullet-to-bullet collision

#### Damage Calculation

```
Standard Hit:
  Player Health = Health - 20

Double Fire Hit (both bullets):
  Player Health = Health - 40

With Shield:
  Player Health = Health - 0 (no damage)
```

#### Kill Credit System

- Killer receives score point
- Victim's death count increases
- Kill notification appears in kill feed
- Leaderboard updates in real-time

### User Interface Elements

#### HUD Components

**Top Bar (Status Display):**
- Player name
- Current health bar with color coding
- Health percentage (numeric)
- Active power-up indicators with timers
- Heat level indicator

**Top-Right Corner:**
- Current FPS counter
- Network ping (ms)
- Kill/Death/K/D ratio

**Leaderboard (Right Side):**
- Ranked player list (top 10)
- Player names
- Kill counts
- Death counts
- K/D ratios
- Color coding: You (cyan), Others (white)

**Minimap (Bottom-Right):**
- Overhead battlefield view (200x150px)
- Your tank (cyan triangle)
- Other players (colored triangles)
- Power-ups (colored dots)
- Real-time position updates

**Kill Feed (Top-Left):**
- Recent kill notifications
- "X eliminated Y" format
- Fades after 5 seconds
- Shows last 5 kills

#### Settings Menu

**Accessible via gear icon. Includes:**

**Visual Settings:**
- Toggle aim line (aiming assistance)
- Toggle minimap visibility
- Toggle leaderboard display
- Enable/disable screen shake

**Audio Settings:**
- Sound effects volume
- Background music volume
- Mute all audio
- Individual effect toggles

**Voice Chat Settings:**
- Enable/disable voice chat
- Microphone selection
- Volume controls
- Push-to-talk options

**Network Settings:**
- Show/hide ping display
- Connection quality indicator

### Chat System

**Features:**
- Real-time text chat
- Visible to all players
- Message history (last 20 messages)
- Auto-scroll to newest

**Usage:**
1. Press `Enter` to open chat input
2. Type message (max 200 characters)
3. Press `Enter` to send
4. Press `Esc` to cancel

**Chat Display:**
- Location: Bottom-left corner
- Format: `[PlayerName]: Message`
- Color-coded by player
- Fades after 10 seconds of inactivity

### Voice Chat (Optional)

**WebRTC-based voice communication:**

**Features:**
- Peer-to-peer voice chat
- Low latency
- Proximity-based volume (future)
- Mute/unmute controls

**Setup:**
1. Enable in settings menu
2. Grant microphone permissions
3. Select input device
4. Adjust volume levels

**Indicators:**
- Microphone icon shows active speakers
- Volume bars for each player
- Muted status visible

## Advanced Strategies

### Combat Tactics

#### Offensive Strategies

**1. Aggressive Rush**
- Use speed boost to close distance
- Fire while approaching
- Circle strafe around target
- Best against: Stationary players

**2. Power-Up Control**
- Memorize spawn locations
- Rush to power-ups
- Deny enemy collection
- Best with: Shield + Double Fire combo

**3. Ambush Tactics**
- Wait near map edges
- Surprise low-health players
- Use cover effectively
- Fire first for advantage

**4. Burst Fire**
- Fire 2-3 shots rapidly
- Retreat while cooling
- Avoid heat penalties
- Maximize damage output

#### Defensive Strategies

**1. Kiting**
- Move backward while firing
- Maintain distance from pursuer
- Use speed boost to escape
- Effective when low health

**2. Shield Management**
- Activate during enemy fire
- Push aggressively while invulnerable
- Deny area with immunity
- Best for: Securing kills

**3. Evasive Movement**
- Constant direction changes
- Diagonal movement (harder to hit)
- Unpredictable patterns
- Use speed boost for dodging

**4. Heat Management**
- Burst → Dodge → Burst pattern
- Never overheat in combat
- Retreat if overheated
- Plan shots carefully

### Power-Up Synergies

**Best Combinations:**

1. **Shield + Double Fire** (Ultimate Combo)
   - Invincible with double damage
   - Duration: Limited by shorter timer
   - Strategy: Aggressive push

2. **Speed Boost + Shield**
   - Fast invulnerable tank
   - Duration: 3 seconds (speed duration)
   - Strategy: Reckless positioning

3. **Speed Boost + Double Fire**
   - High mobility offense
   - Duration: 3 seconds (speed duration)
   - Strategy: Hit-and-run attacks

### Map Awareness

**Key Principles:**

1. **Use Minimap**
   - Check frequently
   - Track enemy positions
   - Anticipate movement
   - Avoid ambushes

2. **Power-Up Timing**
   - Mental timer for spawns
   - Rush to spawn locations
   - Deny enemy power-ups

3. **Positioning**
   - Stay near map center (more options)
   - Avoid corners (trapped)
   - Keep escape routes open
   - Control high-traffic areas

4. **Awareness**
   - Monitor kill feed
   - Track player count
   - Identify threats
   - Target low-health enemies

### Scoring Strategies

**Maximize K/D Ratio:**

1. **Target Selection**
   - Weakened enemies first
   - Avoid shielded players
   - Focus distracted players
   - Confirm kills

2. **Survival Priority**
   - Don't trade kills
   - Retreat at low health
   - Use power-ups defensively
   - Better alive than dead

3. **Kill Stealing** (controversial)
   - Finish weak enemies
   - Third-party fights
   - Clean up after battles
   - Efficient but risky

4. **Farming Strategy**
   - Control power-up spawns
   - Let others fight first
   - Clean up survivors
   - High reward, medium risk

## Game Modes

### Current Mode: Free-For-All

- All players compete individually
- First to target score wins
- No teams or alliances
- Respawn enabled
- No score limit (configurable)

### Future Modes (Planned)

- **Team Deathmatch**: 2-4 teams competing
- **Capture the Flag**: Base capture objectives
- **King of the Hill**: Control zone scoring
- **Last Tank Standing**: No respawns
- **Time Trial**: Score rush in time limit

## Performance & Technical

### Network Requirements

**Recommended:**
- Ping: < 50ms (excellent)
- Ping: 50-100ms (good)
- Ping: 100-200ms (playable)
- Ping: > 200ms (laggy)

**Bandwidth:**
- Upstream: ~50 KB/s per player
- Downstream: ~100 KB/s per player

### Client Performance

**Minimum Requirements:**
- Modern browser (Chrome/Firefox/Edge)
- 60 FPS rendering capability
- WebSocket support
- Canvas 2D support
- 4 GB RAM

**Optimal Experience:**
- Gaming mouse (better aim)
- 144Hz monitor (smoother)
- Wired internet (lower latency)
- Dedicated GPU (higher FPS)

### Server Capacity

- **Max Players**: 100 concurrent
- **Tick Rate**: 20 FPS (50ms)
- **Update Rate**: 20 updates/second to each client
- **Latency**: < 10ms server processing

## Tips & Tricks

### Beginner Tips

1. **Learn the Controls**
   - Practice movement in safe areas
   - Get comfortable with mouse aiming
   - Test fire rates and bullet travel

2. **Watch Your Health**
   - Retreat at 40 HP or lower
   - Use health bar color indicators
   - Don't engage at critical health

3. **Start Conservatively**
   - Avoid early deaths
   - Observe other players
   - Learn power-up locations
   - Practice aiming

4. **Use the Minimap**
   - Check constantly
   - Avoid crowded areas when weak
   - Find isolated targets

### Advanced Tips

1. **Lead Your Shots**
   - Predict enemy movement
   - Aim ahead of moving targets
   - Account for bullet travel time

2. **Strafe Shooting**
   - Move side-to-side while firing
   - Harder to hit
   - Maintain accuracy

3. **Heat Management**
   - Never exceed 80% heat in combat
   - Cooldown between engagements
   - Burst fire > sustained fire

4. **Power-Up Routes**
   - Plan paths between spawns
   - Minimize backtracking
   - Collect while rotating

5. **Psychological Warfare**
   - Bait enemies into traps
   - Fake retreats
   - Control power-ups
   - Deny enemy resources

6. **Spawn Camping** (risky)
   - Watch respawn locations
   - Quick elimination
   - Vulnerable to third parties
   - High risk, high reward

## Frequently Asked Questions

**Q: How do I know if I hit someone?**
A: The bullet disappears on impact, and you'll see the target's health decrease in their status bar.

**Q: Why can't I fire?**
A: Check your heat level. Wait for cooldown if overheated (red indicator).

**Q: Do power-ups stack?**
A: Same-type power-ups refresh duration. Different types stack (e.g., shield + speed).

**Q: Can I shoot through other players?**
A: No, bullets stop on first player hit.

**Q: What happens when I disconnect?**
A: Your tank is removed from the game. Reconnect to start fresh.

**Q: Can I change my name mid-game?**
A: No, you must disconnect and rejoin with a new name.

**Q: Is friendly fire enabled?**
A: Currently no teams, so all players are enemies. Your bullets don't hit yourself.

**Q: How are spawn locations chosen?**
A: Random positions on the map, avoiding current player locations.

**Q: Can I spectate after death?**
A: No, but you respawn after 3 seconds automatically.

**Q: What's the max player limit?**
A: Server supports up to 100 concurrent players.

---

**Last Updated**: November 14, 2025

Enjoy the battle! 🎮💥
