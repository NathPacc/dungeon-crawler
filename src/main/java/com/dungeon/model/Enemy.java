package com.dungeon.model;

import java.util.Random;

public class Enemy extends Entity {
    private EnemyType type;

    public Enemy() {
        this(EnemyType.values()[new Random().nextInt(EnemyType.values().length)]);
    }

    private Enemy(EnemyType randomType) {
        super(randomType.name(), randomType.defaultHp, randomType.defaultAttack);
        this.type = randomType;
    }

    @Override
    public void attack(Entity target) {
        System.out.println("Le " + name + " grogne et attaque !");
        target.takeDamage(this.attackPower);
    }
}