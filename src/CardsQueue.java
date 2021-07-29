import java.sql.ResultSet;
import java.sql.SQLException;

public class CardsQueue {
    final static String nextCardQuery = """
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
            """;
    ResultSet rs;

    {
        try {
            rs = DBUtils.getData(nextCardQuery);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Card getNextCard() {
        try {
            if (rs.next()) {
                return new Card(rs.getInt("id"),
                        rs.getString("question"),
                        rs.getString("answer"),
                        rs.getLong("lastInteraction"),
                        rs.getDouble("successfulInteractions"),
                        rs.getInt("totalInteractions"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
