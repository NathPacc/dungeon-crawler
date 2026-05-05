package com.dungeon.system;

import com.dungeon.model.*;
import java.util.ArrayList;
import java.util.List;

/**
 * GameEngine : contient toute la logique de jeu.
 * Aucune dépendance à Swing — facilement testable et extensible.
 */
public class GameEngine {

    private Dungeon dungeon;
    private Hero hero;
    private List<Enemy> enemies = new ArrayList<>();
    private int currentLevel = 1;
    private boolean gameOver = false;
    private boolean levelChanged = false;

    // Listener pour notifier GameWindow des changements d'état
    public interface StateListener {
        void onLevelChanged(int newLevel);
        void onGameOver(int levelReached);
        void onStateUpdated();
    }

    private StateListener listener;

    public GameEngine(Dungeon dungeon, Hero hero) {
        this.dungeon = dungeon;
        this.hero = hero;
        setupLevel();
    }

    public void setListener(StateListener listener) {
        this.listener = listener;
    }

    // --- Setup ---

    private void setupLevel() {
        enemies.clear();
        hero.setPosition(dungeon.heroSpawn);

        // generate() retourne les cases FLOOR disponibles (hors spawn et escalier)
        java.util.List<Coordinates> available = dungeon.generate(hero.getPosition());

        // On place autant d'ennemis que possible sans dépasser les cases dispo
        int count = Math.min(currentLevel, available.size());
        for (int i = 0; i < count; i++) {
            Enemy enemy = new Enemy();
            enemy.setPosition(available.get(i)); // la liste est déjà mélangée par generate()
            enemies.add(enemy);
        }
    }

    // --- Input principal ---

    public void moveHero(int dx, int dy) {
        if (gameOver) return;

        Coordinates nextPos = new Coordinates(
            hero.getPosition().getX() + dx,
            hero.getPosition().getY() + dy
        );

        // Escalier : passage au niveau suivant
        if (nextPos.isEqual(dungeon.getStairPos())) {
            currentLevel++;
            setupLevel();
            if (listener != null) listener.onLevelChanged(currentLevel);
            if (listener != null) listener.onStateUpdated();
            return;
        }

        // Attaque si un ennemi vivant est sur la case cible
        Enemy target = enemyAt(nextPos);
        if (target != null) {
            resolveCombat(hero, target);
            enemyTurns();
            if (listener != null) listener.onStateUpdated();
            return;
        }

        if (!dungeon.isWalkable(nextPos)) return;

        hero.move(dx, dy);
        enemyTurns();

        if (listener != null) listener.onStateUpdated();
    }

    // --- Tours des ennemis ---

    private void enemyTurns() {
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;

            List<Coordinates> occupied = new ArrayList<>();
            for (Enemy other : enemies) {
                if (other != enemy && other.isAlive()) {
                    occupied.add(other.getPosition());
                }
            }

            enemy.takeTurn(dungeon, hero.getPosition(), occupied);

            // L'ennemi a marché sur le héros → il attaque puis recule sur sa case précédente
            if (enemy.getPosition().isEqual(hero.getPosition())) {
                resolveCombat(enemy, hero);
                if (gameOver) return;
                // L'ennemi recule : on annule son déplacement en le remettant une case en arrière
                // (takeTurn a déjà stocké le mouvement — on replace l'ennemi adjacent au héros)
                pushBackEnemy(enemy);
            }
        }
    }

    /**
     * Replace l'ennemi sur la première case libre adjacente au héros.
     * Évite qu'il reste superposé et bloque les tours suivants.
     */
    private void pushBackEnemy(Enemy enemy) {
        int hx = hero.getPosition().getX();
        int hy = hero.getPosition().getY();
        int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};
        for (int[] d : dirs) {
            Coordinates candidate = new Coordinates(hx + d[0], hy + d[1]);
            if (dungeon.isWalkable(candidate) && enemyAt(candidate) == null) {
                enemy.setPosition(candidate);
                return;
            }
        }
        // Aucune case libre adjacente — on exile l'ennemi temporairement hors carte
        enemy.setPosition(new Coordinates(-99, -99));
    }

    // --- Combat ---

    private void resolveCombat(Entity attacker, Entity defender) {
        attacker.attack(defender);

        if (!defender.isAlive()) {
            if (defender == hero) {
                triggerGameOver();
            } else {
                // Repousse l'ennemi hors de la carte
                ((Enemy) defender).setPosition(new Coordinates(-99, -99));
                System.out.println("Ennemi éliminé !");
            }
        }
    }

    private void triggerGameOver() {
        gameOver = true;
        hero.setPosition(new Coordinates(-99, -99));
        enemies.forEach(e -> e.setPosition(new Coordinates(-99, -99)));
        System.out.println("Game Over — niveau atteint : " + currentLevel);
        if (listener != null) listener.onGameOver(currentLevel);
    }

    // --- Utilitaires ---

    private Enemy enemyAt(Coordinates pos) {
        return enemies.stream()
            .filter(e -> e.isAlive() && e.getPosition().isEqual(pos))
            .findFirst()
            .orElse(null);
    }

    // --- Getters pour GameWindow ---

    public Dungeon getDungeon() { return dungeon; }
    public Hero getHero() { return hero; }
    public List<Enemy> getEnemies() { return enemies; }
    public int getCurrentLevel() { return currentLevel; }
    public boolean isGameOver() { return gameOver; }
}
