package ni.edu.uam.fact_app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ni.edu.uam.fact_app.model.Cargo;

public class CargoController {

    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtBuscar;

    @FXML private TableView<Cargo> tblCargos;
    @FXML private TableColumn<Cargo, Integer> colId;
    @FXML private TableColumn<Cargo, String> colNombre;
    @FXML private TableColumn<Cargo, String> colDescripcion;

    // Lista estática para que no se reinicie al cerrar y abrir
    private static final ObservableList<Cargo> cargos = FXCollections.observableArrayList();
    private static int correlativoId = 1;
    private static boolean inicializado = false;

    public static ObservableList<Cargo> getCargos() {
        if (!inicializado) {
            cargos.add(new Cargo(correlativoId++, "Administrador", "Acceso total al sistema de facturación"));
            cargos.add(new Cargo(correlativoId++, "Cajero", "Encargado del registro de ventas y cobro"));
            cargos.add(new Cargo(correlativoId++, "Bodeguero", "Control de existencias y almacén"));
            inicializado = true;
        }
        return cargos;
    }

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        getCargos();
        tblCargos.setItems(cargos);

        tblCargos.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, seleccionado) -> {
            if (seleccionado != null) {
                txtNombre.setText(seleccionado.getNombre());
                txtDescripcion.setText(seleccionado.getDescripcion());
            }
        });
    }

    @FXML
    private void guardar() {
        if (txtNombre.getText().isBlank()) {
            mensaje(Alert.AlertType.WARNING, "El nombre del cargo es obligatorio.");
            return;
        }

        Cargo seleccionado = tblCargos.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            seleccionado.setNombre(txtNombre.getText().trim());
            seleccionado.setDescripcion(txtDescripcion.getText().trim());
            tblCargos.refresh();
            mensaje(Alert.AlertType.INFORMATION, "Cargo modificado correctamente.");
        } else {
            Cargo nuevo = new Cargo(correlativoId++, txtNombre.getText().trim(), txtDescripcion.getText().trim());
            cargos.add(nuevo);
            mensaje(Alert.AlertType.INFORMATION, "Cargo agregado correctamente.");
        }

        limpiar();
    }

    @FXML
    private void eliminar() {
        Cargo seleccionado = tblCargos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione un cargo de la tabla para eliminar.");
            return;
        }

        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea eliminar el cargo seleccionado?", ButtonType.OK, ButtonType.CANCEL);
        if (alerta.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            cargos.remove(seleccionado);
            limpiar();
            mensaje(Alert.AlertType.INFORMATION, "Cargo eliminado.");
        }
    }

    @FXML
    private void limpiar() {
        txtNombre.clear();
        txtDescripcion.clear();
        tblCargos.getSelectionModel().clearSelection();
    }

    @FXML
    private void cerrar() {
        ((Stage) txtNombre.getScene().getWindow()).close();
    }

    private void mensaje(Alert.AlertType tipo, String texto) {
        new Alert(tipo, texto, ButtonType.OK).showAndWait();
    }
}