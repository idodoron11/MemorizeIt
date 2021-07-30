import java.sql.*;

public class CardsManager {
    private Connection con;
    private boolean hasData = false;
    ResultSet cardsQueue;

    public CardsManager() {
        refreshQueue();
    }

    private void getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        con = DriverManager.getConnection("jdbc:sqlite:data.db");
        initialise();
    }

    private void initialise() throws SQLException {
        if (!hasData) {
            hasData = true;
        }
        Statement state = con.createStatement();
        ResultSet res = state.executeQuery("SELECT * FROM sqlite_master WHERE type = 'table' AND name = 'cards';");
        if (!res.next()) {
            System.out.println("Building the questions table with pre-populated values");
            Statement state2 = con.createStatement();
            state2.execute("""
                CREATE TABLE "cards" (
                 	"id"	INTEGER NOT NULL UNIQUE,
                 	"question"	varchar(60) DEFAULT "" NOT NULL,
                 	"answer"	varchar(60) DEFAULT "" NOT NULL,
                 	"lastInteraction"	NUMERIC DEFAULT 0 NOT NULL,
                 	"successfulInteractions"	REAL DEFAULT 0 NOT NULL,
                 	"totalInteractions"	INTEGER DEFAULT 0 NOT NULL,
                 	PRIMARY KEY("id")
                 )
                """);
        }
    }

    public void refreshQueue() {
        try {
            if (con == null) {
                getConnection();
            }
            Statement state = con.createStatement();
            cardsQueue = state.executeQuery("""
                SELECT
                    cards.id as "id",
                    cards.question as "question",
                    cards.answer as "answer",
                    cards.lastInteraction as "lastInteraction",
                    cards.successfulInteractions as "successfulInteractions",
                    cards.totalInteractions as "totalInteractions",
                    cards.successfulInteractions / cards.totalInteractions as "successRate"
                FROM cards
                WHERE
                    lastInteraction < strftime('%s', 'now') - 60 * 60
                ORDER BY
                    successRate ASC,
                    lastInteraction DESC
                """);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Card getNextCard() {
        try {
            if (cardsQueue.next()) {
                return new Card(cardsQueue);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int insertNewCard(String question, String answer) {
        int id = -1;
        try {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO cards (question, answer) VALUES(?, ?);",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, question);
            ps.setString(2, answer);
            ps.execute();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    public class Card implements Comparable<Card> {
        private final int ID;
        private String question;
        private String answer;
        private final long lastInteraction;
        private final double successfulInteractions;
        private final int totalInteractions;

        private Card(int ID, String question, String answer, long lastInteraction, double successfulInteractions, int totalInteractions) {
            this.ID = ID;
            this.question = question;
            this.answer = answer;
            this.lastInteraction = lastInteraction;
            this.successfulInteractions = successfulInteractions;
            this.totalInteractions = totalInteractions;
        }

        public Card(ResultSet rs) throws SQLException {
            this(rs.getInt("id"),
                    rs.getString("question"),
                    rs.getString("answer"),
                    rs.getLong("lastInteraction"),
                    rs.getDouble("successfulInteractions"),
                    rs.getInt("totalInteractions"));
        }

        public double getSuccessRate() {
            return this.successfulInteractions / this.totalInteractions;
        }

        public String getQuestion() {
            return question;
        }

        public int getID() {
            return ID;
        }

        public String getAnswer() {
            return answer;
        }

        public void interact(double interactionResult) {
            try {
                if (con == null) {
                    getConnection();
                }
                PreparedStatement ps = con.prepareStatement("""
                    UPDATE cards
                    SET
                    	totalInteractions = totalInteractions + 1,
                    	successfulInteractions = successfulInteractions + ?,
                    	lastInteraction = strftime('%s', 'now')
                    WHERE id = ?
                    """);
                ps.setDouble(1, interactionResult);
                ps.setInt(2, this.getID());
                ps.execute();
                System.out.println("Interaction completed.");
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        public void updateCard(String question, String answer) {
            this.question = question;
            this.answer = answer;
            try {
                PreparedStatement ps = con.prepareStatement("""
                    UPDATE cards
                    SET
                    	question = ?,
                    	answer = ?
                    WHERE id = ?
                    """);
                ps.setString(1, this.question);
                ps.setString(2, this.answer);
                ps.setInt(3, this.ID);
                ps.executeUpdate();
                System.out.println("Update completed.");
            } catch (SQLException e) {
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

}
