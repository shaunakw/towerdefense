import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class TD_WartyLai {
    public static void main(String[] args) {
        JFrame f = new JFrame("Tower Defense");
        TowerDefense td = new TowerDefense();

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().setPreferredSize(new Dimension(TowerDefense.SIZE, TowerDefense.SIZE + 100));
        f.pack();
        f.add(td);
        f.setVisible(true);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                td.update();
                td.repaint();
            }
        }, 0, 1000 / 60);
    }
}

class TowerDefense extends JPanel implements KeyListener, MouseListener {
    public static final int SIZE = 600;
    public static final int PATH_WIDTH = 50;
    public static final int PERIOD = 1000;

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

    private final ArrayList<Enemy> enemies = new ArrayList<>();
    private final ArrayList<Tower> towers = new ArrayList<>();
    private final Timer timer = new Timer();

    private int stage = 0;
    private int preview = 0;
    private boolean canPlace = false;
    private Point mouse = new Point(SIZE / 2, SIZE / 2); // center (default)
    private Point direction = new Point(-1, 0); // left (default)

    public TowerDefense() {
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        start();
    }

    private Tower getPreviewTower(boolean permanent) {
        return generateTower(preview, mouse, permanent);
    }

    private Tower generateTower(int n, Point p, boolean permanent) {
        Tower tower = switch (n) {
            case 1 -> new Cannon(p, direction);
            case 2 -> new Spreader(p);
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

    private void start() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (stage < order.length) {
                    if (order[stage] != 0) {
                        enemies.add(new Enemy(path[0], order[stage]));
                    }
                    stage++;
                } else {
                    timer.cancel();
                }
            }
        }, 0, PERIOD);
    }

    private void end(boolean win) {
        for (Tower t : towers) {
            t.stop();
        }
        JOptionPane.showMessageDialog(null, win ? "You win!" : "You lose!");
        System.exit(0);
    }

    public void update() {
        if (enemies.size() == 0 && stage == order.length) {
            end(true);
        }

        mouse = MouseInfo.getPointerInfo().getLocation();
        canPlace = true;
        for (int i = 1; i < path.length; i++) {
            if (new Line2D.Double(path[i-1], path[i]).ptSegDist(mouse) < PATH_WIDTH / 2.0 + Tower.RADIUS) {
                canPlace = false;
                break;
            }
        }

        for (Tower t : towers) {
            if (t.getLocation().distance(mouse) < Tower.RADIUS * 2) {
                canPlace = false;
            }

            for (int i = 0; i < enemies.size(); i++) {
                if (t.damage(enemies.get(i))) {
                    enemies.remove(i);
                    i--;
                }
            }
            t.update();
        }

        e:
        for (Enemy e : enemies) {
            Point p = e.getLocation();
            for (int i = 1; i < path.length; i++) {
                if (inBetween(p, path[i-1], path[i])) {
                    e.move(path[i].x - p.x, path[i].y - p.y);
                    continue e;
                }
            }
            end(false);
        }
    }

    private void paintKey(Graphics2D g2d, Point p, String key) {
        paintKey(g2d, p, key, 20);
    }

    private void paintKey(Graphics2D g2d, Point p, String key, int width) {
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.black);
        g2d.drawString(key, p.x - 4, p.y + 4);
        g2d.drawRoundRect(p.x - 10, p.y - 10, width, 20, 5, 5);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.drawRect(0, 0, SIZE, SIZE);
        g2d.setColor(new Color(0, 154, 23));
        g2d.fillRect(0, 0, SIZE, SIZE);

        g2d.setStroke(new BasicStroke(PATH_WIDTH));
        g2d.setColor(new Color(255, 253, 208));
        for (int i = 1; i < path.length; i++) {
            g2d.drawLine(path[i-1].x, path[i-1].y, path[i].x, path[i].y);
        }

        for (Enemy e : enemies) {
            e.paint(g2d);
        }

        for (Tower t : towers) {
            t.paint(g2d);
        }

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

        paintKey(g2d, new Point(55, SIZE + 35), "W");
        paintKey(g2d, new Point(30, SIZE + 60), "A");
        paintKey(g2d, new Point(55, SIZE + 60), "S");
        paintKey(g2d, new Point(80, SIZE + 60), "D");
        for (int i = 0; i <= 2; i++) {
            Point text = new Point((4 * i + 1) * Tower.RADIUS + 120, SIZE + 50);
            Point tower = new Point((4 * i + 3) * Tower.RADIUS + 120, SIZE + 50);
            if (i > 0) {
                paintKey(g2d, text, "" + i);
                generateTower(i, tower, false).paint(g2d);
            } else {
                paintKey(g2d, text, "Esc", 32);
                g2d.setStroke(new BasicStroke(10));
                g2d.setColor(Color.red);
                g2d.drawLine(tower.x - 5, tower.y - 10, tower.x + 15, tower.y + 10);
                g2d.drawLine(tower.x - 5, tower.y + 10, tower.x + 15, tower.y - 10);
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_1 -> preview = 1;
            case KeyEvent.VK_2 -> preview = 2;
            case KeyEvent.VK_ESCAPE -> preview = 0;
            case KeyEvent.VK_W, KeyEvent.VK_UP -> direction = new Point(0, -1);
            case KeyEvent.VK_A, KeyEvent.VK_LEFT -> direction = new Point(-1, 0);
            case KeyEvent.VK_S, KeyEvent.VK_DOWN -> direction = new Point(0, 1);
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> direction = new Point(1, 0);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {
        if (preview != 0 && canPlace) {
            towers.add(getPreviewTower(true));
            preview = 0;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
}

abstract class GameObject {
    protected int x;
    protected int y;

    public GameObject(Point p) {
        x = p.x;
        y = p.y;
    }

    public Point getLocation() {
        return new Point(x, y);
    }

    /**
     * Render the tower
     */
    abstract void paint(Graphics2D g2d);
}

abstract class Tower extends GameObject {
    public static final int RADIUS = 25;

    public Tower(Point p) {
        super(p);
    }

    /**
     * Update internal data
     */
    abstract void update();

    /**
     * @return whether this tower killed e
     */
    abstract boolean damage(Enemy e);

    /**
     * Stop any ongoing tasks (e.g. timers)
     */
    abstract void stop();
}

class Cannon extends Tower {
    public static final int PROJ_RADIUS = 10;
    public static final int PROJ_SPEED = 4;
    public static final int PERIOD = 1500;

    private final int dx;
    private final int dy;

    private final ArrayList<Point> projectiles = new ArrayList<>();
    private final Timer timer;

    public Cannon(Point p, Point d) {
        super(p);
        dx = d.x;
        dy = d.y;
        timer = new Timer();
        start();
    }

    /**
     * Start timer
     */
    private void start() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                projectiles.add(new Point(x + dx * 20, y + dy * 20));
            }
        }, 0, PERIOD);
    }

    @Override
    public void update() {
        for (Point p : projectiles) {
            p.translate(dx * PROJ_SPEED, dy * PROJ_SPEED);
        }
    }

    @Override
    public boolean damage(Enemy e) {
        for (int i = 0; i < projectiles.size(); i++) {
            if (e.getLocation().distance(projectiles.get(i)) < Enemy.RADIUS) {
                projectiles.remove(i);
                return e.damage();
            }
        }
        return false;
    }

    @Override
    public void paint(Graphics2D g2d) {
        g2d.setColor(new Color(54, 34, 4));
        g2d.fillOval(x - RADIUS, y - RADIUS, 2 * RADIUS, 2 * RADIUS);

        g2d.setColor(Color.black);
        for (Point p : projectiles) {
            g2d.fillOval(p.x - PROJ_RADIUS, p.y - PROJ_RADIUS, PROJ_RADIUS * 2, PROJ_RADIUS * 2);
        }

        g2d.setColor(new Color(188, 158, 130));
        g2d.fillRect(x + dx * 20 - 10, y + dy * 20 - 10, 20, 20);
        g2d.fillOval(x - RADIUS + 10, y - RADIUS + 10, 2 * RADIUS - 20, 2 * RADIUS - 20);
    }

    @Override
    public void stop() {
        timer.cancel();
    }
}

class Spreader extends Tower {
    public static final int PROJ_RADIUS = 5;
    public static final int SPREAD_SPEED = 2;
    public static final int RANGE = 100;

    private boolean[] render;
    private int spread = 0;

    public Spreader(Point p) {
        super(p);
        resetProjectiles();
    }

    private void resetProjectiles() {
        render = new boolean[]{true, true, true, true, true, true, true, true};
    }

    private Point getProjectile(int i) {
        double angle = Math.toRadians(i * 45);
        return new Point(x + (int) (Math.cos(angle) * spread), y + (int) (Math.sin(angle) * spread));
    }

    @Override
    public void update() {
        spread = (spread + SPREAD_SPEED) % RANGE;
        if (spread == 0) resetProjectiles();
    }

    @Override
    public boolean damage(Enemy e) {
        for (int i = 0; i < 8; i++) {
            if (render[i] && e.getLocation().distance(getProjectile(i)) < Enemy.RADIUS) {
                render[i] = false;
                return e.damage();
            }
        }
        return false;
    }

    @Override
    public void paint(Graphics2D g2d) {
        for (int i = 0; i < 8; i++) {
            if (render[i]) {
                Point proj = getProjectile(i);
                g2d.setColor(Color.black);
                g2d.fillOval(proj.x - PROJ_RADIUS, proj.y - PROJ_RADIUS, 2 * PROJ_RADIUS, 2 * PROJ_RADIUS);
            }

            double angle = Math.toRadians(i * 45);
            int[] xPts = {
                    x + (int) (Math.cos(angle - 0.2) * RADIUS),
                    x + (int) (Math.cos(angle - 0.2) * (RADIUS + 5)),
                    x + (int) (Math.cos(angle + 0.2) * (RADIUS + 5)),
                    x + (int) (Math.cos(angle + 0.2) * RADIUS),
            };
            int[] yPts = {
                    y + (int) (Math.sin(angle - 0.2) * RADIUS),
                    y + (int) (Math.sin(angle - 0.2) * (RADIUS + 5)),
                    y + (int) (Math.sin(angle + 0.2) * (RADIUS + 5)),
                    y + (int) (Math.sin(angle + 0.2) * RADIUS),
            };
            g2d.setColor(Color.darkGray);
            g2d.fillPolygon(xPts, yPts, 4);
        }

        g2d.setColor(Color.gray);
        g2d.fillOval(x - RADIUS, y - RADIUS, 2 * RADIUS, 2 * RADIUS);
        g2d.setColor(Color.darkGray);
        g2d.fillOval(x - RADIUS + 10, y - RADIUS + 10, 2 * RADIUS - 20, 2 * RADIUS - 20);
    }

    @Override
    public void stop() {}
}

class Enemy extends GameObject {
    public static final int RADIUS = 20;
    public static final int SPEED = 2;

    private int health;

    public Enemy(Point p, int health) {
        super(p);
        this.health = health;
    }

    /**
     * Normalize dx and dy and move in that direction
     */
    public void move(int dx, int dy) {
        x += Math.signum(dx) * SPEED;
        y += Math.signum(dy) * SPEED;
    }

    /**
     * Reduce health by 1
     * @return whether the enemy is dead
     */
    public boolean damage() {
        health--;
        return health == 0;
    }

    @Override
    public void paint(Graphics2D g2d) {
        g2d.setColor(getColor());
        g2d.fillOval(x - RADIUS, y - RADIUS, RADIUS * 2, RADIUS * 2);
    }

    /**
     * @return enemy color (based on health)
     */
    private Color getColor() {
        return switch (health) {
            case 1 -> Color.red;
            case 2 -> Color.blue;
            case 3 -> Color.green;
            case 4 -> Color.yellow;
            case 5 -> Color.pink;
            default -> new Color(0, 0, 0, 0);
        };
    }
}
