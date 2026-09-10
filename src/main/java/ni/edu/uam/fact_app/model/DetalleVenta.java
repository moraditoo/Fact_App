package ni.edu.uam.fact_app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleVenta {
    private Producto producto;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    public DetalleVenta(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecioVenta();
        this.subtotal = this.precioUnitario.multiply(new BigDecimal(cantidad));
    }

    public String getNombreProducto() {
        return producto != null ? producto.getNombre() : "";
    }

    public String getCodigoProducto() {
        return producto != null ? producto.getCodigo() : "";
    }
}