# Ping Display Location

## Screen Layout

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  ┌─ TOP BAR ──────────────────────────────────────────────────────────────┐ │
│  │  ❤️ 100    🎯 0    💀 0    📶 45 ms    [POWERUP]    [🎤] [⚙️]        │ │
│  │  └──┬──┘   └─┬─┘   └─┬─┘    └──┬───┘                                    │ │
│  │   Health  Kills  Deaths   PING ← NEW!                                   │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                                                              │
│  ┌─ LEADERBOARD ──┐                                    ┌─ KILL FEED ──────┐ │
│  │ 🏆 LEADERBOARD │                                    │ Player1 💀 You   │ │
│  │  #1 Tank123: 5│                                    └──────────────────┘ │
│  │  #2 You: 3    │                                                          │
│  │  #3 Pro99: 2  │                                                          │
│  └───────────────┘                                                          │
│                                                                              │
│                         GAME CANVAS AREA                                    │
│                     (Tanks, Bullets, PowerUps)                              │
│                                                                              │
│                                                                              │
│  ┌─ HEAT BAR ──┐  ┌─ MINIMAP ──┐                     ┌─ CHAT ────────────┐ │
│  │ H │         │  │ ┌─────────┐ │                     │ 💬 TEAM CHAT      │ │
│  │ E │  100%   │  │ │  ●   ■  │ │                     │ Player: Hi!       │ │
│  │ A │█████████│  │ │    ●    │ │                     │ Tank99: GG        │ │
│  │ T │█████████│  │ │  ■      │ │                     │ [Type message...] │ │
│  └───┴─────────┘  └─┴─────────┴─┘                     └───────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Ping Indicator Details

### Position

- **Location**: Top-left section of screen
- **Container**: `#top-bar > .hud-section > #player-stats`
- **Order**: 4th item (after Health, Kills, Deaths)

### Visual Appearance

```
┌─────────────┐
│ 📶 45 ms   │  ← Green (Good: <50ms)
└─────────────┘

┌─────────────┐
│ 📶 75 ms   │  ← Orange (Medium: 50-100ms)
└─────────────┘

┌─────────────┐
│ 📶 150 ms  │  ← Red (Bad: >100ms)
└─────────────┘
```

### Color Coding System

| Ping Range | Color  | Icon Color | Text Color | Connection Quality |
| ---------- | ------ | ---------- | ---------- | ------------------ |
| 0-49ms     | Green  | #00ff88    | #00ff88    | Excellent ⚡       |
| 50-99ms    | Orange | #ffaa00    | #ffaa00    | Good ✓             |
| 100ms+     | Red    | #ff4444    | #ff4444    | Poor ⚠️            |

### Responsive Behavior

#### Desktop (>1250px)

- Full visibility with icon + value + unit
- Font size: 1.1rem
- Padding: 8px 15px

#### Tablet (768px - 1250px)

- Slightly reduced padding: 6px 10px
- Font size: 0.9rem
- Still fully visible

#### Mobile (<768px)

- Condensed padding: 5px 8px
- Font size: 0.8rem
- Icon + value only (unit may wrap)

## Accessibility Features

- ARIA label: "Ping: X milliseconds"
- Tooltip: "Network Latency"
- High contrast colors for all connection states
- Updates every 2 seconds (non-intrusive)
- No animation (reduces motion sickness)

## Technical Integration

The ping display integrates seamlessly with existing UI:

- Uses same styling as other stats (`.stat-item`)
- Updates via `UIManager.updateHUD()`
- Data source: `NetworkManager.getPing()`
- No layout shifts (fixed space allocation)
