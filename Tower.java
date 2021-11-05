import java.awt.*;
/**
 * Creates a Tower class with superclass GameObject
 */
public abstract class Tower extends GameObject {
    public static final int RADIUS = 25;
    /**
     * Creates Tower constructor using superclass constructor
     * @param p
     */
    public Tower(Point p) {
        super(p);
    }

    /**
     * Update internal data
     */
    abstract void update();

    /**
     * Interact with an enemy, potentially damaging it
     */
    abstract void interact(Enemy e);

    /**
     * Stop any ongoing tasks (e.g. timers)
     */
    abstract void stop();
}
