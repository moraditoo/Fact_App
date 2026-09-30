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
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtExistencia;
    @FXML private CheckBox chkActivo;
    @FXML private ImageView imgProducto;

    @FXML private Button btnGuardar;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbFiltroCategoria;
    @FXML private ComboBox<String> cmbFiltroEstado;

    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, Categoria> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colExistencia;
    @FXML private TableColumn<Producto, String> colActivo;

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    private final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private FilteredList<Producto> productosFiltrados;
    private Producto productoSeleccionado = null;
    private String rutaImagenActual = null;

    @FXML
    private void initialize() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colExistencia.setCellValueFactory(new PropertyValueFactory<>("existencia"));
        colActivo.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().isActivo() ? "Sí" : "No"));

        productosFiltrados = new FilteredList<>(productos, p -> true);
        tblProductos.setItems(productosFiltrados);

        cargarCategorias();
        inicializarFiltrosAdicionales();
        cargarDatos();

        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        cmbFiltroCategoria.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        cmbFiltroEstado.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());

        tblProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, sel) -> {
            productoSeleccionado = sel;
            if (sel != null) {
                txtCodigo.setText(sel.getCodigo());
                txtCodigo.setDisable(true);
                txtNombre.setText(sel.getNombre());
                txtPrecio.setText(sel.getPrecioVenta() != null ? sel.getPrecioVenta().toString() : "");
                txtExistencia.setText(String.valueOf(sel.getExistencia()));
                chkActivo.setSelected(sel.isActivo());

                if (sel.getCategoria() != null) {
                    for (Categoria c : cmbCategoria.getItems()) {
                        if (c.getId().equals(sel.getCategoria().getId())) {
                            cmbCategoria.setValue(c);
                            break;
                        }
                    }
                }

                rutaImagenActual = sel.getRutaImagen();
                cargarImagenEnVista(rutaImagenActual);

                btnGuardar.setDisable(true);
                btnActualizar.setDisable(false);
                btnEliminar.setDisable(false);
            }
        });
    }

    private void cargarCategorias() {
        try {
            ObservableList<Categoria> cats = FXCollections.observableArrayList(categoriaDAO.listar());
            cmbCategoria.setItems(cats);

            ObservableList<String> nombresCats = FXCollections.observableArrayList();
            nombresCats.add("Todas las categorías");
            for (Categoria c : cats) {
                nombresCats.add(c.getNombre());
            }
            cmbFiltroCategoria.setItems(nombresCats);
            cmbFiltroCategoria.setValue("Todas las categorías");
        } catch (SQLException e) {
            mensaje(Alert.AlertType.ERROR, "Error al cargar categorías: " + e.getMessage());
        }
    }

    private void inicializarFiltrosAdicionales() {
        cmbFiltroEstado.setItems(FXCollections.observableArrayList("Todos los estados", "Activos", "Inactivos"));
        cmbFiltroEstado.setValue("Todos los estados");
    }

    private void cargarDatos() {
        try {
            productos.setAll(productoDAO.listar());
        } catch (SQLException e) {
            mensaje(Alert.AlertType.ERROR, "Error al leer productos: " + e.getMessage());
        }
    }

    private void aplicarFiltros() {
        productosFiltrados.setPredicate(p -> {
            String busqueda = txtBuscar.getText() != null ? txtBuscar.getText().trim().toLowerCase() : "";
            boolean coincideTexto = busqueda.isBlank()
                    || p.getCodigo().toLowerCase().contains(busqueda)
                    || p.getNombre().toLowerCase().contains(busqueda);

            String catFiltro = cmbFiltroCategoria.getValue();
            boolean coincideCategoria = catFiltro == null
                    || catFiltro.equals("Todas las categorías")
                    || (p.getCategoria() != null && p.getCategoria().getNombre().equalsIgnoreCase(catFiltro));

            String estadoFiltro = cmbFiltroEstado.getValue();
            boolean coincideEstado = estadoFiltro == null
                    || estadoFiltro.equals("Todos los estados")
                    || (estadoFiltro.equals("Activos") && p.isActivo())
                    || (estadoFiltro.equals("Inactivos") && !p.isActivo());

            return coincideTexto && coincideCategoria && coincideEstado;
        });
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
    private void restablecerFiltros() {
        txtBuscar.clear();
        cmbFiltroCategoria.setValue("Todas las categorías");
        cmbFiltroEstado.setValue("Todos los estados");
        aplicarFiltros();
    }

    @FXML
    private void guardar() {
        if (!validarFormulario()) return;

        String cod = txtCodigo.getText().trim().toUpperCase();

        for (Producto p : productos) {
            if (p.getCodigo().equalsIgnoreCase(cod)) {
                mensaje(Alert.AlertType.WARNING, "No se permiten códigos duplicados. El código ya existe.");
                return;
            }
        }

        try {
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
            int existencia = Integer.parseInt(txtExistencia.getText().trim());

            Producto nuevo = new Producto(
                    null,
                    cod,
                    txtNombre.getText().trim(),
                    cmbCategoria.getValue(),
                    precio,
                    existencia,
                    rutaImagenActual,
                    chkActivo.isSelected()
            );

            productoDAO.guardar(nuevo);
            productos.add(nuevo);
            mensaje(Alert.AlertType.INFORMATION, "Producto registrado exitosamente.");
            limpiar();

        } catch (SQLException e) {
            mensaje(Alert.AlertType.ERROR, "Error de base de datos: " + e.getMessage());
        }
    }

    @FXML
    private void actualizar() {
        if (productoSeleccionado == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione un producto de la tabla para actualizar.");
            return;
        }

        if (!validarFormulario()) return;

        try {
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
            int existencia = Integer.parseInt(txtExistencia.getText().trim());

            productoSeleccionado.setNombre(txtNombre.getText().trim());
            productoSeleccionado.setCategoria(cmbCategoria.getValue());
            productoSeleccionado.setPrecioVenta(precio);
            productoSeleccionado.setExistencia(existencia);
            productoSeleccionado.setRutaImagen(rutaImagenActual);
            productoSeleccionado.setActivo(chkActivo.isSelected());

            productoDAO.actualizar(productoSeleccionado);
            tblProductos.refresh();
            mensaje(Alert.AlertType.INFORMATION, "Producto actualizado correctamente.");
            limpiar();

        } catch (SQLException e) {
            mensaje(Alert.AlertType.ERROR, "Error al actualizar en la base de datos: " + e.getMessage());
        }
    }

    @FXML
    private void eliminar() {
        if (productoSeleccionado == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione un producto para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Está seguro de eliminar el producto '" + productoSeleccionado.getNombre() + "'?", ButtonType.OK, ButtonType.CANCEL);
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                productoDAO.eliminar(productoSeleccionado.getId());
                productos.remove(productoSeleccionado);
                mensaje(Alert.AlertType.INFORMATION, "Producto eliminado.");
                limpiar();
            } catch (SQLException e) {
                mensaje(Alert.AlertType.ERROR, "Error al eliminar: " + e.getMessage());
            }
        }
    }

    private boolean validarFormulario() {
        if (txtCodigo.getText().isBlank()) {
            mensaje(Alert.AlertType.WARNING, "El código es obligatorio.");
            return false;
        }
        if (txtNombre.getText().isBlank()) {
            mensaje(Alert.AlertType.WARNING, "El nombre es obligatorio.");
            return false;
        }
        if (cmbCategoria.getValue() == null) {
            mensaje(Alert.AlertType.WARNING, "Debe seleccionar una categoría.");
            return false;
        }
        if (txtPrecio.getText().isBlank()) {
            mensaje(Alert.AlertType.WARNING, "El precio de venta es obligatorio.");
            return false;
        }
        if (txtExistencia.getText().isBlank()) {
            mensaje(Alert.AlertType.WARNING, "La existencia es obligatoria.");
            return false;
        }

        try {
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
            if (precio.compareTo(BigDecimal.ZERO) <= 0) {
                mensaje(Alert.AlertType.WARNING, "El precio debe ser mayor que cero.");
                return false;
            }
        } catch (NumberFormatException e) {
            mensaje(Alert.AlertType.WARNING, "El precio debe ser un valor numérico válido.");
            return false;
        }

        try {
            int existencia = Integer.parseInt(txtExistencia.getText().trim());
            if (existencia < 0) {
                mensaje(Alert.AlertType.WARNING, "La existencia no puede ser negativa.");
                return false;
            }
        } catch (NumberFormatException e) {
            mensaje(Alert.AlertType.WARNING, "La existencia debe ser un número entero.");
            return false;
        }

        return true;
    }

    @FXML
    private void limpiar() {
        productoSeleccionado = null;
        rutaImagenActual = null;
        imgProducto.setImage(null);
        txtCodigo.setDisable(false);
        txtCodigo.clear();
        txtNombre.clear();
        txtPrecio.clear();
        txtExistencia.clear();
        cmbCategoria.getSelectionModel().clearSelection();
        chkActivo.setSelected(true);
        tblProductos.getSelectionModel().clearSelection();

        btnGuardar.setDisable(false);
        btnActualizar.setDisable(true);
        btnEliminar.setDisable(true);
    }

    @FXML
    private void cerrar() {
        ((Stage) txtCodigo.getScene().getWindow()).close();
    }

    private void mensaje(Alert.AlertType tipo, String texto) {
        new Alert(tipo, texto, ButtonType.OK).showAndWait();
    }
}