package com.dungeon.model;

public enum EnemyType {
    GOBLIN(20, 5),  // HP, Attack
    ORC(50, 12),
    SLIME(10, 2);

    public final int defaultHp;
    public final int defaultAttack;

    EnemyType(int hp, int attack) {
        this.defaultHp = hp;
        this.defaultAttack = attack;
    }
}