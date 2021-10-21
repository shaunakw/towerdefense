import java.awt.*;

public abstract class Tower extends GameObject {
    public static final int RADIUS = 25;

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
