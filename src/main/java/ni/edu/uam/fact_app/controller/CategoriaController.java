package ni.edu.uam.fact_app.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Duration;
import ni.edu.uam.fact_app.dao.CategoriaDAO;
import ni.edu.uam.fact_app.model.Categoria;

import java.sql.SQLException;
import java.util.List;

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
    private Timeline autoRefrescoTimeline;

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colActiva.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActiva() ? "Activa" : "Inactiva"));

        tblCategorias.setItems(categoriasObservable);
        cargarCategorias();

        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, texto) -> buscarCategorias());
        }

        tblCategorias.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, sel) -> {
            if (sel != null) {
                txtNombre.setText(sel.getNombre());
                chkActiva.setSelected(sel.isActiva());
            }
        });

        chkActiva.setSelected(true);
        iniciarAutoRefresco();
    }

    private void cargarCategorias() {
        try {
            categoriasObservable.setAll(categoriaDAO.listar());
        } catch (SQLException e) {
            mostrarError("Error de base de datos", "No fue posible conectar con el servidor o consultar categorías.");
            System.err.println(e.getMessage());
        }
    }

    private void buscarCategorias() {
        try {
            if (txtBuscar == null || txtBuscar.getText().isBlank()) {
                categoriasObservable.setAll(categoriaDAO.listar());
            } else {
                categoriasObservable.setAll(categoriaDAO.buscarPorNombre(txtBuscar.getText()));
            }
        } catch (SQLException e) {
            mostrarError("Error de base de datos", "No fue posible filtrar las categorías.");
        }
    }

    private void iniciarAutoRefresco() {
        autoRefrescoTimeline = new Timeline(new KeyFrame(Duration.seconds(2.5), event -> {
            if (tblCategorias.getSelectionModel().getSelectedItem() != null) return;
            if (txtBuscar != null && !txtBuscar.getText().isBlank()) return;

            Thread hilo = new Thread(() -> {
                try {
                    List<Categoria> nuevas = categoriaDAO.listar();
                    Platform.runLater(() -> {
                        if (tblCategorias.getSelectionModel().getSelectedItem() == null
                                && (txtBuscar == null || txtBuscar.getText().isBlank())) {
                            categoriasObservable.setAll(nuevas);
                        }
                    });
                } catch (SQLException ignored) { }
            });
            hilo.setDaemon(true);
            hilo.start();
        }));

        autoRefrescoTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefrescoTimeline.play();
    }

    private boolean validarCategoria() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            mostrarError("Validación", "El nombre de la categoría es obligatorio.");
            txtNombre.requestFocus();
            return false;
        }
        return true;
    }

    @FXML
    private void guardar() {
        if (!validarCategoria()) return;

        String nombre = txtNombre.getText().trim();

        try {
            if (categoriaDAO.existeNombre(nombre, null)) {
                mostrarAdvertencia("Categoría duplicada", "Ya existe una categoría registrada con el nombre ingresado.");
                txtNombre.requestFocus();
                return;
            }

            Categoria nueva = new Categoria(nombre, chkActiva.isSelected());
            categoriaDAO.guardar(nueva);

            mostrarExito("Categoría registrada", "La categoría fue registrada correctamente.");
            limpiar();
            cargarCategorias();

        } catch (SQLException e) {
            mostrarError("Error de base de datos", "No fue posible registrar la categoría.");
            System.err.println(e.getMessage());
        }
    }

    @FXML
    private void actualizar() {
        Categoria seleccionada = tblCategorias.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mostrarAdvertencia("Seleccione una categoría", "Debe seleccionar la categoría que desea actualizar.");
            return;
        }

        if (!validarCategoria()) return;

        String nombre = txtNombre.getText().trim();

        try {
            if (categoriaDAO.existeNombre(nombre, seleccionada.getId())) {
                mostrarAdvertencia("Categoría duplicada", "Ya existe otra categoría registrada con ese nombre.");
                txtNombre.requestFocus();
                return;
            }

            seleccionada.setNombre(nombre);
            seleccionada.setActiva(chkActiva.isSelected());

            categoriaDAO.actualizar(seleccionada);
            mostrarExito("Categoría actualizada", "Los cambios fueron guardados correctamente.");
            limpiar();
            cargarCategorias();

        } catch (SQLException e) {
            mostrarError("Error de base de datos", "No fue posible actualizar la categoría.");
            System.err.println(e.getMessage());
        }
    }

    @FXML
    private void eliminar() {
        Categoria seleccionada = tblCategorias.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mostrarAdvertencia("Seleccione una categoría", "Debe seleccionar la categoría que desea eliminar.");
            return;
        }

        try {
            if (categoriaDAO.tieneProductos(seleccionada.getId())) {
                mostrarError("Integridad referencial", "No puede eliminar la categoría porque tiene productos asociados.");
                return;
            }

            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea eliminar la categoría seleccionada?", ButtonType.OK, ButtonType.CANCEL);
            if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                categoriaDAO.eliminar(seleccionada.getId());
                mostrarExito("Categoría eliminada", "El registro fue eliminado correctamente.");
                limpiar();
                cargarCategorias();
            }

        } catch (SQLException e) {
            mostrarError("Error de base de datos", "No fue posible eliminar la categoría.");
            System.err.println(e.getMessage());
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
        if (autoRefrescoTimeline != null) {
            autoRefrescoTimeline.stop();
        }
        ((Stage) txtNombre.getScene().getWindow()).close();
    }

    private void mostrarExito(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void mostrarAdvertencia(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING, mensaje, ButtonType.OK);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}