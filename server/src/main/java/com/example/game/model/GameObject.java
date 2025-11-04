package com.example.game.model;

import java.io.Serializable;

/**
 * Abstract base class for all game objects that have position and can be serialized.
 */
public abstract class GameObject implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String id;
    protected int x, y;

    public GameObject(String id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public String getId() { return id; }
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    /**
     * Update the object's state (position, etc.)
     */
    public abstract void update();
}