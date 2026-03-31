package com.dungeon.system;

import com.dungeon.model.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

public class GameWindow extends JFrame {
    private Dungeon dungeon;
    private Hero hero;
    private Enemy enemy;
    private final int TILE_SIZE = 40;

    public GameWindow(Dungeon dungeon, Hero hero, Enemy enemy) {
        Random rand = new Random();
        this.dungeon = dungeon;
        this.hero = hero;
        this.enemy = enemy;

        this.hero.setPosition(new Coordinates(1, 0));

        int randomX = 0;
        int randomY = 0;

        boolean positionValide = false;
        while (!positionValide) {
            randomX = rand.nextInt(dungeon.getWidth() - 2) + 1;
            randomY = rand.nextInt(dungeon.getHeight() - 2) + 1;

            if (dungeon.isWalkable(new Coordinates(randomX, randomY)) && (randomX != 1 || randomY != 0)) {
                positionValide = true;
            }
        }

        this.enemy.setPosition(new Coordinates(randomX, randomY));

        this.setTitle("Java Dungeon Adventure");
        this.setSize(dungeon.getWidth() * TILE_SIZE, dungeon.getHeight() * TILE_SIZE + 40);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        // Ajout de l'écouteur de touches
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleInput(e);
            }
        });

        this.add(new GamePanel());
        this.setVisible(true);
    }

    private void handleInput(KeyEvent e) {
        int dx = 0, dy = 0;

        // On gère ZQSD ou les flèches
        switch (e.getKeyCode()) {
            case KeyEvent.VK_Z: case KeyEvent.VK_UP:    dy = -1; break;
            case KeyEvent.VK_S: case KeyEvent.VK_DOWN:  dy = 1;  break;
            case KeyEvent.VK_Q: case KeyEvent.VK_LEFT:  dx = -1; break;
            case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: dx = 1;  break;
        }

        // Calcul de la position future
        Coordinates nextPos = new Coordinates(
            hero.getPosition().getX() + dx, 
            hero.getPosition().getY() + dy
        );

        if (dungeon.isWalkable(nextPos)) {
            hero.move(dx, dy);
            enemy.takeTurn(dungeon);
            if (hero.getPosition().getX() == enemy.getPosition().getX() && 
            hero.getPosition().getY() == enemy.getPosition().getY()) {
                
               while (hero.getHp() > 0 && enemy.getHp() > 0) {
                    hero.attack(enemy);
                    
                    if (enemy.isAlive()) {
                        enemy.attack(hero);
                    } else {
                        System.out.println("L'ennemi est terrassé !");
                        enemy.setPosition(new Coordinates(-99, -99));
                    }
                    
                    System.out.println("PV Héros : " + hero.getHp() + " | PV Ennemi : " + enemy.getHp());
                }
            }
            repaint(); // Force le rafraîchissement du dessin
        }
    }

    private class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Dessin du donjon (murs, sol, escalier)
            for (int x = 0; x < dungeon.getWidth(); x++) {
                for (int y = 0; y < dungeon.getHeight(); y++) {
                    Cell cell = dungeon.getCell(x, y);
                    if (cell.getType().equals("WALL")) g.setColor(Color.BLACK);
                    else if (cell.getType().equals("STAIRS")) g.setColor(Color.YELLOW);
                    else g.setColor(Color.WHITE);

                    g.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
            // Dessin du héros (un rond bleu)
            g.setColor(Color.BLUE);
            g.fillOval(hero.getPosition().getX() * TILE_SIZE + 5, 
                       hero.getPosition().getY() * TILE_SIZE + 5, 
                       TILE_SIZE - 10, TILE_SIZE - 10);

            g.setColor(Color.RED);
            g.fillOval(enemy.getPosition().getX() * TILE_SIZE + 5, 
                       enemy.getPosition().getY() * TILE_SIZE + 5, 
                       TILE_SIZE - 10, TILE_SIZE - 10);
        }
    }
}