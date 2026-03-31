package com.dungeon.model;

import java.util.Random;

public class Dungeon {
    private int width;
    private int height;
    private Cell[][] grid;
    private Coordinates stairPos;
    private final Random rand = new Random();

    public Dungeon(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new Cell[width][height];
        // On ne génère rien ici, on laisse le Main décider quand lancer la génération
    }

    public void generate(Coordinates heroPos) {
        boolean dungeonReady = false;
        int tries = 0;

        while (!dungeonReady) {
            // 1. Remplissage de base
            fillWithRandomTiles();
            
            // 2. Placement de l'escalier sur une case FLOOR
            this.stairPos = findRandomEmptyPos();
            grid[stairPos.getX()][stairPos.getY()].setType("STAIRS");

            // 3. Test de connectivité (Héros -> Escalier)
            // On ne teste pas l'ennemi ici, on le placera sur une case déjà validée après !
            if (isReachable(heroPos, stairPos)) {
                dungeonReady = true;
            }
            tries++;
            if (tries%1000 == 0) {
                System.err.println("Milles tentatives passées");
            }
        }
    }

    private void fillWithRandomTiles() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                // 1. On définit d'abord les murs de bordure
                if (i == 0 || i == width - 1 || j == 0 || j == height - 1) {
                    grid[i][j] = new Cell("WALL");
                } 
                // 2. On définit le reste aléatoirement
                else {
                    grid[i][j] = new Cell(rand.nextInt(3) == 0 ? "WALL" : "FLOOR");
                }
            }
        }
        // 3. À LA TOUTE FIN, on force le passage pour le héros
        // On utilise (1, 1) car (1, 0) est dans le mur de bordure !
        grid[1][0].setType("FLOOR"); 
    }

    // Trouve une case FLOOR qui n'est pas l'entrée
    public Coordinates findRandomEmptyPos() {
        int rx, ry;
        do {
            rx = rand.nextInt(width - 2) + 1;
            ry = rand.nextInt(height - 2) + 1;
        } while (!grid[rx][ry].getType().equals("FLOOR"));
        return new Coordinates(rx, ry);
    }

    private boolean isReachable(Coordinates start, Coordinates end) {
        boolean[][] visited = new boolean[width][height];
        floodFill(start.getX(), start.getY(), visited);
        return visited[end.getX()][end.getY()];
    }

    private void floodFill(int x, int y, boolean[][] visited) {
        if (isOutOfBounds(x, y) || visited[x][y] || grid[x][y].getType().equals("WALL")) {
            return;
        }
        visited[x][y] = true;
        floodFill(x + 1, y, visited);
        floodFill(x - 1, y, visited);
        floodFill(x, y + 1, visited);
        floodFill(x, y - 1, visited);
    }

    public boolean isWalkable(Coordinates pos) {
        if (isOutOfBounds(pos.getX(), pos.getY())) return false;
        return !grid[pos.getX()][pos.getY()].getType().equals("WALL");
    }

    private boolean isOutOfBounds(int x, int y) {
        return x < 0 || x >= width || y < 0 || y >= height;
    }

    // Getters
    public Cell getCell(int x, int y) { return grid[x][y]; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Coordinates getStairPos() { return stairPos; }
}