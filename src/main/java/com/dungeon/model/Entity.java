package com.dungeon.model;

public abstract class Entity {
    protected String name;
    protected boolean alive;
    protected int hp;
    protected int maxHp; // To prevent healing further
    protected int attackPower;

    protected Coordinates position;

    public Entity(String name, int hp, int attackPower) {
        this.name = name;
        this.maxHp = hp; //Initialized with starting value
        this.hp = hp;
        this.attackPower = attackPower;
        this.position = new Coordinates(-99, -99);
    }

    public void move(int dx, int dy) {
        this.position.add(dx, dy);
    }

    public Coordinates getPosition() {
        return position;
    }

    public abstract void attack(Entity target);

    public void takeDamage(int damage) {
    if (damage > 0) {
        this.hp = Math.max(0, this.hp - damage);
    }
}

    public boolean isAlive() {
       return this.hp > 0;
    }

    // Getters
    public String getName() { return name; }
    public int getHp() { return hp; }
    public void setPosition(Coordinates position) {
        this.position = position;
    }
}