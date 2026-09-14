package ni.edu.uam.fact_app.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ni.edu.uam.fact_app.model.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class DataManager {

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    private static final String DATA_DIR = "data";
    private static final File FILE_PRODUCTOS = new File(DATA_DIR, "productos.json");
    private static final File FILE_CATEGORIAS = new File(DATA_DIR, "categorias.json");
    private static final File FILE_CARGOS = new File(DATA_DIR, "cargos.json");
    private static final File FILE_EMPLEADOS = new File(DATA_DIR, "empleados.json");

    private static final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private static final ObservableList<Categoria> categorias = FXCollections.observableArrayList();
    private static final ObservableList<Cargo> cargos = FXCollections.observableArrayList();
    private static final ObservableList<Empleado> empleados = FXCollections.observableArrayList();

    private static boolean datosCargados = false;

    private DataManager() { }

    public static void inicializar() {
        if (datosCargados) return;

        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        cargarCargos();
        cargarCategorias();
        cargarEmpleados();
        cargarProductos();

        datosCargados = true;
    }

    public static ObservableList<Cargo> getCargos() {
        inicializar();
        return cargos;
    }

    public static void guardarCargos() {
        try {
            mapper.writeValue(FILE_CARGOS, cargos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void cargarCargos() {
        if (FILE_CARGOS.exists()) {
            try {
                List<Cargo> lista = mapper.readValue(FILE_CARGOS, new TypeReference<List<Cargo>>() {});
                cargos.setAll(lista);
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        cargos.addAll(
                new Cargo(1, "Administrador", "Acceso total al sistema"),
                new Cargo(2, "Cajero", "Facturación y cobro"),
                new Cargo(3, "Bodeguero", "Gestión de existencias")
        );
        guardarCargos();
    }

    public static ObservableList<Categoria> getCategorias() {
        inicializar();
        return categorias;
    }

    public static void guardarCategorias() {
        try {
            mapper.writeValue(FILE_CATEGORIAS, categorias);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void cargarCategorias() {
        if (FILE_CATEGORIAS.exists()) {
            try {
                List<Categoria> lista = mapper.readValue(FILE_CATEGORIAS, new TypeReference<List<Categoria>>() {});
                categorias.setAll(lista);
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        categorias.addAll(
                new Categoria(1, "Granos Básicos"),
                new Categoria(2, "Lácteos y Derivados"),
                new Categoria(3, "Bebidas y Refrescos"),
                new Categoria(4, "Carnes y Embutidos"),
                new Categoria(5, "Cuidado del Hogar")
        );
        guardarCategorias();
    }

    public static ObservableList<Empleado> getEmpleados() {
        inicializar();
        return empleados;
    }

    public static void guardarEmpleados() {
        try {
            mapper.writeValue(FILE_EMPLEADOS, empleados);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void cargarEmpleados() {
        if (FILE_EMPLEADOS.exists()) {
            try {
                List<Empleado> lista = mapper.readValue(FILE_EMPLEADOS, new TypeReference<List<Empleado>>() {});
                empleados.setAll(lista);
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Cargo admin = cargos.get(0);
        Cargo cajero = cargos.size() > 1 ? cargos.get(1) : admin;
        empleados.addAll(
                new Empleado(1, "Carlos", "Perez", admin, LocalDate.of(2025, 1, 15)),
                new Empleado(2, "Maria", "Lopez", cajero, LocalDate.of(2025, 3, 1))
        );
        guardarEmpleados();
    }

    public static ObservableList<Producto> getProductos() {
        inicializar();
        return productos;
    }

    public static void guardarProductos() {
        try {
            mapper.writeValue(FILE_PRODUCTOS, productos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void cargarProductos() {
        if (FILE_PRODUCTOS.exists()) {
            try {
                List<Producto> lista = mapper.readValue(FILE_PRODUCTOS, new TypeReference<List<Producto>>() {});
                productos.setAll(lista);
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Categoria granos = categorias.get(0);
        Categoria lacteos = categorias.size() > 1 ? categorias.get(1) : granos;
        Categoria bebidas = categorias.size() > 2 ? categorias.get(2) : granos;
        Categoria carnes = categorias.size() > 3 ? categorias.get(3) : granos;
        Categoria limpieza = categorias.size() > 4 ? categorias.get(4) : granos;

        // Catálogo representativo de Nicaragua (18 productos)
        productos.addAll(
                new Producto(1, "GRA-001", "Arroz Faisán 80/20 1lb", granos, new BigDecimal("24.00"), 200, null),
                new Producto(2, "GRA-002", "Frijol Rojo Nacional 1lb", granos, new BigDecimal("32.50"), 180, null),
                new Producto(3, "GRA-003", "Azúcar Sulinsa Blanca 2lb", granos, new BigDecimal("28.00"), 120, null),
                new Producto(4, "GRA-004", "Aceite Corona Vegetal 1L", granos, new BigDecimal("65.00"), 75, null),
                new Producto(5, "GRA-005", "Pinolillo Tradicional El Chontaleño 400g", granos, new BigDecimal("30.00"), 60, null),
                new Producto(6, "GRA-006", "Café Presto Instantáneo Frasco 100g", granos, new BigDecimal("48.00"), 90, null),

                new Producto(7, "LAC-001", "Leche Parmalat Entera UHT 1L", lacteos, new BigDecimal("42.00"), 110, null),
                new Producto(8, "LAC-002", "Queso Seco Chontaleño 1lb", lacteos, new BigDecimal("85.00"), 50, null),
                new Producto(9, "LAC-003", "Crema Pura La Perfecta 1lb", lacteos, new BigDecimal("45.00"), 40, null),
                new Producto(10, "LAC-004", "Helado Eskimo Vainilla 1 Galón", lacteos, new BigDecimal("195.00"), 25, null),

                new Producto(11, "BEB-001", "Coca Cola 2L Descartable", bebidas, new BigDecimal("48.00"), 130, null),
                new Producto(12, "BEB-002", "Cerveza Toña Botella 12oz", bebidas, new BigDecimal("40.00"), 150, null),
                new Producto(13, "BEB-003", "Cerveza Victoria Clásica 12oz", bebidas, new BigDecimal("40.00"), 100, null),
                new Producto(14, "BEB-004", "Agua Purificada Fuente Pura 600ml", bebidas, new BigDecimal("16.00"), 220, null),

                new Producto(15, "CAR-001", "Pollo Tip-Top Pierna y Muslo 1lb", carnes, new BigDecimal("48.00"), 80, null),
                new Producto(16, "CAR-002", "Salchichón Cainsa Tradicional 1lb", carnes, new BigDecimal("62.00"), 60, null),

                new Producto(17, "LMP-001", "Detergente Roma en Bolsa 1kg", limpieza, new BigDecimal("55.00"), 95, null),
                new Producto(18, "LMP-002", "Jabón Axion Limón Pasta 450g", limpieza, new BigDecimal("36.00"), 110, null)
        );
        guardarProductos();
    }
}