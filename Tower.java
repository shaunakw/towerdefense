import java.awt.*;
/**
 * Creates a Tower class which is a subclass of GameObject
 */
public abstract class Tower extends GameObject {
    public static final int RADIUS = 25;
    public static final int INNER_RADIUS = RADIUS - 10;

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
