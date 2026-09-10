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
import ni.edu.uam.fact_app.model.Categoria;
import ni.edu.uam.fact_app.model.Producto;

import java.io.File;
import java.math.BigDecimal;

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

    private static final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private static int correlativoId = 1;
    private static boolean inicializado = false;
    private String rutaImagen;

    public static ObservableList<Producto> getProductos() {
        if (!inicializado) {
            ObservableList<Categoria> cats = CategoriaController.getCategorias();
            Categoria c1 = cats.size() > 0 ? cats.get(0) : new Categoria(1, "General", true);
            Categoria c2 = cats.size() > 1 ? cats.get(1) : c1;

            productos.add(new Producto(correlativoId++, "PRD-001", "Arroz Faisán 1lb", c1, new BigDecimal("22.50"), 120, null, true));
            productos.add(new Producto(correlativoId++, "PRD-002", "Coca Cola 2L", c2, new BigDecimal("45.00"), 50, null, true));
            inicializado = true;
        }
        return productos;
    }

    @FXML
    private void initialize() {
        // Cargar categorías activas
        ObservableList<Categoria> activas = FXCollections.observableArrayList();
        for (Categoria c : CategoriaController.getCategorias()) {
            if (c.isActiva()) activas.add(c);
        }
        cmbCategoria.setItems(activas);

        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colExistencia.setCellValueFactory(new PropertyValueFactory<>("existencia"));
        colActivo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActivo() ? "Activo" : "Inactivo"));

        getProductos();

        // Búsqueda reactiva en vivo con FilteredList
        FilteredList<Producto> filtro = new FilteredList<>(productos, p -> true);
        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldVal, texto) -> {
                filtro.setPredicate(p -> {
                    if (texto == null || texto.isBlank()) return true;
                    String busqueda = texto.toLowerCase();
                    return p.getNombre().toLowerCase().contains(busqueda) || p.getCodigo().toLowerCase().contains(busqueda);
                });
            });
        }
        tblProductos.setItems(filtro);

        // Seleccionar de la tabla para editar
        tblProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldV, p) -> {
            if (p != null) {
                txtCodigo.setText(p.getCodigo());
                txtCodigo.setDisable(true); // Bloquear código al editar
                txtNombre.setText(p.getNombre());
                txtPrecio.setText(p.getPrecioVenta().toString());
                txtExistencia.setText(String.valueOf(p.getExistencia()));
                cmbCategoria.setValue(p.getCategoria());
                chkActivo.setSelected(p.isActivo());
                rutaImagen = p.getRutaImagen();
                if (rutaImagen != null && !rutaImagen.isBlank()) {
                    try {
                        imgProducto.setImage(new Image(rutaImagen));
                    } catch (Exception e) {
                        imgProducto.setImage(null);
                    }
                } else {
                    imgProducto.setImage(null);
                }
            }
        });

        chkActivo.setSelected(true);
    }

    @FXML
    private void seleccionarImagen() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        File archivo = chooser.showOpenDialog(txtCodigo.getScene().getWindow());
        if (archivo != null) {
            rutaImagen = archivo.toURI().toString();
            imgProducto.setImage(new Image(rutaImagen));
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

            if (precio.signum() <= 0 || existencia < 0) {
                mensaje(Alert.AlertType.WARNING, "El precio debe ser > 0 y existencia >= 0.");
                return;
            }

            Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                seleccionado.setNombre(txtNombre.getText().trim());
                seleccionado.setCategoria(cmbCategoria.getValue());
                seleccionado.setPrecioVenta(precio);
                seleccionado.setExistencia(existencia);
                seleccionado.setRutaImagen(rutaImagen);
                seleccionado.setActivo(chkActivo.isSelected());
                tblProductos.refresh();
                mensaje(Alert.AlertType.INFORMATION, "Producto actualizado.");
            } else {
                // Verificar código único
                for (Producto p : productos) {
                    if (p.getCodigo().equalsIgnoreCase(txtCodigo.getText().trim())) {
                        mensaje(Alert.AlertType.ERROR, "Ya existe un producto con ese código.");
                        return;
                    }
                }
                productos.add(new Producto(correlativoId++, txtCodigo.getText().trim(), txtNombre.getText().trim(),
                        cmbCategoria.getValue(), precio, existencia, rutaImagen, chkActivo.isSelected()));
                mensaje(Alert.AlertType.INFORMATION, "Producto registrado correctamente.");
            }
            limpiar();
        } catch (NumberFormatException e) {
            mensaje(Alert.AlertType.ERROR, "Precio o existencia no válidos.");
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
        rutaImagen = null;
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