package com.dungeon;

import com.dungeon.model.*;
import com.dungeon.system.*;

public class Main {
    public static void main(String[] args) {
        Dungeon dungeon = new Dungeon(15, 15);
        Hero hero = new Hero("Héros", 100, 15);

        GameEngine engine = new GameEngine(dungeon, hero);

        javax.swing.SwingUtilities.invokeLater(() -> new GameWindow(engine));
    }
}
