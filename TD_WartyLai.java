import javax.swing.JFrame;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;
/**
 * Creates the Main class including creating the frame and executing all actions
 */
public class TD_WartyLai {
    public static void main(String[] args) {
        JFrame f = new JFrame("Tower Defense");
        TowerDefense td = TowerDefense.getInstance();

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().setPreferredSize(new Dimension(TowerDefense.SIZE, TowerDefense.SIZE + 100));
        f.pack();
        f.add(td);
        f.setVisible(true);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                td.update();
                td.repaint();
            }
        }, 0, 1000 / 60);
    }
}
