import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
    final CardsManager queue = new CardsManager();
    private CardsManager.Card currentCard;

    public CardGUI(String title){
        super(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();
        loadMenuBar();
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
                currentCard.interact(0);
            }
            updateQuestion();
        });
    }

    private void loadMenuBar() {
        JMenuBar topMenu = new JMenuBar();
        this.setJMenuBar(topMenu);

        // Create file sub menu
        JMenu fileMenu = new JMenu("FIle");
        JMenuItem addNewCardMenuItem = new JMenuItem("Add new card");
        fileMenu.add(addNewCardMenuItem);
        JMenuItem removeCurrentCardMenuItem = new JMenuItem("Remove current card");
        fileMenu.add(removeCurrentCardMenuItem);
        JMenuItem settingsMenuItem = new JMenuItem("Settings");
        fileMenu.add(settingsMenuItem);
        settingsMenuItem.addActionListener(e -> {
            JDialog settingsFrame = new Settings("Settings", CardGUI.this);
            settingsFrame.setVisible(true);
        });

        // Add sub-menus to top menu
        topMenu.add(fileMenu);
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
            this.questionDescription.setText("<html><div style=\"width: 300px;\"><h2>There are no more questions for now. Please come back later.</h2></div></html>");
            this.questionAnswer.setText("");
            this.questionAnswer.setVisible(false);
            this.wellButton.setVisible(false);
            this.badButton.setVisible(false);
            this.vagueButton.setVisible(false);
            this.exposeAnswerButton.setVisible(false);
        } else {
            this.questionDescription.setText("<html><div style=\"width: 300px;\"><h1>"+currentCard.getQuestion()+"</h1></div></html>");
            this.questionAnswer.setText("<html><div style=\"width: 300px;\">"+currentCard.getAnswer()+"</div></html>");
            this.questionAnswer.setVisible(false);
            this.exposeAnswerButton.setVisible(true);
            this.wellButton.setVisible(true);
            this.badButton.setVisible(true);
            this.vagueButton.setVisible(true);
        }
    }
}
