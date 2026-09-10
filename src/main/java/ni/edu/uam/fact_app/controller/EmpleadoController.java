package ni.edu.uam.fact_app.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ni.edu.uam.fact_app.model.Cargo;
import ni.edu.uam.fact_app.model.Empleado;

import java.time.LocalDate;

public class EmpleadoController {

    @FXML private TextField txtNombres;
    @FXML private TextField txtApellidos;
    @FXML private ComboBox<Cargo> cmbCargo;
    @FXML private DatePicker dpFechaContratacion;
    @FXML private CheckBox chkActivo;
    @FXML private TextField txtBuscar;

    @FXML private TableView<Empleado> tblEmpleados;
    @FXML private TableColumn<Empleado, Integer> colId;
    @FXML private TableColumn<Empleado, String> colNombreCompleto;
    @FXML private TableColumn<Empleado, String> colCargo;
    @FXML private TableColumn<Empleado, LocalDate> colFecha;
    @FXML private TableColumn<Empleado, String> colActivo;

    private static final ObservableList<Empleado> empleados = FXCollections.observableArrayList();
    private static int correlativoId = 1;
    private static boolean inicializado = false;

    public static ObservableList<Empleado> getEmpleados() {
        if (!inicializado) {
            ObservableList<Cargo> cargos = CargoController.getCargos();
            Cargo admin = cargos.size() > 0 ? cargos.get(0) : new Cargo(1, "Administrador", "Admin");
            Cargo cajero = cargos.size() > 1 ? cargos.get(1) : admin;

            empleados.add(new Empleado(correlativoId++, "Carlos", "Perez", admin, LocalDate.of(2025, 1, 15), true));
            empleados.add(new Empleado(correlativoId++, "Maria", "Lopez", cajero, LocalDate.of(2025, 3, 1), true));
            inicializado = true;
        }
        return empleados;
    }

    @FXML
    private void initialize() {
        cmbCargo.setItems(CargoController.getCargos());
        dpFechaContratacion.setValue(LocalDate.now());

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombreCompleto.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getNombres() + " " + e.getValue().getApellidos()));
        colCargo.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getCargo() != null ? e.getValue().getCargo().getNombre() : ""));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaContratacion"));
        colActivo.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().isActivo() ? "Activo" : "Inactivo"));

        getEmpleados();

        FilteredList<Empleado> filtro = new FilteredList<>(empleados, p -> true);
        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldV, texto) -> {
                filtro.setPredicate(emp -> {
                    if (texto == null || texto.isBlank()) return true;
                    String b = texto.toLowerCase();
                    return emp.getNombres().toLowerCase().contains(b) || emp.getApellidos().toLowerCase().contains(b);
                });
            });
        }
        tblEmpleados.setItems(filtro);

        tblEmpleados.getSelectionModel().selectedItemProperty().addListener((obs, oldV, emp) -> {
            if (emp != null) {
                txtNombres.setText(emp.getNombres());
                txtApellidos.setText(emp.getApellidos());
                cmbCargo.setValue(emp.getCargo());
                dpFechaContratacion.setValue(emp.getFechaContratacion());
                chkActivo.setSelected(emp.isActivo());
            }
        });

        chkActivo.setSelected(true);
    }

    @FXML
    private void guardar() {
        if (txtNombres.getText().isBlank() || txtApellidos.getText().isBlank()
                || cmbCargo.getValue() == null || dpFechaContratacion.getValue() == null) {
            mensaje(Alert.AlertType.WARNING, "Complete todos los campos.");
            return;
        }

        Empleado sel = tblEmpleados.getSelectionModel().getSelectedItem();
        if (sel != null) {
            sel.setNombres(txtNombres.getText().trim());
            sel.setApellidos(txtApellidos.getText().trim());
            sel.setCargo(cmbCargo.getValue());
            sel.setFechaContratacion(dpFechaContratacion.getValue());
            sel.setActivo(chkActivo.isSelected());
            tblEmpleados.refresh();
            mensaje(Alert.AlertType.INFORMATION, "Empleado actualizado.");
        } else {
            empleados.add(new Empleado(correlativoId++, txtNombres.getText().trim(), txtApellidos.getText().trim(),
                    cmbCargo.getValue(), dpFechaContratacion.getValue(), chkActivo.isSelected()));
            mensaje(Alert.AlertType.INFORMATION, "Empleado registrado.");
        }
        limpiar();
    }

    @FXML
    private void limpiar() {
        txtNombres.clear();
        txtApellidos.clear();
        cmbCargo.getSelectionModel().clearSelection();
        dpFechaContratacion.setValue(LocalDate.now());
        chkActivo.setSelected(true);
        tblEmpleados.getSelectionModel().clearSelection();
    }

    @FXML
    private void cerrar() {
        ((Stage) txtNombres.getScene().getWindow()).close();
    }

    private void mensaje(Alert.AlertType tipo, String texto) {
        new Alert(tipo, texto, ButtonType.OK).showAndWait();
    }
}