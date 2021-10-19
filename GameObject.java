import java.awt.*;

public abstract class GameObject {
    protected int x;
    protected int y;

    public GameObject(Point p) {
        x = p.x;
        y = p.y;
    }

    public Point getLocation() {
        return new Point(x, y);
    }

    /**
     * Render the tower
     */
    abstract void paint(Graphics2D g2d);
}
