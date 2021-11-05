import java.awt.*;

/**
 * Tower that is a game object and can interact with enemies
 */
public abstract class Tower extends GameObject {
    public static final int RADIUS = 25;
    public static final int INNER_RADIUS = RADIUS - 10;

    /**
     * Creates a new tower at the given position
     */
    public Tower(Point p) {
        super(p);
    }

    /**
     * Updates internal data
     */
    abstract void update();

    /**
     * Interacts with an enemy, potentially damaging it
     */
    abstract void interact(Enemy e);

    /**
     * Stops any ongoing tasks (e.g. timers)
     */
    abstract void stop();
}
