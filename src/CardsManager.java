import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Set;

public class CardsManager {
    private Connection con;
    private boolean hasData = false;
    ResultSet cardsQueue;

    public CardsManager() {
        refreshQueue();
    }

    private void openConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        con = DriverManager.getConnection("jdbc:sqlite:data.db");
        initialise();
    }

    public void closeConnection() throws SQLException {
        if (con != null) {
            con.close();
            con = null;
        }
    }

    private void initialise() throws SQLException {
        if (!hasData) {
            hasData = true;
            Statement state = con.createStatement();
            ResultSet res = state.executeQuery("SELECT * FROM sqlite_master WHERE type = 'table' AND name = 'cards';");
            if (!res.next()) {
                System.out.println("Creating cards table.");
                Statement state2 = con.createStatement();
                state2.execute("""
                CREATE TABLE "cards" (
                	"id"	INTEGER NOT NULL UNIQUE,
                	"question"	varchar(60) NOT NULL,
                	"answer"	varchar(60) NOT NULL,
                	PRIMARY KEY("id")
                );
                """);
            }

            res = state.executeQuery("SELECT * FROM sqlite_master WHERE type = 'table' AND name = 'interactions';");
            if (!res.next()) {
                System.out.println("Creating interactions table.");
                Statement state2 = con.createStatement();
                state2.execute("""
                CREATE TABLE "interactions" (
                	"id"	INTEGER NOT NULL UNIQUE,
                	"cardID"	INTEGER NOT NULL,
                	"result"	NUMERIC NOT NULL DEFAULT 0,
                	"time"	NUMERIC NOT NULL DEFAULT 0,
                	PRIMARY KEY("id")
                );
                """);
            }
        }
    }

    /***
     * Dispose the current queue, and re-query db to create a new queue.
     * @return the number of cards in the new queue.
     */
    public void refreshQueue() {
        try {
            if (con == null) {
                openConnection();
            }
            PreparedStatement ps = con.prepareStatement("""
                        SELECT
                        	c.id as id,
                        	c.question as question,
                        	c.answer as answer,
                        	i.tmpLastInteraction as lastInteraction,
                        	i.tmpSuccessfulInteractions as successfulInteractions,
                        	i.tmpTotalInteractions as totalInteractions,
                        	i.tmpSuccessRate as successRate
                        FROM cards c
                        LEFT JOIN (
                        	SELECT
                        		tmp.cardID as tmpCardID,
                        		tmp.time as tmpLastInteraction,
                        		COALESCE(SUM(tmp.result), 0) tmpSuccessfulInteractions,
                        		COALESCE(COUNT(tmp.result), 0) tmpTotalInteractions,
                        		COALESCE(AVG(tmp.result), 0) tmpSuccessRate
                        	FROM (
                        		SELECT *, ROW_NUMBER()
                        		  OVER (PARTITION BY interactions.cardID
                        		  ORDER BY interactions.time DESC) rn
                        		  FROM interactions
                        	) as tmp
                        	WHERE tmp.rn <= ? OR 0 = ?
                        	GROUP BY tmpCardID
                        ) as i ON i.tmpCardID = c.id
                        WHERE (
                        	COALESCE(i.tmpLastInteraction, 0) < strftime('%s', 'now') - 60 * 60 * ? AND
                        	COALESCE(i.tmpSuccessRate, 0) <= 0.01 * ?
                        ) OR (
                        	COALESCE(i.tmpLastInteraction, 0) < strftime('%s', 'now') - 24 * 60 * 60 * ?
                        )
                        ORDER BY
                        	i.tmpSuccessRate ASC,
                        	i.tmpLastInteraction ASC
                        """);
            ps.setInt(1, Integer.parseInt(Settings.config.getProperty("interactionsFocus")));
            ps.setInt(2, Integer.parseInt(Settings.config.getProperty("interactionsFocus")));
            ps.setInt(3, Integer.parseInt(Settings.config.getProperty("waitAfterInteraction")));
            ps.setDouble(4, Double.parseDouble(Settings.config.getProperty("successRateThreshold")));
            ps.setInt(5, Integer.parseInt(Settings.config.getProperty("maxWaitAfterInteraction")));
            cardsQueue = ps.executeQuery();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Card getNextCard() {
        try {
            if (cardsQueue.next()) {
                Card result = new Card(cardsQueue);
                System.out.println(result);
                return result;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int insertNewCard(String question, String answer) {
        int id = -1;
        try {
            if (con == null) {
                openConnection();
            }
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
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return id;
    }

    public boolean importCards(File file) {
        try (BufferedReader bf = new BufferedReader(new FileReader(file))) {
            if (con == null) {
                openConnection();
            }

            String line;
            String[] values;

            con.setAutoCommit(false);
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO cards (question, answer) VALUES(?, ?);",
                    Statement.RETURN_GENERATED_KEYS);
            while ((line = bf.readLine()) != null) {
                values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                if (values.length != 2) {
                    throw new IOException("The structure of the csv file is invalid.");
                }
                if (values[0].charAt(0) == '"' && values[0].charAt(values[0].length()-1) == '"') {
                    values[0] = values[0].substring(1, values[0].length() - 1);
                }
                if (values[1].charAt(0) == '"' && values[1].charAt(values[1].length()-1) == '"') {
                    values[1] = values[1].substring(1, values[1].length() - 1);
                }
                ps.setString(1, values[0]);
                ps.setString(2, values[1]);
                ps.execute();
            }
            con.commit();
            con.setAutoCommit(true);
        } catch (IOException | SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            try {
                con.rollback();
            } catch (SQLException throwable) {
                throwable.printStackTrace();
            }
            return false;
        }

        return true;
    }

    public void resetInteractions(long from, long until) {
        try {
            if (con == null) {
                openConnection();
            }
            PreparedStatement ps = con.prepareStatement("""
                    DELETE FROM interactions
                    WHERE
                        interactions.time <= ? AND
                        interactions.time >= ?
                    """);
            ps.setLong(1, until);
            ps.setLong(2, from);
            ps.execute();
            System.out.println("Reset completed.");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void resetAllInteractions() {
        try {
            if (con == null) {
                openConnection();
            }
            PreparedStatement ps = con.prepareStatement("DELETE FROM interactions");
            ps.execute();
            System.out.println("Reset completed.");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public class Card implements Comparable<Card> {
        private final int ID;
        private String question;
        private String answer;
        private final long lastInteraction;
        private double successfulInteractions;
        private int totalInteractions;
        private int cardIndex;

        private Card(int ID, String question, String answer, long lastInteraction, double successfulInteractions,
                     int totalInteractions, int cardIndex) {
            this.ID = ID;
            this.question = question;
            this.answer = answer;
            this.lastInteraction = lastInteraction;
            this.successfulInteractions = successfulInteractions;
            this.totalInteractions = totalInteractions;
            this.cardIndex = cardIndex;
        }

        public Card(ResultSet rs) throws SQLException {
            this(rs.getInt("id"),
                    rs.getString("question"),
                    rs.getString("answer"),
                    rs.getLong("lastInteraction"),
                    rs.getDouble("successfulInteractions"),
                    rs.getInt("totalInteractions"),
                    rs.getRow());
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

        public int getCardIndex() {
            return cardIndex;
        }

        public long getLastInteraction() {
            return lastInteraction;
        }

        public double getSuccessfulInteractions() {
            return successfulInteractions;
        }

        public int getTotalInteractions() {
            return totalInteractions;
        }

        public String getAnswer() {
            return answer;
        }

        public void interact(double interactionResult) {
            try {
                if (con == null) {
                    openConnection();
                }
                PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO interactions (cardID, result, time) VALUES(?, ?, strftime('%s', 'now'));
                    """);
                ps.setInt(1, this.getID());
                ps.setDouble(2, interactionResult);
                ps.execute();
                ++this.totalInteractions;
                this.successfulInteractions += interactionResult;
                System.out.println("Interaction completed.");
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        public void updateCard(String question, String answer) {
            this.question = question;
            this.answer = answer;
            try {
                if (con == null) {
                    openConnection();
                }
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
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        public void deleteCard() {
            try {
                if (con == null) {
                    openConnection();
                }
                con.setAutoCommit(false);
                PreparedStatement ps1 = con.prepareStatement("DELETE FROM cards WHERE cards.id = ?");
                ps1.setInt(1, this.ID);
                ps1.executeUpdate();
                PreparedStatement ps2 = con.prepareStatement("DELETE FROM interactions WHERE interactions.cardID = ?");
                ps2.setInt(1, this.ID);
                ps2.executeUpdate();
                con.commit();
                con.setAutoCommit(true);
                System.out.println("Update completed.");
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

        @Override
        public String toString() {
            return "Card{" +
                    "ID=" + ID +
                    ", question='" + question + '\'' +
                    ", answer='" + answer + '\'' +
                    ", lastInteraction=" + lastInteraction +
                    ", successfulInteractions=" + successfulInteractions +
                    ", totalInteractions=" + totalInteractions +
                    ", cardIndex=" + cardIndex +
                    '}';
        }
    }

}
