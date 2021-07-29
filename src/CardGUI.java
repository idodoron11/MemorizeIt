import javax.swing.*;
import java.awt.event.*;

public class CardGUI extends JFrame {
    private JPanel mainPanel;
    private JPanel Content;
    private JPanel Dashboard;
    private JButton wellButton;
    private JButton badButton;
    private JButton vagueButton;
    private JButton exposeAnswerButton;
    private JLabel questionDescription;
    private JLabel questionAnswer;
    private CardsQueue queue = new CardsQueue();
    private Card currentCard;

    public CardGUI(String title){
        super(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();
        this.hideAnswer();
        this.currentCard = null;
        updateQuestion();
        exposeAnswerButton.addActionListener(e -> exposeAnswer());
        wellButton.addActionListener(e -> {
            if (currentCard != null) {
                currentCard.interact(1);
            }
            updateQuestion();
        });
        vagueButton.addActionListener(e -> {
            if (currentCard != null) {
                currentCard.interact(0.5);
            }
            updateQuestion();
        });
        badButton.addActionListener(e -> {
            if (currentCard != null) {
                currentCard.interact();
            }
            updateQuestion();
        });
    }

    public void exposeAnswer() {
        this.exposeAnswerButton.setVisible(false);
        this.questionAnswer.setVisible(true);
    }

    public void hideAnswer() {
        this.exposeAnswerButton.setVisible(true);
        this.questionAnswer.setVisible(false);
    }

    public void updateQuestion() {
        currentCard = queue.getNextCard();
        if (currentCard == null) {
            this.questionDescription.setText("There are no more questions for now. Please come back later.");
            this.questionAnswer.setText("");
            this.questionAnswer.setVisible(false);
            this.wellButton.setVisible(false);
            this.badButton.setVisible(false);
            this.vagueButton.setVisible(false);
            this.exposeAnswerButton.setVisible(false);
        } else {
            this.questionDescription.setText(currentCard.getQuestion());
            this.questionAnswer.setText(currentCard.getAnswer());
            this.questionAnswer.setVisible(false);
            this.exposeAnswerButton.setVisible(true);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new CardGUI("MemorizeIt");
        frame.setVisible(true);
    }
}
