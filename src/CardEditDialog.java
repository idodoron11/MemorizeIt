import javax.swing.*;
import java.awt.*;

/**
 * This dialog is used both for inserting new cards and updating existing ones.
 */

public class CardEditDialog extends JDialog {
    private JTextField questionInput;
    private JTextArea answerInput;
    private JButton commitButton;
    private JButton cancelButton;
    private JPanel mainPanel;
    private String[] APIResult = null;

    private CardEditDialog(Frame owner, String title) {
        super(owner, title, true);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.commitButton.addActionListener(e -> {
            this.APIResult = getFormData();
            dispose();
        });
        this.cancelButton.addActionListener(e -> dispose());
        answerInput.setFont(questionInput.getFont());
        this.pack();
    }

    public CardEditDialog(Frame owner, CardsManager.Card card) {
        this(owner, String.format("Edit Card Number %d", card.getID()));
        commitButton.setText("Update Card");
        questionInput.setText(card.getQuestion());
        answerInput.setText(card.getAnswer());
    }

    public CardEditDialog(Frame owner) {
        this(owner, "Create New Card");
        commitButton.setText("Add Card");
    }

    /**
     * Opens a new CardEdit dialog instance and returns the relevant data from the user input.
     * @return String array of size 2. The first item is the question description, and the second item is the answer.
     */
    public String[] showDialog() {
        this.setVisible(true);
        return this.APIResult;
    }

    private String[] getFormData() {
        String[] result = new String[2];
        result[0] = this.questionInput.getText();
        result[1] = this.answerInput.getText();
        return result;
    }
}
