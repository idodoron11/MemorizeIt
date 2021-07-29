import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CardGUI extends JFrame {
    private JPanel mainPanel;
    private JPanel Content;
    private JPanel Dashboard;
    private JButton wellButton;
    private JButton vagueButton;
    private JButton badButton;
    private JButton exposeAnswerButton;
    private JLabel questionDescription;
    private JLabel questionAnswer;

    public CardGUI(String title){
        super(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();
        this.hideAnswer();
        exposeAnswerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exposeAnswer();
            }
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

    public static void main(String[] args) {
        JFrame frame = new CardGUI("MemorizeIt");
        frame.setVisible(true);
        DBUtils db = new DBUtils();
        try {
            ResultSet rs = db.displayQuestions();
            while(rs.next()) {
                System.out.println(rs.getString("question"));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
