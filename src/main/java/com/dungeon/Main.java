package com.dungeon;

import com.dungeon.model.*;
import com.dungeon.system.GameWindow;

public class Main {
       public static void main(String[] args) {
        
        Dungeon dungeon = new Dungeon(15, 15);
        Hero hero = new Hero("Lancelot", 100, 15);
        Enemy enemy = new Enemy();

        javax.swing.SwingUtilities.invokeLater(() -> {
            new GameWindow(dungeon, hero, enemy);
        });

        System.out.println("Jeu lancé ! Utilisez ZQSD pour vous déplacer.");
    }
}