package com.dungeon.system;

import com.dungeon.model.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * GameWindow : uniquement le rendu Swing et la capture d'input.
 * Toute la logique est déléguée à GameEngine.
 */
public class GameWindow extends JFrame implements GameEngine.StateListener {

    private final GameEngine engine;
    private final GamePanel gamePanel;
    private final int TILE_SIZE = 40;

    // HUD
    private JLabel levelLabel;
    private JLabel hpLabel;

    public GameWindow(GameEngine engine) {
        this.engine = engine;
        this.engine.setListener(this);

        setTitle("Java Dungeon Adventure");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel de jeu
        gamePanel = new GamePanel();
        add(gamePanel, BorderLayout.CENTER);

        // HUD en bas
        JPanel hud = buildHud();
        add(hud, BorderLayout.SOUTH);

        updateHud();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleInput(e);
            }
        });

        updateWindowSize();      // setPreferredSize + pack()
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // --- HUD ---

    private JPanel buildHud() {
        JPanel hud = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 6));
        hud.setBackground(new Color(8, 8, 14));
        hud.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(50, 48, 65)));

        levelLabel = new JLabel();
        hpLabel    = new JLabel();

        Font hudFont = new Font("Courier New", Font.BOLD, 13);
        levelLabel.setFont(hudFont);
        hpLabel.setFont(hudFont);

        levelLabel.setForeground(new Color(255, 210, 60));   // jaune escalier
        hpLabel.setForeground(new Color(100, 185, 255));     // bleu héros

        hud.add(levelLabel);
        hud.add(hpLabel);
        return hud;
    }

    private void updateHud() {
        levelLabel.setText("  NIVEAU " + engine.getCurrentLevel());
        hpLabel.setText("♥ " + engine.getHero().getHp() + " PV");
    }

    private void updateWindowSize() {
        Dungeon d = engine.getDungeon();
        gamePanel.setPreferredSize(new Dimension(
            d.getWidth()  * TILE_SIZE,
            d.getHeight() * TILE_SIZE
        ));
        pack();
    }

    // --- Input ---

    private void handleInput(KeyEvent e) {
        int dx = 0, dy = 0;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_Z: case KeyEvent.VK_UP:    dy = -1; break;
            case KeyEvent.VK_S: case KeyEvent.VK_DOWN:  dy =  1; break;
            case KeyEvent.VK_Q: case KeyEvent.VK_LEFT:  dx = -1; break;
            case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: dx =  1; break;
            default: return;
        }
        engine.moveHero(dx, dy);
    }

    // --- StateListener ---

    @Override
    public void onLevelChanged(int newLevel) {
        updateWindowSize();
    }

    @Override
    public void onGameOver(int levelReached) {
        // Petit délai pour laisser le dernier repaint se faire
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                "Game Over !\nNiveau atteint : " + levelReached,
                "Fin de partie",
                JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        });
    }

    @Override
    public void onStateUpdated() {
        updateHud();
        gamePanel.repaint();
    }

    // --- Rendu ASCII ---

    private class GamePanel extends JPanel {

        // Palette terminal sombre
        private static final Color COL_BG       = new Color(12, 12, 18);
        private static final Color COL_WALL_FG  = new Color(50, 48, 65);   // █ mur
        private static final Color COL_FLOOR_FG = new Color(55, 55, 50);   // · sol
        private static final Color COL_STAIRS   = new Color(255, 210, 60); // >
        private static final Color COL_HERO     = new Color(100, 185, 255);
        private static final Color COL_GOBLIN   = new Color(120, 210, 90);  // vert
        private static final Color COL_ORC      = new Color(210, 80,  60);  // rouge
        private static final Color COL_SLIME    = new Color(160, 230, 130); // vert clair
        private static final Color COL_AGGRO_FG = new Color(255, 160, 40);  // orange quand en chasse
        private static final Color COL_HUD_BG   = new Color(8, 8, 14);

        // Glyphe ASCII pour chaque entité
        private static final String GLYPH_HERO   = "@";
        private static final String GLYPH_GOBLIN = "g";
        private static final String GLYPH_ORC    = "O";
        private static final String GLYPH_SLIME  = "~";
        private static final String GLYPH_WALL   = "█";
        private static final String GLYPH_FLOOR  = "·";
        private static final String GLYPH_STAIR  = ">";

        private Font asciiFont;

        public GamePanel() {
            setBackground(COL_BG);
            // Tente de charger une police monospace avec bons glyphes Unicode
            asciiFont = new Font("Courier New", Font.BOLD, 22);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,       RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,  RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2.setFont(asciiFont);
            FontMetrics fm = g2.getFontMetrics();
            // Centrage du glyphe dans la tile
            int charY = (TILE_SIZE + fm.getAscent() - fm.getDescent()) / 2;

            Dungeon dungeon = engine.getDungeon();

            // --- Tiles ---
            for (int x = 0; x < dungeon.getWidth(); x++) {
                for (int y = 0; y < dungeon.getHeight(); y++) {
                    int px = x * TILE_SIZE;
                    int py = y * TILE_SIZE;

                    // Fond uniforme
                    g2.setColor(COL_BG);
                    g2.fillRect(px, py, TILE_SIZE, TILE_SIZE);

                    Cell cell = dungeon.getCell(x, y);
                    switch (cell.getType()) {
                        case "WALL":
                            g2.setColor(COL_WALL_FG);
                            drawCentered(g2, fm, GLYPH_WALL, px, py + charY);
                            break;
                        case "STAIRS":
                            g2.setColor(COL_STAIRS);
                            drawCentered(g2, fm, GLYPH_STAIR, px, py + charY);
                            break;
                        default: // FLOOR
                            g2.setColor(COL_FLOOR_FG);
                            drawCentered(g2, fm, GLYPH_FLOOR, px, py + charY);
                            break;
                    }
                }
            }

            // --- Ennemis ---
            for (Enemy enemy : engine.getEnemies()) {
                if (!enemy.isAlive()) continue;
                drawEnemy(g2, fm, enemy, charY);
            }

            // --- Héros ---
            drawHero(g2, fm, engine.getHero(), charY);
        }

        private void drawHero(Graphics2D g2, FontMetrics fm, Hero hero, int charY) {
            int px = hero.getPosition().getX() * TILE_SIZE;
            int py = hero.getPosition().getY() * TILE_SIZE;

            // Légère lueur de fond
            g2.setColor(new Color(100, 185, 255, 30));
            g2.fillRect(px, py, TILE_SIZE, TILE_SIZE);

            g2.setColor(COL_HERO);
            drawCentered(g2, fm, GLYPH_HERO, px, py + charY);

            // PV sous le glyphe, petit et discret
            g2.setFont(new Font("Courier New", Font.PLAIN, 9));
            g2.setColor(new Color(100, 185, 255, 180));
            String hpStr = String.valueOf(engine.getHero().getHp());
            g2.drawString(hpStr, px + TILE_SIZE/2 - g2.getFontMetrics().stringWidth(hpStr)/2, py + TILE_SIZE - 3);
            g2.setFont(asciiFont); // restore
        }

        private void drawEnemy(Graphics2D g2, FontMetrics fm, Enemy enemy, int charY) {
            int px = enemy.getPosition().getX() * TILE_SIZE;
            int py = enemy.getPosition().getY() * TILE_SIZE;

            int dist = Math.abs(enemy.getPosition().getX() - engine.getHero().getPosition().getX())
                     + Math.abs(enemy.getPosition().getY() - engine.getHero().getPosition().getY());
            boolean agro = enemy.isAgro();

            // Couleur de base selon le type, orange si en chasse
            Color baseColor = enemyColor(enemy.getEnemyType());
            Color col = agro ? COL_AGGRO_FG : baseColor;

            // Fond subtil
            g2.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 25));
            g2.fillRect(px, py, TILE_SIZE, TILE_SIZE);

            g2.setColor(col);
            drawCentered(g2, fm, enemyGlyph(enemy.getEnemyType()), px, py + charY);

            // PV sous le glyphe
            g2.setFont(new Font("Courier New", Font.PLAIN, 9));
            g2.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 180));
            String hpStr = String.valueOf(enemy.getHp());
            g2.drawString(hpStr, px + TILE_SIZE/2 - g2.getFontMetrics().stringWidth(hpStr)/2, py + TILE_SIZE - 3);
            g2.setFont(asciiFont); // restore
        }

        // Centre horizontalement un glyphe dans sa tile
        private void drawCentered(Graphics2D g2, FontMetrics fm, String glyph, int tileX, int baselineY) {
            int charX = tileX + (TILE_SIZE - fm.stringWidth(glyph)) / 2;
            g2.drawString(glyph, charX, baselineY);
        }

        private String enemyGlyph(EnemyType type) {
            switch (type) {
                case GOBLIN: return GLYPH_GOBLIN;
                case ORC:    return GLYPH_ORC;
                case SLIME:  return GLYPH_SLIME;
                default:     return "?";
            }
        }

        private Color enemyColor(EnemyType type) {
            switch (type) {
                case GOBLIN: return COL_GOBLIN;
                case ORC:    return COL_ORC;
                case SLIME:  return COL_SLIME;
                default:     return Color.WHITE;
            }
        }
    }
}
