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
 * Creates the TowerDefense class which JPanel superclass and KeyListener and MouseListener interfaces
 */
public class TowerDefense extends JPanel implements KeyListener, MouseListener {
    public static final int SIZE = 600;
    public static final int BOTTOM_HEIGHT = 120;
    public static final int PATH_WIDTH = 50;
    public static final int PERIOD = 1000;
    public static final int START_CURRENCY = 10;
    public static final int CURRENCY_FONT_SIZE = 20;

    private final Point[] path = {
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
    private final int[] order = {
            0, 0, 0, 1, 1, 1, 1, 1, 1,
            0, 0, 0, 1, 1, 1, 2, 2, 2,
            0, 0, 0, 3, 3, 0, 2, 2, 2,
            0, 0, 0, 3, 2, 3, 2, 3, 2,
            0, 0, 0, 4, 3, 4, 3, 4, 3,
            0, 0, 0, 5, 5, 0, 5, 5, 5
    };
    private final int[] towerPrices = {5, 10, 15};
    // Creates an arraylist of enemies (balls), arraylist of towers, and timer
    private ArrayList<Enemy> enemies;
    private ArrayList<Tower> towers;
    private Timer timer;

    private int currency = 5;
    private int stage = 0;
    private int preview = 0;
    private boolean canPlace = false;
    private Point mouse = new Point(SIZE / 2, SIZE / 2); // center (default)
    private Point direction = new Point(-1, 0); // left (default)
    private int cheatCode = 0;

    private static TowerDefense instance;
    // Creates TowerDefense constructor
    private TowerDefense() {
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        start();
    }
    // Returns an instance of Tower Defense whether it exists or not
    public static TowerDefense getInstance() {
        if (instance == null) {
            instance = new TowerDefense();
        }
        return instance;
    }
    // Gets a rectangle for size of arena
    private Rectangle getArena() {
        return new Rectangle(0, 0, SIZE, SIZE);
    }

    // Previews a tower when mouse hovers
    private Tower getPreviewTower(boolean permanent) {
        return generateTower(preview, mouse, permanent);
    }
    // Creates a tower
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
    // Creates start method which basically resets everything and starts the game
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
                if (stage < order.length) {
                    if (order[stage] != 0) {
                        enemies.add(new Enemy(path[0], order[stage]));
                    }
                    stage++;
                }
            }
        }, 0, PERIOD);
    }
    // Initiates ending the game
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
    // Creates and update method for the game
    public void update() {
        if (enemies.size() == 0 && stage == order.length) {
            end(true);
        }

        mouse = MouseInfo.getPointerInfo().getLocation();
        canPlace = getArena().contains(mouse);
        for (int i = 1; i < path.length; i++) {
            if (new Line2D.Double(path[i - 1], path[i]).ptSegDist(mouse) < PATH_WIDTH / 2.0 + Tower.RADIUS) {
                canPlace = false;
                break;
            }
        }
        // Places towers
        for (Tower t : towers) {
            if (t.getLocation().distance(mouse) < Tower.RADIUS * 2) {
                canPlace = false;
            }
            //Detects for towers hitting the enemies
            for (int i = 0; i < enemies.size(); i++) {
                t.interact(enemies.get(i));
                if (enemies.get(i).isDead()) {
                    enemies.remove(i);
                    i--;
                }
            }
            t.update();
        }
        //Updates enemies
        e:
        for (Enemy e : enemies) {
            Point p = e.getLocation();
            for (int i = 1; i < path.length; i++) {
                if (inBetween(p, path[i - 1], path[i])) {
                    e.move(path[i].x - p.x, path[i].y - p.y);
                    continue e;
                }
            }
            end(false);
        }
    }

    // Creates add currency method for currency system
    public void addCurrency(int amount) {
        currency += amount;
    }
    // Creates paint key method for painting onto the game
    private void paintKey(Graphics2D g2d, Point p, String key) {
        paintKey(g2d, p, key, 20);
    }
    // Creates paint key method with different parameters
    private void paintKey(Graphics2D g2d, Point p, String key, int width) {
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.black);
        g2d.drawString(key, p.x - 4, p.y + 4);
        g2d.drawRoundRect(p.x - 10, p.y - 10, width, 20, 5, 5);
    }

    // Paints everything into the game
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.draw(getArena());
        g2d.setColor(new Color(0, 154, 23));
        g2d.fill(getArena());


        g2d.setStroke(new BasicStroke(PATH_WIDTH));
        g2d.setColor(new Color(255, 253, 208));
        for (int i = 1; i < path.length; i++) {
            g2d.drawLine(path[i - 1].x, path[i - 1].y, path[i].x, path[i].y);
        }
        // Paint all the enemies
        for (Enemy e : enemies) {
            e.paint(g2d);
        }
        // Paint all the towers
        for (Tower t : towers) {
            t.paint(g2d);
        }
        // Creates preview interaction for towers
        Tower preview = getPreviewTower(false);
        if (preview != null) {
            preview.paint(g2d);
            if (!canPlace) {
                g2d.setStroke(new BasicStroke(5));
                g2d.setColor(Color.red);
                g2d.drawLine(mouse.x - 5, mouse.y - 5, mouse.x + 5, mouse.y + 5);
                g2d.drawLine(mouse.x - 5, mouse.y + 5, mouse.x + 5, mouse.y - 5);
            }
        }

        g2d.setColor(Color.white);
        g2d.fillRect(0, SIZE, SIZE, BOTTOM_HEIGHT);

        // Paints the lower game bar
        paintKey(g2d, new Point(55, SIZE + 35), "W");
        paintKey(g2d, new Point(30, SIZE + 60), "A");
        paintKey(g2d, new Point(55, SIZE + 60), "S");
        paintKey(g2d, new Point(80, SIZE + 60), "D");
        for (int i = 0; i <= towerPrices.length; i++) {
            Point text = new Point((4 * i + 1) * Tower.RADIUS + 120, SIZE + 50);
            Point tower = new Point((4 * i + 3) * Tower.RADIUS + 120, SIZE + 50);
            if (i > 0) {
                paintKey(g2d, text, "" + i);
                generateTower(i, tower, false).paint(g2d);
                g2d.setColor(currency < towerPrices[i-1] ? Color.red : Color.black);
                g2d.drawString("$" + towerPrices[i-1], tower.x - 8, tower.y + Tower.RADIUS + 20);
            } else {
                paintKey(g2d, text, "Esc", 32);
                g2d.setStroke(new BasicStroke(10));
                g2d.setColor(Color.red);
                g2d.drawLine(tower.x - 5, tower.y - 10, tower.x + 15, tower.y + 10);
                g2d.drawLine(tower.x - 5, tower.y + 10, tower.x + 15, tower.y - 10);
            }
        }

        Font font = g2d.getFont();
        g2d.setColor(Color.black);
        g2d.setFont(font.deriveFont((float) CURRENCY_FONT_SIZE));
        g2d.drawString("$" + currency, 10, 10 + CURRENCY_FONT_SIZE);
    }
    //Checks for a key being clicked
    @Override
    public void keyTyped(KeyEvent e) {
    }
    //Method detecting which key is pressed
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_1 -> {
                if (currency >= towerPrices[0]) preview = 1;
            }
            case KeyEvent.VK_2 -> {
                if (currency >= towerPrices[1]) preview = 2;
            }
            case KeyEvent.VK_3 -> {
                if (currency >= towerPrices[2]) preview = 3;
            }
            case KeyEvent.VK_ESCAPE -> preview = 0;
            case KeyEvent.VK_W -> direction = new Point(0, -1);
            case KeyEvent.VK_A -> direction = new Point(-1, 0);
            case KeyEvent.VK_S -> direction = new Point(0, 1);
            case KeyEvent.VK_D -> direction = new Point(1, 0);
        }
        // Creating the best cheat code in history
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
            // yes
            currency = 999999;
            cheatCode = 0;
        }
    }
    // Checks for when a key is released
    @Override
    public void keyReleased(KeyEvent e) {
    }
    // Places tower upon the click of the mouse
    @Override
    public void mouseClicked(MouseEvent e) {
        if (preview != 0 && canPlace) {
            towers.add(getPreviewTower(true));
            currency -= towerPrices[preview-1];
            preview = 0;
        }
        for (int i = 0; i < towerPrices.length; i++) {
            Point tower = new Point((4 * i + 7) * Tower.RADIUS + 120, SIZE + 50);
            Rectangle hitbox = new Rectangle(tower.x - Tower.RADIUS, tower.y - Tower.RADIUS, 2 * Tower.RADIUS, 2 * Tower.RADIUS);
            if (hitbox.contains(e.getPoint()) && currency >= towerPrices[i]) {
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
