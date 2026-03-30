package com.dungeon.model;

public class Hero extends Entity {
    public Hero(String name, int hp, int attackPower) {
        super(name, hp, attackPower);
    }

    @Override
    public void attack(Entity target) {
        System.out.println(name + " attaque " + target.getName() + " !");
        target.takeDamage(this.attackPower);
    }
}