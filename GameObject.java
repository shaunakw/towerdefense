import java.awt.*;

/**
 * Any object in the game that has a location
 */
public abstract class GameObject {
    protected int x;
    protected int y;

    /**
     * Creates a new object at the given location
     */
    public GameObject(Point p) {
        x = p.x;
        y = p.y;
    }

    /**
     * @return the location of the object
     */
    public Point getLocation() {
        return new Point(x, y);
    }

    /**
     * Render the object
     */
    abstract void paint(Graphics2D g2d);
}
