import java.util.Date;

public class Question implements Comparable<Question> {
    private final int ID;
    private String question;
    private String answer;
    private long lastInteraction;
    private double successRate;


    public Question(int ID) {
        this.ID = ID;
        // TODO: Go to database and load the values of the rest of the variables.
        this.question = "";
        this.answer = "";
        this.lastInteraction = 0;
        this.successRate = 0;
    }

    public Question(String question, String answer) {
        this.question = question;
        this.answer = answer;
        this.lastInteraction = 0;
        this.successRate = 0;
        // TODO: Add this new question to database, and then update its new ID.
        this.ID = 0;
    }

    public int getID() {
        return ID;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setQuestion(String question) {
        this.question = question;
        // TODO: Update question in the database.
    }

    public void setAnswer(String answer) {
        this.answer = answer;
        // TODO: Update answer in the database.
    }

    @Override
    public int compareTo(Question o) {
        if (this.successRate < o.successRate) {
            return -1;
        } else if (this.successRate > o.successRate) {
            return 1;
        } else {
            if (this.lastInteraction < o.lastInteraction) {
                return -1;
            } else if (this.lastInteraction == o.lastInteraction) {
                return 0;
            } else {
                return 1;
            }
        }
    }
}
