import javax.swing.JFrame;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Creates the Main class including creating the frame and executing all actions
 */
public class TD_WartyLai {
    public static void main(String[] args) {
        //Starts the JFrame
        JFrame f = new JFrame("Tower Defense");
        TowerDefense td = TowerDefense.getInstance();
        //Add components and properties to the JFrame
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().setPreferredSize(new Dimension(TowerDefense.SIZE, TowerDefense.SIZE + TowerDefense.BOTTOM_HEIGHT));
        f.pack();
        f.add(td);
        f.setVisible(true);
        //Starts a timer
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            //Runs
            @Override
            public void run() {
                td.update();
                td.repaint();
            }
        }, 0, 1000 / 60);
    }
}
