package com.tankgame.model;

/**
 * Represents a 2D position with coordinates and angle
 */
public class Position {
    private int x;
    private int y;
    private int angle; // in degrees
    
    public Position(int x, int y, int angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
    }
    
    public Position(int x, int y) {
        this(x, y, 0);
    }
    
    // Getters and Setters
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    
    public int getAngle() { return angle; }
    public void setAngle(int angle) { this.angle = angle; }
    
    /**
     * Update position based on velocity
     */
    public void move(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }
    
    /**
     * Calculate velocity components based on angle and speed
     */
    public int[] getVelocity(int speed) {
        double radians = Math.toRadians(angle);
        int dx = (int) Math.round(speed * Math.cos(radians));
        int dy = (int) Math.round(speed * Math.sin(radians));
        return new int[]{dx, dy};
    }
    
    @Override
    public String toString() {
        return String.format("(%d, %d, %d°)", x, y, angle);
    }
}
