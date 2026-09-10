package ni.edu.uam.fact_app.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ni.edu.uam.fact_app.model.Categoria;

public class CategoriaController {

    @FXML private TextField txtNombre;
    @FXML private CheckBox chkActiva;
    @FXML private TextField txtBuscar;

    @FXML private TableView<Categoria> tblCategorias;
    @FXML private TableColumn<Categoria, Integer> colId;
    @FXML private TableColumn<Categoria, String> colNombre;
    @FXML private TableColumn<Categoria, String> colActiva;

    // Lista estática para que los cambios se mantengan al cerrar y reabrir la ventana
    private static final ObservableList<Categoria> categorias = FXCollections.observableArrayList();
    private static int correlativoId = 1;
    private static boolean inicializado = false;

    public static ObservableList<Categoria> getCategorias() {
        if (!inicializado) {
            categorias.add(new Categoria(correlativoId++, "Alimentos", true));
            categorias.add(new Categoria(correlativoId++, "Bebidas", true));
            categorias.add(new Categoria(correlativoId++, "Limpieza", true));
            inicializado = true;
        }
        return categorias;
    }

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        // Muestra visualmente "Activo" o "Inactivo" en la columna
        colActiva.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isActiva() ? "Activo" : "Inactivo")
        );

        // Asegurar que contenga datos de prueba iniciales
        getCategorias();
        tblCategorias.setItems(categorias);
        chkActiva.setSelected(true);

        // Al seleccionar de la tabla, carga los datos en el formulario
        tblCategorias.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, seleccionado) -> {
            if (seleccionado != null) {
                txtNombre.setText(seleccionado.getNombre());
                chkActiva.setSelected(seleccionado.isActiva());
            }
        });
    }

    @FXML
    private void guardar() {
        if (txtNombre.getText().isBlank()) {
            mensaje(Alert.AlertType.WARNING, "El nombre de la categoría es obligatorio.");
            return;
        }

        Categoria seleccionada = tblCategorias.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            seleccionada.setNombre(txtNombre.getText().trim());
            seleccionada.setActiva(chkActiva.isSelected());
            tblCategorias.refresh();
            mensaje(Alert.AlertType.INFORMATION, "Categoría modificada correctamente.");
        } else {
            Categoria nueva = new Categoria(correlativoId++, txtNombre.getText().trim(), chkActiva.isSelected());
            categorias.add(nueva);
            mensaje(Alert.AlertType.INFORMATION, "Categoría agregada correctamente.");
        }

        limpiar();
    }

    @FXML
    private void desactivar() {
        Categoria seleccionada = tblCategorias.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione una categoría de la tabla.");
            return;
        }

        seleccionada.setActiva(false);
        chkActiva.setSelected(false);
        tblCategorias.refresh();
        mensaje(Alert.AlertType.INFORMATION, "Categoría desactivada.");
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