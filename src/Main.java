import javax.swing.*;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            Settings.loadConfiguration();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            /*for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new MainGUI("MemorizeIt");
        frame.setVisible(true);
    }
}
