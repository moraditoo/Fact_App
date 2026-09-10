package ni.edu.uam.fact_app.controller;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ni.edu.uam.fact_app.model.Categoria;
import ni.edu.uam.fact_app.model.Producto;
import ni.edu.uam.fact_app.util.DataManager;

import java.io.File;
import java.math.BigDecimal;
import java.util.function.UnaryOperator;

public class ProductoContoller {

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtExistencia;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private ImageView imgProducto;
    @FXML private TextField txtBuscar;

    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, Categoria> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colExistencia;

    private ObservableList<Producto> productos;
    private String rutaImagenActual;

    @FXML
    private void initialize() {
        productos = DataManager.getProductos();
        cmbCategoria.setItems(DataManager.getCategorias());

        configurarFiltrosEntradaNumerica();

        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colExistencia.setCellValueFactory(new PropertyValueFactory<>("existencia"));

        FilteredList<Producto> filtro = new FilteredList<>(productos, p -> true);
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
                cmbCategoria.setValue(p.getCategoria());
                rutaImagenActual = p.getRutaImagen();
                cargarImagenEnVista(rutaImagenActual);
            }
        });
    }

    private void configurarFiltrosEntradaNumerica() {
        UnaryOperator<TextFormatter.Change> filtroEnteros = change ->
                change.getControlNewText().matches("\\d*") ? change : null;
        txtExistencia.setTextFormatter(new TextFormatter<>(filtroEnteros));

        UnaryOperator<TextFormatter.Change> filtroDecimales = change ->
                change.getControlNewText().matches("\\d*(\\.\\d{0,2})?") ? change : null;
        txtPrecio.setTextFormatter(new TextFormatter<>(filtroDecimales));
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
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.webp"));
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

        BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
        int existencia = Integer.parseInt(txtExistencia.getText().trim());

        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            mensaje(Alert.AlertType.WARNING, "El precio debe ser mayor a 0.");
            return;
        }

        Producto sel = tblProductos.getSelectionModel().getSelectedItem();
        if (sel != null) {
            sel.setNombre(txtNombre.getText().trim());
            sel.setCategoria(cmbCategoria.getValue());
            sel.setPrecioVenta(precio);
            sel.setExistencia(existencia);
            sel.setRutaImagen(rutaImagenActual);
            tblProductos.refresh();
            DataManager.guardarProductos();
            mensaje(Alert.AlertType.INFORMATION, "Producto actualizado correctamente.");
        } else {
            String cod = txtCodigo.getText().trim().toUpperCase();
            for (Producto p : productos) {
                if (p.getCodigo().equalsIgnoreCase(cod)) {
                    mensaje(Alert.AlertType.ERROR, "Ya existe un producto con el código: " + cod);
                    return;
                }
            }
            int nuevoId = productos.size() + 1;
            productos.add(new Producto(nuevoId, cod, txtNombre.getText().trim(),
                    cmbCategoria.getValue(), precio, existencia, rutaImagenActual));
            DataManager.guardarProductos();
            mensaje(Alert.AlertType.INFORMATION, "Producto registrado correctamente.");
        }

        limpiar();
    }

    @FXML
    private void eliminar() {
        Producto sel = tblProductos.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione un producto para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea eliminar '" + sel.getNombre() + "' permanentemente?", ButtonType.OK, ButtonType.CANCEL);
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            productos.remove(sel);
            DataManager.guardarProductos();
            limpiar();
            mensaje(Alert.AlertType.INFORMATION, "Producto eliminado.");
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