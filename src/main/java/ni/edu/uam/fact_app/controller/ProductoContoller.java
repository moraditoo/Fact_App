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
import java.util.function.UnaryOperator;

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
    private String rutaImagenActual;

    public static ObservableList<Producto> getProductos() {
        if (!inicializado) {
            ObservableList<Categoria> cats = CategoriaController.getCategorias();
            Categoria catAlimentos = cats.stream().filter(c -> c.getNombre().equalsIgnoreCase("Alimentos")).findFirst().orElse(cats.get(0));
            Categoria catBebidas = cats.stream().filter(c -> c.getNombre().equalsIgnoreCase("Bebidas")).findFirst().orElse(cats.get(0));
            Categoria catLimpieza = cats.stream().filter(c -> c.getNombre().equalsIgnoreCase("Limpieza")).findFirst().orElse(cats.get(0));

            // Catálogo extenso con imágenes por defecto
            productos.add(new Producto(correlativoId++, "ALM-001", "Arroz Faisán 1lb", catAlimentos, new BigDecimal("24.50"), 150, "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=150", true));
            productos.add(new Producto(correlativoId++, "ALM-002", "Frijoles Rojos 1lb", catAlimentos, new BigDecimal("32.00"), 120, "https://images.unsplash.com/photo-1551462147-ff29053bfc14?w=150", true));
            productos.add(new Producto(correlativoId++, "ALM-003", "Azúcar Sulinsa 2lb", catAlimentos, new BigDecimal("28.00"), 85, "https://images.unsplash.com/photo-1622484216805-4c07b461fa82?w=150", true));
            productos.add(new Producto(correlativoId++, "ALM-004", "Aceite Corona 1L", catAlimentos, new BigDecimal("65.00"), 60, "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=150", true));
            productos.add(new Producto(correlativoId++, "ALM-005", "Café Presto Instantáneo 100g", catAlimentos, new BigDecimal("48.00"), 75, "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=150", true));
            productos.add(new Producto(correlativoId++, "ALM-006", "Avena Quaker 400g", catAlimentos, new BigDecimal("35.00"), 40, "https://images.unsplash.com/photo-1614961909013-1e2212a2ca87?w=150", true));

            productos.add(new Producto(correlativoId++, "BEB-001", "Coca Cola 2L Descartable", catBebidas, new BigDecimal("48.00"), 90, "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=150", true));
            productos.add(new Producto(correlativoId++, "BEB-002", "Jugo Del Valle Naranja 1L", catBebidas, new BigDecimal("38.00"), 45, "https://images.unsplash.com/photo-1613478223719-2ab802602423?w=150", true));
            productos.add(new Producto(correlativoId++, "BEB-003", "Agua Fuente Pura 600ml", catBebidas, new BigDecimal("16.00"), 180, "https://images.unsplash.com/photo-1548839140-29a749e1bc4e?w=150", true));
            productos.add(new Producto(correlativoId++, "BEB-004", "Leche Parmalat Entera 1L", catBebidas, new BigDecimal("42.00"), 110, "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=150", true));

            productos.add(new Producto(correlativoId++, "LMP-001", "Detergente Roma 1kg", catLimpieza, new BigDecimal("55.00"), 65, "https://images.unsplash.com/photo-1583947215259-38e31be8751f?w=150", true));
            productos.add(new Producto(correlativoId++, "LMP-002", "Jabón Axion Limón 450g", catLimpieza, new BigDecimal("36.00"), 80, "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=150", true));
            productos.add(new Producto(correlativoId++, "LMP-003", "Cloro Magia Blanca 1L", catLimpieza, new BigDecimal("26.00"), 50, "https://images.unsplash.com/photo-1585421514738-01798e348b17?w=150", true));
            productos.add(new Producto(correlativoId++, "LMP-004", "Papel Higiénico Scott 4 rollos", catLimpieza, new BigDecimal("60.00"), 70, "https://images.unsplash.com/photo-1584556812952-905ffd0c611a?w=150", true));

            inicializado = true;
        }
        return productos;
    }

    @FXML
    private void initialize() {
        configurarFiltrosEntradaNumerica();

        // Categorías activas
        ObservableList<Categoria> cats = FXCollections.observableArrayList();
        for (Categoria c : CategoriaController.getCategorias()) {
            if (c.isActiva()) cats.add(c);
        }
        cmbCategoria.setItems(cats);

        // Bindings de columnas
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colExistencia.setCellValueFactory(new PropertyValueFactory<>("existencia"));
        colActivo.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().isActivo() ? "Activo" : "Inactivo"));

        getProductos();

        // Búsqueda en vivo reactiva
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

        // Selección en tabla para edición
        tblProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldV, p) -> {
            if (p != null) {
                txtCodigo.setText(p.getCodigo());
                txtCodigo.setDisable(true); // Bloquear PK al editar
                txtNombre.setText(p.getNombre());
                txtPrecio.setText(p.getPrecioVenta().toString());
                txtExistencia.setText(String.valueOf(p.getExistencia()));
                cmbCategoria.setValue(p.getCategoria());
                chkActivo.setSelected(p.isActivo());
                rutaImagenActual = p.getRutaImagen();
                cargarImagenEnVista(rutaImagenActual);
            }
        });

        chkActivo.setSelected(true);
    }

    /**
     * Valida en tiempo real que el usuario no escriba letras o símbolos inválidos
     */
    private void configurarFiltrosEntradaNumerica() {
        UnaryOperator<TextFormatter.Change> filtroEnteros = change -> {
            String newText = change.getControlNewText();
            return newText.matches("\\d*") ? change : null;
        };
        txtExistencia.setTextFormatter(new TextFormatter<>(filtroEnteros));

        UnaryOperator<TextFormatter.Change> filtroDecimales = change -> {
            String newText = change.getControlNewText();
            return newText.matches("\\d*(\\.\\d{0,2})?") ? change : null;
        };
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
            mensaje(Alert.AlertType.WARNING, "Todos los campos con (*) son obligatorios.");
            return;
        }

        BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
        int existencia = Integer.parseInt(txtExistencia.getText().trim());

        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            mensaje(Alert.AlertType.WARNING, "El precio de venta debe ser mayor a C$ 0.00.");
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
            tblProductos.refresh();
            mensaje(Alert.AlertType.INFORMATION, "Producto modificado exitosamente.");
        } else {
            // Validar unicidad de código
            String cod = txtCodigo.getText().trim().toUpperCase();
            for (Producto p : productos) {
                if (p.getCodigo().equalsIgnoreCase(cod)) {
                    mensaje(Alert.AlertType.ERROR, "Ya existe un producto con el código: " + cod);
                    return;
                }
            }
            productos.add(new Producto(correlativoId++, cod, txtNombre.getText().trim(),
                    cmbCategoria.getValue(), precio, existencia, rutaImagenActual, chkActivo.isSelected()));
            mensaje(Alert.AlertType.INFORMATION, "Producto registrado en el inventario.");
        }

        limpiar();
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