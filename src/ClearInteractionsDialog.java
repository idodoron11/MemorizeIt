import javax.swing.*;
import java.util.Date;

/**
 * This dialog is used for deleting cards interaction records.
 */

public class ClearInteractionsDialog extends JDialog {
    private JSpinner fromInput;
    private JSpinner untilInput;
    private JButton removeButton;
    private JButton resetALLInteractionsButton;
    private JPanel mainPanel;
    private JButton cancelButton;
    private long[] APIClearParameters;

    public ClearInteractionsDialog(MainGUI owner) {
        super(owner, true);
        this.setTitle("Reset Interactions History");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();
        fromInput.setModel(new SpinnerDateModel());
        fromInput.setEditor(new JSpinner.DateEditor(fromInput, "dd/MM/yyyy HH:MM"));
        untilInput.setModel(new SpinnerDateModel());
        untilInput.setEditor(new JSpinner.DateEditor(untilInput, "dd/MM/yyyy HH:MM"));
        cancelButton.addActionListener(e -> ClearInteractionsDialog.super.dispose());
        resetALLInteractionsButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(ClearInteractionsDialog.this,
                    "Are you sure you want to reset all interactions?",
                    "Confirmation Dialog",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_NO_OPTION) {
                this.APIClearParameters = new long[] {-1};
                dispose();
            }
        });
        removeButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(ClearInteractionsDialog.this,
                    "Are you sure you want to reset all interactions in the specified period?",
                    "Confirmation Dialog",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_NO_OPTION) {
                Date fromDate = (Date) fromInput.getValue();
                Date untilDate = (Date) untilInput.getValue();
                this.APIClearParameters = new long[] {
                        fromDate.getTime() / 1000,
                        untilDate.getTime() / 1000
                };
                dispose();
            }
        });
    }

    /**
     * Opens a new ClearInteractions dialog and returns the relevant input
     * @return null : if the user canceled the reset operation.
     *          Long[] {fromTime, untilTime} : if the user limited the reset to a range of dates.
     *          Long[] {-1} : if the user asked to reset all the interactions ever recorded.
     */
    public long[] showDialog() {
        this.APIClearParameters = null;
        this.setVisible(true);
        return APIClearParameters;
    }
}
