package vanillacoffeesystem;

import java.sql.Connection;
import java.sql.DriverManager;

// Opens a JDBC connection to the local MySQL database.
public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/vanilla_db";
    private static final String USER = "root";
    private static final String PASSWORD = "Rand@2004";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
