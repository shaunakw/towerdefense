import java.awt.*;
/**
 * Creates the Spreader class of Tower superclass
 */
public class Spreader extends Tower {
    //Initiates instance variables of Spreader class
    public static final int PROJ_RADIUS = 5;
    public static final int SPREAD_SPEED = 2;
    public static final int RANGE = 100;

    private boolean[] render;
    private int spread = 0;
    //Creates a constructor for Spreader class
    public Spreader(Point p) {
        super(p);
        resetProjectiles();
    }
    //Resets the spreader projectiles
    private void resetProjectiles() {
        render = new boolean[]{true, true, true, true, true, true, true, true};
    }
    //Method that accesses the angle and points of spread projectiles
    private Point getProjectile(int i) {
        double angle = Math.toRadians(i * 45);
        return new Point(x + (int) (Math.cos(angle) * spread), y + (int) (Math.sin(angle) * spread));
    }
    //Creates the update method for spreader
    @Override
    public void update() {
        spread = (spread + SPREAD_SPEED) % RANGE;
        if (spread == 0) resetProjectiles();
    }
    //Creates interact method for spreader
    @Override
    public void interact(Enemy e) {
        for (int i = 0; i < 8; i++) {
            if (render[i] && e.getLocation().distance(getProjectile(i)) < Enemy.RADIUS) {
                render[i] = false;
                e.damage();
                return;
            }
        }
    }
    //Paints the spreader components into the game
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
    // Creates stop method that ends timer
    @Override
    public void stop() {
    }
}
