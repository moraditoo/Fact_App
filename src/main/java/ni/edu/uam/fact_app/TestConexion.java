package ni.edu.uam.fact_app;

import ni.edu.uam.fact_app.util.DatabaseConnection;
import java.sql.Connection;

public class TestConexion {
    public static void main(String[] args) {
        try (Connection con = DatabaseConnection.getConnection()) {
            if (con != null) {
                System.out.println("-------------------------");
                System.out.println("  Conexión exitosa       ");
                System.out.println("-------------------------");
            }
        } catch (Exception e) {
            System.err.println("Error al conectar con la base de datos:");
            e.printStackTrace();
        }
    }
}