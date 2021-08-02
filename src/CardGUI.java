import javax.swing.*;

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
    private JLabel remainingCardLabel;
    private JLabel successRateLabel;
    private JScrollPane answerWrapper;
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
        showNextCard();
        exposeAnswerButton.addActionListener(e -> exposeAnswer());
        wellButton.addActionListener(e -> {
            if (currentCard != null) {
                currentCard.interact(1);
            }
            showNextCard();
        });
        vagueButton.addActionListener(e -> {
            if (currentCard != null) {
                currentCard.interact(0.5);
            }
            showNextCard();
        });
        badButton.addActionListener(e -> {
            if (currentCard != null) {
                currentCard.interact(0);
            }
            showNextCard();
        });
    }

    private void loadMenuBar() {
        JMenuBar topMenu = new JMenuBar();
        this.setJMenuBar(topMenu);

        // Create file sub-menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem clearInteractionsMenuItem = new JMenuItem("Reset interactions history");
        fileMenu.add(clearInteractionsMenuItem);
        JMenuItem settingsMenuItem = new JMenuItem("Settings");
        fileMenu.add(settingsMenuItem);

        // Create edit sub-menu
        JMenu editMenu = new JMenu("Edit");
        JMenuItem addNewCardMenuItem = new JMenuItem("Add new card");
        editMenu.add(addNewCardMenuItem);
        JMenuItem editCurrentCard = new JMenuItem("Edit current card");
        editMenu.add(editCurrentCard);
        JMenuItem removeCurrentCardMenuItem = new JMenuItem("Remove current card");
        editMenu.add(removeCurrentCardMenuItem);

        // Add sub-menus to top menu
        topMenu.add(fileMenu);
        topMenu.add(editMenu);

        // Define actionListeners for every menu-item
        clearInteractionsMenuItem.addActionListener(e -> {
            JDialog clearInteractionsFrame = new ClearInteractionsDialog(CardGUI.this);
            clearInteractionsFrame.setVisible(true);
        });
        settingsMenuItem.addActionListener(e -> {
            JDialog settingsFrame = new Settings("Settings", CardGUI.this);
            settingsFrame.setVisible(true);
        });
        addNewCardMenuItem.addActionListener(e -> {
            String[] card = new CardEditDialog(CardGUI.this).showDialog();
            if (card != null) {
                queue.insertNewCard(card[0], card[1]);
                queue.refreshQueue();
                this.showNextCard();
            }
        });
        editCurrentCard.addActionListener(e -> {
            String[] card = new CardEditDialog(CardGUI.this, this.currentCard).showDialog();
            if (card != null) {
                this.currentCard.updateCard(card[0], card[1]);
                this.showCard(currentCard);
            }
        });
        removeCurrentCardMenuItem.addActionListener( e -> {
            int result = JOptionPane.showConfirmDialog(CardGUI.this,
                    "Are you sure you want to delete the current card?",
                    "Confirmation Dialog",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_NO_OPTION) {
                this.currentCard.deleteCard();
                this.showNextCard();
            }
        });
    }

    public void exposeAnswer() {
        this.exposeAnswerButton.setVisible(false);
        this.answerWrapper.setVisible(true);
        this.questionAnswer.setVisible(true);
    }

    public void hideAnswer() {
        this.exposeAnswerButton.setVisible(true);
        this.answerWrapper.setVisible(false);
        this.questionAnswer.setVisible(false);
    }

    public void exposeDashboard() {
        this.wellButton.setVisible(true);
        this.badButton.setVisible(true);
        this.vagueButton.setVisible(true);
    }

    public void hideDashboard() {
        this.wellButton.setVisible(false);
        this.badButton.setVisible(false);
        this.vagueButton.setVisible(false);
    }

    public void showNextCard() {
        currentCard = queue.getNextCard();
        showCard(currentCard);
    }

    public void showCard(CardsManager.Card card) {
        hideAnswer();
        if (card == null) {
            this.questionDescription.setText("<html><div style=\"width: 300px;\"><h2>There are no more questions for now. Please come back later.</h2></div></html>");
            this.questionAnswer.setText("");
            this.successRateLabel.setText("");
            this.remainingCardLabel.setText("");
            hideDashboard();
            this.exposeAnswerButton.setVisible(false);
        } else {
            this.questionDescription.setText("<html><div style=\"width: 300px;\"><h1>"+card.getQuestion()+"</h1></div></html>");
            this.questionAnswer.setText("<html><div style=\"width: 300px;\">"+card.getAnswer()
                    .replace("\n", "<br>")
                    .replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;")+"</div></html>");
            this.remainingCardLabel.setText(String.format(
                    "<html><div style=\"width: 160px\">%d card%s been shown in this session.</div></html>",
                    card.getCardIndex(),
                    (card.getCardIndex() == 1) ? " has" : "s have"));
            this.successRateLabel.setText(String.format(
                    "<html><div style=\"width: 160px\">This card has %.2f success rate.</div></html>",
                    card.getSuccessRate()));
            exposeDashboard();
        }
    }
}
