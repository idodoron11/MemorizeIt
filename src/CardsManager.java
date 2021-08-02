import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.sql.*;

public class CardsManager {
    private Connection con;
    private boolean hasData = false;
    ResultSet cardsQueue;

    public CardsManager() {
        refreshQueue();
    }

    private void openConnection() throws ClassNotFoundException, SQLException {
        if (con == null) {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:data.db");
            initialise();
        }
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
            con.setAutoCommit(false);
            Statement state = con.createStatement();
            System.out.println("Creating cards table.");
            Statement state2 = con.createStatement();
            state2.execute("""
                CREATE TABLE IF NOT EXISTS "cards" (
                	"id"	INTEGER NOT NULL UNIQUE,
                	"question"	varchar(60) NOT NULL,
                	"answer"	varchar(60) NOT NULL,
                	PRIMARY KEY("id")
                );
                """);

            System.out.println("Creating interactions table.");
            state2 = con.createStatement();
            state2.execute("""
                CREATE TABLE IF NOT EXISTS "interactions" (
                	"id"	INTEGER NOT NULL UNIQUE,
                	"cardID"	INTEGER NOT NULL,
                	"result"	NUMERIC NOT NULL DEFAULT 0,
                	"time"	NUMERIC NOT NULL DEFAULT 0,
                	PRIMARY KEY("id")
                );
                """);
            con.commit();
            con.setAutoCommit(true);
        }
    }

    /***
     * Dispose the current queue, and re-query db to create a new queue.
     * @return the number of cards in the new queue.
     */
    public void refreshQueue() {
        try {
            openConnection();
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
            openConnection();
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
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            openConnection();
            CSVReader reader = new CSVReader(br);
            String[] line;

            con.setAutoCommit(false);
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO cards (question, answer) VALUES(?, ?);",
                    Statement.RETURN_GENERATED_KEYS);
            while ((line = reader.readNext()) != null) {
                if (line.length != 2) {
                    throw new IOException("The structure of the csv file is invalid.");
                }
                ps.setString(1, line[0]);
                ps.setString(2, line[1]);
                ps.execute();
            }
            reader.close();
            con.commit();
            con.setAutoCommit(true);
        } catch (IOException | SQLException | ClassNotFoundException | CsvValidationException e) {
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

    public boolean exportCards(File target) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(target, false))) {
            openConnection();
            CSVWriter writer = new CSVWriter(bw);
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery("SELECT question, answer FROM cards");
            while (rs.next()) {
                writer.writeNext(new String[] {
                        rs.getString("question"),
                        rs.getString("answer")
                });
            }
            rs.close();
            statement.close();
            writer.close();
        } catch (IOException | SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void resetInteractions(long from, long until) {
        try {
            openConnection();
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

    public void deleteAllCards() {
        try {
            openConnection();
            con.setAutoCommit(false);
            Statement state = con.createStatement();
            state.executeUpdate("DELETE FROM cards");
            state = con.createStatement();
            state.executeUpdate("DELETE FROM interactions");
            con.commit();
            con.setAutoCommit(true);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void resetAllInteractions() {
        try {
            openConnection();
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
                openConnection();
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
                openConnection();
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
                openConnection();
                con.setAutoCommit(false);
                PreparedStatement ps1 = con.prepareStatement("DELETE FROM cards WHERE cards.id = ?");
                ps1.setInt(1, this.ID);
                ps1.executeUpdate();
                PreparedStatement ps2 = con.prepareStatement("DELETE FROM interactions WHERE interactions.cardID = ?");
                ps2.setInt(1, this.ID);
                ps2.executeUpdate();
                con.commit();
                con.setAutoCommit(true);
                System.out.println("Card removed successfully.");
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
