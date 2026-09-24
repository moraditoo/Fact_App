package ni.edu.uam.fact_app.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import ni.edu.uam.fact_app.dao.CategoriaDAO;
import ni.edu.uam.fact_app.dao.ProductoDAO;
import ni.edu.uam.fact_app.model.Categoria;
import ni.edu.uam.fact_app.model.Producto;

import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ProductoContoller {

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtExistencia;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private CheckBox chkActivo;
    @FXML private ImageView imgProducto;
    @FXML private TextField txtBuscar;

    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, Categoria> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colExistencia;
    @FXML private TableColumn<Producto, String> colActivo;

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final ObservableList<Producto> productosObservable = FXCollections.observableArrayList();

    // Variable fija para retener el ID de la base de datos durante la edición
    private Integer idSeleccionadoParaEditar = null;
    private String rutaImagenActual;
    private Timeline autoRefrescoTimeline;

    @FXML
    private void initialize() {
        recargarCategorias();

        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colExistencia.setCellValueFactory(new PropertyValueFactory<>("existencia"));
        colActivo.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().isActivo() ? "Activo" : "Inactivo"));

        cargarDatos();

        FilteredList<Producto> filtro = new FilteredList<>(productosObservable, p -> true);
        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, texto) -> {
                filtro.setPredicate(p -> {
                    if (texto == null || texto.isBlank()) return true;
                    String busq = texto.toLowerCase();
                    return p.getNombre().toLowerCase().contains(busq) || p.getCodigo().toLowerCase().contains(busq);
                });
            });
        }
        tblProductos.setItems(filtro);

        // Al seleccionar de la tabla, se entra en modo edición
        tblProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldV, p) -> {
            if (p != null) {
                idSeleccionadoParaEditar = p.getId(); // Retener ID de PostgreSQL
                txtCodigo.setText(p.getCodigo());
                txtCodigo.setDisable(true); // El código no se edita (es único)
                txtNombre.setText(p.getNombre());
                txtPrecio.setText(p.getPrecioVenta() != null ? p.getPrecioVenta().toString() : "0.00");
                txtExistencia.setText(String.valueOf(p.getExistencia()));
                chkActivo.setSelected(p.isActivo());

                // Emparejar categoría exacta
                if (p.getCategoria() != null) {
                    for (Categoria c : cmbCategoria.getItems()) {
                        if (c.getId().equals(p.getCategoria().getId())) {
                            cmbCategoria.setValue(c);
                            break;
                        }
                    }
                }

                rutaImagenActual = p.getRutaImagen();
                cargarImagenEnVista(rutaImagenActual);
            }
        });

        chkActivo.setSelected(true);
        iniciarAutoRefresco();
    }

    private void iniciarAutoRefresco() {
        autoRefrescoTimeline = new Timeline(new KeyFrame(Duration.seconds(2.5), event -> {
            // Si el usuario está editando un producto, no sobrescribir la pantalla
            if (idSeleccionadoParaEditar != null) {
                return;
            }

            Thread hilo = new Thread(() -> {
                try {
                    List<Producto> nuevos = productoDAO.listar();
                    Platform.runLater(() -> {
                        // Solo refrescar si no hay edición activa
                        if (idSeleccionadoParaEditar == null) {
                            productosObservable.setAll(nuevos);
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

    private void recargarCategorias() {
        try {
            cmbCategoria.setItems(FXCollections.observableArrayList(categoriaDAO.listar()));
        } catch (SQLException e) {
            mensaje(Alert.AlertType.ERROR, "Error al cargar categorías: " + e.getMessage());
        }
    }

    private void cargarDatos() {
        try {
            productosObservable.setAll(productoDAO.listar());
        } catch (SQLException e) {
            mensaje(Alert.AlertType.ERROR, "Error al cargar productos: " + e.getMessage());
        }
    }

    private void cargarImagenEnVista(String ruta) {
        if (ruta != null && !ruta.isBlank()) {
            try {
                imgProducto.setImage(new Image(ruta, true));
            } catch (Exception e) {
                imgProducto.setImage(null);
            }
        } else {
            imgProducto.setImage(null);
        }
    }

    @FXML
    private void seleccionarImagen() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        File archivo = chooser.showOpenDialog(txtCodigo.getScene().getWindow());
        if (archivo != null) {
            rutaImagenActual = archivo.toURI().toString();
            cargarImagenEnVista(rutaImagenActual);
        }
    }

    @FXML
    private void guardar() {
        if (txtCodigo.getText().isBlank() || txtNombre.getText().isBlank()
                || txtPrecio.getText().isBlank() || txtExistencia.getText().isBlank()
                || cmbCategoria.getValue() == null) {
            mensaje(Alert.AlertType.WARNING, "Complete todos los campos obligatorios.");
            return;
        }

        try {
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
            int existencia = Integer.parseInt(txtExistencia.getText().trim());

            if (precio.compareTo(BigDecimal.ZERO) <= 0 || existencia < 0) {
                mensaje(Alert.AlertType.WARNING, "El precio debe ser > 0 y la existencia >= 0.");
                return;
            }

            if (idSeleccionadoParaEditar != null) {
                // ACTUALIZACIÓN DIRECTA EN POSTGRESQL USANDO EL ID RETENIDO
                Producto productoAEditar = new Producto(
                        idSeleccionadoParaEditar,
                        txtCodigo.getText().trim(),
                        txtNombre.getText().trim(),
                        cmbCategoria.getValue(),
                        precio,
                        existencia,
                        rutaImagenActual,
                        chkActivo.isSelected()
                );

                productoDAO.actualizar(productoAEditar);
                mensaje(Alert.AlertType.INFORMATION, "Producto actualizado correctamente en PostgreSQL.");
            } else {
                // INSERCIÓN NUEVA EN POSTGRESQL
                Producto nuevo = new Producto(
                        txtCodigo.getText().trim().toUpperCase(),
                        txtNombre.getText().trim(),
                        cmbCategoria.getValue(),
                        precio,
                        existencia,
                        rutaImagenActual,
                        chkActivo.isSelected()
                );

                productoDAO.guardar(nuevo);
                mensaje(Alert.AlertType.INFORMATION, "Producto registrado correctamente en PostgreSQL.");
            }

            limpiar();
            cargarDatos(); // Recargar inmediatamente la tabla desde PostgreSQL

        } catch (NumberFormatException e) {
            mensaje(Alert.AlertType.ERROR, "Precio o existencia no válidos.");
        } catch (SQLException e) {
            mensaje(Alert.AlertType.ERROR, "Error en la base de datos: " + e.getMessage());
        }
    }

    @FXML
    private void eliminar() {
        if (idSeleccionadoParaEditar == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione un producto de la tabla para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea eliminar el producto permanentemente de la base de datos?", ButtonType.OK, ButtonType.CANCEL);
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                productoDAO.eliminar(idSeleccionadoParaEditar);
                mensaje(Alert.AlertType.INFORMATION, "Producto eliminado.");
                limpiar();
                cargarDatos();
            } catch (SQLException e) {
                mensaje(Alert.AlertType.ERROR, "Error al eliminar: " + e.getMessage());
            }
        }
    }

    @FXML
    private void limpiar() {
        idSeleccionadoParaEditar = null; // Reiniciar estado de edición
        txtCodigo.setDisable(false);
        txtCodigo.clear();
        txtNombre.clear();
        txtPrecio.clear();
        txtExistencia.clear();
        cmbCategoria.getSelectionModel().clearSelection();
        chkActivo.setSelected(true);
        imgProducto.setImage(null);
        rutaImagenActual = null;
        tblProductos.getSelectionModel().clearSelection();
    }

    @FXML
    private void cerrar() {
        if (autoRefrescoTimeline != null) {
            autoRefrescoTimeline.stop();
        }
        ((Stage) txtCodigo.getScene().getWindow()).close();
    }

    private void mensaje(Alert.AlertType tipo, String texto) {
        new Alert(tipo, texto, ButtonType.OK).showAndWait();
    }
}