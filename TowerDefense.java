import javax.swing.JPanel;
import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Main game class.
 * Handles the game logic and manages all the game objects
 */
public class TowerDefense extends JPanel implements KeyListener, MouseListener {
    // Game dimensions/mechanics
    public static final int FPS = 60;
    public static final int SIZE = 600;
    public static final int BOTTOM_HEIGHT = 120;
    public static final int PATH_WIDTH = 50;
    public static final int ENEMY_PERIOD = 1000;
    public static final int START_CURRENCY = 10;

    // Graphics constants
    public static final int CURRENCY_FONT_SIZE = 20;
    public static final int CURRENCY_OFFSET = 10;
    public static final int X_SIZE = 10;
    public static final int ESC_WIDTH = 32;
    public static final int KEY_STRING_OFFSET = 4;
    public static final int KEY_SIZE = 20;
    public static final int KEY_ARC = 5;
    public static final Point TOWER_PRICE_OFFSET = new Point(-8, 20);

    // Enemy path
    private static final Point[] PATH = {
            new Point(-Enemy.RADIUS, 500),
            new Point(200, 500),
            new Point(200, 300),
            new Point(100, 300),
            new Point(100, 100),
            new Point(500, 100),
            new Point(500, 300),
            new Point(400, 300),
            new Point(400, 500),
            new Point(600 + Enemy.RADIUS, 500)
    };

    // Locations of the WASD keys
    private static final Point[] KEY_LOCATIONS = {
            new Point(55, SIZE + 50),
            new Point(30, SIZE + 75),
            new Point(55, SIZE + 75),
            new Point(80, SIZE + 75)
    };

    // Order of enemies (health)
    private static final int[] ORDER = {
            0, 0, 0, 1, 1, 1, 0, 1, 1,
            0, 0, 0, 1, 1, 1, 2, 2, 2,
            0, 0, 0, 3, 3, 0, 2, 2, 2,
            0, 0, 0, 3, 2, 3, 2, 3, 2,
            0, 0, 0, 4, 3, 4, 3, 4, 3,
            0, 0, 0, 5, 5, 0, 5, 5, 5,
            0, 3, 3, 5, 5, 3, 5, 5, 5,
            0, 0, 4, 4, 4, 4, 5, 5, 5,
            0, 0, 0, 0, 6, 6, 6, 6, 6
    };

    // Prices of towers
    private static final int[] TOWER_PRICES = { 5, 10, 20 };

    private ArrayList<Enemy> enemies;
    private ArrayList<Tower> towers;
    private Timer timer;

    private int currency;
    private int stage = 0;
    private int preview = 0;
    private boolean canPlace = false;
    private Point mouse = new Point(SIZE / 2, SIZE / 2); // center (default)
    private Point direction = new Point(-1, 0); // left (default)
    private int cheatCode = 0;

    private static TowerDefense instance;

    /**
     * Initialize the game
     */
    private TowerDefense() {
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        start();
    }

    /**
     * @return singleton instance of the game
     */
    public static TowerDefense getInstance() {
        if (instance == null) {
            instance = new TowerDefense();
        }
        return instance;
    }

    /**
     * @return game bounds
     */
    private Rectangle getArena() {
        return new Rectangle(0, 0, SIZE, SIZE);
    }

    /**
     * @param permanent whether the tower is being placed
     * @return preview tower object
     */
    private Tower getPreviewTower(boolean permanent) {
        return generateTower(preview, mouse, permanent);
    }

    /**
     * @param n type of tower
     * @param p location of tower
     * @param permanent whether the tower is being placed
     * @return tower object with the given parameters
     */
    private Tower generateTower(int n, Point p, boolean permanent) {
        Tower tower = switch (n) {
            case 1 -> new Cannon(p, direction);
            case 2 -> new Bomber(p, direction);
            case 3 -> new Spreader(p);
            default -> null;
        };

        if (!permanent && tower != null) {
            tower.stop();
        }
        return tower;
    }

    /**
     * @return whether p is between l1 and l2
     */
    private boolean inBetween(Point p, Point l1, Point l2) {
        boolean x = p.x == l1.x || Math.min(l1.x, l2.x) < p.x && p.x < Math.max(l1.x, l2.x);
        boolean y = p.y == l1.y || Math.min(l1.y, l2.y) < p.y && p.y < Math.max(l1.y, l2.y);
        return x && y;
    }

    /**
     * Starts the game logic
     */
    private void start() {
        currency = START_CURRENCY;
        stage = 0;
        preview = 0;
        enemies = new ArrayList<>();
        towers = new ArrayList<>();
        timer = new Timer();
        //Starts the game
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (stage < ORDER.length) {
                    if (ORDER[stage] != 0) {
                        enemies.add(new Enemy(PATH[0], ORDER[stage]));
                    }
                    stage++;
                }
            }
        }, 0, ENEMY_PERIOD);
    }

    /**
     * End the game logic
     * @param win whether the player won
     */
    private void end(boolean win) {
        timer.cancel();
        for (Tower t : towers) {
            t.stop();
        }
        // Restarts the game if yes is typed. Stops the game if no is typed.
        String m = JOptionPane.showInputDialog((win ? "You win!" : "You lose!") + " Want to play again? (yes/no)");
        if (m != null && (m.equalsIgnoreCase("yes") || m.equalsIgnoreCase("y"))) {
            start();
        } else {
            System.exit(0);
        }
    }

    /**
     * Update the preview tower, placed towers, and enemies
     */
    public void update() {
        if (enemies.size() == 0 && stage == ORDER.length) {
            end(true);
        }

        mouse = MouseInfo.getPointerInfo().getLocation();
        canPlace = getArena().contains(mouse);
        for (int i = 1; i < PATH.length; i++) {
            if (new Line2D.Double(PATH[i - 1], PATH[i]).ptSegDist(mouse) < PATH_WIDTH / 2.0 + Tower.RADIUS) {
                canPlace = false;
                break;
            }
        }

        for (Tower t : towers) {
            if (t.getLocation().distance(mouse) < Tower.RADIUS * 2) {
                canPlace = false;
            }
            for (int i = 0; i < enemies.size(); i++) {
                t.interact(enemies.get(i));
                if (enemies.get(i).isDead()) {
                    enemies.remove(i);
                    i--;
                }
            }
            t.update();
        }

        e:
        for (Enemy e : enemies) {
            Point p = e.getLocation();
            for (int i = 1; i < PATH.length; i++) {
                if (inBetween(p, PATH[i - 1], PATH[i])) {
                    e.move(PATH[i].x - p.x, PATH[i].y - p.y);
                    continue e;
                }
            }
            end(false);
        }
    }

    /**
     * Update the user currency
     */
    public void addCurrency(int amount) {
        currency += amount;
    }

    /**
     * Render a key at the given location
     */
    private void paintKey(Graphics2D g2d, Point p, String key) {
        paintKey(g2d, p, key, KEY_SIZE);
    }

    /**
     * Render a key at the given location with a custom width
     */
    private void paintKey(Graphics2D g2d, Point p, String key, int width) {
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.black);
        g2d.drawString(key, p.x - KEY_STRING_OFFSET, p.y + KEY_STRING_OFFSET);
        g2d.drawRoundRect(p.x - KEY_SIZE / 2, p.y - KEY_SIZE / 2, width, KEY_SIZE, KEY_ARC, KEY_ARC);
    }

    /**
     * Draw the game
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.draw(getArena());
        g2d.setColor(new Color(0, 154, 23));
        g2d.fill(getArena());


        g2d.setStroke(new BasicStroke(PATH_WIDTH));
        g2d.setColor(new Color(255, 253, 208));
        for (int i = 1; i < PATH.length; i++) {
            g2d.drawLine(PATH[i - 1].x, PATH[i - 1].y, PATH[i].x, PATH[i].y);
        }
        // Paint all the enemies
        for (Enemy e : enemies) {
            e.paint(g2d);
        }
        // Paint all the towers
        for (Tower t : towers) {
            t.paint(g2d);
        }

        Tower preview = getPreviewTower(false);
        if (preview != null) {
            preview.paint(g2d);
            if (!canPlace) {
                g2d.setStroke(new BasicStroke(5));
                g2d.setColor(Color.red);
                g2d.drawLine(mouse.x - X_SIZE / 2, mouse.y - X_SIZE / 2, mouse.x + X_SIZE / 2, mouse.y + X_SIZE / 2);
                g2d.drawLine(mouse.x - X_SIZE / 2, mouse.y + X_SIZE / 2, mouse.x + X_SIZE / 2, mouse.y - X_SIZE / 2);
            }
        }

        g2d.setColor(Color.white);
        g2d.fillRect(0, SIZE, SIZE, BOTTOM_HEIGHT);

        // Paints the lower game bar
        paintKey(g2d, KEY_LOCATIONS[0], "W");
        paintKey(g2d, KEY_LOCATIONS[1], "A");
        paintKey(g2d, KEY_LOCATIONS[2], "S");
        paintKey(g2d, KEY_LOCATIONS[3], "D");
        for (int i = 0; i <= TOWER_PRICES.length; i++) {
            Point tower = new Point((4 * i + 8) * Tower.RADIUS, SIZE + BOTTOM_HEIGHT / 2);
            Point key = new Point(tower.x - 2 * Tower.RADIUS, tower.y);
            if (i > 0) {
                paintKey(g2d, key, "" + i);
                generateTower(i, tower, false).paint(g2d);
                g2d.setColor(currency < TOWER_PRICES[i-1] ? Color.red : Color.black);
                g2d.drawString("$" + TOWER_PRICES[i-1], tower.x + TOWER_PRICE_OFFSET.x, tower.y + Tower.RADIUS + TOWER_PRICE_OFFSET.y);
            } else {
                paintKey(g2d, key, "Esc", ESC_WIDTH);
                g2d.setStroke(new BasicStroke(10));
                g2d.setColor(Color.red);
                g2d.drawLine(tower.x - Tower.RADIUS / 4, tower.y - Tower.RADIUS / 2, tower.x + Tower.RADIUS * 3 / 4, tower.y + Tower.RADIUS / 2);
                g2d.drawLine(tower.x - Tower.RADIUS / 4, tower.y + Tower.RADIUS / 2, tower.x + Tower.RADIUS * 3 / 4, tower.y - Tower.RADIUS / 2);
            }
        }

        Font font = g2d.getFont();
        g2d.setColor(Color.black);
        g2d.setFont(font.deriveFont((float) CURRENCY_FONT_SIZE));
        g2d.drawString("$" + currency, CURRENCY_OFFSET, CURRENCY_OFFSET + CURRENCY_FONT_SIZE);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    /**
     * Handle key presses
     */
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_1 -> {
                if (currency >= TOWER_PRICES[0]) preview = 1;
            }
            case KeyEvent.VK_2 -> {
                if (currency >= TOWER_PRICES[1]) preview = 2;
            }
            case KeyEvent.VK_3 -> {
                if (currency >= TOWER_PRICES[2]) preview = 3;
            }
            case KeyEvent.VK_ESCAPE -> preview = 0;
            case KeyEvent.VK_W -> direction = new Point(0, -1);
            case KeyEvent.VK_A -> direction = new Point(-1, 0);
            case KeyEvent.VK_S -> direction = new Point(0, 1);
            case KeyEvent.VK_D -> direction = new Point(1, 0);
        }

        if (e.getKeyCode() == KeyEvent.VK_UP && cheatCode <= 1 ||
            e.getKeyCode() == KeyEvent.VK_DOWN && cheatCode > 1 && cheatCode <= 3 ||
            e.getKeyCode() == KeyEvent.VK_LEFT && (cheatCode == 4 || cheatCode == 6) ||
            e.getKeyCode() == KeyEvent.VK_RIGHT && (cheatCode == 5 || cheatCode == 7) ||
            e.getKeyCode() == KeyEvent.VK_B && cheatCode == 8 ||
            e.getKeyCode() == KeyEvent.VK_A && cheatCode == 9) {
            cheatCode++;
        } else {
            cheatCode = 0;
        }
        if (cheatCode == 10) {
            currency = Integer.MAX_VALUE / 2;
            cheatCode = 0;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    /**
     * Handle mouse clicks
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if (preview != 0 && canPlace) {
            towers.add(getPreviewTower(true));
            currency -= TOWER_PRICES[preview-1];
            preview = 0;
        }
        for (int i = 0; i < TOWER_PRICES.length; i++) {
            Point tower = new Point((4 * i + 12) * Tower.RADIUS, SIZE + BOTTOM_HEIGHT / 2);
            Rectangle hitbox = new Rectangle(tower.x - Tower.RADIUS, tower.y - Tower.RADIUS, 2 * Tower.RADIUS, 2 * Tower.RADIUS);
            if (hitbox.contains(e.getPoint()) && currency >= TOWER_PRICES[i]) {
                preview = i + 1;
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
