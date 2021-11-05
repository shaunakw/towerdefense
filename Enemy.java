import java.awt.*;
/**
 * Creates Enemy class of superclass GameObject
 */
public class Enemy extends GameObject {
    //Initiates instsance variables for enemy class
    public static final int RADIUS = 20;
    public static final int SPEED = 2;
    //Creates health isntance variable
    private int health;
    //Creates Enemy constructor with point and health
    public Enemy(Point p, int health) {
        super(p);
        this.health = health;
    }

    /**
     * @return whether the enemy is dead and can be safely removed
     */
    public boolean isDead() {
        return health <= 0;
    }

    /**
     * Normalize dx and dy and move in that direction
     */
    public void move(int dx, int dy) {
        x += Math.signum(dx) * SPEED;
        y += Math.signum(dy) * SPEED;
    }

    /**
     * Reduce health by 1 and increase money by 1
     */
    public void damage() {
        health--;
        TowerDefense.getInstance().addCurrency(1);
    }
    //Paints components for enemy
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
