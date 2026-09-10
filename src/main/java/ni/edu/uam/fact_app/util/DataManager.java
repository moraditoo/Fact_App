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

    // --- CARGOS ---
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

    // --- CATEGORÍAS ---
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
                new Categoria(1, "Alimentos"),
                new Categoria(2, "Bebidas"),
                new Categoria(3, "Limpieza")
        );
        guardarCategorias();
    }

    // --- EMPLEADOS ---
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

    // --- PRODUCTOS ---
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
        Categoria cAlim = categorias.get(0);
        Categoria cBeb = categorias.size() > 1 ? categorias.get(1) : cAlim;
        Categoria cLimp = categorias.size() > 2 ? categorias.get(2) : cAlim;

        productos.addAll(
                new Producto(1, "ALM-001", "Arroz Faisán 1lb", cAlim, new BigDecimal("24.50"), 150, null),
                new Producto(2, "ALM-002", "Frijoles Rojos 1lb", cAlim, new BigDecimal("32.00"), 120, null),
                new Producto(3, "BEB-001", "Coca Cola 2L", cBeb, new BigDecimal("48.00"), 90, null),
                new Producto(4, "LMP-001", "Detergente Roma 1kg", cLimp, new BigDecimal("55.00"), 65, null)
        );
        guardarProductos();
    }
}