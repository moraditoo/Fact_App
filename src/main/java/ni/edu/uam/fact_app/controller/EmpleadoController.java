package ni.edu.uam.fact_app.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ni.edu.uam.fact_app.model.Cargo;
import ni.edu.uam.fact_app.model.Empleado;
import ni.edu.uam.fact_app.util.DataManager;

import java.time.LocalDate;

public class EmpleadoController {

    @FXML private TextField txtNombres;
    @FXML private TextField txtApellidos;
    @FXML private ComboBox<Cargo> cmbCargo;
    @FXML private DatePicker dpFechaContratacion;
    @FXML private TextField txtBuscar;

    @FXML private TableView<Empleado> tblEmpleados;
    @FXML private TableColumn<Empleado, Integer> colId;
    @FXML private TableColumn<Empleado, String> colNombreCompleto;
    @FXML private TableColumn<Empleado, String> colCargo;
    @FXML private TableColumn<Empleado, LocalDate> colFecha;

    private ObservableList<Empleado> empleados;

    @FXML
    private void initialize() {
        empleados = DataManager.getEmpleados();
        cmbCargo.setItems(DataManager.getCargos());
        dpFechaContratacion.setValue(LocalDate.now());

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombreCompleto.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getNombres() + " " + e.getValue().getApellidos()));
        colCargo.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getCargo() != null ? e.getValue().getCargo().getNombre() : ""));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaContratacion"));

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
            }
        });
    }

    @FXML
    private void guardar() {
        if (txtNombres.getText().isBlank() || txtApellidos.getText().isBlank()
                || cmbCargo.getValue() == null || dpFechaContratacion.getValue() == null) {
            mensaje(Alert.AlertType.WARNING, "Complete todos los campos obligatorios.");
            return;
        }

        Empleado sel = tblEmpleados.getSelectionModel().getSelectedItem();
        if (sel != null) {
            sel.setNombres(txtNombres.getText().trim());
            sel.setApellidos(txtApellidos.getText().trim());
            sel.setCargo(cmbCargo.getValue());
            sel.setFechaContratacion(dpFechaContratacion.getValue());
            tblEmpleados.refresh();
            DataManager.guardarEmpleados();
            mensaje(Alert.AlertType.INFORMATION, "Empleado actualizado.");
        } else {
            int nuevoId = empleados.size() + 1;
            empleados.add(new Empleado(nuevoId, txtNombres.getText().trim(), txtApellidos.getText().trim(),
                    cmbCargo.getValue(), dpFechaContratacion.getValue()));
            DataManager.guardarEmpleados();
            mensaje(Alert.AlertType.INFORMATION, "Empleado registrado.");
        }
        limpiar();
    }

    @FXML
    private void eliminar() {
        Empleado sel = tblEmpleados.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione un empleado para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea eliminar al empleado seleccionado?", ButtonType.OK, ButtonType.CANCEL);
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            empleados.remove(sel);
            DataManager.guardarEmpleados();
            limpiar();
            mensaje(Alert.AlertType.INFORMATION, "Empleado eliminado.");
        }
    }

    @FXML
    private void limpiar() {
        txtNombres.clear();
        txtApellidos.clear();
        cmbCargo.getSelectionModel().clearSelection();
        dpFechaContratacion.setValue(LocalDate.now());
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