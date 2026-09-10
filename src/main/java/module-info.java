module ni.edu.uam.facturacion {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;

    opens ni.edu.uam.fact_app.controller to javafx.fxml;
    opens ni.edu.uam.fact_app.model to javafx.base;

    exports ni.edu.uam.fact_app.application;
}

