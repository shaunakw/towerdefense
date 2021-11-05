import java.awt.*;
/**
 * Creates GameObject superclass for all objects in the game
 */
public abstract class GameObject {
    //Initiates integer x and y variables
    protected int x;
    protected int y;
    /**
     * Creates the GameObject constructor
     * @param p
     */
    public GameObject(Point p) {
        x = p.x;
        y = p.y;
    }
    /**
     * Creates the getLocation method
     * @return Point(x,y)
     */
    public Point getLocation() {
        return new Point(x, y);
    }

    /**
     * Render the tower
     */
    abstract void paint(Graphics2D g2d);
}
