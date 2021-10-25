import java.awt.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Bomber extends Tower {
    public static final int PROJ_RADIUS = 15;
    public static final int PROJ_SPEED = 2;
    public static final int EXPLODE_DISTANCE = 250;
    public static final int FRAG_RADIUS = 5;
    public static final int FRAG_SPEED = 4;
    public static final int FRAG_MAX_DISTANCE = 100;
    public static final int PERIOD = 4000;

    private final int dx;
    private final int dy;

    private final ArrayList<Projectile> projectiles = new ArrayList<>();
    private final Timer timer;

    private static class Projectile {
        Point pos;
        int fragPos;
        boolean[] frags;
        Enemy initialHit;

        Projectile(int x, int y) {
            pos = new Point(x, y);
            fragPos = 0;
            frags = new boolean[]{true, true, true, true, true, true, true, true};
        }
    }

    public Bomber(Point p, Point d) {
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
                projectiles.add(new Projectile(x + dx * 20, y + dy * 20));
            }
        }, 0, PERIOD);
    }

    private Point getFrag(Projectile p, int i) {
        double angle = Math.toRadians(i * 45);
        return new Point(p.pos.x + (int) (Math.cos(angle) * p.fragPos), p.pos.y + (int) (Math.sin(angle) * p.fragPos));
    }

    @Override
    public void update() {
        for (int i = 0; i < projectiles.size(); i++) {
            Projectile p = projectiles.get(i);
            if (p.fragPos > 0 || getLocation().distance(p.pos) >= EXPLODE_DISTANCE) {
                p.fragPos += FRAG_SPEED;
                if (p.fragPos > FRAG_MAX_DISTANCE) {
                    projectiles.remove(i);
                    i--;
                }
            } else {
                p.pos.translate(dx * PROJ_SPEED, dy * PROJ_SPEED);
            }
        }
    }

    @Override
    public void interact(Enemy e) {
        for (Projectile p : projectiles) {
            if (p.fragPos > 0) {
                for (int i = 0; i < 8; i++) {
                    if (p.frags[i] && e != p.initialHit && e.getLocation().distance(getFrag(p, i)) < Enemy.RADIUS) {
                        p.frags[i] = false;
                        e.damage();
                        return;
                    }
                }
            } else {
                if (e.getLocation().distance(p.pos) < Enemy.RADIUS) {
                    p.fragPos += FRAG_SPEED;
                    p.initialHit = e;
                    e.damage();
                    e.damage();
                    return;
                }
            }
        }
    }

    @Override
    public void paint(Graphics2D g2d) {
        g2d.setColor(Color.black);
        g2d.fillRoundRect(x - RADIUS, y - RADIUS, 2 * RADIUS, 2 * RADIUS, 10, 10);

        g2d.setColor(Color.black);
        for (Projectile p : projectiles) {
            if (p.fragPos > 0) {
                for (int i = 0; i < 8; i++) {
                    if (p.frags[i]) {
                        Point frag = getFrag(p, i);
                        g2d.fillOval(frag.x - FRAG_RADIUS, frag.y - FRAG_RADIUS, 2 * FRAG_RADIUS, 2 * FRAG_RADIUS);
                    }
                }
            } else {
                g2d.fillOval(p.pos.x - PROJ_RADIUS, p.pos.y - PROJ_RADIUS, PROJ_RADIUS * 2, PROJ_RADIUS * 2);
            }
        }

        g2d.setColor(Color.darkGray);
        g2d.fillRect(x + dx * 20 - 10, y + dy * 20 - 10, 20, 20);
        g2d.fillOval(x - RADIUS + 10, y - RADIUS + 10, 2 * RADIUS - 20, 2 * RADIUS - 20);
    }

    @Override
    public void stop() {
        timer.cancel();
    }
}
