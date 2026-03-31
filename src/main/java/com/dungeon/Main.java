package com.dungeon;

import com.dungeon.model.*;
import com.dungeon.system.GameWindow;

public class Main {
    public static void main(String[] args) {
        // 1. Initialisation
        Dungeon dungeon = new Dungeon(15, 15);
        Hero hero = new Hero("Lancelot", 100, 15);
        hero.setPosition(new Coordinates(1, 0));
        
        // 2. Génération du donjon (le donjon s'assure d'être valide par rapport au héros)
        dungeon.generate(hero.getPosition());

        // 3. Placement de l'ennemi sur une case vide aléatoire
        Enemy enemy = new Enemy();
        Coordinates enemySpawn = dungeon.findRandomEmptyPos();
        enemy.setPosition(enemySpawn);

        // 4. Lancement de l'interface
        javax.swing.SwingUtilities.invokeLater(() -> {
            new GameWindow(dungeon, hero, enemy);
        });

        System.out.println("Donjon généré et validé !");
    }
}