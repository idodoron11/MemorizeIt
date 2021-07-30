import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SettingsGUI extends JFrame{
    private JPanel mainPanel;
    private JPanel settingsLayout;
    private JPanel settingsDashboard;
    private JButton saveSettingsButton;
    private JButton cancelButton;
    private JLabel waitAfterIneractionLabel;
    private JFormattedTextField waitAfterIneractionInput;

    public SettingsGUI(String title) {
        super(title);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();
        saveSettingsButton.addActionListener(e -> SettingsGUI.super.dispose());
        cancelButton.addActionListener(e -> SettingsGUI.super.dispose());
    }
}
