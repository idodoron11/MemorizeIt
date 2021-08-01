import javax.swing.*;
import java.util.Date;

public class ClearInteractionsDialog extends JDialog {
    private JSpinner fromInput;
    private JSpinner untilInput;
    private JButton removeButton;
    private JButton resetALLInteractionsButton;
    private JPanel mainPanel;
    private JButton cancelButton;

    public ClearInteractionsDialog(CardGUI owner) {
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
                CardGUI parent = (CardGUI) ClearInteractionsDialog.this.getParent();
                parent.queue.resetAllInteractions();
                ClearInteractionsDialog.super.dispose();
                parent.queue.refreshQueue();
                parent.showNextCard();
            }
        });
        removeButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(ClearInteractionsDialog.this,
                    "Are you sure you want to reset all interactions in the specified period?",
                    "Confirmation Dialog",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_NO_OPTION) {
                CardGUI parent = (CardGUI) ClearInteractionsDialog.this.getParent();
                Date fromDate = (Date) fromInput.getValue();
                Date untilDate = (Date) untilInput.getValue();
                parent.queue.resetInteractions(fromDate.getTime() / 1000, untilDate.getTime() / 1000);
                ClearInteractionsDialog.super.dispose();
                parent.queue.refreshQueue();
                parent.showNextCard();
            }
        });
    }
}