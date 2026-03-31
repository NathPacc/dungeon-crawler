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

    public void takeTurn(Dungeon dungeon) {
        Random rand = new Random();
        Coordinates nextPos = new Coordinates(0,0);
        int trycount = 0;
        int dx = 0, dy = 0;
        while (!dungeon.isWalkable(nextPos) && trycount<50) {
            // 0: Down, 1: Up, 2: Left, 3: Right
            int direction = rand.nextInt(4);
            dx = 0;
            dy = 0;

            switch (direction) {
                case 0: dy = -1; break;
                case 1: dy = 1;  break;
                case 2: dx = -1; break;
                case 3: dx = 1;  break;
            }

            nextPos.setX(this.position.getX() + dx);
            nextPos.setY(this.position.getY() + dy);
            trycount++;

        }
        if (dungeon.isWalkable(nextPos)) {
            this.move(dx, dy);
        } else {
             System.out.println("Enemi bloqué.");
        }
    }
}