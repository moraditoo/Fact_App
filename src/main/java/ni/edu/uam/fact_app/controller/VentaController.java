package ni.edu.uam.fact_app.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ni.edu.uam.fact_app.model.*;
import ni.edu.uam.fact_app.util.SesionUsuario;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public class VentaController {

    @FXML private Label lblNumeroFactura;
    @FXML private Label lblVendedor;
    @FXML private ComboBox<Producto> cmbProducto;
    @FXML private TextField txtCantidad;
    @FXML private Label lblStockDisponible;

    @FXML private TableView<DetalleVenta> tblDetalle;
    @FXML private TableColumn<DetalleVenta, String> colCodigo;
    @FXML private TableColumn<DetalleVenta, String> colProducto;
    @FXML private TableColumn<DetalleVenta, Integer> colCantidad;
    @FXML private TableColumn<DetalleVenta, BigDecimal> colPrecio;
    @FXML private TableColumn<DetalleVenta, BigDecimal> colSubtotal;

    @FXML private Label lblSubtotal;
    @FXML private Label lblIva;
    @FXML private Label lblTotal;

    private final ObservableList<DetalleVenta> carrito = FXCollections.observableArrayList();
    private static int correlativoFactura = 1001;

    @FXML
    private void initialize() {
        lblNumeroFactura.setText("FAC-" + correlativoFactura);
        Usuario actual = SesionUsuario.getUsuarioActual();
        if (actual != null && actual.getEmpleado() != null) {
            lblVendedor.setText(actual.getEmpleado().getNombres() + " " + actual.getEmpleado().getApellidos());
        }

        // Cargar productos activos con existencia disponible
        ObservableList<Producto> disponibles = FXCollections.observableArrayList();
        for (Producto p : ProductoContoller.getProductos()) {
            if (p.isActivo() && p.getExistencia() > 0) disponibles.add(p);
        }
        cmbProducto.setItems(disponibles);

        cmbProducto.getSelectionModel().selectedItemProperty().addListener((obs, oldV, prod) -> {
            if (prod != null) {
                lblStockDisponible.setText("Stock: " + prod.getExistencia());
            } else {
                lblStockDisponible.setText("Stock: -");
            }
        });

        colCodigo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCodigoProducto()));
        colProducto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreProducto()));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        tblDetalle.setItems(carrito);
        calcularTotales();
    }

    @FXML
    private void agregarAlCarrito() {
        Producto prod = cmbProducto.getValue();
        if (prod == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione un producto.");
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(txtCantidad.getText().trim());
            if (cantidad <= 0) {
                mensaje(Alert.AlertType.WARNING, "La cantidad debe ser mayor a 0.");
                return;
            }
        } catch (NumberFormatException e) {
            mensaje(Alert.AlertType.ERROR, "Cantidad no válida.");
            return;
        }

        if (cantidad > prod.getExistencia()) {
            mensaje(Alert.AlertType.ERROR, "Existencias insuficientes. Disponibles: " + prod.getExistencia());
            return;
        }

        carrito.add(new DetalleVenta(prod, cantidad));
        calcularTotales();
        txtCantidad.setText("1");
        cmbProducto.getSelectionModel().clearSelection();
    }

    @FXML
    private void removerDelCarrito() {
        DetalleVenta sel = tblDetalle.getSelectionModel().getSelectedItem();
        if (sel != null) {
            carrito.remove(sel);
            calcularTotales();
        } else {
            mensaje(Alert.AlertType.WARNING, "Seleccione un artículo para remover.");
        }
    }

    private void calcularTotales() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (DetalleVenta d : carrito) {
            subtotal = subtotal.add(d.getSubtotal());
        }
        BigDecimal iva = subtotal.multiply(new BigDecimal("0.15")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(iva).setScale(2, RoundingMode.HALF_UP);

        lblSubtotal.setText("C$ " + subtotal.setScale(2, RoundingMode.HALF_UP));
        lblIva.setText("C$ " + iva);
        lblTotal.setText("C$ " + total);
    }

    @FXML
    private void finalizarVenta() {
        if (carrito.isEmpty()) {
            mensaje(Alert.AlertType.WARNING, "El carrito de venta está vacío.");
            return;
        }

        // Descontar inventario de cada producto vendido
        for (DetalleVenta d : carrito) {
            Producto p = d.getProducto();
            p.setExistencia(p.getExistencia() - d.getCantidad());
        }

        mensaje(Alert.AlertType.INFORMATION, "¡Venta FAC-" + correlativoFactura + " procesada con éxito!");
        correlativoFactura++;
        ((Stage) txtCantidad.getScene().getWindow()).close();
    }

    @FXML
    private void cancelar() {
        ((Stage) txtCantidad.getScene().getWindow()).close();
    }

    private void mensaje(Alert.AlertType tipo, String texto) {
        new Alert(tipo, texto, ButtonType.OK).showAndWait();
    }
}