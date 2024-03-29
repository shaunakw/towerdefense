import java.awt.*;

/**
 * Tower that repeatedly shoots projectiles in 8 directions
 */
public class Spreader extends Tower {
    public static final int N_PROJ = 8;
    public static final int PROJ_RADIUS = 5;
    public static final int SPREAD_SPEED = 2;
    public static final int RANGE = 100;
    public static final double SHOOTER_ANGLE = 0.2;
    public static final int SHOOTER_LENGTH = 5;

    private boolean[] render;
    private int spread = 0;

    /**
     * Creates a spreader at the given location
     */
    public Spreader(Point p) {
        super(p);
        resetProjectiles();
    }

    /**
     * Clears all the projectiles
     */
    private void resetProjectiles() {
        render = new boolean[]{ true, true, true, true, true, true, true, true };
    }

    /**
     * @return the location of projectile i
     */
    private Point getProjectile(int i) {
        double angle = Math.toRadians(i * 360.0 / N_PROJ);
        return new Point(x + (int) (Math.cos(angle) * spread), y + (int) (Math.sin(angle) * spread));
    }

    /**
     * Updates the location of each projectile
     */
    @Override
    public void update() {
        spread = (spread + SPREAD_SPEED) % RANGE;
        if (spread == 0) resetProjectiles();
    }

    /**
     * Checks if the given enemy is in contact with a projectile
     */
    @Override
    public void interact(Enemy e) {
        for (int i = 0; i < N_PROJ; i++) {
            if (render[i] && e.getLocation().distance(getProjectile(i)) < Enemy.RADIUS) {
                render[i] = false;
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
        for (int i = 0; i < N_PROJ; i++) {
            if (render[i]) {
                Point proj = getProjectile(i);
                g2d.setColor(Color.black);
                g2d.fillOval(proj.x - PROJ_RADIUS, proj.y - PROJ_RADIUS, 2 * PROJ_RADIUS, 2 * PROJ_RADIUS);
            }

            double angle = Math.toRadians(i * 360. / N_PROJ);
            int[] xPts = {
                    x + (int) (Math.cos(angle - SHOOTER_ANGLE) * RADIUS),
                    x + (int) (Math.cos(angle - SHOOTER_ANGLE) * (RADIUS + SHOOTER_LENGTH)),
                    x + (int) (Math.cos(angle + SHOOTER_ANGLE) * (RADIUS + SHOOTER_LENGTH)),
                    x + (int) (Math.cos(angle + SHOOTER_ANGLE) * RADIUS),
            };
            int[] yPts = {
                    y + (int) (Math.sin(angle - SHOOTER_ANGLE) * RADIUS),
                    y + (int) (Math.sin(angle - SHOOTER_ANGLE) * (RADIUS + SHOOTER_LENGTH)),
                    y + (int) (Math.sin(angle + SHOOTER_ANGLE) * (RADIUS + SHOOTER_LENGTH)),
                    y + (int) (Math.sin(angle + SHOOTER_ANGLE) * RADIUS),
            };
            g2d.setColor(Color.darkGray);
            g2d.fillPolygon(xPts, yPts, 4);
        }

        g2d.setColor(Color.gray);
        g2d.fillOval(x - RADIUS, y - RADIUS, 2 * RADIUS, 2 * RADIUS);
        g2d.setColor(Color.darkGray);
        g2d.fillOval(x - INNER_RADIUS, y - INNER_RADIUS, 2 * INNER_RADIUS, 2 * INNER_RADIUS);
    }

    @Override
    public void stop() {
    }
}
