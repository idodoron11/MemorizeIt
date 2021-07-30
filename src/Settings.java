import javax.swing.*;
import java.io.*;
import java.util.Properties;

public class Settings extends JFrame{
    private JPanel mainPanel;
    private JPanel settingsLayout;
    private JPanel settingsDashboard;
    private JButton saveSettingsButton;
    private JButton cancelButton;
    private JLabel waitAfterInteractionLabel;
    private JFormattedTextField waitAfterInteractionInput;
    private CardGUI parent;

    public Settings(String title, CardGUI parent) {
        super(title);
        this.parent = parent;
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();

        // Load settings to form
        waitAfterInteractionInput.setText(config.getProperty("waitAfterInteraction"));

        // Listeners
        saveSettingsButton.addActionListener(e -> {
            try {
                int waitAfterInteractionValue = Integer.parseInt(waitAfterInteractionInput.getText());
                if (waitAfterInteractionValue < 0) {
                    waitAfterInteractionValue *= -1;
                }
                config.setProperty("waitAfterInteraction", String.valueOf(waitAfterInteractionValue));
                saveConfiguration();
            } catch (IOException | NumberFormatException exp) {
                exp.printStackTrace();
            }
            Settings.super.dispose();
        });
        cancelButton.addActionListener(e -> Settings.super.dispose());
    }

    final static File configFile = new File("user.properties");
    final static File defaultConfigFile = new File("default.properties");
    final static Properties defaultConfig = new Properties();
    static Properties config;

    public static void loadConfiguration() throws IOException {
        FileReader defaultConfigFileReader = new FileReader(defaultConfigFile);
        defaultConfig.load(defaultConfigFileReader);
        defaultConfigFileReader.close();
        config = new Properties(defaultConfig);
        configFile.createNewFile();
        FileReader configFileReader = new FileReader(configFile);
        config.load(configFileReader);
        configFileReader.close();
    }

    private void resetConfiguration() throws IOException {
        config.clear();
        saveConfiguration();
    }

    public void saveConfiguration() throws IOException {
        Writer fw = new FileWriter(configFile);
        config.store(fw, "Saved Settings");
        fw.close();
        parent.queue.refreshQueue();
        parent.updateQuestion();
    }
}
