package ni.edu.uam.fact_app.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import ni.edu.uam.fact_app.util.SceneManager;

import java.io.IOException;

public class MenuPrincipalController {

    @FXML
    private void abrirProductos() {
        try {
            SceneManager.abrirVentana(
                    "/ni/edu/uam/fact_app/fxml/producto-view.fxml",
                    "Gestión de productos"
            );
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "No fue posible abrir Productos.").showAndWait();
        }
    }

    @FXML
    private void abrirCargos() {
        try {
            SceneManager.abrirVentana(
                    "/ni/edu/uam/fact_app/fxml/cargo-view.fxml",
                    "Gestión de cargos"
            );
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "No fue posible abrir Cargos.").showAndWait();
        }
    }

    @FXML
    private void abrirCategorias() {
        try {
            SceneManager.abrirVentana(
                    "/ni/edu/uam/fact_app/fxml/categoria-view.fxml",
                    "Gestión de categorías"
            );
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "No fue posible abrir Categorías.").showAndWait();
        }
    }

    @FXML
    private void salir() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea cerrar la aplicación?", ButtonType.OK, ButtonType.CANCEL);
        if (a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            Platform.exit();
        }
    }
}