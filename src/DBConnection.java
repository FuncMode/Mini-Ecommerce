// import java.sql.Connection;
// import java.sql.DriverManager;
// import java.sql.SQLException;
// import java.io.FileInputStream;
// import java.io.IOException;
// import java.util.Properties;

// public class DBConnection {

//     private static String URL;
//     private static String USER;
//     private static String PASSWORD;

//     static {
//         try {
//             Properties env = new Properties();
//             env.load(new FileInputStream(".env"));

//             URL = env.getProperty("DB_URL");
//             USER = env.getProperty("DB_USER");
//             PASSWORD = env.getProperty("DB_PASSWORD");
//         } catch (IOException e) {
//             System.out.println(" Could not load .env file: " + e.getMessage());
//         }
//     }

//     public static Connection getConnection() {
//         Connection connection = null;
//         try {
            
//             Class.forName("com.mysql.cj.jdbc.Driver");

//             connection = DriverManager.getConnection(URL, USER, PASSWORD);
//         } catch (ClassNotFoundException e) {
//             System.out.println(" JDBC Driver not found: " + e.getMessage());
//         } catch (SQLException e) {
//             System.out.println(" Connection failed: " + e.getMessage());
//         }
//         return connection;
//     }

//     public static void main(String[] args) {
//         // test
//         getConnection();
//     }
// }

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DBConnection {

    private static String URL;
    private static String USER;
    private static String PASSWORD;

    static {
        URL = System.getenv("DB_URL");
        USER = System.getenv("DB_USER");
        PASSWORD = System.getenv("DB_PASS");

        if (URL == null || USER == null || PASSWORD == null) {
            try (FileInputStream fis = new FileInputStream(".env")) {
                Properties env = new Properties();
                env.load(fis);

                URL = env.getProperty("DB_URL");
                USER = env.getProperty("DB_USER");
                PASSWORD = env.getProperty("DB_PASS", env.getProperty("DB_PASSWORD"));

                System.out.println(" Loaded DB credentials from .env (local mode)");
            } catch (IOException e) {
                System.out.println(" Could not load .env file: " + e.getMessage());
            }
        } else {
            System.out.println(" Loaded DB credentials from environment (Render/Docker)");
        }
    }

    public static Connection getConnection() {
        Connection connection = null;
        try {
            // ✅ Load JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // ✅ Try to connect
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println(" Database connected successfully!");
        } catch (ClassNotFoundException e) {
            System.out.println(" JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println(" Connection failed: " + e.getMessage());
        }
        return connection;
    }

    public static void main(String[] args) {
        // Simple test
        getConnection();
    }
}
