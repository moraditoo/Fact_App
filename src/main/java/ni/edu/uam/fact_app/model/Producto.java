package ni.edu.uam.fact_app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {
    private Integer id;
    private String codigo;
    private String nombre;
    private Categoria categoria;
    private BigDecimal precioVenta;
    private int existencia;
    private String rutaImagen;
    private boolean activo;

    public Producto(String codigo, String nombre, Categoria categoria, BigDecimal precioVenta, int existencia, String rutaImagen, boolean activo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precioVenta = precioVenta;
        this.existencia = existencia;
        this.rutaImagen = rutaImagen;
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "[" + codigo + "] " + nombre + " - C$ " + precioVenta;
    }
}