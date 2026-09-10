package ni.edu.uam.fact_app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ni.edu.uam.fact_app.model.Cargo;
import ni.edu.uam.fact_app.model.Empleado;
import ni.edu.uam.fact_app.model.Usuario;
import ni.edu.uam.fact_app.util.SceneManager;
import ni.edu.uam.fact_app.util.SesionUsuario;

import java.io.IOException;
import java.time.LocalDate;

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    private static final ObservableList<Usuario> usuarios = FXCollections.observableArrayList();

    static {
        Cargo admin = new Cargo(1, "Administrador", "Acceso total");
        Cargo cajero = new Cargo(2, "Cajero", "Ventas");
        Cargo bodeguero = new Cargo(3, "Bodeguero", "Inventario");

        // Sin el booleano 'true' al final:
        Empleado emp1 = new Empleado(1, "Carlos", "Perez", admin, LocalDate.now());
        Empleado emp2 = new Empleado(2, "Maria", "Lopez", cajero, LocalDate.now());
        Empleado emp3 = new Empleado(3, "Juan", "Gomez", bodeguero, LocalDate.now());

        // Cuentas de acceso demo
        usuarios.add(new Usuario("admin", "admin123", emp1));
        usuarios.add(new Usuario("cajero", "cajero123", emp2));
        usuarios.add(new Usuario("bodega", "bodega123", emp3));
    }

    @FXML
    private void iniciarSesion() {
        String user = txtUsuario.getText().trim();
        String pass = txtPassword.getText().trim();

        if (user.isBlank() || pass.isBlank()) {
            lblError.setText("Ingrese usuario y contraseña.");
            lblError.setVisible(true);
            return;
        }

        Usuario encontrado = null;
        for (Usuario u : usuarios) {
            if (u.getUsername().equals(user) && u.getPassword().equals(pass)) {
                encontrado = u;
                break;
            }
        }

        if (encontrado == null) {
            lblError.setText("Credenciales inválidas.");
            lblError.setVisible(true);
            return;
        }

        SesionUsuario.setUsuarioActual(encontrado);
        try {
            SceneManager.cambiarEscena("/ni/edu/uam/fact_app/fxml/menu-principal.fxml", "Sistema de Facturación - Dashboard");
        } catch (IOException e) {
            e.printStackTrace();
            lblError.setText("Error al abrir el menú principal.");
            lblError.setVisible(true);
        }
    }
}