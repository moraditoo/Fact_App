package ni.edu.uam.fact_app.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import ni.edu.uam.fact_app.model.*;
import ni.edu.uam.fact_app.util.DataManager;
import ni.edu.uam.fact_app.util.SesionUsuario;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class VentaController {

    @FXML private Label lblNumeroFactura;
    @FXML private Label lblVendedor;
    @FXML private ComboBox<Producto> cmbProducto;
    @FXML private TextField txtCantidad;
    @FXML private Label lblStockDisponible;
    @FXML private ImageView imgMiniatura;

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

        recargarComboProductos();

        cmbProducto.getSelectionModel().selectedItemProperty().addListener((obs, oldV, prod) -> {
            if (prod != null) {
                lblStockDisponible.setText("Disponibles: " + prod.getExistencia() + " unids.");
                if (prod.getRutaImagen() != null && !prod.getRutaImagen().isBlank()) {
                    try {
                        imgMiniatura.setImage(new Image(prod.getRutaImagen(), true));
                    } catch (Exception e) {
                        imgMiniatura.setImage(null);
                    }
                } else {
                    imgMiniatura.setImage(null);
                }
            } else {
                lblStockDisponible.setText("Disponibles: -");
                if (imgMiniatura != null) {
                    imgMiniatura.setImage(null);
                }
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

    private void recargarComboProductos() {
        ObservableList<Producto> disponibles = FXCollections.observableArrayList();
        // Lee directamente desde DataManager y valida existencia sin el campo activo
        for (Producto p : DataManager.getProductos()) {
            if (p.getExistencia() > 0) {
                disponibles.add(p);
            }
        }
        cmbProducto.setItems(disponibles);
    }

    @FXML
    private void agregarAlCarrito() {
        Producto prod = cmbProducto.getValue();
        if (prod == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione un producto del catálogo.");
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(txtCantidad.getText().trim());
            if (cantidad <= 0) {
                mensaje(Alert.AlertType.WARNING, "La cantidad a facturar debe ser mayor a 0.");
                return;
            }
        } catch (NumberFormatException e) {
            mensaje(Alert.AlertType.ERROR, "La cantidad debe ser un número entero válido.");
            return;
        }

        int yaEnCarrito = 0;
        for (DetalleVenta d : carrito) {
            if (d.getProducto().getCodigo().equals(prod.getCodigo())) {
                yaEnCarrito += d.getCantidad();
            }
        }

        if ((yaEnCarrito + cantidad) > prod.getExistencia()) {
            mensaje(Alert.AlertType.ERROR, "Stock insuficiente. En stock: " + prod.getExistencia()
                    + " (Ya tienes " + yaEnCarrito + " en la factura)");
            return;
        }

        boolean existe = false;
        for (DetalleVenta d : carrito) {
            if (d.getProducto().getCodigo().equals(prod.getCodigo())) {
                d.setCantidad(d.getCantidad() + cantidad);
                d.setSubtotal(d.getPrecioUnitario().multiply(new BigDecimal(d.getCantidad())));
                existe = true;
                break;
            }
        }

        if (!existe) {
            carrito.add(new DetalleVenta(prod, cantidad));
        }

        tblDetalle.refresh();
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
            mensaje(Alert.AlertType.WARNING, "Seleccione una fila de la tabla para quitar.");
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
            mensaje(Alert.AlertType.WARNING, "No hay productos agregados a la factura.");
            return;
        }

        for (DetalleVenta d : carrito) {
            Producto p = d.getProducto();
            p.setExistencia(p.getExistencia() - d.getCantidad());
        }

        // Guarda el stock actualizado permanentemente en disco
        DataManager.guardarProductos();

        mensaje(Alert.AlertType.INFORMATION, "¡Factura " + lblNumeroFactura.getText() + " procesada con éxito!\nInventario actualizado.");
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