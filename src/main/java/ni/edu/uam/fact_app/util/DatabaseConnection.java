package ni.edu.uam.fact_app.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:postgresql://localhost:5432/tienda_javafx";
    private static final String USER = "postgres";
    private static final String PASSWORD = "1234"; // Tu contraseña de PostgreSQL

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}