import java.awt.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Cannon extends Tower {
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