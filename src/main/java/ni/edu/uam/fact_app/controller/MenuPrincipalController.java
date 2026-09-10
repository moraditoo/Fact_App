package ni.edu.uam.fact_app.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ni.edu.uam.fact_app.model.Usuario;
import ni.edu.uam.fact_app.util.SceneManager;
import ni.edu.uam.fact_app.util.SesionUsuario;

import java.io.IOException;

public class MenuPrincipalController {

    @FXML private MenuItem mnuProductos;
    @FXML private MenuItem mnuCategorias;
    @FXML private MenuItem mnuCargos;
    @FXML private MenuItem mnuEmpleados;
    @FXML private MenuItem mnuVentas;

    @FXML private Button btnProductos;
    @FXML private Button btnCategorias;
    @FXML private Button btnCargos;
    @FXML private Button btnEmpleados;
    @FXML private Button btnVentas;

    @FXML private Label lblUsuarioActual;
    @FXML private Label lblRolActual;

    @FXML
    private void initialize() {
        Usuario user = SesionUsuario.getUsuarioActual();
        if (user != null && user.getEmpleado() != null) {
            lblUsuarioActual.setText(user.getEmpleado().getNombres() + " " + user.getEmpleado().getApellidos());
            lblRolActual.setText(user.getEmpleado().getCargo().getNombre());
            configurarPermisosSegunRol();
        }
    }

    /**
     * Aplica el control de acceso basado en roles (RBAC)
     */
    private void configurarPermisosSegunRol() {
        if (SesionUsuario.esAdministrador()) {
            // Administrador tiene acceso completo
            habilitarTodo(true);
        } else if (SesionUsuario.esCajero()) {
            // Cajero solo tiene acceso al punto de venta y consulta de catálogo
            btnCargos.setDisable(true);
            mnuCargos.setDisable(true);
            btnEmpleados.setDisable(true);
            mnuEmpleados.setDisable(true);
            btnCategorias.setDisable(true);
            mnuCategorias.setDisable(true);
        } else if (SesionUsuario.esBodeguero()) {
            // Bodeguero solo gestiona inventario
            btnVentas.setDisable(true);
            mnuVentas.setDisable(true);
            btnCargos.setDisable(true);
            mnuCargos.setDisable(true);
            btnEmpleados.setDisable(true);
            mnuEmpleados.setDisable(true);
        }
    }

    private void habilitarTodo(boolean estado) {
        btnProductos.setDisable(!estado);
        btnCategorias.setDisable(!estado);
        btnCargos.setDisable(!estado);
        btnEmpleados.setDisable(!estado);
        btnVentas.setDisable(!estado);
    }

    @FXML
    private void abrirProductos() {
        try {
            SceneManager.abrirVentana("/ni/edu/uam/fact_app/fxml/producto-view.fxml", "Gestión de Productos");
        } catch (IOException e) {
            e.printStackTrace();
            mensaje(Alert.AlertType.ERROR, "No fue posible abrir Productos.");
        }
    }

    @FXML
    private void abrirCategorias() {
        try {
            SceneManager.abrirVentana("/ni/edu/uam/fact_app/fxml/categoria-view.fxml", "Gestión de Categorías");
        } catch (IOException e) {
            e.printStackTrace();
            mensaje(Alert.AlertType.ERROR, "No fue posible abrir Categorías.");
        }
    }

    @FXML
    private void abrirCargos() {
        try {
            SceneManager.abrirVentana("/ni/edu/uam/fact_app/fxml/cargo-view.fxml", "Gestión de Cargos");
        } catch (IOException e) {
            e.printStackTrace();
            mensaje(Alert.AlertType.ERROR, "No fue posible abrir Cargos.");
        }
    }

    @FXML
    private void abrirEmpleados() {
        try {
            SceneManager.abrirVentana("/ni/edu/uam/fact_app/fxml/empleado-view.fxml", "Gestión de Empleados");
        } catch (IOException e) {
            e.printStackTrace();
            mensaje(Alert.AlertType.ERROR, "No fue posible abrir Empleados.");
        }
    }

    @FXML
    private void abrirVentas() {
        try {
            SceneManager.abrirVentana("/ni/edu/uam/fact_app/fxml/venta-view.fxml", "Módulo de Facturación y Ventas");
        } catch (IOException e) {
            e.printStackTrace();
            mensaje(Alert.AlertType.ERROR, "No fue posible abrir Ventas.");
        }
    }

    @FXML
    private void cerrarSesion() {
        SesionUsuario.cerrarSesion();
        try {
            SceneManager.cambiarEscena("/ni/edu/uam/fact_app/fxml/login-view.fxml", "Iniciar Sesión - Fact App");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void salir() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea cerrar la aplicación?", ButtonType.OK, ButtonType.CANCEL);
        if (a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            Platform.exit();
        }
    }

    private void mensaje(Alert.AlertType tipo, String texto) {
        new Alert(tipo, texto, ButtonType.OK).showAndWait();
    }
}