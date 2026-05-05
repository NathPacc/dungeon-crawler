package com.dungeon.model;

import com.dungeon.system.Pathfinder;
import java.util.Random;

public class Enemy extends Entity {
    private EnemyType type;

    private static final int AGGRO_RANGE = 6;

    // État stocké pour que le rendu soit cohérent avec la logique
    private boolean agro = false;

    public Enemy() {
        this(EnemyType.values()[new Random().nextInt(EnemyType.values().length)]);
    }

    private Enemy(EnemyType randomType) {
        super(randomType.name(), randomType.defaultHp, randomType.defaultAttack);
        this.type = randomType;
    }

    public EnemyType getEnemyType() { return type; }
    public boolean isAgro() { return agro; }

    @Override
    public void attack(Entity target) {
        target.takeDamage(this.attackPower);
    }

    /**
     * IA à deux états :
     *  - PATROL : mouvement aléatoire quand le héros est hors portée
     *  - CHASE  : pathfinding A* vers le héros quand il est en portée
     *
     * @param occupiedPositions positions déjà occupées par d'autres ennemis (évite la superposition)
     */
    public void takeTurn(Dungeon dungeon, Coordinates heroPos, java.util.List<Coordinates> occupiedPositions) {
        if (!this.isAlive()) return;

        int dist = manhattanDistance(this.position, heroPos);
        agro = dist <= AGGRO_RANGE;

        if (agro) {
            chase(dungeon, heroPos, occupiedPositions);
        } else {
            patrol(dungeon, occupiedPositions);
        }
    }

    // --- CHASE : A* vers le héros ---
    private void chase(Dungeon dungeon, Coordinates heroPos, java.util.List<Coordinates> occupied) {
        Coordinates next = Pathfinder.nextStep(dungeon, this.position, heroPos);
        if (next == null) return;

        // Si la prochaine case est le héros, on s'y déplace (combat déclenché par GameEngine)
        // Si c'est un autre ennemi, on reste sur place
        if (!next.isEqual(heroPos) && isOccupied(next, occupied)) return;

        int dx = next.getX() - this.position.getX();
        int dy = next.getY() - this.position.getY();
        this.move(dx, dy);
    }

    // --- PATROL : déplacement aléatoire ---
    private void patrol(Dungeon dungeon, java.util.List<Coordinates> occupied) {
        Random rand = new Random();
        int[][] dirs = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

        for (int i = dirs.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int[] tmp = dirs[i]; dirs[i] = dirs[j]; dirs[j] = tmp;
        }

        for (int[] dir : dirs) {
            Coordinates next = new Coordinates(
                this.position.getX() + dir[0],
                this.position.getY() + dir[1]
            );
            if (dungeon.isWalkable(next) && !isOccupied(next, occupied)) {
                this.move(dir[0], dir[1]);
                return;
            }
        }
    }

    private boolean isOccupied(Coordinates pos, java.util.List<Coordinates> occupied) {
        return occupied.stream().anyMatch(c -> c.isEqual(pos));
    }

    private int manhattanDistance(Coordinates a, Coordinates b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }
}
