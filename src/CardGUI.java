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
        clearInteractionsMenuItem.addActionListener(e -> {
            JDialog clearInteractionsFrame = new ClearInteractionsDialog(CardGUI.this);
            clearInteractionsFrame.setVisible(true);
        });
        JMenuItem settingsMenuItem = new JMenuItem("Settings");
        fileMenu.add(settingsMenuItem);
        settingsMenuItem.addActionListener(e -> {
            JDialog settingsFrame = new Settings("Settings", CardGUI.this);
            settingsFrame.setVisible(true);
        });

        // Create edit sub-menu
        JMenu editMenu = new JMenu("Edit");
        JMenuItem addNewCardMenuItem = new JMenuItem("Add new card");
        editMenu.add(addNewCardMenuItem);
        addNewCardMenuItem.addActionListener(e -> {
            String[] card = new CardEditDialog(CardGUI.this).showDialog();
            if (card != null) {
                queue.insertNewCard(card[0], card[1]);
                queue.refreshQueue();
                this.showNextCard();
            }
        });
        JMenuItem editCurrentCard = new JMenuItem("Edit current card");
        editMenu.add(editCurrentCard);
        editCurrentCard.addActionListener(e -> {
            String[] card = new CardEditDialog(CardGUI.this, this.currentCard).showDialog();
            if (card != null) {
                this.currentCard.updateCard(card[0], card[1]);
                this.showCard(currentCard);
            }
        });
        JMenuItem removeCurrentCardMenuItem = new JMenuItem("Remove current card");
        editMenu.add(removeCurrentCardMenuItem);
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

        // Add sub-menus to top menu
        topMenu.add(fileMenu);
        topMenu.add(editMenu);
    }

    public void exposeAnswer() {
        this.exposeAnswerButton.setVisible(false);
        this.questionAnswer.setVisible(true);
    }

    public void hideAnswer() {
        this.exposeAnswerButton.setVisible(true);
        this.questionAnswer.setVisible(false);
    }

    public void showNextCard() {
        currentCard = queue.getNextCard();
        showCard(currentCard);
    }

    public void showCard(CardsManager.Card card) {
        if (card == null) {
            this.questionDescription.setText("<html><div style=\"width: 300px;\"><h2>There are no more questions for now. Please come back later.</h2></div></html>");
            this.questionAnswer.setText("");
            this.questionAnswer.setVisible(false);
            this.wellButton.setVisible(false);
            this.badButton.setVisible(false);
            this.vagueButton.setVisible(false);
            this.exposeAnswerButton.setVisible(false);
        } else {
            this.questionDescription.setText("<html><div style=\"width: 300px;\"><h1>"+card.getQuestion()+"</h1></div></html>");
            this.questionAnswer.setText("<html><div style=\"width: 300px;\">"+card.getAnswer()+"</div></html>");
            this.questionAnswer.setVisible(false);
            this.exposeAnswerButton.setVisible(true);
            this.wellButton.setVisible(true);
            this.badButton.setVisible(true);
            this.vagueButton.setVisible(true);
        }
    }
}
