package ni.edu.uam.fact_app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {
    private Integer id;
    private String nombre;
    private boolean activa;

    public Categoria(String nombre, boolean activa) {
        this.nombre = nombre;
        this.activa = activa;
    }

    @Override
    public String toString() {
        return nombre;
    }
}