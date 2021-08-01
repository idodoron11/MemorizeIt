import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Properties;

public class Settings extends JDialog{
    private JPanel mainPanel;
    private JPanel settingsLayout;
    private JPanel settingsDashboard;
    private JButton saveSettingsButton;
    private JButton cancelButton;
    private JSpinner waitAfterInteractionInput;
    private JSpinner maxWaitAfterInteractionInput;
    private JSpinner successRateThresholdInput;
    private JSpinner interactionsFocusInput;

    public Settings(String title, CardGUI parent) {
        super(parent);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();
        waitAfterInteractionInput.setModel(new SpinnerNumberModel(0, 0, null, 1));
        waitAfterInteractionInput.setSize(28, 20);
        waitAfterInteractionInput.setMinimumSize(new Dimension(-1, -1));
        maxWaitAfterInteractionInput.setModel(new SpinnerNumberModel(1, 1, null, 1));
        successRateThresholdInput.setModel(new SpinnerNumberModel(100.0, 0.0, 100.0, 0.5));
        interactionsFocusInput.setModel(new SpinnerNumberModel(0, 0, null, 1));

        // Load settings to form
        waitAfterInteractionInput.setValue(Integer.parseInt(config.getProperty("waitAfterInteraction")));
        maxWaitAfterInteractionInput.setValue(Integer.parseInt(config.getProperty("maxWaitAfterInteraction")));
        successRateThresholdInput.setValue(Double.parseDouble(config.getProperty("successRateThreshold")));
        interactionsFocusInput.setValue(Integer.parseInt(config.getProperty("interactionsFocus")));

        // Listeners
        saveSettingsButton.addActionListener(e -> {
            try {
                int waitAfterInteractionValue = (int)waitAfterInteractionInput.getValue();
                int maxWaitAfterInteractionValue = (int)maxWaitAfterInteractionInput.getValue();
                double successRateThresholdValue = (double)successRateThresholdInput.getValue();
                int interactionsFocusValue = (int)interactionsFocusInput.getValue();
                config.setProperty("waitAfterInteraction", String.valueOf(waitAfterInteractionValue));
                config.setProperty("maxWaitAfterInteraction", String.valueOf(maxWaitAfterInteractionValue));
                config.setProperty("successRateThreshold", String.valueOf(successRateThresholdValue));
                config.setProperty("interactionsFocus", String.valueOf(interactionsFocusValue));
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
        CardGUI parent = (CardGUI) this.getOwner();
        parent.queue.refreshQueue();
        parent.updateQuestion();
    }
}
