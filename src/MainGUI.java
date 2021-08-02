import javax.swing.*;
import java.io.File;

public class MainGUI extends JFrame {
    private JPanel mainPanel;
    private JPanel Content;
    private JButton wellButton;
    private JButton badButton;
    private JButton vagueButton;
    private JButton exposeAnswerButton;
    private JLabel questionDescription;
    private JLabel questionAnswer;
    private JLabel remainingCardLabel;
    private JLabel successRateLabel;
    private JScrollPane answerWrapper;
    final CardsManager mgr = new CardsManager();
    private CardsManager.Card currentCard;
    private JMenuBar topMenu;
    private JMenu fileMenu;
    private JMenuItem clearInteractionsMenuItem;
    private JMenuItem importCardsMenuItem;
    private JMenuItem settingsMenuItem;
    private JMenu editMenu;
    private JMenuItem addNewCardMenuItem;
    private JMenuItem editCurrentCardMenuItem;
    private JMenuItem removeCurrentCardMenuItem;
    private JMenuItem exportCardsMenuItem;
    private JMenuItem deleteAllCardsMenuItem;

    public MainGUI(String title){
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
        topMenu = new JMenuBar();
        this.setJMenuBar(topMenu);
        setupFileMenu();
        setupEditMenu();
        topMenu.add(fileMenu);
        topMenu.add(editMenu);
        setupMenuBarActionListeners();
    }

    private void setupFileMenu() {
        fileMenu = new JMenu("File");
        clearInteractionsMenuItem = new JMenuItem("Reset interactions history");
        fileMenu.add(clearInteractionsMenuItem);
        deleteAllCardsMenuItem = new JMenuItem("Permanently delete all cards");
        fileMenu.add(deleteAllCardsMenuItem);
        exportCardsMenuItem = new JMenuItem("Export cards to csv file.");
        fileMenu.add(exportCardsMenuItem);
        importCardsMenuItem = new JMenuItem("Import cards from csv file.");
        fileMenu.add(importCardsMenuItem);
        settingsMenuItem = new JMenuItem("Settings");
        fileMenu.add(settingsMenuItem);
    }

    private void setupEditMenu() {
        editMenu = new JMenu("Edit");
        addNewCardMenuItem = new JMenuItem("Add new card");
        editMenu.add(addNewCardMenuItem);
        editCurrentCardMenuItem = new JMenuItem("Edit current card");
        editMenu.add(editCurrentCardMenuItem);
        removeCurrentCardMenuItem = new JMenuItem("Remove current card");
        editMenu.add(removeCurrentCardMenuItem);
    }

    private void setupMenuBarActionListeners() {
        // File -> Reset interactions history
        clearInteractionsMenuItem.addActionListener(e -> {
            JDialog clearInteractionsFrame = new ClearInteractionsDialog(MainGUI.this);
            clearInteractionsFrame.setVisible(true);
        });

        // File -> Permanently delete all cards
        deleteAllCardsMenuItem.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(MainGUI.this,
                    "Are you sure you want to permanently delete all cards?",
                    "Confirmation Dialog",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_NO_OPTION) {
                MainGUI.this.mgr.deleteAllCards();
                MainGUI.this.mgr.queue.refreshQueue();
                MainGUI.this.showNextCard();
            }
        });

        // File -> Export cards to csv.
        exportCardsMenuItem.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(Utils.csvFileFilter);
            int returnVal = fc.showSaveDialog(MainGUI.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                if (!Utils.csvFileFilter.accept(file)) {
                    file = new File(file.toString() + ".csv");
                }
                if (MainGUI.this.mgr.exportCards(file)) {
                    JOptionPane.showMessageDialog(MainGUI.this,
                            "The cards were successfully exported to " +
                                    file.getPath() +
                                    ".",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(MainGUI.this,
                            "An error occur. See log for details.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // File -> Import cards from csv.
        importCardsMenuItem.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(Utils.csvFileFilter);
            int returnVal = fc.showDialog(MainGUI.this, "Import");
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                if (!MainGUI.this.mgr.importCards(file)) {
                    JOptionPane.showMessageDialog(MainGUI.this,
                            "An error occur. Please make sure to choose a comma-separated csv " +
                                    "file with exactly two columns.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(MainGUI.this,
                            "The cards were successfully imported.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    MainGUI.this.mgr.queue.refreshQueue();
                    MainGUI.this.showNextCard();
                }
            }
        });

        // File -> Settings
        settingsMenuItem.addActionListener(e -> {
            JDialog settingsFrame = new Settings("Settings", MainGUI.this);
            settingsFrame.setVisible(true);
        });

        // Edit -> Add new card.
        addNewCardMenuItem.addActionListener(e -> {
            String[] card = new CardEditDialog(MainGUI.this).showDialog();
            if (card != null) {
                mgr.insertNewCard(card[0], card[1]);
                mgr.queue.refreshQueue();
                this.showNextCard();
            }
        });

        // Edit -> Edit current card.
        editCurrentCardMenuItem.addActionListener(e -> {
            String[] card = new CardEditDialog(MainGUI.this, this.currentCard).showDialog();
            if (card != null) {
                this.currentCard.updateCard(card[0], card[1]);
                this.showCard(currentCard);
            }
        });

        // Edit -> Remove current card.
        removeCurrentCardMenuItem.addActionListener( e -> {
            int result = JOptionPane.showConfirmDialog(MainGUI.this,
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
        currentCard = mgr.queue.getNextCard();
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
            this.editCurrentCardMenuItem.setEnabled(false);
            this.removeCurrentCardMenuItem.setEnabled(false);
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
                    "<html><div style=\"width: 160px\">This card has %.1f%% success rate.</div></html>",
                    card.getSuccessRate() * 100));
            exposeDashboard();
            this.editCurrentCardMenuItem.setEnabled(true);
            this.removeCurrentCardMenuItem.setEnabled(true);
        }
    }
}
