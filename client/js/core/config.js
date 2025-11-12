// Game configuration constants
export const CONFIG = {
    CANVAS: {
        WIDTH: 1920,
        HEIGHT: 1080,
        GRID_SIZE: 50
    },
    
    MINIMAP: {
        WIDTH: 200,
        HEIGHT: 150,
        GRID_SIZE_X: 40,
        GRID_SIZE_Y: 30
    },
    
    PLAYER: {
        SIZE: 36,
        BARREL_LENGTH: 25,
        BARREL_OFFSET: 8,
        SPEED_NORMAL: 12,
        SPEED_BOOSTED: 20,
        MIN_X: 15,
        MAX_X: 1905,
        MIN_Y: 15,
        MAX_Y: 1065
    },
    
    WEAPON: {
        FIRE_RATE: 500,
        BASE_DAMAGE: 25,
        HEAT_INCREASE: 20,
        HEAT_DECAY_RATE: 0.2,
        MAX_HEAT: 100,
        FIRE_RATES: {
            NORMAL: 500,
            HEAT_40: 800,
            HEAT_60: 1200,
            HEAT_80: 2000
        }
    },
    
    BULLET: {
        RADIUS: 4,
        TRAIL_LENGTH: 2
    },
    
    POWERUP: {
        RADIUS: 14,
        PULSE_SPEED: 200,
        PULSE_AMOUNT: 3,
        COLORS: {
            SHIELD: '#00ffff',
            SPEED_BOOST: '#ffff00',
            DOUBLE_FIRE: '#ff6600'
        }
    },
    
    EFFECTS: {
        EXPLOSION_PARTICLES: 30,
        PARTICLE_DECAY: 0.02,
        SHIELD_PULSE_SPEED: 200,
        SHIELD_RADIUS: 30,
        ANIMATION_DURATION: 1000,
        NOTIFICATION_DURATION: 3000
    },
    
    RESPAWN: {
        COUNTDOWN: 3,
        HEALTH: 100
    }
};
