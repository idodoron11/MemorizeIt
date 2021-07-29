import java.sql.*;

public class DBUtils {
    private static Connection con;
    private static boolean hasData = false;

    public static ResultSet displayQuestions() throws SQLException, ClassNotFoundException {
        return getData("SELECT * FROM Questions");
    }

    public static ResultSet getData(String query)  throws SQLException, ClassNotFoundException {
        if (con == null) {
            getConnection();
        }
        Statement state = con.createStatement();
        return state.executeQuery(query);
    }

    public static void executeUpdate(String query) throws SQLException, ClassNotFoundException {
        if (con == null) {
            getConnection();
        }

        Statement state2 = con.createStatement();
        state2.execute(query);
    }

    private static void getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        con = DriverManager.getConnection("jdbc:sqlite:data.db");
        initialise();
    }

    private static void initialise() throws SQLException {
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
}
