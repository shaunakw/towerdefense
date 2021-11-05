import java.awt.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
/**
 * Creates Bomber class of Tower superclass
 */
public class Bomber extends Tower {
    //Creates instance variables for Bomber
    public static final int N_PROJ = 8;
    public static final int PROJ_RADIUS = 15;
    public static final int PROJ_SPEED = 2;
    public static final int EXPLODE_DISTANCE = 250;
    public static final int FRAG_RADIUS = 5;
    public static final int FRAG_SPEED = 4;
    public static final int FRAG_MAX_DISTANCE = 100;
    public static final int PERIOD = 4000;
    public static final int SHOOTER_SIZE = 20;
    public static final int BORDER_ARC = 10;

    private final int dx;
    private final int dy;
    //Creates projectiles arraylist of the small projectiles
    private final ArrayList<Projectile> projectiles = new ArrayList<>();
    private final Timer timer;
    /**
     * Creates a projectile class for our game projectiles
     */
    private static class Projectile {
        Point pos;
        int fragPos;
        boolean[] frags;
        Enemy initialHit;

        Projectile(int x, int y) {
            pos = new Point(x, y);
            fragPos = 0;
            frags = new boolean[]{ true, true, true, true, true, true, true, true };
        }
    }
    //Creates Bomber constructor
    public Bomber(Point p, Point d) {
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
                projectiles.add(new Projectile(x + dx * 20, y + dy * 20));
            }
        }, 0, PERIOD);
    }
    /**
     * Creates getFrag method which returns a point
     * @param p
     * @param i
     * @return Point
     */
    private Point getFrag(Projectile p, int i) {
        double angle = Math.toRadians(i * 360. / N_PROJ);
        return new Point(p.pos.x + (int) (Math.cos(angle) * p.fragPos), p.pos.y + (int) (Math.sin(angle) * p.fragPos));
    }
    /**
     * Creates update method that detects if projectiles hits enemies
     */
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
    //Creates interact method that checks if projectiles hit enemy + inflicts damage
    @Override
    public void interact(Enemy e) {
        for (Projectile p : projectiles) {
            if (p.fragPos > 0) {
                for (int i = 0; i < N_PROJ; i++) {
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
    //Paints Bomber components into the game
    @Override
    public void paint(Graphics2D g2d) {
        g2d.setColor(Color.black);
        g2d.fillRoundRect(x - RADIUS, y - RADIUS, 2 * RADIUS, 2 * RADIUS, BORDER_ARC, BORDER_ARC);

        g2d.setColor(Color.black);
        for (Projectile p : projectiles) {
            if (p.fragPos > 0) {
                for (int i = 0; i < N_PROJ; i++) {
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
        g2d.fillRect(x + (dx * 2 - 1) * SHOOTER_SIZE / 2, y + (dy * 2 - 1) * SHOOTER_SIZE / 2, SHOOTER_SIZE, SHOOTER_SIZE);
        g2d.fillOval(x - INNER_RADIUS, y - INNER_RADIUS, 2 * INNER_RADIUS, 2 * INNER_RADIUS);
    }
    // Creates stop method that ends timer
    @Override
    public void stop() {
        timer.cancel();
    }
}
