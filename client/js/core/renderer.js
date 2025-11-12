import { CONFIG } from '../core/config.js';

// Renderer - Handles all canvas rendering
export class Renderer {
    constructor(canvas, minimapCanvas) {
        this.canvas = canvas;
        this.ctx = canvas.getContext('2d');
        this.minimapCanvas = minimapCanvas;
        this.minimapCtx = minimapCanvas ? minimapCanvas.getContext('2d') : null;
        this.particles = [];
    }

    render(players, bullets, powerUps, myPlayer, playerId, mouseX, mouseY, angle, aimLineEnabled, heatLevel, maxHeatLevel) {
        const ctx = this.ctx;
        ctx.clearRect(0, 0, CONFIG.CANVAS.WIDTH, CONFIG.CANVAS.HEIGHT);

        this.renderBackground();
        this.renderPowerUps(powerUps);
        this.renderBullets(bullets);
        this.renderParticles();
        this.renderPlayers(players, myPlayer, playerId, mouseX, mouseY, angle, aimLineEnabled, heatLevel, maxHeatLevel);
    }

    renderBackground() {
        const ctx = this.ctx;
        ctx.strokeStyle = 'rgba(0, 255, 136, 0.1)';
        ctx.lineWidth = 1;
        
        for (let x = 0; x <= CONFIG.CANVAS.WIDTH; x += CONFIG.CANVAS.GRID_SIZE) {
            ctx.beginPath();
            ctx.moveTo(x, 0);
            ctx.lineTo(x, CONFIG.CANVAS.HEIGHT);
            ctx.stroke();
        }
        
        for (let y = 0; y <= CONFIG.CANVAS.HEIGHT; y += CONFIG.CANVAS.GRID_SIZE) {
            ctx.beginPath();
            ctx.moveTo(0, y);
            ctx.lineTo(CONFIG.CANVAS.WIDTH, y);
            ctx.stroke();
        }
    }

    renderPowerUps(powerUps) {
        const ctx = this.ctx;
        
        Object.values(powerUps).forEach(pu => {
            const color = CONFIG.POWERUP.COLORS[pu.type] || '#ffffff';
            ctx.fillStyle = color;
            ctx.shadowBlur = 20;
            ctx.shadowColor = color;
            
            ctx.save();
            ctx.translate(pu.x, pu.y);
            
            const rotation = (Date.now() / 500) % (2 * Math.PI);
            ctx.rotate(rotation);
            
            if (pu.type === 'SHIELD') {
                this.renderShieldPowerUp(color);
            } else if (pu.type === 'DOUBLE_FIRE') {
                this.renderDoubleFirePowerUp(color, rotation);
            } else if (pu.type === 'SPEED_BOOST') {
                this.renderSpeedBoostPowerUp(color, rotation);
            } else {
                ctx.beginPath();
                ctx.arc(0, 0, CONFIG.POWERUP.RADIUS, 0, Math.PI * 2);
                ctx.fill();
            }
            
            ctx.restore();
            ctx.shadowBlur = 0;
        });
    }

    renderShieldPowerUp(color) {
        const ctx = this.ctx;
        const pulseSize = CONFIG.POWERUP.RADIUS + Math.sin(Date.now() / CONFIG.POWERUP.PULSE_SPEED) * CONFIG.POWERUP.PULSE_AMOUNT;
        
        ctx.strokeStyle = color;
        ctx.lineWidth = 3;
        ctx.beginPath();
        ctx.arc(0, 0, pulseSize, 0, Math.PI * 2);
        ctx.stroke();
        
        ctx.fillStyle = color;
        ctx.globalAlpha = 0.3;
        ctx.beginPath();
        ctx.arc(0, 0, pulseSize - 3, 0, Math.PI * 2);
        ctx.fill();
        ctx.globalAlpha = 1;
        
        ctx.fillStyle = color;
        ctx.font = 'bold 16px Arial';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText('⬢', 0, 0);
    }

    renderDoubleFirePowerUp(color, rotation) {
        const ctx = this.ctx;
        ctx.fillStyle = color;
        
        ctx.save();
        ctx.translate(-8, -2);
        ctx.rotate(-rotation);
        ctx.beginPath();
        ctx.arc(0, 0, 5, 0, Math.PI * 2);
        ctx.fill();
        ctx.restore();
        
        ctx.save();
        ctx.translate(8, 2);
        ctx.rotate(-rotation);
        ctx.beginPath();
        ctx.arc(0, 0, 5, 0, Math.PI * 2);
        ctx.fill();
        ctx.restore();
        
        const sparkCount = 4;
        for (let i = 0; i < sparkCount; i++) {
            const sparkAngle = (i / sparkCount) * Math.PI * 2;
            const sparkX = Math.cos(sparkAngle) * 12;
            const sparkY = Math.sin(sparkAngle) * 12;
            ctx.fillStyle = color;
            ctx.globalAlpha = 0.6;
            ctx.fillRect(sparkX - 1, sparkY - 1, 2, 6);
        }
        ctx.globalAlpha = 1;
    }

    renderSpeedBoostPowerUp(color, rotation) {
        const ctx = this.ctx;
        const lightningPoints = [
            {x: 0, y: -15}, {x: 5, y: -5}, {x: 2, y: 0},
            {x: 8, y: 5}, {x: 0, y: 15}, {x: -3, y: 5},
            {x: 0, y: 2}, {x: -5, y: -8}
        ];
        
        ctx.fillStyle = color;
        ctx.beginPath();
        ctx.moveTo(lightningPoints[0].x, lightningPoints[0].y);
        lightningPoints.forEach(p => ctx.lineTo(p.x, p.y));
        ctx.closePath();
        ctx.fill();
        
        ctx.strokeStyle = color;
        ctx.lineWidth = 2;
        ctx.globalAlpha = 0.5;
        ctx.stroke();
        ctx.globalAlpha = 1;
    }

    renderBullets(bullets) {
        const ctx = this.ctx;
        ctx.fillStyle = '#ff0000';
        ctx.shadowBlur = 10;
        ctx.shadowColor = '#ff0000';
        
        Object.values(bullets).forEach(b => {
            ctx.beginPath();
            ctx.arc(b.x, b.y, CONFIG.BULLET.RADIUS, 0, Math.PI * 2);
            ctx.fill();
            
            ctx.strokeStyle = 'rgba(255, 0, 0, 0.3)';
            ctx.lineWidth = 2;
            ctx.beginPath();
            ctx.moveTo(b.x, b.y);
            ctx.lineTo(b.x - b.dx * CONFIG.BULLET.TRAIL_LENGTH, b.y - b.dy * CONFIG.BULLET.TRAIL_LENGTH);
            ctx.stroke();
        });
        
        ctx.shadowBlur = 0;
    }

    renderParticles() {
        const ctx = this.ctx;
        
        this.particles.forEach(p => {
            ctx.fillStyle = p.color;
            ctx.globalAlpha = p.life;
            ctx.fillRect(p.x - 2, p.y - 2, 4, 4);
        });
        
        ctx.globalAlpha = 1;
    }

    renderPlayers(players, myPlayer, playerId, mouseX, mouseY, angle, aimLineEnabled, heatLevel, maxHeatLevel) {
        const ctx = this.ctx;
        
        Object.values(players).forEach(p => {
            if (!p.alive) return;
            
            const isMe = p.id === playerId;
            ctx.fillStyle = isMe ? '#00ff88' : '#ff4444';
            
            this.renderShieldEffect(p);
            this.renderBonusCollectionEffect(p);
            this.renderTankBody(p, ctx);
            this.renderPowerUpGlow(p, ctx);
            this.renderTurret(p, isMe, angle, ctx);
            this.renderAimLine(p, isMe, mouseX, mouseY, aimLineEnabled, ctx);
            this.renderHeatGlow(p, isMe, heatLevel, maxHeatLevel, angle, ctx);
            this.renderHealthBar(p, ctx);
            this.renderNameTag(p, ctx);
            this.renderSpeedBoostTrail(p, ctx);
        });
    }

    renderShieldEffect(p) {
        if (!p.shield) return;
        
        const ctx = this.ctx;
        const pulseAmount = Math.sin(Date.now() / 200) * 0.5 + 1.5;
        const shieldRadius = 30 + pulseAmount;
        
        ctx.strokeStyle = '#00ffff';
        ctx.lineWidth = 2 + pulseAmount * 0.5;
        ctx.shadowBlur = 20;
        ctx.shadowColor = '#00ffff';
        ctx.globalAlpha = 0.6 + Math.sin(Date.now() / 200) * 0.2;
        ctx.beginPath();
        ctx.arc(p.x, p.y, shieldRadius, 0, Math.PI * 2);
        ctx.stroke();
        ctx.globalAlpha = 1;
        
        ctx.strokeStyle = '#00ffff';
        ctx.lineWidth = 1;
        ctx.globalAlpha = 0.4;
        ctx.beginPath();
        ctx.arc(p.x, p.y, shieldRadius - 5, 0, Math.PI * 2);
        ctx.stroke();
        ctx.globalAlpha = 1;
        
        const cornerSize = 22 + pulseAmount * 0.3;
        ctx.strokeStyle = '#00ffff';
        ctx.lineWidth = 2.5;
        ctx.globalAlpha = 0.7;
        ctx.strokeRect(p.x - cornerSize, p.y - cornerSize, cornerSize * 2, cornerSize * 2);
        ctx.globalAlpha = 1;
        
        ctx.shadowBlur = 0;
    }

    renderBonusCollectionEffect(p) {
        if (!p.lastPowerUpCollectTime) return;
        
        const timeSinceCollect = Date.now() - p.lastPowerUpCollectTime;
        const animationDuration = CONFIG.EFFECTS.ANIMATION_DURATION;
        
        if (timeSinceCollect >= animationDuration) return;
        
        const ctx = this.ctx;
        const progress = timeSinceCollect / animationDuration;
        const expandRadius = 40 + (progress * 60);
        const fadeOut = 1 - progress;
        
        ctx.save();
        
        let bonusColor = '#ffff00';
        if (p.lastPowerUpType === 'SHIELD') {
            bonusColor = '#00ffff';
        } else if (p.lastPowerUpType === 'DOUBLE_FIRE') {
            bonusColor = '#ff6600';
        } else if (p.lastPowerUpType === 'SPEED_BOOST') {
            bonusColor = '#ffff00';
        }
        
        ctx.strokeStyle = bonusColor;
        ctx.globalAlpha = fadeOut;
        ctx.lineWidth = 3;
        ctx.shadowBlur = 20;
        ctx.shadowColor = bonusColor;
        ctx.beginPath();
        ctx.arc(p.x, p.y, expandRadius, 0, Math.PI * 2);
        ctx.stroke();
        
        for (let i = 0; i < 8; i++) {
            const particleAngle = (i / 8) * Math.PI * 2;
            const particleX = p.x + Math.cos(particleAngle) * expandRadius;
            const particleY = p.y + Math.sin(particleAngle) * expandRadius;
            ctx.fillStyle = bonusColor;
            ctx.beginPath();
            ctx.arc(particleX, particleY, 3, 0, Math.PI * 2);
            ctx.fill();
        }
        
        ctx.fillStyle = bonusColor;
        ctx.globalAlpha = fadeOut * 0.5;
        ctx.beginPath();
        ctx.arc(p.x, p.y, 25, 0, Math.PI * 2);
        ctx.fill();
        
        ctx.restore();
        ctx.globalAlpha = 1;
        ctx.shadowBlur = 0;
    }

    renderTankBody(p, ctx) {
        ctx.shadowBlur = 10;
        ctx.shadowColor = ctx.fillStyle;
        ctx.fillRect(p.x - 18, p.y - 18, 36, 36);
        ctx.shadowBlur = 0;
    }

    renderPowerUpGlow(p, ctx) {
        if (!p.shield && !p.speedBoost && !p.doubleFire) return;
        
        let glowColor = '#ffffff';
        let glowIntensity = 0;
        
        if (p.shield) {
            glowColor = '#00ffff';
            glowIntensity = 1;
        } else if (p.doubleFire) {
            glowColor = '#ff6600';
            glowIntensity = 0.9;
        } else if (p.speedBoost) {
            glowColor = '#ffff00';
            glowIntensity = 0.8;
        }
        
        const glowPulse = Math.sin(Date.now() / 150) * 0.3 + 0.7;
        ctx.strokeStyle = glowColor;
        ctx.lineWidth = 3 * glowIntensity;
        ctx.globalAlpha = glowPulse * glowIntensity;
        ctx.shadowBlur = 15 + (glowPulse * 10);
        ctx.shadowColor = glowColor;
        ctx.strokeRect(p.x - 20, p.y - 20, 40, 40);
        ctx.shadowBlur = 0;
        ctx.globalAlpha = 1;
    }

    renderTurret(p, isMe, angle, ctx) {
        const turretAngle = isMe ? angle : p.angle;
        const rad = turretAngle * Math.PI / 180;
        
        if (p.doubleFire) {
            const barrelOffset = CONFIG.PLAYER.BARREL_OFFSET;
            const barrelLength = CONFIG.PLAYER.BARREL_LENGTH;
            
            ctx.strokeStyle = '#ff6600';
            ctx.lineWidth = 4;
            ctx.globalAlpha = 0.8;
            ctx.shadowBlur = 15;
            ctx.shadowColor = '#ff6600';
            
            ctx.beginPath();
            ctx.moveTo(p.x - barrelOffset * Math.sin(rad), p.y + barrelOffset * Math.cos(rad));
            ctx.lineTo(p.x - barrelOffset * Math.sin(rad) + Math.cos(rad) * barrelLength, 
                       p.y + barrelOffset * Math.cos(rad) + Math.sin(rad) * barrelLength);
            ctx.stroke();
            
            ctx.beginPath();
            ctx.moveTo(p.x + barrelOffset * Math.sin(rad), p.y - barrelOffset * Math.cos(rad));
            ctx.lineTo(p.x + barrelOffset * Math.sin(rad) + Math.cos(rad) * barrelLength,
                       p.y - barrelOffset * Math.cos(rad) + Math.sin(rad) * barrelLength);
            ctx.stroke();
            
            ctx.globalAlpha = 1;
            ctx.shadowBlur = 0;
            
            this.renderMuzzleFlare(p, barrelOffset, barrelLength, rad, ctx);
        } else {
            ctx.strokeStyle = '#ffffff';
            ctx.lineWidth = 4;
            ctx.beginPath();
            ctx.moveTo(p.x, p.y);
            ctx.lineTo(p.x + Math.cos(rad) * CONFIG.PLAYER.BARREL_LENGTH, p.y + Math.sin(rad) * CONFIG.PLAYER.BARREL_LENGTH);
            ctx.stroke();
        }
    }

    renderMuzzleFlare(p, barrelOffset, barrelLength, rad, ctx) {
        const flareTime = Date.now() % 200;
        if (flareTime >= 100) return;
        
        const flareIntensity = 1 - (flareTime / 100);
        
        ctx.fillStyle = '#ffaa00';
        ctx.globalAlpha = flareIntensity * 0.6;
        
        ctx.beginPath();
        ctx.arc(p.x - barrelOffset * Math.sin(rad) + Math.cos(rad) * barrelLength, 
                p.y + barrelOffset * Math.cos(rad) + Math.sin(rad) * barrelLength,
                8 + flareIntensity * 4, 0, Math.PI * 2);
        ctx.fill();
        
        ctx.beginPath();
        ctx.arc(p.x + barrelOffset * Math.sin(rad) + Math.cos(rad) * barrelLength,
                p.y - barrelOffset * Math.cos(rad) + Math.sin(rad) * barrelLength,
                8 + flareIntensity * 4, 0, Math.PI * 2);
        ctx.fill();
        
        ctx.globalAlpha = 1;
    }

    renderAimLine(p, isMe, mouseX, mouseY, aimLineEnabled, ctx) {
        if (!isMe || !aimLineEnabled) return;
        
        ctx.strokeStyle = 'rgba(255, 100, 100, 0.6)';
        ctx.lineWidth = 1;
        ctx.setLineDash([5, 5]);
        ctx.beginPath();
        ctx.moveTo(p.x, p.y);
        ctx.lineTo(mouseX, mouseY);
        ctx.stroke();
        ctx.setLineDash([]);
    }

    renderHeatGlow(p, isMe, heatLevel, maxHeatLevel, angle, ctx) {
        if (!isMe) return;
        
        const heatPercent = heatLevel / maxHeatLevel;
        if (heatPercent <= 0) return;
        
        const rad = angle * Math.PI / 180;
        const barrelEndX = p.x + Math.cos(rad) * CONFIG.PLAYER.BARREL_LENGTH;
        const barrelEndY = p.y + Math.sin(rad) * CONFIG.PLAYER.BARREL_LENGTH;
        
        ctx.shadowBlur = 5 + heatPercent * 15;
        ctx.shadowColor = heatPercent > 0.7 ? '#ff0000' : '#ffaa00';
        ctx.fillStyle = ctx.shadowColor;
        ctx.globalAlpha = heatPercent * 0.6;
        ctx.beginPath();
        ctx.arc(barrelEndX, barrelEndY, 5 + heatPercent * 5, 0, Math.PI * 2);
        ctx.fill();
        ctx.globalAlpha = 1;
        ctx.shadowBlur = 0;
    }

    renderHealthBar(p, ctx) {
        const healthPercent = 100;
        const healthWidth = 36 * (healthPercent / 100);
        
        ctx.fillStyle = 'rgba(0, 0, 0, 0.7)';
        ctx.fillRect(p.x - 18, p.y - 30, 36, 4);
        
        ctx.fillStyle = healthPercent > 50 ? '#00ff88' : healthPercent > 25 ? '#ffaa00' : '#ff0000';
        ctx.fillRect(p.x - 18, p.y - 30, healthWidth, 4);
    }

    renderNameTag(p, ctx) {
        ctx.fillStyle = '#ffffff';
        ctx.font = 'bold 12px Arial';
        ctx.textAlign = 'center';
        ctx.shadowBlur = 3;
        ctx.shadowColor = '#000000';
        ctx.fillText(p.name, p.x, p.y - 35);
        ctx.shadowBlur = 0;
    }

    renderSpeedBoostTrail(p, ctx) {
        if (!p.speedBoost) return;
        
        ctx.strokeStyle = '#ffff00';
        ctx.lineWidth = 2;
        
        for (let i = 0; i < 3; i++) {
            const offset = (i + 1) * 15;
            ctx.globalAlpha = 0.3 - i * 0.1;
            ctx.strokeRect(p.x - 18 - offset, p.y - 18, 36, 36);
        }
        
        ctx.globalAlpha = 1;
    }

    renderMinimap(players, playerId) {
        if (!this.minimapCtx) return;
        
        const ctx = this.minimapCtx;
        ctx.clearRect(0, 0, CONFIG.MINIMAP.WIDTH, CONFIG.MINIMAP.HEIGHT);
        
        ctx.fillStyle = 'rgba(0, 20, 10, 0.8)';
        ctx.fillRect(0, 0, CONFIG.MINIMAP.WIDTH, CONFIG.MINIMAP.HEIGHT);
        
        ctx.strokeStyle = 'rgba(0, 255, 136, 0.2)';
        ctx.lineWidth = 1;
        
        for (let x = 0; x <= CONFIG.MINIMAP.WIDTH; x += CONFIG.MINIMAP.GRID_SIZE_X) {
            ctx.beginPath();
            ctx.moveTo(x, 0);
            ctx.lineTo(x, CONFIG.MINIMAP.HEIGHT);
            ctx.stroke();
        }
        
        for (let y = 0; y <= CONFIG.MINIMAP.HEIGHT; y += CONFIG.MINIMAP.GRID_SIZE_Y) {
            ctx.beginPath();
            ctx.moveTo(0, y);
            ctx.lineTo(CONFIG.MINIMAP.WIDTH, y);
            ctx.stroke();
        }
        
        Object.values(players).forEach(p => {
            if (!p.alive) return;
            
            const mx = (p.x / CONFIG.CANVAS.WIDTH) * CONFIG.MINIMAP.WIDTH;
            const my = (p.y / CONFIG.CANVAS.HEIGHT) * CONFIG.MINIMAP.HEIGHT;
            
            ctx.fillStyle = p.id === playerId ? '#00ff88' : '#ff4444';
            ctx.beginPath();
            ctx.arc(mx, my, 4, 0, Math.PI * 2);
            ctx.fill();
        });
    }

    createExplosion(x, y, count = 30, color = '#ff0000') {
        for (let i = 0; i < count; i++) {
            this.particles.push({
                x, y,
                vx: (Math.random() - 0.5) * 10,
                vy: (Math.random() - 0.5) * 10,
                life: 1.0,
                color
            });
        }
    }

    updateParticles() {
        this.particles = this.particles.filter(p => {
            p.x += p.vx;
            p.y += p.vy;
            p.vy += 0.3; // gravity
            p.life -= CONFIG.EFFECTS.PARTICLE_DECAY;
            return p.life > 0;
        });
    }

    screenShake() {
        this.canvas.style.transform = 'translate(' + (Math.random() * 4 - 2) + 'px, ' + (Math.random() * 4 - 2) + 'px)';
        setTimeout(() => {
            this.canvas.style.transform = '';
        }, 50);
    }
}
