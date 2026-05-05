package com.dungeon.system;

import com.dungeon.model.Coordinates;
import com.dungeon.model.Dungeon;

import java.util.*;

public class Pathfinder {

    // Node interne pour l'algorithme A*
    private static class Node implements Comparable<Node> {
        final int x, y;
        final int g; // coût depuis le départ
        final int h; // heuristique (distance de Manhattan vers la cible)
        final Node parent;

        Node(int x, int y, int g, int h, Node parent) {
            this.x = x;
            this.y = y;
            this.g = g;
            this.h = h;
            this.parent = parent;
        }

        int f() { return g + h; }

        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.f(), other.f());
        }
    }

    /**
     * Calcule le prochain pas optimal depuis `start` vers `goal`.
     * Retourne null si aucun chemin n'existe ou si on est déjà sur la cible.
     */
    public static Coordinates nextStep(Dungeon dungeon, Coordinates start, Coordinates goal) {
        int width = dungeon.getWidth();
        int height = dungeon.getHeight();

        PriorityQueue<Node> open = new PriorityQueue<>();
        boolean[][] closed = new boolean[width][height];

        open.add(new Node(start.getX(), start.getY(), 0, heuristic(start.getX(), start.getY(), goal), null));

        int[][] dirs = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

        while (!open.isEmpty()) {
            Node current = open.poll();

            if (current.x == goal.getX() && current.y == goal.getY()) {
                // On remonte le chemin jusqu'au premier pas depuis start
                return firstStep(current, start);
            }

            if (closed[current.x][current.y]) continue;
            closed[current.x][current.y] = true;

            for (int[] dir : dirs) {
                int nx = current.x + dir[0];
                int ny = current.y + dir[1];

                Coordinates next = new Coordinates(nx, ny);

                // La case goal est walkable même si un héros s'y trouve — on veut l'atteindre
                boolean isGoal = (nx == goal.getX() && ny == goal.getY());

                if (!isGoal && !dungeon.isWalkable(next)) continue;
                if (nx < 0 || ny < 0 || nx >= width || ny >= height) continue;
                if (closed[nx][ny]) continue;

                int g = current.g + 1;
                int h = heuristic(nx, ny, goal);
                open.add(new Node(nx, ny, g, h, current));
            }
        }

        // Aucun chemin trouvé
        return null;
    }

    // Distance de Manhattan
    private static int heuristic(int x, int y, Coordinates goal) {
        return Math.abs(x - goal.getX()) + Math.abs(y - goal.getY());
    }

    // Remonte les parents jusqu'au nœud dont le parent est le départ
    private static Coordinates firstStep(Node node, Coordinates start) {
        Node step = node;
        while (step.parent != null &&
               !(step.parent.x == start.getX() && step.parent.y == start.getY())) {
            step = step.parent;
        }
        // Si on est déjà adjacent, step est la destination directe
        if (step.parent == null) return null;
        return new Coordinates(step.x, step.y);
    }
}
