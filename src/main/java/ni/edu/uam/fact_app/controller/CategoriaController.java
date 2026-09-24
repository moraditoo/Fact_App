package ni.edu.uam.fact_app.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ni.edu.uam.fact_app.dao.CategoriaDAO;
import ni.edu.uam.fact_app.model.Categoria;

import java.sql.SQLException;

public class CategoriaController {

    @FXML private TextField txtNombre;
    @FXML private CheckBox chkActiva;
    @FXML private TextField txtBuscar;

    @FXML private TableView<Categoria> tblCategorias;
    @FXML private TableColumn<Categoria, Integer> colId;
    @FXML private TableColumn<Categoria, String> colNombre;
    @FXML private TableColumn<Categoria, String> colActiva;

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final ObservableList<Categoria> categoriasObservable = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colActiva.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActiva() ? "Activa" : "Inactiva"));

        cargarDatos();

        FilteredList<Categoria> filtro = new FilteredList<>(categoriasObservable, c -> true);
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
                chkActiva.setSelected(sel.isActiva());
            }
        });

        chkActiva.setSelected(true);
    }

    private void cargarDatos() {
        try {
            categoriasObservable.setAll(categoriaDAO.listar());
        } catch (SQLException e) {
            mensaje(Alert.AlertType.ERROR, "Error al cargar categorías: " + e.getMessage());
        }
    }

    @FXML
    private void guardar() {
        if (txtNombre.getText().isBlank()) {
            mensaje(Alert.AlertType.WARNING, "El nombre de la categoría es obligatorio.");
            return;
        }

        Categoria sel = tblCategorias.getSelectionModel().getSelectedItem();
        try {
            if (sel != null) {
                sel.setNombre(txtNombre.getText().trim());
                sel.setActiva(chkActiva.isSelected());
                categoriaDAO.actualizar(sel);
                mensaje(Alert.AlertType.INFORMATION, "Categoría actualizada.");
            } else {
                Categoria nueva = new Categoria(txtNombre.getText().trim(), chkActiva.isSelected());
                categoriaDAO.guardar(nueva);
                mensaje(Alert.AlertType.INFORMATION, "Categoría guardada en la base de datos.");
            }
            cargarDatos();
            limpiar();
        } catch (SQLException e) {
            mensaje(Alert.AlertType.ERROR, "Error en base de datos: " + e.getMessage());
        }
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
            try {
                categoriaDAO.eliminar(sel.getId());
                mensaje(Alert.AlertType.INFORMATION, "Categoría eliminada.");
                cargarDatos();
                limpiar();
            } catch (SQLException e) {
                mensaje(Alert.AlertType.ERROR, "No se puede eliminar (posiblemente tenga productos asociados).");
            }
        }
    }

    @FXML
    private void limpiar() {
        txtNombre.clear();
        chkActiva.setSelected(true);
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