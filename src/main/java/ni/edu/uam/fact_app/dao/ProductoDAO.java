package ni.edu.uam.fact_app.dao;

import ni.edu.uam.fact_app.model.Categoria;
import ni.edu.uam.fact_app.model.Producto;
import ni.edu.uam.fact_app.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public void guardar(Producto producto) throws SQLException {
        String sql = """
            INSERT INTO producto
            (codigo, nombre, categoria_id, precio_venta, existencia, ruta_imagen, activo)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre());
            ps.setInt(3, producto.getCategoria().getId());
            ps.setBigDecimal(4, producto.getPrecioVenta());
            ps.setInt(5, producto.getExistencia());
            ps.setString(6, producto.getRutaImagen());
            ps.setBoolean(7, producto.isActivo());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    producto.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<Producto> listar() throws SQLException {
        List<Producto> lista = new ArrayList<>();
        String sql = """
            SELECT p.*, c.nombre AS categoria_nombre, c.activa AS categoria_activa
            FROM producto p
            INNER JOIN categoria c ON p.categoria_id = c.id
            ORDER BY p.id ASC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Categoria cat = new Categoria(
                        rs.getInt("categoria_id"),
                        rs.getString("categoria_nombre"),
                        rs.getBoolean("categoria_activa")
                );

                lista.add(new Producto(
                        rs.getInt("id"),
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        cat,
                        rs.getBigDecimal("precio_venta"),
                        rs.getInt("existencia"),
                        rs.getString("ruta_imagen"),
                        rs.getBoolean("activo")
                ));
            }
        }
        return lista;
    }

    public Producto buscar(int id) throws SQLException {
        String sql = """
            SELECT p.*, c.nombre AS categoria_nombre, c.activa AS categoria_activa
            FROM producto p
            INNER JOIN categoria c ON p.categoria_id = c.id
            WHERE p.id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Categoria cat = new Categoria(
                            rs.getInt("categoria_id"),
                            rs.getString("categoria_nombre"),
                            rs.getBoolean("categoria_activa")
                    );
                    return new Producto(
                            rs.getInt("id"),
                            rs.getString("codigo"),
                            rs.getString("nombre"),
                            cat,
                            rs.getBigDecimal("precio_venta"),
                            rs.getInt("existencia"),
                            rs.getString("ruta_imagen"),
                            rs.getBoolean("activo")
                    );
                }
            }
        }
        return null;
    }

    public void actualizar(Producto producto) throws SQLException {
        String sql = """
            UPDATE producto
            SET codigo = ?, nombre = ?, categoria_id = ?, precio_venta = ?, existencia = ?, ruta_imagen = ?, activo = ?
            WHERE id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre());
            ps.setInt(3, producto.getCategoria().getId());
            ps.setBigDecimal(4, producto.getPrecioVenta());
            ps.setInt(5, producto.getExistencia());
            ps.setString(6, producto.getRutaImagen());
            ps.setBoolean(7, producto.isActivo());
            ps.setInt(8, producto.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM producto WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}