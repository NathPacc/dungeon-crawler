package com.dungeon.model;

import java.util.Random;

public class Dungeon {
    private int width;
    private int height;
    private Cell[][] grid;
    private Coordinates stairPos;
    private final Random rand = new Random();
    public Coordinates heroSpawn = new Coordinates(1,1);


    // Constructor
    public Dungeon(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new Cell[width][height];
    }

    /**
     * Génère un donjon valide et retourne toutes les cases FLOOR accessibles
     * depuis spawnPos (hors spawn lui-même), prêtes à accueillir ennemis et escalier.
     */
    public java.util.List<Coordinates> generate(Coordinates spawnPos) {
        java.util.List<Coordinates> availableFloors;
        int tries = 0;
        do {
            fillWithRandomTiles();
            cullUnreachableFloors(spawnPos);
            availableFloors = collectFloors(spawnPos);
            tries++;
        } while (availableFloors.size() < 2 && tries < 10000);
        // Au moins 2 cases : une pour l'escalier, une pour les ennemis

        // Place l'escalier sur une case aléatoire parmi les disponibles
        java.util.Collections.shuffle(availableFloors, rand);
        Coordinates stairs = availableFloors.remove(0);
        this.stairPos = stairs;
        grid[stairs.getX()][stairs.getY()].setType("STAIRS");

        return availableFloors; // cases restantes pour les ennemis
    }

    /**
     * Retourne toutes les cases FLOOR accessibles, en excluant le spawn.
     */
    private java.util.List<Coordinates> collectFloors(Coordinates exclude) {
        java.util.List<Coordinates> floors = new java.util.ArrayList<>();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (grid[i][j].getType().equals("FLOOR")) {
                    Coordinates c = new Coordinates(i, j);
                    if (!c.isEqual(exclude)) floors.add(c);
                }
            }
        }
        return floors;
    }

    /**
     * Convertit en WALL toutes les cases FLOOR qui ne sont pas accessibles
     * depuis spawnPos en déplacement 4-directionnel.
     * Après cet appel, toute case FLOOR est garantie dans la même zone connexe.
     */
    private void cullUnreachableFloors(Coordinates spawnPos) {
        boolean[][] reachable = new boolean[width][height];
        floodFill(spawnPos.getX(), spawnPos.getY(), reachable);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (!reachable[i][j] && !grid[i][j].getType().equals("WALL")) {
                    grid[i][j].setType("WALL");
                }
            }
        }
    }

    private void fillWithRandomTiles() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (i == 0 || i == width - 1 || j == 0 || j == height - 1) {
                    grid[i][j] = new Cell("WALL");
                } 
                else {
                    grid[i][j] = new Cell(rand.nextInt(3) == 0 ? "WALL" : "FLOOR");
                }
            }
        }
        grid[1][1].setType("FLOOR"); // garantit que le spawn est toujours praticable
    }

    // Flood fill itératif
    private void floodFill(int startX, int startY, boolean[][] visited) {
        if (isOutOfBounds(startX, startY) || grid[startX][startY].getType().equals("WALL")) return;

        java.util.Deque<int[]> stack = new java.util.ArrayDeque<>();
        stack.push(new int[]{startX, startY});

        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};

        while (!stack.isEmpty()) {
            int[] cur = stack.pop();
            int x = cur[0], y = cur[1];

            if (isOutOfBounds(x, y) || visited[x][y] || grid[x][y].getType().equals("WALL")) continue;

            visited[x][y] = true;
            for (int[] d : dirs) stack.push(new int[]{x + d[0], y + d[1]});
        }
    }

    public boolean isWalkable(Coordinates pos) {
        if (isOutOfBounds(pos.getX(), pos.getY())) return false;
        return !grid[pos.getX()][pos.getY()].getType().equals("WALL");
    }

    private boolean isOutOfBounds(int x, int y) {
        return x < 0 || x >= width || y < 0 || y >= height;
    }

    public Cell getCell(int x, int y) { return grid[x][y]; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Coordinates getStairPos() { return stairPos; }
}