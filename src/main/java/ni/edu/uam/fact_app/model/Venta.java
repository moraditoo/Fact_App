package ni.edu.uam.fact_app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Venta {
    private Integer id;
    private String numeroFactura;
    private LocalDateTime fecha;
    private Empleado vendedor;
    private List<DetalleVenta> detalles = new ArrayList<>();
    private BigDecimal subtotal;
    private BigDecimal iva;
    private BigDecimal total;
}