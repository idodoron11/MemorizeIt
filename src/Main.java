import javax.swing.*;
import java.io.*;
import java.util.Properties;

public class Main {

    public static void main(String[] args) {
        try {
            Settings.loadConfiguration();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JFrame frame = new CardGUI("MemorizeIt");
        frame.setVisible(true);
    }
}
