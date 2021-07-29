import java.sql.*;

public class DBUtils {
    private static Connection con;
    private static boolean hasData = false;

    public ResultSet displayQuestions() throws SQLException, ClassNotFoundException {
        if (con == null) {
            getConnection();
        }
        Statement state = con.createStatement();
        ResultSet res = state.executeQuery("SELECT * FROM Questions");
        return res;
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
        ResultSet res = state.executeQuery("SELECT * FROM sqlite_master WHERE type = 'table' AND name = 'questions';");
        if (!res.next()) {
            System.out.println("Building the questions table with pre-populated values");
            Statement state2 = con.createStatement();
            state2.execute("CREATE TABLE questions(id integer," +
                    "question varchar(60), answer varchar(60)," +
                    "lastInteraction numeric, primary key(id));");
            state2 = con.createStatement();
            state2.execute("CREATE TABLE interactions(id integer," +
                    "questionID integer, interactionResult integer," +
                    "primary key(id));");
        }
    }
}
