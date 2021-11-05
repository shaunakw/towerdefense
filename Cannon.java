import java.awt.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Basic tower that shoots projectiles in a given direction
 */
public class Cannon extends Tower {
    public static final int PROJ_RADIUS = 10;
    public static final int PROJ_SPEED = 4;
    public static final int PERIOD = 1500;
    public static final int SHOOTER_SIZE = 20;

    private final int dx;
    private final int dy;

    private final ArrayList<Point> projectiles = new ArrayList<>();
    private final Timer timer;

    /**
     * Creates a cannon at the given location (p) with the given direction (d)
     */
    public Cannon(Point p, Point d) {
        super(p);
        dx = d.x;
        dy = d.y;
        timer = new Timer();
        start();
    }

    /**
     * Starts timer
     */
    private void start() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                projectiles.add(new Point(x, y));
            }
        }, 0, PERIOD);
    }

    /**
     * Updates the location of each projectile
     */
    @Override
    public void update() {
        for (Point p : projectiles) {
            p.translate(dx * PROJ_SPEED, dy * PROJ_SPEED);
        }
    }

    /**
     * Checks if a given enemy is in contact with a projectile
     */
    @Override
    public void interact(Enemy e) {
        for (int i = 0; i < projectiles.size(); i++) {
            if (e.getLocation().distance(projectiles.get(i)) < Enemy.RADIUS) {
                projectiles.remove(i);
                e.damage();
                return;
            }
        }
    }

    /**
     * Draws the tower and its projectiles
     */
    @Override
    public void paint(Graphics2D g2d) {
        g2d.setColor(new Color(54, 34, 4));
        g2d.fillOval(x - RADIUS, y - RADIUS, 2 * RADIUS, 2 * RADIUS);

        g2d.setColor(Color.black);
        for (Point p : projectiles) {
            g2d.fillOval(p.x - PROJ_RADIUS, p.y - PROJ_RADIUS, PROJ_RADIUS * 2, PROJ_RADIUS * 2);
        }

        g2d.setColor(new Color(188, 158, 130));
        g2d.fillRect(x + (dx * 2 - 1) * SHOOTER_SIZE / 2, y + (dy * 2 - 1) * SHOOTER_SIZE / 2, SHOOTER_SIZE, SHOOTER_SIZE);
        g2d.fillOval(x - INNER_RADIUS, y - INNER_RADIUS, 2 * INNER_RADIUS, 2 * INNER_RADIUS);
    }

    /**
     * Stops the timer
     */
    @Override
    public void stop() {
        timer.cancel();
    }
}
