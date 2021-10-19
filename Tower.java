import java.awt.*;

abstract class Tower extends GameObject {
    public static final int RADIUS = 25;

    public Tower(Point p) {
        super(p);
    }

    /**
     * Update internal data
     */
    abstract void update();

    /**
     * @return whether this tower killed e
     */
    abstract boolean damage(Enemy e);

    /**
     * Stop any ongoing tasks (e.g. timers)
     */
    abstract void stop();
}
