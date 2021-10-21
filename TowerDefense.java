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

public class TowerDefense extends JPanel implements KeyListener, MouseListener {
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
            case 3 -> new Bomber(p, direction);
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
            if (new Line2D.Double(path[i - 1], path[i]).ptSegDist(mouse) < PATH_WIDTH / 2.0 + Tower.RADIUS) {
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
            for (int i = 1; i < path.length; i++) {
                if (inBetween(p, path[i - 1], path[i])) {
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
            g2d.drawLine(path[i - 1].x, path[i - 1].y, path[i].x, path[i].y);
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
        for (int i = 0; i <= 3; i++) {
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
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_1 -> preview = 1;
            case KeyEvent.VK_2 -> preview = 2;
            case KeyEvent.VK_3 -> preview = 3;
            case KeyEvent.VK_ESCAPE -> preview = 0;
            case KeyEvent.VK_W, KeyEvent.VK_UP -> direction = new Point(0, -1);
            case KeyEvent.VK_A, KeyEvent.VK_LEFT -> direction = new Point(-1, 0);
            case KeyEvent.VK_S, KeyEvent.VK_DOWN -> direction = new Point(0, 1);
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> direction = new Point(1, 0);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (preview != 0 && canPlace) {
            towers.add(getPreviewTower(true));
            preview = 0;
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
