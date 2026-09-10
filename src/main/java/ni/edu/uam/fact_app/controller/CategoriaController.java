package ni.edu.uam.fact_app.controller;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ni.edu.uam.fact_app.model.Categoria;
import ni.edu.uam.fact_app.util.DataManager;

public class CategoriaController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtBuscar;

    @FXML private TableView<Categoria> tblCategorias;
    @FXML private TableColumn<Categoria, Integer> colId;
    @FXML private TableColumn<Categoria, String> colNombre;

    private ObservableList<Categoria> categorias;

    @FXML
    private void initialize() {
        categorias = DataManager.getCategorias();

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        FilteredList<Categoria> filtro = new FilteredList<>(categorias, c -> true);
        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, texto) -> {
                filtro.setPredicate(c -> {
                    if (texto == null || texto.isBlank()) return true;
                    return c.getNombre().toLowerCase().contains(texto.toLowerCase());
                });
            });
        }
        tblCategorias.setItems(filtro);

        tblCategorias.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, sel) -> {
            if (sel != null) {
                txtNombre.setText(sel.getNombre());
            }
        });
    }

    @FXML
    private void guardar() {
        if (txtNombre.getText().isBlank()) {
            mensaje(Alert.AlertType.WARNING, "El nombre de la categoría es obligatorio.");
            return;
        }

        Categoria sel = tblCategorias.getSelectionModel().getSelectedItem();
        if (sel != null) {
            sel.setNombre(txtNombre.getText().trim());
            tblCategorias.refresh();
            DataManager.guardarCategorias();
            mensaje(Alert.AlertType.INFORMATION, "Categoría modificada correctamente.");
        } else {
            int nuevoId = categorias.size() + 1;
            categorias.add(new Categoria(nuevoId, txtNombre.getText().trim()));
            DataManager.guardarCategorias();
            mensaje(Alert.AlertType.INFORMATION, "Categoría agregada correctamente.");
        }

        limpiar();
    }

    @FXML
    private void eliminar() {
        Categoria sel = tblCategorias.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione una categoría de la tabla.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea eliminar la categoría seleccionada?", ButtonType.OK, ButtonType.CANCEL);
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            categorias.remove(sel);
            DataManager.guardarCategorias();
            limpiar();
            mensaje(Alert.AlertType.INFORMATION, "Categoría eliminada.");
        }
    }

    @FXML
    private void limpiar() {
        txtNombre.clear();
        tblCategorias.getSelectionModel().clearSelection();
    }

    @FXML
    private void cerrar() {
        ((Stage) txtNombre.getScene().getWindow()).close();
    }

    private void mensaje(Alert.AlertType tipo, String texto) {
        new Alert(tipo, texto, ButtonType.OK).showAndWait();
    }
}