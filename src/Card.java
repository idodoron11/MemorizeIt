import java.sql.SQLException;
import java.sql.Statement;

public class Card implements Comparable<Card> {
    private final int ID;
    private String question;
    private String answer;
    private long lastInteraction;
    private double successfulInteractions;
    private int totalInteractions;

    public Card(int ID, String question, String answer, long lastInteraction, double successfulInteractions, int totalInteractions) {
        this.ID = ID;
        this.question = question;
        this.answer = answer;
        this.lastInteraction = lastInteraction;
        this.successfulInteractions = successfulInteractions;
        this.totalInteractions = totalInteractions;
    }

    public double getSuccessRate() {
        return this.successfulInteractions / this.totalInteractions;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public void interact() {
        interact(0);
    }

    public void interact(double interactionResult) {
        try {
            DBUtils.executeUpdate(String.format("""
                    UPDATE cards
                    SET
                    	totalInteractions = totalInteractions + 1,
                    	successfulInteractions = successfulInteractions + %f,
                    	lastInteraction = strftime('%s', 'now')
                    WHERE id = %d
                    """, interactionResult, "%s", ID));
            System.out.println("Interaction completed.");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int compareTo(Card o) {
        if (this.getSuccessRate() < o.getSuccessRate()) {
            return -1;
        } else if (this.getSuccessRate() > o.getSuccessRate()) {
            return 1;
        } else {
            return Long.compare(this.lastInteraction, o.lastInteraction);
        }
    }
}
