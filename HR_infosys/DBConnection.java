import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // 1. Connection Parameters
    private static final String URL = "jdbc:postgresql://localhost:5432/Human_Resources_FP";
    private static final String USER = "postgres";
    private static final String PASSWORD = "admin123"; 

    // 2. Method to get connection
    public static Connection connect() {
        Connection conn = null;
        try {
            // Load Driver
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.out.println("SQL Connection Error: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Driver Error: " + e.getMessage());
        }
        return conn;
    }
}
