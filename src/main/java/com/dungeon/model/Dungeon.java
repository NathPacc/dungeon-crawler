package com.dungeon.model;

import java.util.Random;

public class Dungeon {
    private int width;
    private int height;
    private Cell[][] grid;

    public Dungeon(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new Cell[width][height];
        
        generate();
    }

    private void generate() {
        Random rand = new Random();
        // Generating grid with entry, walls around the room and random cells elswhere
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (i==1 && j==0) {
                    grid[i][j] = new Cell("FLOOR");
                } else if (i == 0 || i == width-1 || j == 0 || j == height-1) {
                    grid[i][j] = new Cell("WALL");
                } else {
                    if (rand.nextInt(3) == 0) {
                        grid[i][j] = new Cell("WALL");
                    } else {
                        grid[i][j] = new Cell("FLOOR");
                    }
                }
            }
        }
        // Placing stair
        int randomX = rand.nextInt(width - 2) + 1;
        int randomY = rand.nextInt(height - 2) + 1;
        grid[randomX][randomY].setType("STAIRS");
    }

    private boolean isOutOfBounds(int x, int y) {
    return x < 0 || x >= this.width || y < 0 || y >= this.height;
}

    public boolean isWalkable(Coordinates nextPos) {
        int x = nextPos.getX();
        int y = nextPos.getY();

        if (isOutOfBounds(x,y)) {
            return false;
        }

        Cell targetedCell=grid[x][y];
        if (targetedCell.getType().equals("WALL")) {
            return false;
        }
        
        return true; 
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public Cell getCell(int x, int y){
        return this.grid[x][y];
    }
}