package ni.edu.uam.fact_app.controller;

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
import ni.edu.uam.fact_app.dao.CategoriaDAO;
import ni.edu.uam.fact_app.dao.ProductoDAO;
import ni.edu.uam.fact_app.model.Categoria;
import ni.edu.uam.fact_app.model.Producto;

import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;

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
    private String rutaImagenActual;

    @FXML
    private void initialize() {
        // Cargar Categorías desde PostgreSQL
        try {
            cmbCategoria.setItems(FXCollections.observableArrayList(categoriaDAO.listar()));
        } catch (SQLException e) {
            mensaje(Alert.AlertType.ERROR, "Error al cargar categorías: " + e.getMessage());
        }

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

        tblProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldV, p) -> {
            if (p != null) {
                txtCodigo.setText(p.getCodigo());
                txtCodigo.setDisable(true);
                txtNombre.setText(p.getNombre());
                txtPrecio.setText(p.getPrecioVenta().toString());
                txtExistencia.setText(String.valueOf(p.getExistencia()));
                chkActivo.setSelected(p.isActivo());

                // Seleccionar la categoría coincidente en el combo
                for (Categoria c : cmbCategoria.getItems()) {
                    if (c.getId().equals(p.getCategoria().getId())) {
                        cmbCategoria.setValue(c);
                        break;
                    }
                }

                rutaImagenActual = p.getRutaImagen();
                cargarImagenEnVista(rutaImagenActual);
            }
        });

        chkActivo.setSelected(true);
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
                mensaje(Alert.AlertType.WARNING, "Precio mayor a 0 y existencia no negativa.");
                return;
            }

            Producto sel = tblProductos.getSelectionModel().getSelectedItem();
            if (sel != null) {
                sel.setNombre(txtNombre.getText().trim());
                sel.setCategoria(cmbCategoria.getValue());
                sel.setPrecioVenta(precio);
                sel.setExistencia(existencia);
                sel.setRutaImagen(rutaImagenActual);
                sel.setActivo(chkActivo.isSelected());

                productoDAO.actualizar(sel);
                mensaje(Alert.AlertType.INFORMATION, "Producto actualizado en la base de datos.");
            } else {
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
                mensaje(Alert.AlertType.INFORMATION, "Producto guardado con éxito en PostgreSQL.");
            }

            cargarDatos();
            limpiar();

        } catch (NumberFormatException e) {
            mensaje(Alert.AlertType.ERROR, "Precio o existencia no válidos.");
        } catch (SQLException e) {
            mensaje(Alert.AlertType.ERROR, "Error de base de datos: " + e.getMessage());
        }
    }

    @FXML
    private void eliminar() {
        Producto sel = tblProductos.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione un producto para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea eliminar el producto?", ButtonType.OK, ButtonType.CANCEL);
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                productoDAO.eliminar(sel.getId());
                mensaje(Alert.AlertType.INFORMATION, "Producto eliminado.");
                cargarDatos();
                limpiar();
            } catch (SQLException e) {
                mensaje(Alert.AlertType.ERROR, "Error al eliminar: " + e.getMessage());
            }
        }
    }

    @FXML
    private void limpiar() {
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
        ((Stage) txtCodigo.getScene().getWindow()).close();
    }

    private void mensaje(Alert.AlertType tipo, String texto) {
        new Alert(tipo, texto, ButtonType.OK).showAndWait();
    }
}